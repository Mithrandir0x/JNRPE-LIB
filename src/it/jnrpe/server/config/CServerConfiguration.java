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

import java.util.ArrayList;
import java.util.List;

public class CServerConfiguration
{
	private List m_vBindings = new ArrayList();
	private List m_vAcceptedHost = new ArrayList();
	private List m_vPluginDirs = new ArrayList();
	private String m_sAcceptParams = "false";
	
	public CServerConfiguration()
	{
		
	}
	
	public void addBinding(CBinding binding)
	{
		m_vBindings.add(binding);
	}
	
	public void addAcceptedHost(CHost host)
	{
		m_vAcceptedHost.add(host);
	}
	
	public void addPluginDir(CPluginDir dir)
	{
		m_vPluginDirs.add(dir);
	}

	public List getBindings()
	{
		return m_vBindings;
	}

	public List getAcceptedHosts()
	{
		return m_vAcceptedHost;
	}

	public List getPluginDirs()
	{
		return m_vPluginDirs;
	}
	
	public void setAcceptParams(String sAcceptParams)
	{
		m_sAcceptParams = sAcceptParams;
	}

	public String getAcceptParams()
	{
		return m_sAcceptParams;
	}
}

