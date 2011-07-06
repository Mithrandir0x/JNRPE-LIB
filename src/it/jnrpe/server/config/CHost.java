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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Generic host information
 * @author Massimiliano Ziccardi
 *
 */
public class CHost
{
	private String m_sIP = null;
	private InetAddress m_InetAddress = null;
	
	
	public CHost()
	{
	}

	public String getIp()
	{
		return m_sIP;
	}

	public void setIp(String sIP)
	{
		m_sIP = sIP;
		
		try
		{
			InetAddress add = InetAddress.getByName(sIP);
			m_InetAddress = add;
		}
		catch (UnknownHostException e)
		{
		    // TODO: this exception should be handled...
		}
	}
	
	public InetAddress getInetAddress()
	{
		return m_InetAddress;
	}
	
}
