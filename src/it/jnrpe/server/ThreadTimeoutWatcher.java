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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements a Thread that kills all the JNRPEServerThread 
 * threads that runs for more than a specified number of seconds.
 * 
 * @author Massimiliano Ziccardi
 */
public class ThreadTimeoutWatcher extends Thread
{
    /**
     * The list of the currently executing thread
     */
    private List m_ThreadList = Collections.synchronizedList(new ArrayList());
    
    /**
     * Checks whether a thread needs to be killed every m_iPollingTime seconds (defaults to 2).
     */
    private static int m_iPollingTime = 2;
    
    /**
     * This variable is used to stop the thread
     */
    private static boolean m_bRun = true;
    
    /**
     * The maximum number of seconds a thread is allowed to run (default 10)
     */
    private static int m_iThreadTimeout = 20;
    
    private Log m_Logger = LogFactory.getLog(ThreadTimeoutWatcher.class);
    
    /**
     * Utility class used to store thread informations
     * 
     * @author Massimiliano Ziccardi
     */
    private static class CThreadData
    {
        private Thread m_thread;
        private long m_lStartTime;
        
        CThreadData(Thread t)
        {
            m_thread = t;
            m_lStartTime = System.currentTimeMillis();
        }
        
        public long getStartTime()
        {
            return m_lStartTime;
        }
        
        public Thread getThread()
        {
            return m_thread;
        }
    }

    public ThreadTimeoutWatcher()
    {
        super("Tindalos");
    }
    
    /**
     * Adds the specified thread to the list of threads to be watched
     * @param t
     */
    public void watch (Thread t)
    {
        m_ThreadList.add(new CThreadData(t));
    }

    /**
     * Stop the thread execution
     */
    public void stopWatching()
    {
        m_bRun = false;
    }
    
    /**
     * Kills the thread identified by the specified thread data
     * @param td The thread data
     */
    private void killThread(CThreadData td)
    {
        Thread t = td.getThread();
        if (t.isAlive())
        {
            if (m_Logger.isInfoEnabled())
                m_Logger.warn("Killing thread. It was running since " + ((System.currentTimeMillis() - td.getStartTime()) / 1000) + " seconds ago");
            ((JNRPEServerThread)t).stopNow();
        }
    }
    
    /**
     * Gets the oldest thread from the list and, if needed, kills it.
     * Threads no longer running gets removed from the list.
     * @return <code>true</code> if at leas one thread has been removed from the list.
     */
    private boolean killOldestThread()
    {
        // The thread in the first position is always the oldest one
        CThreadData td = (CThreadData) m_ThreadList.get(0);
        Thread t = td.getThread();
        
        // If the thread is not alive, or if the thread is older than THREAD_TIMEOUT, it must be killed
        // and removed from the list
        if (!t.isAlive() || (System.currentTimeMillis() - td.getStartTime() >= (m_iThreadTimeout * 1000)))
        {
            killThread(td);
            m_ThreadList.remove(0);
            return true;
        }
        
        return false;
    }

    public void setThreadTimeout(int iTimeout)
    {
        m_iThreadTimeout = iTimeout;
    }
    
    public void run()
    {
        while (m_bRun)
        {
            try
            {
                Thread.sleep(m_iPollingTime * 1000);
            }
            catch (InterruptedException e)
            {
            }
            try
            {
                // Keep on killing threads as long as there are threads to kill
                while (!m_ThreadList.isEmpty() && killOldestThread());
            }
            catch (Throwable thr)
            {
                // This thread must never die
                m_Logger.fatal("Error " + thr.getMessage(), thr);
            }
        }
        
        // Stops all the threads currently running
        for (Iterator it = m_ThreadList.iterator(); it.hasNext(); )
            killThread((CThreadData) it.next());
    }
}
