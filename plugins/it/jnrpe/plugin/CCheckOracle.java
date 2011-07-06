/*
 * Copyright (c) 2008 Massimiliano Ziccardi
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package it.jnrpe.plugin;

import it.jnrpe.commands.CCommandLine;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugin.util.ThresholdUtil;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.plugins.IPluginInterface;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performs standard checks against an oracle database server
 * 
 * @author Massimiliano Ziccardi
 * 
 */
public class CCheckOracle implements IPluginInterface
{
    private Log m_Logger = LogFactory.getLog(CCheckOracle.class);

    public CCheckOracle()
    {

    }

    /**
     * Connects to the database
     * @param cl
     * @return
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private Connection getConnection(CCommandLine cl) throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException
    {
        DriverManager.registerDriver // load driver
                ((Driver) Class.forName("oracle.jdbc.driver.OracleDriver")
                        .newInstance());
        
        m_Logger.debug("GETTING CONNECTION TO " + cl.getOptionValue("db")
                + "@" + cl.getOptionValue("server"));

        
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@"
                + cl.getOptionValue("server") + ":"
                + cl.getOptionValue("port", "1521") + ":"
                + cl.getOptionValue("db"), cl.getOptionValue("username"), cl
                .getOptionValue("password"));

        return conn;
    }

    /**
     * Checks if the database is reacheble
     * @param c
     * @param cl
     * @return
     */
    private CReturnValue checkAlive(Connection c, CCommandLine cl)
    {
        Statement stmt = null;
        ResultSet rs = null;

        String sMsg = "{0} : {1} - {2} {3}";
        
        Object[] vObjs = new Object[4];
        vObjs[0] = "CHECK_ORACLE";
        vObjs[1] = cl.getOptionValue("db");
        vObjs[2] = "OK";
        vObjs[3] = "";
        
        MessageFormat mf = new MessageFormat(sMsg);
        
        try
        {
            stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT SYSDATE FROM DUAL");

            return new CReturnValue(IJNRPEConstants.STATE_OK, mf.format(vObjs));
        }
        catch (SQLException sqle)
        {
            vObjs[2] = "UNKNOWN";
            vObjs[3] = sqle.getMessage();
                
            return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, mf.format(vObjs));
        }
        catch (Exception e)
        {
            vObjs[2] = "CRITICAL";
            vObjs[3] = e.getMessage();
            
            return new CReturnValue(IJNRPEConstants.STATE_CRITICAL, mf.format(vObjs));
        }
        finally
        {
            try
            {
                stmt.close();
                rs.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * Checks database usage
     * @param c
     * @param cl
     * @return
     */
    private CReturnValue checkTablespace(Connection c, CCommandLine cl)
    {
//        Integer iWarning = new Integer(cl.getOptionValue("warning", "70"));
//        Integer iCritical = new Integer(cl.getOptionValue("critical", "80"));
        
        String sWarning = cl.getOptionValue("warning", "70");
        String sCritical = cl.getOptionValue("critical", "80");
        
        String sTablespace = cl.getOptionValue("tablespace").toUpperCase();
        
        String sQry = "select NVL(b.free,0.0),a.total,100 - trunc(NVL(b.free,0.0)/a.total * 1000) / 10 prc"
                + " from ("
                + " select tablespace_name,sum(bytes)/1024/1024 total"
                + " from dba_data_files group by tablespace_name) A"
                + " LEFT OUTER JOIN"
                + " ( select tablespace_name,sum(bytes)/1024/1024 free"
                + " from dba_free_space group by tablespace_name) B"
                + " ON a.tablespace_name=b.tablespace_name WHERE a.tablespace_name='" + sTablespace + "'";

        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = c.createStatement();
            rs = stmt.executeQuery(sQry);
            
            boolean bFound = rs.next();
            
            if (!bFound)
                return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "CHECK_ORACLE : UNKNOWN - Tablespace " + cl.getOptionValue("tablespace") + " do not exist?");
            
            BigDecimal ts_free = rs.getBigDecimal(1);
            BigDecimal ts_total = rs.getBigDecimal(2);
            BigDecimal ts_pct = rs.getBigDecimal(3);
            
            String sMsg = "{0} : {1} {2} - {3,number,0.#}% used [ {4,number,0.#} / {5,number,0.#} MB available ]|{1}={3,number,0.#}%;{6};{7};0;100";
            
            Object[] vObjs = new Object[8];
            vObjs[0] = cl.getOptionValue("db");
            vObjs[1] = cl.getOptionValue("tablespace");
            vObjs[2] = "OK";
            vObjs[3] = ts_pct;
            vObjs[4] = ts_free;
            vObjs[5] = ts_total;
            vObjs[6] = sWarning;
            vObjs[7] = sCritical;

            MessageFormat mf = new MessageFormat(sMsg);
            
            //if (ts_pct.compareTo(new BigDecimal(iCritical.intValue())) == 1)
            if (ThresholdUtil.isValueInRange(sCritical, ts_pct))
            {
                vObjs[2] = "CRITICAL";
                CReturnValue rv = new CReturnValue(IJNRPEConstants.STATE_CRITICAL, mf.format(vObjs)); 
                return rv;
            }
            
            if (ThresholdUtil.isValueInRange(sWarning, ts_pct))
            {
                vObjs[2] = "WARNING";                
                CReturnValue rv = new CReturnValue(IJNRPEConstants.STATE_WARNING, mf.format(vObjs));
                
                return rv;
            }
            
            CReturnValue rv =  new CReturnValue(IJNRPEConstants.STATE_OK,mf.format(vObjs));
            
            return rv;
            
        }
        catch (Exception e)
        {
            return new CReturnValue(IJNRPEConstants.STATE_CRITICAL,
                    "CHECK_ORACLE : CRITICAL - " + e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                rs.close();
            }
            catch (Exception e)
            {
            }
        }
        
    }

