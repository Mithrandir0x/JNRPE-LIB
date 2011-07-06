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
package it.jnrpe.net;

import java.io.IOException;
import java.io.InputStream;

/**
 * This object represent a generic request packet
 * 
 * @author Massimiliano Ziccardi
 */
public class CJNRPERequest extends CJNRPEProtocolPacket
{
	public CJNRPERequest(InputStream in) throws IOException, BadCRCException
	{
		fromInputStream(in);
		
		validate();
	}

}
