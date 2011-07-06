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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for handling streams.
 * @author Massimiliano Ziccardi
 *
 */
public class CStreamManager
{
	private List m_vStreams = new ArrayList();
	private Log m_Logger = LogFactory.getLog(CStreamManager.class);
	
	public CStreamManager()
	{
		// TODO Auto-generated constructor stub
	}

	/**
     * Handles the received InputStream and returns it. 
     * @param in
     * @return
	 */
    public InputStream handle(InputStream in)
	{
		m_vStreams.add(in);
		return in;
	}

    /**
     * Handles the received OutputStream and returns it.
     * @param out
     * @return
     */
	public OutputStream handle(OutputStream out)
	{
		m_vStreams.add(out);
		return out;
	}

    /**
     * Handles the received Reader and returns it
     * @param r
     * @return
     */
	public Reader handle(Reader r)
	{
		m_vStreams.add(r);
		return r;
	}

    /**
     * Handles the received Writer and returns it
     * @param w
     * @return
     */
	public Writer handle(Writer w)
	{
		m_vStreams.add(w);
		return w;
	}
	
	/**
     * Returns an InputStream on the given file 
     * @param f
     * @return
     * @throws FileNotFoundException
	 */
    public InputStream getInputStream(File f) throws FileNotFoundException
	{
		return (InputStream) handle(new FileInputStream(f));
	}

    /**
     * Returns an OutputStream on the given file
     * @param f
     * @return
     * @throws FileNotFoundException
     */
	public OutputStream getOutputStream(File f) throws FileNotFoundException
	{
		return handle(new FileOutputStream(f));
	}
	
    /**
     * Closes all handles streams and readers. Non exception is thrown.
     * This method should be called in the finally block.
     */
	public void closeAll()
	{
		for (Iterator iterator = m_vStreams.iterator(); iterator.hasNext();)
		{
			Object obj = (Object) iterator.next();

			try
			{
				if (obj instanceof InputStream)
				{
					((InputStream) obj).close();
					continue;
				}
				if (obj instanceof OutputStream)
				{
					((OutputStream) obj).flush();
					((OutputStream) obj).close();
					continue;
				}
				if (obj instanceof Reader)
				{
					((Reader) obj).close();
					continue;
				}
				if (obj instanceof Writer)
				{
					((Writer) obj).flush();
					((Writer) obj).close();
					continue;
				}
			}
			catch (Exception e)
			{
				if (m_Logger.isDebugEnabled())
					m_Logger.debug("EXCEPTION CLOSING STREAM/READER : " + e.getMessage());
			}
		}
	}
	
}