    /**
     * Checks cache hit rates
     * @param c
     * @param cl
     * @return
     */
    private CReturnValue checkCache(Connection c, CCommandLine cl)
    {
        String sWarning = cl.getOptionValue("warning", "70");
        String sCritical = cl.getOptionValue("critical", "80");
        
        String sQry1 = "select (1-(pr.value/(dbg.value+cg.value)))*100"
                    + " from v$sysstat pr, v$sysstat dbg, v$sysstat cg"
                    + " where pr.name='physical reads'"
                    + " and dbg.name='db block gets'"
                    + " and cg.name='consistent gets'";   
        
        String sQry2 = "select sum(lc.pins)/(sum(lc.pins)+sum(lc.reloads))*100 from v$librarycache lc";
        
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = c.createStatement();
            
            rs = stmt.executeQuery(sQry1);
            rs.next();
            
            BigDecimal buf_hr = rs.getBigDecimal(1);
            
            rs = stmt.executeQuery(sQry2);
            rs.next();
            
            BigDecimal lib_hr = rs.getBigDecimal(1);
            
            String sMessage = "{0} {1} - Cache Hit Rates: {2,number,0.#}% Lib -- {3,number,0.#}% Buff|lib={4,number,0.#}%;{5};{6};0;100 buffer={3,number,0.#};{5};{6};0;100";

            MessageFormat mf = new MessageFormat(sMessage);

            Object[] vValues = new Object[7];
            vValues[0] = cl.getOptionValue("db");
            vValues[1] = "OK";
            vValues[2] = lib_hr;
            vValues[3] = buf_hr;
            vValues[4] = lib_hr;
            vValues[5] = sWarning;
            vValues[6] = sCritical;
            
            //if (buf_hr.compareTo(new BigDecimal(iCritical.intValue())) == -1)
            if (ThresholdUtil.isValueInRange(sCritical, buf_hr))
            {
                vValues[1] = "CRITICAL";
                       
                CReturnValue rv = new CReturnValue(IJNRPEConstants.STATE_CRITICAL, mf.format(vValues));
                return rv;
            }
            
            //if (buf_hr.compareTo(new BigDecimal(iWarning.intValue())) == -1)
            if (ThresholdUtil.isValueInRange(sWarning, buf_hr))
            {
                vValues[1] = "WARNING";
                
                CReturnValue rv = new CReturnValue(IJNRPEConstants.STATE_WARNING,mf.format(vValues)); 

                return rv;
            }
            
            CReturnValue rv = new CReturnValue(IJNRPEConstants.STATE_WARNING, mf.format(vValues)); 
            
            return rv;
            
        }
        catch (Exception e)
        {
            return new CReturnValue(IJNRPEConstants.STATE_CRITICAL,
                    "CHECK_ORACLE : CRITICAL - " + e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                rs.close();
            }
            catch (Exception e)
            {
            }
        }
      
    }
    
    /*
     * (non-Javadoc)
     * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.commands.CCommandLine)
     */
    public CReturnValue execute(CCommandLine cl)
    {
        Connection conn = null;

        try
        {
            conn = getConnection(cl);

            if (cl.hasOption("alive"))
                return checkAlive(conn, cl);

            if (cl.hasOption("tablespace"))
                return checkTablespace(conn, cl);

            if (cl.hasOption("cache"))
                return checkCache(conn, cl);

            conn.close();
        }
        catch (ClassNotFoundException cnfe)
        {
            m_Logger
                    .error("ORACLE DRIVER LIBRARY IS NOT IN THE CLASSPATH. (PUT IT IN THE SAME DIRECTORY OF THIS PLUGIN)");
            return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN,
                    "Configuration error");
        }
        catch (SQLException sqle)
        {
            m_Logger.info("Error communicating with database.", sqle);
            return new CReturnValue(IJNRPEConstants.STATE_CRITICAL,
                    sqle.getMessage()); 
        }
        catch (Exception e)
        {
            m_Logger.fatal("Error communicating with database.", e);
            return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN,
                    "Configuration error");
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e)
                {
                    m_Logger.warn("ERROR CLOSING DB CONNECTION.", e);
                }
            }
        }

        return null;
    }

}
