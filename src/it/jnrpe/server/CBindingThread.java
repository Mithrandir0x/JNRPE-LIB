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

import it.jnrpe.server.config.CBinding;
import it.jnrpe.server.config.CHost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Thread that listen on a given IP:PORT.
 * 
 * @author Massimiliano Ziccardi
 */
public class CBindingThread extends Thread
{
    private Log m_Logger = LogFactory.getLog(CBindingThread.class);

    private ServerSocket m_serverSocket = null;
    
    private CBinding m_Binding = null;
    private List m_vAcceptedHosts = new ArrayList();
    private CThreadFactory m_threadFactory = null;
    
    public CBindingThread(CBinding binding) throws IOException
    {
        m_Binding = binding;
        try
        {
            init();
        }
        catch (Exception e)
        {
            throw new BindException(e.getMessage());
        }
    }

    /**
     * Returns the SSL factory to be used to create the Server Socket
     * @throws KeyStoreException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws CertificateException 
     * @throws UnrecoverableKeyException 
     * @throws KeyManagementException 
     * 
     * @see it.intesa.fi2.client.network.ISSLObjectsFactory#getSSLSocketFactory(String, String, String)
     */
    public SSLServerSocketFactory getSSLSocketFactory(
        String sKeyStoreFile,
        String sKeyStorePwd,
        String sKeyStoreType) throws KeyStoreException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException 
    {
        if (sKeyStoreFile == null)
            throw new KeyStoreException("KEYSTORE HAS NOT BEEN SPECIFIED");
        if (this.getClass().getClassLoader().getResourceAsStream(sKeyStoreFile) == null)
            throw new KeyStoreException("COULD NOT FIND KEYSTORE '" + sKeyStoreFile + "'");

        if (sKeyStorePwd == null)
            throw new KeyStoreException("KEYSTORE PASSWORD HAS NOT BEEN SPECIFIED");
        
        SSLContext ctx;
        KeyManagerFactory kmf;

        try
        {
            ctx = SSLContext.getInstance("SSLv3");
            
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            
            //KeyStore ks = getKeystore(sKeyStoreFile, sKeyStorePwd, sKeyStoreType);
            KeyStore ks = KeyStore.getInstance(sKeyStoreType);
            ks.load(this.getClass().getClassLoader().getResourceAsStream(sKeyStoreFile), sKeyStorePwd.toCharArray());
            
            char[] passphrase = sKeyStorePwd.toCharArray();
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());           
            
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SSLException ("Unable to initialize SSLSocketFactory.\n" + e.getMessage());
        }
        
        return ctx.getServerSocketFactory();
    }
    
    
    private void init() throws IOException, KeyManagementException, KeyStoreException, CertificateException, UnrecoverableKeyException
    {
        InetAddress addr = InetAddress.getByName(m_Binding.getIP());
        ServerSocketFactory sf = null;
        
        if (m_Binding.useSSL())
            sf = getSSLSocketFactory(m_Binding.getKeyStoreFile(), m_Binding.getKeyStorePassword(), "JKS");
        else
            sf = ServerSocketFactory.getDefault();

        m_serverSocket = sf.createServerSocket(m_Binding.getPort(), 0, addr);
        //m_serverSocket.setSoTimeout(10000); //Ten seconds timeout added by oriol.lopez
        if (m_serverSocket instanceof SSLServerSocket)
            ((SSLServerSocket)m_serverSocket).setEnabledCipherSuites(((SSLServerSocket) m_serverSocket).getSupportedCipherSuites());
        
        // Init the thread factory
        m_threadFactory = new CThreadFactory(m_Binding.getThreadFactoryConfig());
    }

    public void setAcceptedHosts(List vHosts)
    {
        m_vAcceptedHosts = vHosts;
    }

    @Override
    public void run()
    {
        if (m_Logger.isInfoEnabled())
            m_Logger.info("LISTENING TO " + m_Binding.getIP() + ":" + m_Binding.getPort());

        try
        {
            while ( !this.isInterrupted() )
            {
                Socket clientSocket = m_serverSocket.accept();
                if ( clientSocket != null )
                {
                    if (!canAccept(clientSocket.getInetAddress()))
                    {
                        if (m_Logger.isInfoEnabled())
                            m_Logger.info("REFUSING CONNECTION FROM "
                                    + clientSocket.getInetAddress());

                        clientSocket.close();
                        continue;
                    }

                    if (m_Logger.isDebugEnabled())
                        m_Logger.trace("ACCEPTING CONNECTION FROM "
                                + clientSocket.getInetAddress());

    //                JNRPEServerThread kk = new JNRPEServerThread(clientSocket);
                    JNRPEServerThread kk = m_threadFactory.createNewThread(clientSocket);
    //                CJNRPEServer.getThreadTimeoutWatcher().watch(kk);
                    kk.start();
                }
            }
        }
        catch (SocketException se)
        {
            // This exception is thrown when the server socket is closed.
            // Ignoring
        }
        catch (Exception e)
        {
            m_Logger.error("INTERNAL ERROR: " + e.getMessage(), e);
        }

        m_Logger.info("STOP LISTENING TO " + m_Binding.getIP() + ":" + m_Binding.getPort());
        exit();
    }

    private synchronized void exit()
    {
        notify();
    }

    public synchronized void close()
    {
        m_Logger.warn("CLOSING THREAD");
        try
        {
            m_serverSocket.close();
            m_threadFactory.shutdown();
        }
        catch (IOException e)
        {
        }
    }

    private boolean canAccept(InetAddress inetAddress)
    {
        for (Iterator iterator = m_vAcceptedHosts.iterator(); iterator
                .hasNext();)
        {
            CHost host = (CHost) iterator.next();
            if (host.getInetAddress() != null
                    && host.getInetAddress().equals(inetAddress))
                return true;
        }

        return false;
    }
}
