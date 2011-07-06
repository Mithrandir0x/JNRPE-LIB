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
package it.jnrpe.server.config;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

/**
 * This class is a factory that all objects can use to access the whole server
 * configuration
 * 
 * @author Massimiliano Ziccardi
 */
public class CJNRPEConfiguration
{
    private Log m_Logger = LogFactory.getLog(CJNRPEConfiguration.class);

    protected static CJNRPEConfiguration m_Instance = null;
    protected static CConfiguration m_Configuration = null;

    /**
     * Default constructor added so the class can be inheritable.
     */
    public CJNRPEConfiguration() { }

    private CJNRPEConfiguration(File fileName)
    {
        if (!fileName.exists() || !fileName.canRead())
        {
            // TODO: throw an exception
            m_Logger.fatal("UNABLE TO READ CONFIGURATION FILE "
                    + fileName.getAbsolutePath());
            System.exit(-1);
        }

        try
        {
            Digester digester = DigesterLoader
                    .createDigester(new InputSource(
                            System.class
                                    .getResourceAsStream("/it/jnrpe/server/config/digester.xml")));
            // turn on XML schema validation
            digester.setFeature("http://xml.org/sax/features/validation",true);
            digester.setFeature("http://apache.org/xml/features/validation/schema",true);
            digester.setFeature("http://xml.org/sax/features/namespaces", true);
            digester.setEntityResolver(new CConfigValidationEntityResolver());
            digester.setErrorHandler(new CConfigErrorHandler());
            
            m_Configuration = (CConfiguration) digester.parse(fileName);
        }
        catch (Exception e)
        {
            // TODO: throw an exception
            m_Logger.fatal("UNABLE TO PARSE CONFIGURATION : " + e.getMessage());
            System.exit(-1);
        }
    }

    public static CJNRPEConfiguration getInstance()
    {
        return m_Instance;
    }

    public static void init(String sFileName)
    {
        m_Instance = new CJNRPEConfiguration(new File(sFileName));
    }

    /**
     * Returns a Map containing the definition of all the commands
     * @return
     */
    public Map getCommandDefinitions()
    {
        CCommands cc = m_Configuration.getCommands();
        
        if (cc != null)
            return cc.getCommandDefinitions();
        else
            return Collections.EMPTY_MAP;
        //return m_Configuration.getCommands().getCommandDefinitions();
    }

    /**
     * Returns a List containing all the directories that contains plugins
     * 
     * TODO: In the future will return a single directory
     * @return
     */
    public List getPluginDirs()
    {
        return m_Configuration.getServerConfiguration().getPluginDirs();
    }

    /**
     * Return a list containing all the couples ADDRESS - PORT that the server will 
     * listen to
     * @return
     */
    public List getServerBindings()
    {
        return m_Configuration.getServerConfiguration().getBindings();
    }

    /**
     * Return a list containing all the IPs that are allowed to connect to this 
     * server
     * @return
     */
    public List getAcceptedHosts()
    {
        return m_Configuration.getServerConfiguration().getAcceptedHosts();
    }

    /**
     * Returns <code>true</code> if MACROs must be expanded
     * @return
     */
    public boolean acceptParams()
    {
        return m_Configuration.getServerConfiguration().getAcceptParams()
                .equalsIgnoreCase("true");
    }
    
    /**
     * Returns the number of seconds the server will wait for a plugin response
     * before the thread gets killed.
     * 
     * @return The number of seconds
     */
    public int getThreadTimeout()
    {
        return m_Configuration.getCommands().getCommandsTimeout();
    }
}
