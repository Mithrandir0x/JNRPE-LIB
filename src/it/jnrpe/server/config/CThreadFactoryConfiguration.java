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
 * Simple class to contain Thread Factory configuration parameters
 * @author Massimiliano Ziccardi
 *
 */
public class CThreadFactoryConfiguration
{
    private int m_iTimeout = 5;
    private String m_sUnit = "seconds";
    
    public CThreadFactoryConfiguration()
    {
        
    }
    
    public void setTimeout(String sTimeout)
    {
        m_iTimeout = Integer.parseInt(sTimeout);
    }
    
    public void setUnit(String sUnit)
    {
        m_sUnit = sUnit;
    }
    
    public int getThreadTimeout()
    {
        return m_iTimeout;
    }
    
    public String getUnit()
    {
        return m_sUnit;
    }
    
}
