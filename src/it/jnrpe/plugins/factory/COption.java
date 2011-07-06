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
package it.jnrpe.plugins.factory;

import org.apache.commons.cli.Option;

public class COption
{
//	<option opt="" hasArgs="" required="" optionalArgs="" 
//		argName="" argsCount="" longOpt="" type="" valueSeparator="" 
//		description=""/>
	
	private String m_sOption = null;
	private boolean m_bHasArgs = false;
	private Integer m_iArgsCount = null;
	private boolean m_bRequired = false;
	private Boolean m_bArgsOptional = null;
	private String m_sArgName = null;
	private String m_sLongOpt = null;
	private String m_sType = null;
	private String m_sValueSeparator = null;
	private String m_sDescription = null;
	
	public COption()
	{
		
	}
	
	public String getOption()
	{
		return m_sOption;
	}
	
	public void setOption(String sOption)
	{
		m_sOption = sOption;
	}
	
	public boolean hasArgs()
	{
		return m_bHasArgs;
	}
	
	public void setHasArgs(String sHasArgs)
	{
		m_bHasArgs = sHasArgs.equals("true");
	}
	
	public Integer getArgsCount()
	{
		return m_iArgsCount;
	}
	
	public void setArgsCount(String sArgsCount)
	{
		m_iArgsCount = new Integer(sArgsCount);
	}
	
	public String getRequired()
	{
		return "" + m_bRequired;
	}
	
	public void setRequired(String sRequired)
	{
		m_bRequired = sRequired.equals("true");
	}
	
	public Boolean getArgsOptional()
	{
		return m_bArgsOptional;
	}
	
	public void setArgsOptional(String sArgsOptional)
	{
		m_bArgsOptional = new Boolean(sArgsOptional.equals("true"));
	}
	
	public String getArgName()
	{
		return m_sArgName;
	}
	
	public void setArgName(String sArgName)
	{
		m_sArgName = sArgName;
	}
	
	public String getLongOpt()
	{
		return m_sLongOpt;
	}
	
	public void setLongOpt(String sLongOpt)
	{
		m_sLongOpt = sLongOpt;
	}
	
	public String getType()
	{
		return m_sType;
	}
	
	public void setType(String sType)
	{
		m_sType = sType;
	}
	
	public String getValueSeparator()
	{
		return m_sValueSeparator;
	}
	
	public void setValueSeparator(String sValueSeparator)
	{
		m_sValueSeparator = sValueSeparator;
	}
	
	public String getDescription()
	{
		return m_sDescription;
	}

	public void setDescription(String sDescription)
	{
		m_sDescription = sDescription;
	}
	
	Option toOption()
	{
		Option ret = new Option(m_sOption, m_sDescription);
		
		if (m_bArgsOptional != null)
			ret.setOptionalArg(m_bArgsOptional.booleanValue());
		
		if (m_bHasArgs)
		{
			if (m_iArgsCount == null)
				ret.setArgs(Option.UNLIMITED_VALUES);
		}
		
		ret.setRequired(m_bRequired);
		if (m_iArgsCount != null)
			ret.setArgs(m_iArgsCount.intValue());
		
		if (m_sArgName != null)
		{
			if (m_iArgsCount == null)
				ret.setArgs(Option.UNLIMITED_VALUES);
			ret.setArgName(m_sArgName);
		}
		
		if (m_sLongOpt != null)
			ret.setLongOpt(m_sLongOpt);
		
		if (m_sValueSeparator != null && m_sValueSeparator.length() != 0)
			ret.setValueSeparator(m_sValueSeparator.charAt(0));
		
		return ret;
	}
}
