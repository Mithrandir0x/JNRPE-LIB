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
package it.jnrpe.plugins;

import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.jnrpe.commands.CCommandLine;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.factory.COptions;

/**
 * This class was intended to abstract the kind of plugin to execute.
 * Hides command line parsing from command invoker.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CPluginProxy
{
	private IPluginInterface m_plugin = null;
	private COptions m_Options = null;
	private Log m_Logger = LogFactory.getLog(CPluginProxy.class);
	private String m_sPluginName = null;
	private String m_sDescription = null;
	
	public CPluginProxy(String sPluginName, String sPluginDescription, IPluginInterface plugin, COptions opts)
	{
		m_plugin = plugin;
		m_Options = opts;
		m_sPluginName = sPluginName;
		m_sDescription = sPluginDescription;
	}
	
	/**
     * Returns a collection of all the options accepted by this plugin 
     * @return
	 */
    public Collection getOptions()
    {
        return m_Options.getOptions();
    }
    
    public CReturnValue execute(String[] args)
	{
		CommandLineParser clp = new PosixParser();
		try
		{
			CommandLine cl = clp.parse(m_Options.toOptions(), args);
			return m_plugin.execute(new CCommandLine(cl));
		}
		catch (ParseException e)
		{
			m_Logger.error("ERROR PARSING PLUGIN ARGUMENTS", e);
			
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Bad arguments");
		}
		
	}

	public void printHelp()
	{
	    if (m_sDescription != null && m_sDescription.trim().length() != 0)
	    {
	        System.out.println ("Description : ");
	        System.out.println (m_sDescription);
	    }
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp(m_sPluginName, m_Options.toOptions());
	}

}
