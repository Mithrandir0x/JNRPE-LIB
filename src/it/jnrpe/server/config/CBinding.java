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
 * Container class for binding configuration
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CBinding
{
	private String m_sIP = null;
	private int m_iPort = 5666;
	private boolean m_bSSL = true;
	private String m_sKeyStoreFile = null;
    private String m_sKeyStorePasswd = null;
    private CThreadFactoryConfiguration m_threadFactoryConf = new CThreadFactoryConfiguration();
    
    
	public CBinding()
	{
		
	}
	
	public void setAddress(String sAddress)
	{
		String[] vsParts = sAddress.split(":");
		
		m_sIP = vsParts[0];
		
		if (vsParts.length > 1)
			m_iPort = Integer.parseInt(vsParts[1]);
	}
	
	public String getIP()
	{
		return m_sIP;
	}

	public int getPort()
	{
		return m_iPort;
	}
    
    public void setSSL(String sSSL)
    {
        m_bSSL = sSSL.equalsIgnoreCase("true");
    }
    
    public boolean useSSL()
    {
        return m_bSSL;
    }
    
    public void setKeyStoreFile(String sKeyStoreFile)
    {
        m_sKeyStoreFile = sKeyStoreFile;
    }
    
    public void setKeyStorePassword(String sPwd)
    {
        m_sKeyStorePasswd = sPwd;
    }
    
    public String getKeyStoreFile()
    {
        return m_sKeyStoreFile;
    }
    
    public String getKeyStorePassword()
    {
        return m_sKeyStorePasswd;
    }
    
    public void setThreadFactoryConfig(CThreadFactoryConfiguration conf)
    {
        m_threadFactoryConf = conf;
    }

    public CThreadFactoryConfiguration getThreadFactoryConfig()
    {
        return m_threadFactoryConf;
    }
}
