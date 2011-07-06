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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import it.jnrpe.commands.CCommandLine;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugin.util.ThresholdUtil;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.utils.CStreamManager;

/**
 * Performs the following checks:
 * <ul>
 * <li>Checks that a file do not exist (param -F)
 * <li>Checks that a file exist (param -f)
 * <li>Checks file age (params -w and -c). Requires param -f
 * <li>Checks file size (params -W and -C). Requires param -f
 * <li>Checks how many time a string is repeatend inside a file (params -O). Requires param -f. The string cannot contain ','. Optionally, you can
 * specify WARNING and CRITICAL range. To specify the ranges, use a syntax like: 
 * <blockquote>
 *      -f path/to/your/file -O *YOURSTRING*,*WARNING*,*CRITICAL*
 * </blockquote>
 * If you do not specify the ranges, it's the same as:
 * <blockquote>
 *      -f path/to/your/file -O *YOURSTRING*,:0,:0
 * </blockquote>
 * This means that a CRITICAL state will be raised if the string is not present.
 * <B>Be careful! The whole file will be read. This can be quite slow with very large files</B>
 * <li>Check that a file do not contains a string (params -N). Strings cannot contains ','. 
 * Strings are separated by comma. Requires param -f
 * </ul>
 *  
 *  The params -w, -c, -W and -C requires as argument a range in the standard
 *  nagios format.
 *  
 * @author Massimiliano Ziccardi
 */
public class CCheckFile implements IPluginInterface
{
    private CReturnValue updateRes(CReturnValue res, CReturnValue newVal)
    {
        if (res == null)
            return newVal;
        
        switch (res.getReturnCode())
        {
        case IJNRPEConstants.STATE_CRITICAL:
            return res;
        case IJNRPEConstants.STATE_WARNING:
            if (newVal.getReturnCode() != IJNRPEConstants.STATE_CRITICAL)
                return res;
            return newVal;
        case IJNRPEConstants.STATE_OK:
            if (newVal.getReturnCode() == IJNRPEConstants.STATE_OK)
                return res;
            return newVal;
        default:
            return res;
        }
    }
    
