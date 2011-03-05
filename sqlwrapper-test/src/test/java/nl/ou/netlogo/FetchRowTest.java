/*
 * Copyright 2010, 2011 Open University of The Netherlands
 * Contributors: Jan Blom, Rene Quakkelaar, Mark Rotteveel
 *
 * This file is part of NetLogo SQL Wrapper extension.
 * 
 * NetLogo SQL Wrapper extension is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * NetLogo SQL Wrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with NetLogo SQL Wrapper extension.  If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package nl.ou.netlogo;

import static nl.ou.netlogo.testsupport.DatabaseHelper.getDefaultPoolConfigurationCommand;
import static nl.ou.netlogo.testsupport.DatabaseHelper.getDefaultSqlConnectCommand;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;

import nl.ou.netlogo.testsupport.DatabaseHelper;
import nl.ou.netlogo.testsupport.HeadlessTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nlogo.api.LogoList;

/**
 * Tests for the sql:fetch-row reporter.
 * 
 * @author Mark Rotteveel
 */
public class FetchRowTest extends HeadlessTest {
	
	private static final String TABLE_PREFIX = "TEST";
	protected static String tableName;
	
	@BeforeClass
	public static void createTable() throws ClassNotFoundException {
		tableName = TABLE_PREFIX + Calendar.getInstance().getTimeInMillis();

		try {
			DatabaseHelper.executeUpdate("CREATE TABLE " + tableName + "( "
					+ "ID INTEGER PRIMARY KEY, "
					+ "CHAR_FIELD CHAR(25), " 
					+ "INT_FIELD INTEGER, "
					+ "VARCHAR_FIELD VARCHAR(200) " 
					+ ")");
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to setup database", e);
		} 
	}
	
	@Before
	public void setupData() {
		String query = "INSERT INTO " + tableName + "(ID, CHAR_FIELD, INT_FIELD, VARCHAR_FIELD) VALUES (%d, '%s', %d, '%s')";
		try {
			DatabaseHelper.executeUpdate(
					String.format(query, 1, "CHAR-content", 1234, "VARCHAR-content"),
					String.format(query, 2, "CHAR-content", 1234, "VARCHAR-content"),
					String.format(query, 3, "CHAR-content", 1234, "VARCHAR-content"),
					String.format(query, 4, "CHAR-content", 1234, "VARCHAR-content")
			);
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to setup data", e);
		}
	}
	
