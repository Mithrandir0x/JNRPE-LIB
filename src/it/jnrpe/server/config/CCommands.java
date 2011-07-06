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

import java.util.HashMap;
import java.util.Map;

/**
 * Container class for list of commands configuration
 * @author Massimiliano Ziccardi
 *
 */
public class CCommands
{
	private Map m_mCommandDefinitions = new HashMap();
	
	/**
	 * The number of seconds the server will wait to receive
	 * the result of the command.
	 */
	private int m_iTimeout = 5;
	
	public CCommands()
	{
	}
	
	public void addCommandDefinition(CCommandDefinition cc)
	{
		m_mCommandDefinitions.put(cc.getName(), cc);
	}
	
	public CCommandDefinition getCommandDefinition(String sCommandName)
	{
		return (CCommandDefinition) m_mCommandDefinitions.get(sCommandName);
	}
	
	public Map getCommandDefinitions()
	{
		return m_mCommandDefinitions;
	}
	
	public void setTimeout(String sTimeout)
	{
	    m_iTimeout = Integer.parseInt(sTimeout);
	}
	
	public int getCommandsTimeout()
	{
	    return m_iTimeout;
	}
}