    private CReturnValue checkFileExists(File f, CReturnValue res)
    {
        if (f.exists())
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK"));
        return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "FILE CRITICAL: File '" + f.getAbsolutePath() + "' do not exists"));
    }
    
    private CReturnValue checkAge(CCommandLine cl, File f, CReturnValue res)
    {
        if (cl.hasOption("critical"))
        {
            long lLastAccess = f.lastModified();
            long lNow = System.currentTimeMillis();
            BigDecimal lAge = new BigDecimal( "" + ((lNow - lLastAccess) / 1000) );
            String sCriticalThreshold = cl.getOptionValue("critical");
            
            if (ThresholdUtil.isValueInRange(sCriticalThreshold, lAge))
                return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "FILE CRITICAL - File '" + f.getName() + "' is older than " + sCriticalThreshold + " seconds"));
        }

        if (cl.hasOption("warning"))
        {
            long lLastAccess = f.lastModified();
            long lNow = System.currentTimeMillis();
            BigDecimal lAge = new BigDecimal( "" + ((lNow - lLastAccess) / 1000) );
            String sWarningThreshold = cl.getOptionValue("warning");
            
            if (ThresholdUtil.isValueInRange(sWarningThreshold, lAge))
                return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_WARNING, "FILE WARNING - '" + f.getName() + "' is older than " + sWarningThreshold + " seconds"));
        }
        
        return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK"));
    }
    
    private CReturnValue checkSize(CCommandLine cl, File f, CReturnValue res)
    {
        if (cl.hasOption("sizecritical"))
        {
            String sCriticalThreshold = cl.getOptionValue("sizecritical");
            BigDecimal bdSize = new BigDecimal("" + f.length());
            
            if (ThresholdUtil.isValueInRange(sCriticalThreshold, bdSize))
                return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "FILE CRITICAL - '" + f.getName() + "' is shorter than " + sCriticalThreshold + " bytes"));
        }


        if (cl.hasOption("sizewarning"))
        {
            String sWarningThreshold = cl.getOptionValue("sizewarning");
            BigDecimal bdSize = new BigDecimal("" + f.length());
            
            if (ThresholdUtil.isValueInRange(sWarningThreshold, bdSize))
                return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_WARNING, "FILE WARNING - '" + f.getName() + "' is shorter than " + sWarningThreshold + " bytes"));
        }

        return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK"));
    }

    private CReturnValue checkContains(CCommandLine cl, File f, CReturnValue res)
    {
        if (!cl.hasOption("contains"))
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK"));
        
        CStreamManager sm = new CStreamManager();
        
        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(sm.getInputStream(f)));
            String sLine = null;
            
            String sWarningThreshold = ":0";
            String sCriticalThreshold = ":0";
            
            String sPattern = cl.getOptionValue("contains");
            if (sPattern.indexOf(',') != -1)
            {
                String[] vsParts = sPattern.split(",");
                sWarningThreshold = vsParts[1];
                if (vsParts.length > 1)
                    sCriticalThreshold = vsParts[2];
                sPattern = vsParts[0];
            }

            int iCount = 0;
            
            while ((sLine = r.readLine()) != null)
            {
                if (sLine.indexOf(sPattern) != -1)
                    iCount ++;
                    //return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK"));
            }
            
            if (ThresholdUtil.isValueInRange(sCriticalThreshold, iCount))
                return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "FILE CRITICAL - String '" + sPattern + "' found " + iCount + " times"));
            if (ThresholdUtil.isValueInRange(sWarningThreshold, iCount))
                return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_WARNING, "FILE WARNING - String '" + sPattern + "' found " + iCount + " times"));
                
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK - String '" + sPattern + "' found " + iCount + " times"));
        }
        catch (Exception e)
        {
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "FILE UNKNOWN - " + e.getMessage()) );        
        }
        finally
        {
            sm.closeAll();
        }
        
    }

    private CReturnValue checkNotContains(CCommandLine cl, File f, CReturnValue res)
    {
        if (!cl.hasOption("notcontains"))
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK"));
        
        CStreamManager sm = new CStreamManager();
        
        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(sm.getInputStream(f)));
            String sLine = null;
            
            String[] vsPatterns = cl.getOptionValue("notcontains").split(",");
            
            while ((sLine = r.readLine()) != null)
            {
                for (int i = 0; i < vsPatterns.length; i++)
                    if (sLine.indexOf(vsPatterns[i]) != -1)
                        return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "FILE CRITICAL - String '" + cl.getOptionValue("notcontains") + "' found"));
            }
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_OK, "FILE OK: String '" + cl.getOptionValue("notcontains") + "' not found"));
        }
        catch (Exception e)
        {
            return updateRes(res, new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "FILE UNKNOWN - " + e.getMessage()) );        
        }
        finally
        {
            sm.closeAll();
        }
        
    }
    
    public CReturnValue execute(CCommandLine cl)
    {
        if (cl.hasOption("FILE"))
        {
            File f = new File(cl.getOptionValue("FILE"));
            if (f.exists())
                return new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "File '" + f.getName() + "' exists");
            else
                return new CReturnValue(IJNRPEConstants.STATE_OK, "File '" + f.getName() + "' is OK");
        }
        
        //CReturnValue res = new CReturnValue(IJNRPEConstants.STATE_OK, "CHECK_FILE: OK");
        CReturnValue res = null;
                
        File f = null;
        
        if (cl.hasOption("file"))
            f = new File(cl.getOptionValue("file"));
        else
            return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Either param -f or -F must be specified");
        
//        if (!f.exists())
//            return new CReturnValue(IJNRPEConstants.STATE_CRITICAL, "File '" + f.getName() + "' not found");
        
        // Verifico che il file esista
        if (res == null || res.getReturnCode() != IJNRPEConstants.STATE_CRITICAL)
            res = checkFileExists(f, res);
        
        if (res == null || res.getReturnCode() != IJNRPEConstants.STATE_CRITICAL)
            res = checkAge(cl, f, res);
        if (res == null || res.getReturnCode() != IJNRPEConstants.STATE_CRITICAL)
            res = checkSize(cl, f, res);
        if (res == null || res.getReturnCode() != IJNRPEConstants.STATE_CRITICAL)
            res = checkContains(cl, f, res);
        if (res == null || res.getReturnCode() != IJNRPEConstants.STATE_CRITICAL)
            res = checkNotContains(cl, f, res);
        
        return res;
    }

}
