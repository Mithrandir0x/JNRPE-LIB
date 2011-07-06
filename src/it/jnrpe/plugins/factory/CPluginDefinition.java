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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

/**
 * This class contains all the data JNRPE will need to instantiate the plugin
 * 
 * @author Massimiliano Ziccardi
 */
public class CPluginDefinition
{
	private String m_sPluginName = null;
	private String m_sPluginClass = null;
	private COptions m_Options = null;
	private String m_sDecription = null;
	
	public CPluginDefinition()
	{
		
	}
	
	public void setName(String sName)
	{
		m_sPluginName = sName;
	}

	/**
	 * Returns the name of the plugin
	 */
	public String getName()
	{
		return m_sPluginName;
	}

	public void setPluginClass(String sClassName)
	{
		m_sPluginClass = sClassName;
	}

	/**
	 * Returns the class name implementing the plugin
	 */
	public String getPluginClass()
	{
		return m_sPluginClass;
	}
	
	public void setOptions(COptions opts)
	{
		m_Options = opts;
	}
	
	public COptions getOptions()
	{
		return m_Options;
	}
	
    private String cleanDesc(String sDesc)
    {
        if (sDesc == null)
            return "";
        
        
        BufferedReader r = new BufferedReader(new StringReader(sDesc));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(new OutputStreamWriter(bout));
        
        String sLine = null;
        try
        {
            while ((sLine = r.readLine()) != null)
            {
                w.println(sLine.trim());
            }
            w.flush();
        }
        catch (IOException ioe)
        {
            // Ignore it. Should never happen
        }
        
        return new String (bout.toByteArray());
    }
	
	public void setDescription(String sDesc)
    {
        m_sDecription = cleanDesc(sDesc);
    }

    public String getDescription()
    {
        return m_sDecription;
    }
}
