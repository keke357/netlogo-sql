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

import nl.ou.netlogo.testsupport.ConnectionInformation;
import nl.ou.netlogo.testsupport.HeadlessTest;
import static nl.ou.netlogo.testsupport.DatabaseHelper.getGenericConnectCommand;
import static nl.ou.netlogo.testsupport.DatabaseHelper.getMySQLConnectCommand;
import static nl.ou.netlogo.testsupport.DatabaseHelper.getMySQLJdbcURL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.EngineException;

/**
 * Tests for the sql:connect command.
 * 
 * @author Mark Rotteveel
 */
public class ConnectTest extends HeadlessTest {

    // TODO use JUnits advanced features to check exception messages

    /**
     * Test if the sql:connect command works for MySQL brandname (default).
     * <p>
     * Expected: connection established.
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test
    public void testConnect_MySQL_default() throws Exception {
        workspace.open("init-sql.nlogo");
        workspace.command(getMySQLConnectCommand());
        assertTrue("Expected connection to be established", (Boolean) workspace.report("sql:is-connected?"));
    }

    /**
     * Test if the sql:connect command works for MySQL brandname (explicit).
     * <p>
     * Expected: connection established.
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test
    public void testConnect_MySQL() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation instance = ConnectionInformation.getInstance();
        String command = String.format("sql:connect [[\"brand\" \"MySQL\"] [\"host\" \"%s\"] [\"port\" \"%s\"] [\"user\" \"%s\"] [\"password\" \"%s\"] [\"database\" \"%s\"]]",
                        instance.getHost(), instance.getPort(), instance.getUsername(), instance.getPassword(),
                        instance.getSchema());
        workspace.command(command);
        assertTrue("Expected connection to be established", (Boolean) workspace.report("sql:is-connected?"));
    }

    /**
     * Test if the sql:connect command works for generic brandname (using a
     * MySQL database).
     * <p>
     * Expected: connection established.
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test
    public void testConnect_generic() throws Exception {
        workspace.open("init-sql.nlogo");
        workspace.command(getGenericConnectCommand());
        assertTrue("Expected connection to be established", (Boolean) workspace.report("sql:is-connected?"));
    }

    /**
     * Test if the sql:connect command throws an exception if the jdbc-url is
     * not specified for brand generic
     * <p>
     * Expected: exception is thrown
     * </p>
     * 
     * @throws Exception
     */
    @Test(expected = EngineException.class)
    public void testConnect_generic_missingJdbcURL() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation instance = ConnectionInformation.getInstance();
        String command = String.format("sql:connect [[\"brand\" \"generic\"] [\"driver\" \"com.mysql.jdbc.Driver\"] [\"user\" \"%s\"] [\"password\" \"%s\"]]",
                        instance.getUsername(), instance.getPassword());
        workspace.command(command);
    }

    /**
     * Test if the sql:connect command throws an exception if the driver is not
     * specified for brand generic
     * <p>
     * Expected: exception is thrown
     * </p>
     * 
     * @throws Exception
     */
    @Test(expected = EngineException.class)
    public void testConnect_generic_missingDriver() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation instance = ConnectionInformation.getInstance();
        String command = String.format("sql:connect [[\"brand\" \"generic\"] [\"jdbc-url\" \"%s\"] [\"user\" \"%s\"] [\"password\" \"%s\"]]",
                getMySQLJdbcURL(), instance.getUsername(), instance.getPassword());
        workspace.command(command);
    }

    /**
     * Test if the sql:connect command throws an exception if an incorrect
     * driver is specified for brand generic
     * <p>
     * Expected: exception is thrown
     * </p>
     * 
     * @throws Exception
     */
    @Test(expected = EngineException.class)
    public void testConnect_generic_incorrectDriver() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation instance = ConnectionInformation.getInstance();
        String command = String.format("sql:connect [[\"brand\" \"generic\"] [\"jdbc-url\" \"%s\"] [\"driver\" \"com.mysql.jdbc.xxxDriver\"] [\"user\" \"%s\"] [\"password\" \"%s\"]]",
                        getMySQLJdbcURL(), instance.getUsername(), instance.getPassword());
        workspace.command(command);
    }

    /**
     * Test if the sql:connect command throws an exception if no driver is
     * loaded for the specified JDBC url for brand generic
     * <p>
     * JDBC queries all loaded drivers if it will accept the jdbc-url, the first
     * driver to accept the url will be used to create the connection. If no
     * driver is found, an exception is thrown.
     * </p>
     * <p>
     * Expected: exception is thrown
     * </p>
     * 
     * @throws Exception
     */
    @Test(expected = EngineException.class)
    public void testConnect_generic_noDriverForURL() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation instance = ConnectionInformation.getInstance();
        String command = String.format("sql:connect [[\"brand\" \"generic\"] [\"jdbc-url\" \"jdbc:nodriver://localhost:3306/sqlwrappertest\"] [\"driver\" \"com.mysql.jdbc.Driver\"] [\"user\" \"%s\"] [\"password\" \"%s\"]]",
                        instance.getUsername(), instance.getPassword());
        workspace.command(command);
    }

    /**
     * Test if the sql:connect command works for agent.
     * <p>
     * Expected: connection established for agent, but not for observer.
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test
    public void testConnect_agent() throws Exception {
        workspace.open("init-sql.nlogo");
        AgentSet breed = workspace.world.getBreed("TESTAGENT");
        assertNotNull("Breed TESTAGENT not defined in workspace", breed);
        Turtle turtle = workspace.world.createTurtle(breed);
        assertNotNull("Unable to create turtle", turtle);

        workspace.evaluateCommands(getMySQLConnectCommand(), turtle, true);
        assertTrue("Expected connection to be established",
                (Boolean) workspace.evaluateReporter("sql:is-connected?", turtle));
        assertFalse("Expected no connection for observer", (Boolean) workspace.report("sql:is-connected?"));
    }

    /**
     * Test if the sql:connect throws an error for incorrect host information.
     * <p>
     * Expected: exception is thrown
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test(expected = EngineException.class)
    public void testConnect_incorrectHost() throws Exception {
        workspace.open("init-sql.nlogo");
        workspace.command("sql:connect [[\"host\" \"non-existent\"][\"port\" \"3306\"] [\"user\" \"test\"] [\"password\" \"test\"] [\"database\" \"test\"]]");
    }

    /**
     * Test if not specifying the host will connect to localhost.
     * <p>
     * Expected: connection to localhost is established.
     * </p>
     * <p>
     * Assumption: the test database runs on localhost
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test
    public void testConnect_defaultHost() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation ci = ConnectionInformation.getInstance();
        workspace.command(String.format("sql:connect [[\"port\" \"%s\"] [\"user\" \"%s\"] [\"password\" \"%s\"] [\"database\" \"%s\"]]",
                ci.getPort(), ci.getUsername(), ci.getPassword(), ci.getSchema()));
        assertTrue("Expected connection to be established", (Boolean) workspace.report("sql:is-connected?"));
    }

    /**
     * Test if not specifying the port will assume default port 3306.
     * <p>
     * Expected: connection to port 3306 is established.
     * </p>
     * <p>
     * Assumption: the test database runs on port 3306!
     * </p>
     * 
     * @throws Exception
     *             For any exceptions during testing
     */
    @Test
    public void testConnect_defaultPort() throws Exception {
        workspace.open("init-sql.nlogo");
        ConnectionInformation ci = ConnectionInformation.getInstance();
        workspace.command(String.format("sql:connect [[\"host\" \"%s\"] [\"user\" \"%s\"] [\"password\" \"%s\"] [\"database\" \"%s\"]]",
                ci.getHost(), ci.getUsername(), ci.getPassword(), ci.getSchema()));
        assertTrue("Expected connection to be established", (Boolean) workspace.report("sql:is-connected?"));
    }
}
