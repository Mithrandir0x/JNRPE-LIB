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
package it.jnrpe.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.CPluginProxy;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.plugins.factory.CPluginFactory;
import it.jnrpe.plugins.factory.PluginInstantiationException;
import it.jnrpe.server.config.CCommandDefinition;
import it.jnrpe.server.config.CJNRPEConfiguration;
import it.jnrpe.utils.CStringUtil;

/**
 * This class is used to invoke a command.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CCommandInvoker
{
	private static Log m_Logger = LogFactory.getLog(CCommandInvoker.class);
	
	private CCommandInvoker()
	{
		
	}

	/**
	 * This method executes built in commands or builds a CommandDefinition 
	 * to execute external commands (plugins).
	 * The methods also expands the $ARG?$ macros
	 * 
	 * @param sCommandName The name of the command, as configured in the 
	 * server configuration XML
	 * @param args The arguments to pass to the command as configured in the
	 * server configuration XML (with the $ARG?$ macros) 
	 * @return The result of the command
	 */
	public static CReturnValue invoke(String sCommandName, String[] args)
	{
		if (sCommandName.equals("_NRPE_CHECK"))
		{
			m_Logger.trace("_NRPE_CHECK RECEIVED. RETURNING SERVER VERSION");
			return new CReturnValue(IJNRPEConstants.STATE_OK, "JNRPE v" + IJNRPEConstants.VERSION);
		}
		
		CCommandDefinition cd = (CCommandDefinition) CJNRPEConfiguration.getInstance().getCommandDefinitions().get(sCommandName);
		
		if (cd == null)
		{
			m_Logger.error("ERROR. REQUESTED COMMAND '" + sCommandName + "' DOES NOT EXISTS");
			
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Bad command");
		}
		
		return invoke(cd, args);
	}
	
	/**
	 * This method executes external commands (plugins)
	 * The methods also expands the $ARG?$ macros 
	 * 
	 * @param cd The command definition
	 * @param args The arguments to pass to the command as configured in the
	 * server configuration XML (with the $ARG?$ macros) 
	 * @return The result of the command
	 */
	public static CReturnValue invoke(CCommandDefinition cd, String[] args)
	{
		if (m_Logger.isInfoEnabled())
			m_Logger.trace("INVOKING " + cd.getName());
		
		if (m_Logger.isDebugEnabled())
			m_Logger.debug("PLUGIN : " + cd.getPluginName());
		
		String sPluginName = cd.getPluginName();
		
		String[] sCommandLine = cd.getCommandLine();
		
		if (CJNRPEConfiguration.getInstance().acceptParams())
            for (int j = 0; sCommandLine != null && j < sCommandLine.length; j++)
    			for (int i = 0; i < args.length; i++)
    			{
                    sCommandLine[j] = CStringUtil.replaceAll(sCommandLine[j], "$ARG" + (i + 1) + "$", args[i]);
    			}
		
		CPluginProxy plugin;
		try
		{
			plugin = CPluginFactory.getInstance().getPlugin(sPluginName);
		}
		catch (PluginInstantiationException e)
		{
			m_Logger.error(e.getMessage(), e);
			
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Configuration error");
		}
		
		if (plugin == null)
		{
			m_Logger.error("CONFIGURATION ERROR. COMMAND " + cd.getName() + " REFERS TO NON EXISTENT PLUGIN '" + sPluginName + "'");
			
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Configuration error");
		}
		
		try
		{
			if (sCommandLine != null)
			{
			    m_Logger.debug("PARAMETERS : " + sCommandLine);
			    return plugin.execute(sCommandLine);
			}
			else
				return plugin.execute(new String[0]);
		}
		catch (RuntimeException re)
		{
		    m_Logger.error("ERROR EXECUTING PLUGIN '" + sPluginName + "'", re);
		    return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Plugin execution error");
		}
		catch (Throwable thr)
		{
			m_Logger.error("ERROR EXECUTING PLUGIN '" + sPluginName + "'", thr);
			
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Plugin execution error");
		}
	}
	
}
