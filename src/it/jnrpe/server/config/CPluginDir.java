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

/**
 * Container class for plugin directory configuration
 * @author Massimiliano Ziccardi
 *
 */
public class CPluginDir
{
	private String m_sPath = null;
	
	
	public CPluginDir()
	{
	}

	public void setPath(String sPath)
	{
		m_sPath = sPath;
	}

	public String getPath()
	{
		return m_sPath;		
	}
}
