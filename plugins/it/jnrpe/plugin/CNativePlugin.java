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
package it.jnrpe.plugin;

import it.jnrpe.commands.CCommandLine;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.utils.CStreamManager;
import it.jnrpe.utils.CStringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Executes external (executable) plugins
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CNativePlugin implements IPluginInterface
{
	
	public CNativePlugin()
	{
		
	}
	
	/**
	 * The first parameter must be the <bold>full</bold> path to the
	 * executable.
	 * 
	 * The rest of the array is sent to the executable as commands parameters
	 */
	public CReturnValue execute(CCommandLine cl)
	{
		File fProcessFile = new File(cl.getOptionValue("executable"));
		CStreamManager streamMgr = new CStreamManager();
		
		if (!fProcessFile.exists())
		{
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Could not exec executable : " + fProcessFile.getAbsolutePath());
		}
		
		try
		{
			//Process p = Runtime.getRuntime().exec(cl.getOptionValue("executable"), CStringUtil.split(cl.getOptionValue("args", ""), false));
		    String[] vsParams = CStringUtil.split(cl.getOptionValue("args", ""), false);
		    String[] vCommand = new String[vsParams.length + 1];
		    vCommand[0] = cl.getOptionValue("executable");
		    System.arraycopy(vsParams, 0, vCommand, 1, vsParams.length);
		    Process p = Runtime.getRuntime().exec(vCommand);
		    //Process p = Runtime.getRuntime().exec(cl.getOptionValue("executable") + " " +  cl.getOptionValue("args", ""));
			BufferedReader br = (BufferedReader) streamMgr.handle(new BufferedReader(new InputStreamReader(p.getInputStream())));
			String sMessage = br.readLine();
			int iReturnCode = p.waitFor();
			
			return new CReturnValue(iReturnCode, sMessage);
		}
		catch (Exception e)
		{
			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Could not exec executable : " + fProcessFile.getName() + " - ERROR : " + e.getMessage());
		}
		finally
		{
			streamMgr.closeAll();
		}
	}
}