	/**
	 * Test for basic behavior of sql:fetch-row.
	 * <p>
	 * Expected: able to fetch 4 rows, rows contain expected values, fetching row after last row returns empty list.
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultSqlConnectCommand());
		workspace.command("sql:exec-direct \"SELECT * FROM " + tableName + " ORDER BY ID\"");
		
		boolean isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
		assertTrue("Should stay connected after select", isConnected);
		
		for (int i = 1; i <= 4; i++) {
			LogoList row = (LogoList)workspace.report("sql:fetch-row");
			assertNotNull("sql:fetch-row should have returned a row", row);
			assertEquals("Unexpected number of values in row", 4, row.size());
			assertEquals(Double.valueOf(i), row.get(0));
			assertEquals("CHAR-content", row.get(1));
			assertEquals(Double.valueOf(1234), row.get(2));
			assertEquals("VARCHAR-content", row.get(3));
		}
		LogoList row = (LogoList)workspace.report("sql:fetch-row");
		assertNotNull("sql:fetch-row should have returned a row", row);
		assertEquals("Expected fetch after last row to return empty result", 0, row.size());
	}
	
	/**
	 * Test for behavior of sql:fetch-row for empty resultset.
	 * <p>
	 * Expected: returned row is an empty list.
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_emptyResult() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultSqlConnectCommand());
		workspace.command("sql:exec-direct \"SELECT * FROM " + tableName + " WHERE 1 = 0\"");
		
		LogoList row = (LogoList)workspace.report("sql:fetch-row");
		assertNotNull("sql:fetch-row should have returned a row", row);
		assertEquals("Expected fetch of empty resultset to return empty result", 0, row.size());
	}
	
	/**
	 * Test if connection obtained through sql:connect is not autodisconnected after fetching last row.
	 * <p>
	 * Expected: connection remains open after last sql:fetch-row
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_connect_noAutodisconnect() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultSqlConnectCommand());
		workspace.command("sql:exec-direct \"SELECT * FROM " + tableName + " ORDER BY ID\"");
		
		boolean isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
		assertTrue("Should stay connected after select", isConnected);
		
		for (int i = 1; i <= 4; i++) {
			LogoList row = (LogoList)workspace.report("sql:fetch-row");
			assertNotNull("sql:fetch-row should have returned a row", row);
			assertEquals("Unexpected number of values in row", 4, row.size());
			
			isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
			assertTrue("Should stay connected after each fetch-row", isConnected);
		}
	}
	
	/**
	 * Test if connection obtained through connectionpool is not autodisconnected after fetching last row when autodisconnect
	 * is disabled.
	 * <p>
	 * Expected: connection remains open after last sql:fetch-row
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_connectionPool_noAutodisconnect() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultPoolConfigurationCommand(false));
		workspace.command("sql:exec-direct \"SELECT * FROM " + tableName + " ORDER BY ID\"");
		
		boolean isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
		assertTrue("Should stay connected after select", isConnected);
		
		for (int i = 1; i <= 4; i++) {
			LogoList row = (LogoList)workspace.report("sql:fetch-row");
			assertNotNull("sql:fetch-row should have returned a row", row);
			assertEquals("Unexpected number of values in row", 4, row.size());
			
			isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
			assertTrue("Should stay connected after each fetch-row", isConnected);
		}
	}
	
	/**
	 * Test if connection obtained through connectionpool is autodisconnected after fetching last row when autodisconnect
	 * is enabled.
	 * <p>
	 * Expected: connection remains open after last sql:fetch-row, and executing sql:fetch-row after last row returns empty list.
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_connectionPool_autodisconnect() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultPoolConfigurationCommand());
		workspace.command("sql:exec-direct \"SELECT * FROM " + tableName + " ORDER BY ID\"");
		
		boolean isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
		assertTrue("Should stay connected after select", isConnected);
		
		for (int i = 1; i <= 4; i++) {
			LogoList row = (LogoList)workspace.report("sql:fetch-row");
			assertNotNull("sql:fetch-row should have returned a row", row);
			assertEquals("Unexpected number of values in row", 4, row.size());
			
			isConnected = (Boolean)workspace.report("sql:debug-is-connected?");
			if (i < 4) {
				assertTrue("Should stay connected after each fetch-row", isConnected);
			} else {
				assertFalse("Should be auto-disconnected after last fetch-row", isConnected);
			}
		}
		LogoList row = (LogoList)workspace.report("sql:fetch-row");
		assertNotNull("sql:fetch-row should have returned a row", row);
		assertEquals("Expected fetch after last row to return empty result", 0, row.size());
	}
	
	/**
	 * Test if sql:fetch-row returns an empty logolist if called without a connection.
	 * <p>
	 * Expected: empty list of type LogoList
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_noConnection() throws Exception {
		workspace.open("init-sql.nlogo");
		Object row = workspace.report("sql:fetch-row");
		
		assertTrue("Expected row of type LogoList", row instanceof LogoList);
		assertEquals("Expected empty list", Collections.EMPTY_LIST, row);
	}
	
	/**
	 * Test if sql:fetch-row returns an empty logolist if called with a connection using sql:connect, but without a statement.
	 * <p>
	 * Expected: empty list of type LogoList
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_connect_noStatement() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultSqlConnectCommand());
		
		Object row = workspace.report("sql:fetch-row");
		
		assertTrue("Expected row of type LogoList", row instanceof LogoList);
		assertEquals("Expected empty list", Collections.EMPTY_LIST, row);
	}
	
	/**
	 * Test if sql:fetch-row throws an empty logolist if called with the connectionpool, but without a statement.
	 * <p>
	 * Expected: empty list of type LogoList
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_connectionpool_noStatement() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultPoolConfigurationCommand());
		
		Object row = workspace.report("sql:fetch-row");
		
		assertTrue("Expected row of type LogoList", row instanceof LogoList);
		assertEquals("Expected empty list", Collections.EMPTY_LIST, row);
	}
	
	/**
	 * Test if sql:fetch-row returns an empty logolist if called after an update statement.
	 * <p>
	 * Expected: empty list of type LogoList
	 * </p>
	 * 
	 * @throws Exception For any exceptions during testing
	 */
	@Test
	public void testFetchRow_updateQuery() throws Exception {
		workspace.open("init-sql.nlogo");
		workspace.command(getDefaultSqlConnectCommand());
		workspace.command("sql:exec-direct \"UPDATE " + tableName + "  SET INT_FIELD = 5\"");

		Object row = workspace.report("sql:fetch-row");
		
		assertTrue("Expected row of type LogoList", row instanceof LogoList);
		assertEquals("Expected empty list", Collections.EMPTY_LIST, row);
	}
	
	// TODO cover more datatypes
	
	@After
	public void teardownData() {
		try {
			DatabaseHelper.executeUpdate("TRUNCATE TABLE " + tableName);
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to delete data", e);
		}
	}

	@AfterClass
	public static void dropTable() {
		try {
			DatabaseHelper.executeUpdate(new String[] {"DROP TABLE " + tableName});
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to drop table " + tableName, e);
		} 
	}
}
