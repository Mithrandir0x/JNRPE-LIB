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
package it.jnrpe.server;
import it.jnrpe.commands.CCommandInvoker;
import it.jnrpe.net.BadCRCException;
import it.jnrpe.net.CJNRPERequest;
import it.jnrpe.net.CJNRPEResponse;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.utils.CStreamManager;

import java.net.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread used to server client request 
 * 
 * @author Massimiliano Ziccardi
 */
class JNRPEServerThread extends Thread
{
	private Log m_Logger = LogFactory.getLog(JNRPEServerThread.class);
	private Socket socket = null;
	private Boolean m_bStopped = Boolean.FALSE;
	
	public JNRPEServerThread(Socket socket)
	{
		super("JNRPEServerThread");
		this.socket = socket;
	}

	public CJNRPEResponse handleRequest (CJNRPERequest req)
	{
		// extracting command name and params
		String[] vParts = req.getStringMessage().split("!");
		
		String sCommandName = vParts[0];
		String[] vArgs = new String[vParts.length - 1];
		
		System.arraycopy(vParts, 1, vArgs, 0, vArgs.length);
		
		CReturnValue ret = CCommandInvoker.invoke(sCommandName, vArgs);
		
		CJNRPEResponse res = new CJNRPEResponse();
		res.setPacketVersion(IJNRPEConstants.NRPE_PACKET_VERSION_2);
		
		res.setResultCode(ret.getReturnCode());
		res.setMessage(ret.getMessage());
		res.updateCRC();
		
		return res;
	}
	
	public void run()
	{
		CStreamManager streamMgr = new CStreamManager();
		
		try
		{
			InputStream in = streamMgr.handle(socket.getInputStream());
			CJNRPEResponse res = null;
			CJNRPERequest req = null;
			
			try
			{
				req = new CJNRPERequest(in);

				switch (req.getPacketType())
				{
				case IJNRPEConstants.QUERY_PACKET:
						res = handleRequest(req);
					break;
				default:
					m_Logger.error("UNKNOWN PACKET TYPE " + req.getPacketType());

					res = new CJNRPEResponse();
					res.setPacketVersion(req.getPacketVersion());
					res.setResultCode(IJNRPEConstants.STATE_UNKNOWN);
					res.setMessage("Invalid Packet Type");
					res.updateCRC();
					
				}
				
			}
			catch (BadCRCException e)
			{
				m_Logger.error("BAD REQUEST CRC");
				
				res = new CJNRPEResponse();
				res.setPacketVersion(IJNRPEConstants.NRPE_PACKET_VERSION_2);
				res.setResultCode(IJNRPEConstants.STATE_UNKNOWN);
				res.setMessage("BAD REQUEST CRC");
				res.updateCRC();
				
			}
			
			synchronized(m_bStopped)
			{
			    if (!m_bStopped.booleanValue())
			    {
        			OutputStream out = streamMgr.handle(socket.getOutputStream());
        			out.write(res.toByteArray());
			    }
			}
		}
		catch (IOException e)
		{
			if (!m_bStopped.booleanValue())
			    m_Logger.error("ERROR DURING SOCKET OPERATION.", e);
		}
		finally
		{
		    try
			{
		        if (socket != null && !socket.isClosed())
		            socket.close();
			}
			catch (IOException e)
			{
				m_Logger.error("ERROR CLOSING SOCKET", e);
			}
			
			streamMgr.closeAll();
		}
		
	}
	
	public void stopNow()
    {
	    CStreamManager streamMgr = new CStreamManager();
	    try
	    {
    	    synchronized (m_bStopped)
    	    {
    	        // If the socket is closed, the thread has finished...
    	        if (!socket.isClosed())
    	        {
        	        m_bStopped = Boolean.TRUE;
        	        
        	        try
        	        {
            	        CJNRPEResponse res = new CJNRPEResponse();
                        res.setPacketVersion(IJNRPEConstants.NRPE_PACKET_VERSION_2);
                        res.setResultCode(IJNRPEConstants.STATE_UNKNOWN);
                        res.setMessage("Command execution timeout");
                        res.updateCRC();
            	        
                        OutputStream out = streamMgr.handle(socket.getOutputStream());
                        out.write(res.toByteArray());
                    
            	        // This is just to stop any socket operations...
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                    }
                    
                    // Let's try to interrupt all other operations...
                    if (this.isAlive())
                        this.interrupt();
                    
                    // We can exit now..
    	        }
    	    }
	    }
	    catch (Exception e)
	    {
	        
	    }
	    finally
	    {
	        streamMgr.closeAll();
	    }
    }
}
