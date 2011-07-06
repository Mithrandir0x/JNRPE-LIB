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

import it.jnrpe.server.config.CThreadFactoryConfiguration;

import java.net.Socket;

/**
 * This class implements a simple thread factory.
 * Each binding has its own thread factory.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CThreadFactory
{
    /**
     * Timeout handler
     */
    private ThreadTimeoutWatcher m_watchDog = null;
    
    /**
     * Constructs a new thread factory
     * @param conf The thread configuration
     */
    public CThreadFactory(CThreadFactoryConfiguration conf)
    {
        // TODO: handle the unit of measure
        int iTimeout = conf.getThreadTimeout();
        
        m_watchDog = new ThreadTimeoutWatcher();
        m_watchDog.setThreadTimeout(iTimeout);
        m_watchDog.start();
    }
    
    /**
     * Asks the system level thread factory for a new thread.
     * @param s The socket to be served by the thread
     * @return The newly created thread
     */
    public JNRPEServerThread createNewThread(Socket s)
    {
        JNRPEServerThread t = JNRPEServerThreadFactory.getInstance().createNewThread(s);
        m_watchDog.watch(t);
        return t;
    }
    
    /**
     * Stops all the created threads and stops the timeout watcher
     */
    public void shutdown()
    {
        try
        {
            m_watchDog.stopWatching();
            // Waits for the thread to stop.
            m_watchDog.interrupt();
            m_watchDog.join(5000);
        }
        catch (InterruptedException ie)
        {
            // This should never happen...
        }        
    }
}
