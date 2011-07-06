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

/**
 * This class is just a container for the plugin result
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CReturnValue
{
	private int m_iReturnCode = 0;
	private String m_sMessage = null;
	
	public CReturnValue(int iReturnCode, String sMessage)
	{
		m_iReturnCode = iReturnCode;
		m_sMessage = sMessage;
	}
	
	public int getReturnCode()
	{
		return m_iReturnCode;
	}
	
	public String getMessage()
	{
		return m_sMessage;
	}
}
