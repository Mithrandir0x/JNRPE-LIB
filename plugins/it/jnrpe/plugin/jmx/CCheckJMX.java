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
package it.jnrpe.plugin.jmx;

import it.jnrpe.commands.CCommandLine;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugin.util.ThresholdUtil;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.plugins.IPluginInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class CCheckJMX implements IPluginInterface
{

    private String url;
    private int verbatim;
    private JMXConnector connector;
    private MBeanServerConnection connection;
    private String warning, critical;
    private String attribute, info_attribute;
    private String attribute_key, info_key;
    private String object;
    private String username, password;

    private long checkData;
    private Object infoData;

    private static final int RETURN_OK = 0; // The plugin was able to check the
    // service and it appeared to be
    // functioning properly
    private static final String OK_STRING = "JMX OK -";
    private static final int RETURN_WARNING = 1; // The plugin was able to check
    // the service, but it appeared
    // to be above some "warning"
    // threshold or did not appear
    // to be working properly
    private static final String WARNING_STRING = "JMX WARNING -";
    private static final int RETURN_CRITICAL = 2; // The plugin detected that
    // either the service was not
    // running or it was above
    // some "critical" threshold
    private static final String CRITICAL_STRING = "JMX CRITICAL -";
    private static final int RETURN_UNKNOWN = 3; // Invalid command line
    // arguments were supplied to
    // the plugin or low-level
    // failures internal to the
    // plugin (such as unable to
    // fork, or open a tcp socket)
    // that prevent it from
    // performing the specified
    // operation. Higher-level
    // errors (such as name
    // resolution errors, socket
    // timeouts, etc) are outside
    // of the control of plugins
    // and should generally NOT be
    // reported as UNKNOWN states.
    private static final String UNKNOWN_STRING = "JMX UNKNOWN";

    public CReturnValue execute(CCommandLine cl)
    {
        String sVersion  = System.getProperty("java.version");
        sVersion = sVersion.replace(".", "");
        int iVersion = Integer.parseInt(sVersion.substring(0,2));
        if (iVersion < 15)
        {
            String sMessage = UNKNOWN_STRING + " - Java 1.5+ required. Current Java version : " + System.getProperty("java.version");
            return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, sMessage);
        }

        parse(cl);

        try
        {
            try
            {
                connect();
            }
            catch (IOException e)
            {
                return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, UNKNOWN_STRING + " - Could not connect to jmx : " + e.getMessage());
            }

            try
            {
                execute();
            }
            catch (Exception e)
            {
                return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, UNKNOWN_STRING + " - Could not execute query : " + e.getMessage());
            }
        }
        finally
        {
            try
            {
                disconnect();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return report();
    }

    private void connect() throws IOException
    {
        JMXServiceURL jmxUrl = new JMXServiceURL(url);

        if (username != null)
        {
            Map m = new HashMap();
            m.put(JMXConnector.CREDENTIALS, new String[]
            { username, password });
            connector = JMXConnectorFactory.connect(jmxUrl, m);
        }
        else
        {
            connector = JMXConnectorFactory.connect(jmxUrl);
        }
        connection = connector.getMBeanServerConnection();
    }

    private void disconnect() throws IOException
    {
        if (connector != null)
        {
            connector.close();
            connector = null;
        }
    }

    /**
     * @param args
     */
    // public static void main(String[] args)
    // {
    //
    // JMXQuery query = new JMXQuery();
    //
    // try
    // {
    // query.parse(args);
    // query.connect();
    // query.execute();
    // int status = query.report(System.out);
    // System.exit(status);
    // }
    // catch (Exception ex)
    // {
    // int status = query.report(ex, System.out);
    // System.exit(status);
    // }
    // finally
    // {
    // try
    // {
    // query.disconnect();
    // }
    // catch (IOException e)
    // {
    // int status = query.report(e, System.out);
    // System.exit(status);
    // }
    // }
    // }

    // private int report(Exception ex, PrintStream out)
    // {
    // if (ex instanceof ParseError)
    // {
    // out.print(UNKNOWN_STRING + " ");
    // reportException(ex, out);
    // out.println(" Usage: check_jmx -help ");
    // return RETURN_UNKNOWN;
    // }
    // else
    // {
    // out.print(CRITICAL_STRING + " ");
    // reportException(ex, out);
    // out.println();
    // return RETURN_CRITICAL;
    // }
    // }

    private void reportException(Exception ex, PrintStream out)
    {

        if (verbatim < 2)
            out.print(rootCause(ex).getMessage());
        else
        {
            out.print(ex.getMessage() + " connecting to " + object + " by URL " + url);
        }

        if (verbatim >= 3)
            ex.printStackTrace(out);

    }

    private static Throwable rootCause(Throwable ex)
    {
        if (ex.getCause() == null)
            return ex;
        return rootCause(ex.getCause());
    }

    private CReturnValue report()
    {
        int status;
        String sMessage = null;

        if (ThresholdUtil.isValueInRange(critical, new BigDecimal(checkData)))
        {
            status = RETURN_CRITICAL;
            sMessage = CRITICAL_STRING;
        }
        else if (ThresholdUtil.isValueInRange(warning, new BigDecimal(checkData)))
        {
            status = RETURN_WARNING;
            sMessage = WARNING_STRING;
        }
        else
        {
            status = RETURN_OK;
            sMessage = OK_STRING;
        }

        boolean shown = false;
        if (infoData == null || verbatim >= 2)
        {
            sMessage += " ";
            if (attribute_key != null)
                sMessage += (attribute + '.' + attribute_key + " is " + checkData);
            else
            {
                sMessage += (attribute + " is " + checkData);
                shown = true;
            }
        }

        if (!shown && infoData != null)
        {
            if (infoData instanceof CompositeDataSupport)
                report((CompositeDataSupport) infoData, sMessage);
            else
                sMessage += (infoData.toString());
        }

        return new CReturnValue(status, sMessage);
    }

    private void report(CompositeDataSupport data, String sMessage)
    {
        CompositeType type = data.getCompositeType();
        sMessage += ",";
        for (Iterator it = type.keySet().iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            if (data.containsKey(key))
                sMessage += (key + '=' + data.get(key));
            if (it.hasNext())
                sMessage += (';');
        }
    }

    private boolean compare(long level, boolean more)
    {
        if (more)
            return checkData >= level;
        else
            return checkData <= level;
    }

    private void execute() throws Exception
    {
        Object attr = connection.getAttribute(new ObjectName(object), attribute);
        if (attr instanceof CompositeDataSupport)
        {
            CompositeDataSupport cds = (CompositeDataSupport) attr;
            if (attribute_key == null)
                throw new Exception("Attribute key is null for composed data " + object);
            checkData = parseData(cds.get(attribute_key));
        }
        else
        {
            checkData = parseData(attr);
        }

        if (info_attribute != null)
        {
            Object info_attr = info_attribute.equals(attribute) ? attr : connection.getAttribute(new ObjectName(object), info_attribute);
            if (info_key != null && (info_attr instanceof CompositeDataSupport) && verbatim < 4)
            {
                CompositeDataSupport cds = (CompositeDataSupport) attr;
                infoData = cds.get(info_key);
            }
            else
            {
                infoData = info_attr;
            }
        }

    }

    private long parseData(Object o)
    {
        if (o instanceof Number)
            return ((Number) o).longValue();
        else
            return Long.parseLong(o.toString());
    }

    private void parse(CCommandLine cli)
    {
        // try
        // {
        // for (int i = 0; i < args.length; i++)
        // {
        // String option = args[i];
        // if (option.equals("-help"))
        // {
        // printHelp(System.out);
        // System.exit(RETURN_UNKNOWN);
        // }
        // -U diventa URL
        this.url = cli.getOptionValue("url");
        // -O diventa OBJECT
        this.object = cli.getOptionValue("object");
        // -A == ATTRIBUTE
        this.attribute = cli.getOptionValue("attribute");
        // -I = INFO_ATTRIBUTE
        this.info_attribute = cli.getOptionValue("infoattribute");
        // -J = info_key
        this.info_key = cli.getOptionValue("infokey");
        // -K = attributekey
        this.attribute_key = cli.getOptionValue("attributekey");
        // -V = verbatim
        // if (cli.hasOption("verbatim"))
        // {
        // this.verbatim = option.length() - 1;
        // }
        // -w = warning
        this.warning = cli.getOptionValue("warning");
        // -c = critical
        this.critical = cli.getOptionValue("critical");
        // -username = username
        this.username = cli.getOptionValue("username");
        // -password = password
        this.password = cli.getOptionValue("password");
        // }

        // url, object e attribute sono mandatory

        // if (url == null || object == null || attribute == null)
        // throw new Exception("Required options not specified");
        // }
        // catch (Exception e)
        // {
        // throw new ParseError(e);
        // }

    }

//    private void printHelp(PrintStream out)
//    {
//        InputStream is = JMXQuery.class.getClassLoader().getResourceAsStream("jmxquery/HELP");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        try
//        {
//            while (true)
//            {
//                String s = reader.readLine();
//                if (s == null)
//                    break;
//                out.println(s);
//            }
//        }
//        catch (IOException e)
//        {
//            out.println(e);
//        }
//        finally
//        {
//            try
//            {
//                reader.close();
//            }
//            catch (IOException e)
//            {
//                out.println(e);
//            }
//        }
//    }

}
