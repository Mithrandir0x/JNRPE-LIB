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
package it.jnrpe.plugin.util;

import java.math.BigDecimal;

/**
 * Utility class for evaluating threshold 
 * This class conforms to the nagios plugin guidelines 
 * (http://nagiosplug.sourceforge.net/developer-guidelines.html#THRESHOLDFORMAT).
 * The generalised range format is: [@]start:end
 * Values are interpreted this way:
 * <ul>
 * <li>if range is of format "start:" and end is not specified, assume end is infinity
 * <li>to specify negative infinity, use "~"
 * <li>alert is raised if metric is outside start and end range (inclusive of endpoints)
 * <li>if range starts with "@", then alert if inside this range (inclusive of endpoints)
 * </ul>
 * start and ":" is not required if start=0.
 * 
 * @author Massimiliano Ziccardi
 */

public class ThresholdUtil
{
    /**
     * Returns <code>true</code> if the value <code>iValue</code> falls into the range <code>sRange</code>
     * @param sRange The range
     * @param iValue The value
     * @return
     */
    public static boolean isValueInRange (String sRange, int iValue)
    {
        return new Threshold(sRange).isValueInRange(iValue);
    }
    
    /**
     * Returns <code>true</code> if the value <code>dalue</code> falls into the range <code>sRange</code>
     * @param sRange The range
     * @param value  The value
     * @return
     */
    public static boolean isValueInRange (String sRange, BigDecimal value)
    {
        return new Threshold(sRange).isValueInRange(value);
    }
}



