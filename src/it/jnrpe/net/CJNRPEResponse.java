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

import java.util.zip.CRC32;

/**
 * This object represent a generic response packet
 * 
 * @author Massimiliano Ziccardi
 */
public class CJNRPEResponse extends CJNRPEProtocolPacket
{
	public CJNRPEResponse()
	{
		super();
		setPacketType(IJNRPEConstants.RESPONSE_PACKET);
	}
	
	public void updateCRC()
	{
		setCRC(0);
		int iCRC = 0;
		
		CRC32 crcAlg = new CRC32();
		crcAlg.update(toByteArray());
		
		iCRC = (int) crcAlg.getValue();
		
		setCRC(iCRC);
	}
	
	public void setMessage (String sMessage)
	{
		initRandomBuffer();
		_setMessage(sMessage);
	}
}
