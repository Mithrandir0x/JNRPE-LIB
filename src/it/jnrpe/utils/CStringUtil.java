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
package it.jnrpe.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains utility methos for manipulating strings.
 * 
 * @author Massimiliano Ziccardi
 * 
 */
public class CStringUtil
{
    /**
     * Replaces all the occurrences of a given string with another string
     * 
     * @param sInString
     *            The original string
     * @param sReplaceWhat
     *            The string to replace
     * @param sReplaceWith
     *            The string to replace with
     * @return The result of the replacement.
     */
    public static String replaceAll(String sInString, String sReplaceWhat, String sReplaceWith)
    {
        int iIndex = 0;

        while ((iIndex = sInString.indexOf(sReplaceWhat)) != -1)
        {
            String sPrefix = "";

            if (iIndex != 0)
                sPrefix = sInString.substring(0, iIndex);

            String sPostfix = "";

            if (iIndex + sReplaceWhat.length() < sInString.length())
                sPostfix = sInString.substring(iIndex + sReplaceWhat.length(), sInString.length());

            sInString = sPrefix + sReplaceWith + sPostfix;
        }

        return sInString;
    }

    /**
     * This is a simple utility to split strings.
     * The string is splitted following these rules (in the order):
     * <ul>
     * <li>If a single quote (') or a double quote (") is found at the start of the word, the split will
     * occour at the next quote or double quote
     * <li>Otherwise, the split occurres as soon as a space is found.
     * </ul>
     * @param sString The string to split
     * @param bIgnoreQuotes For future implementation
     * @return The splitted string
     * 
     * @since JNRPE Server 1.04
     */
    public static String[] split(String sString, boolean bIgnoreQuotes)
    {
        List vRes = new ArrayList();
        char splitChar = ' ';
        char[] vChars = sString.trim().toCharArray();

        StringBuffer sbCurrentToken = new StringBuffer();

        for (int i = 0; i < vChars.length; i++)
        {
            if (vChars[i] == splitChar)
            {
                if (sbCurrentToken.toString().length() != 0)
                {
                    vRes.add(sbCurrentToken.toString());
                    sbCurrentToken = new StringBuffer();
                    splitChar = ' ';
                    
                }
                continue;
            }
            
            switch (vChars[i])
            {
            case '"':
            case '\'':
                if (sbCurrentToken.length() == 0 && splitChar == ' ')
                {
                    splitChar = vChars[i];
                    break;
                }
            default:
                sbCurrentToken.append(vChars[i]);
            }
        }

        if (sbCurrentToken.length() != 0)
            vRes.add(sbCurrentToken.toString());
        
        String[] vRet = new String[vRes.size()];
        
        for (int i = 0; i < vRet.length; i++)
            vRet[i] = (String) vRes.get(i);
            
        return vRet;
    }
    
    public static void main(String[] args)
    {
        String sCommand = "'c:/program files/prova' -a '\"param 1\" \"param 2\"'";
        String[] sSplitted = split(sCommand, false);
        
        for (int i = 0; i < sSplitted.length; i++)
        {
            System.out.println (sSplitted[i]);
        }
    }
}
