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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Container class for command definition configuration
 * @author Massimiliano Ziccardi
 *
 */
public class CCommandDefinition
{
    private static final Log LOG = LogFactory.getLog(CCommandDefinition.class);
    
	private String m_sName = null;
	private String m_sPluginName = null;
	private String m_sArgs = null;

    private List m_vArguments = new ArrayList();
    
	public CCommandDefinition()
	{
	}
	
	public void setName(String sName)
	{
		m_sName = sName;
	}

	public void setPluginName(String sPluginName)
	{
		m_sPluginName = sPluginName;
	}
	
	public void setArgs(String sArgs)
	{
		m_sArgs = sArgs;
	}

	public String getName()
	{
		return m_sName;
	}

	public String getPluginName()
	{
		return m_sPluginName;
	}
	
	public String getArgs()
    {
	    return m_sArgs;
    }
    
    private static String quote(String s)
    {
        if (s.indexOf(' ') != -1)
            return "\"" + s + "\"";
        return s;
    }
    
    /**
     * Merges the command line definition read from the server config file
     * with the values received from check_nrpe and produces a clean command line.
     * 
     * @return
     */
    // FIXME: Every parameter is added 2 times
    public String[] getCommandLine()
	{
		// Building args using old args syntax and new args syntax
//        if (m_sArgs == null)
//            m_sArgs = "";
//        
//        for (Iterator iter = m_vArguments.iterator(); iter.hasNext();)
//        {
//            CCommandArgument arg = (CCommandArgument) iter.next();
//            
//            String sArgName = arg.getName();
//            String sArgVal = arg.getValue();
//            
//            m_sArgs += " " + (sArgName.length() == 1 ? "-" : "--") + sArgName;
//            if (sArgVal != null)
//                m_sArgs += " " + sArgVal;
//        }
//        
//        return m_sArgs;
        
        LOG.debug("PARSING COMMAND LINE DEFINITION");
        
        String[] vsRes = null;
        String[] args = m_sArgs != null ? split(m_sArgs) : new String[0];
        List vArgs = new ArrayList();
        
        if (args.length != 0)
            LOG.warn("params ATTRIBUTE OF TAG command IS DEPRECATED");
        
        LOG.debug("OLD COMMAND LINE FORMAT : " + m_sArgs);
        
        int iStartIndex = 0;

        for (Iterator iter = m_vArguments.iterator(); iter.hasNext();)
        {
            CCommandArgument arg = (CCommandArgument) iter.next();
            
            if (LOG.isDebugEnabled())
                LOG.debug("NEW FORMAT COMMAND ARGUMENT " + arg.getName());
            
            String sArgName = arg.getName();
            String sArgVal = arg.getValue();
          
            vArgs.add((sArgName.length() == 1 ? "-" : "--") + sArgName);
            
            if (sArgVal != null)
                vArgs.add(quote(sArgVal));
        }
        
        vsRes = new String[args.length + vArgs.size()];
        
        for (Iterator iter = vArgs.iterator(); iter.hasNext();)
        {
            String sArg = (String) iter.next();
            
            if (LOG.isDebugEnabled())
                LOG.debug("ADDING NEW ARGUMENT TO ARG LIST : " + sArg);
            
            vsRes[iStartIndex++] = sArg;
        }
        
        //vsRes = new String[args.length + m_vArguments.size()];
        System.arraycopy(args, 0, vsRes, iStartIndex, args.length);
        
        if (LOG.isDebugEnabled())
            LOG.debug("RETURNING " + vsRes.length + " PARAMETERS");
        
        return vsRes;
	}

    /**
     * This method splits the command line.
     * This release does not handle correctly the ' and the " character
     * @param sCommandLine
     * @return
     */
    private static String[] split(String sCommandLine)
    {
        char[] vc = sCommandLine.trim().toCharArray();
        char[] vcTmp = new char[vc.length];
        
        boolean bOpenQuote = false;
        List vArgs = new ArrayList();
        int iLen = 0;
        
        for (int i = 0; i < vc.length; i++)
        {
            if (vc[i] == '\'' || vc[i] =='\"')
            {
                bOpenQuote = !bOpenQuote;
                continue;
            }
            
            if (vc[i] == ' ' && !bOpenQuote)
            {
                vArgs.add(new String(vcTmp, 0, iLen));
                iLen = 0;
                vcTmp = new char[vc.length];
                continue;
            }
            
            vcTmp[iLen++] = vc[i];
        }
        
        if (iLen != 0)
            vArgs.add(new String(vcTmp, 0, iLen));
        
        String[] vsRes = new String[vArgs.size()];
        
        int i = 0;
        //for (String s: vArgs)
        for (Iterator iter = vArgs.iterator(); iter.hasNext(); )
            vsRes[i++] = (String) iter.next();
        
        return vsRes;
        
    }
    
    
    
    public void addArgument(CCommandArgument arg)
    {
        m_vArguments.add(arg);
    }
}
