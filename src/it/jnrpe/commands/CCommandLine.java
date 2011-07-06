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
package it.jnrpe.commands;

import org.apache.commons.cli.CommandLine;

/**
 * Incapsulate the command line object, so that the plugins have
 * no dependencies against the command line library
 * @author Massimiliano Ziccardi
 *
 */
public class CCommandLine
{
    private CommandLine m_CommandLine = null;
    
    public CCommandLine(CommandLine cl)
    {
        m_CommandLine = cl;
    }
    
    /**
     * Returns the value of the specified option
     * @param sOptionName The option name
     * @return
     */
    public String getOptionValue(String sOptionName)
    {
        return m_CommandLine.getOptionValue(sOptionName);
    }

    /**
     * Returns the value of the specified option.
     * If the option is not present, returns the default value
     * @param sOptionName The option name
     * @param sDefaultValue The default value
     * @return
     */
    public String getOptionValue(String sOptionName, String sDefaultValue)
    {
        return m_CommandLine.getOptionValue(sOptionName, sDefaultValue);
    }

    /**
     * Returns the value of the specified option
     * @param cOption The option short name
     * @return
     */
    public String getOptionValue(char cOption)
    {
        return m_CommandLine.getOptionValue(cOption);
    }

    /**
     * Returns the value of the specified option
     * If the option is not present, returns the default value
     * @param cOption The option short name
     * @param sDefaultValue The default value
     * @return
     */
    public String getOptionValue(char cOption, String sDefaultValue)
    {
        return m_CommandLine.getOptionValue(cOption, sDefaultValue);
    }
    
    /**
     * Returns <code>true</code> if the option is present
     * @param sOptionName The option name
     * @return
     */
    public boolean hasOption(String sOptionName)
    {
        return m_CommandLine.hasOption(sOptionName);
    }

    /**
     * Returns <code>true</code> if the option is present
     * @param cOption The option short name
     * @return
     */
    public boolean hasOption(char cOption)
    {
        return m_CommandLine.hasOption(cOption);
    }
}
