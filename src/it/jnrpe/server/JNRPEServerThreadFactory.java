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

import java.net.Socket;

/**
 * This class is a system level thread factory.
 * In later version, it will be used to define the behavior of 
 * thread creation at the whole server scope.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class JNRPEServerThreadFactory 
{
    private static JNRPEServerThreadFactory m_instance = null;
    
    private JNRPEServerThreadFactory()
    {
        
    }
  
    /**
     * In this version thread are always created
     * 
     * @param s The socked served by the thread
     * @return The thread
     */
    public JNRPEServerThread createNewThread(Socket s)
    {
        return new JNRPEServerThread(s);
    }
    
    /**
     * Return an instance of the system level thread factory
     * @return
     */
    public static JNRPEServerThreadFactory getInstance()
    {
        if (m_instance == null)
            m_instance = new JNRPEServerThreadFactory();
        
        return m_instance;
    }
}
