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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.Options;

public class COptions
{
	private List m_vOptions = new ArrayList();
	
	public COptions()
	{
		
	}
	
	public void addOption(COption opt)
	{
		m_vOptions.add(opt);
	}
	
	public Options toOptions()
	{
		Options opts = new Options();
		
		//for (COption opt: m_vOptions)
		for (Iterator i = m_vOptions.iterator(); i.hasNext(); )
			opts.addOption(((COption) i.next()).toOption());
		
		return opts;
	}
    
    public Collection getOptions()
    {
        return m_vOptions;
    }
}
