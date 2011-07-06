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
package it.jnrpe.plugins.factory;

import it.jnrpe.plugins.CPluginProxy;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.utils.CStreamManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class instantiate the plugin classes.
 * 
 * @author Massimiliano Ziccardi
 */
public class CPluginFactory
{
	private Log m_Logger = LogFactory.getLog(CPluginFactory.class);
	private static CPluginFactory m_Instance = null;
	private Map m_mPlugins = new HashMap();

	private class CPluginData
	{
		private String m_sClassName = null;
		private String m_sPluginName = null;
		private COptions m_opts = null; 
		private String m_sDescription = null;
		
		private CPluginData(String sPluginName, String sDescription, String sPluginClassName, COptions opts)
		{
			m_sClassName = sPluginClassName;
			m_sPluginName = sPluginName;
			m_opts = opts;
			m_sDescription = sDescription;
		}
		
		public IPluginInterface getPluginInstance() throws PluginInstantiationException
		{
			try
			{
				//Thread.currentThread().setContextClassLoader(m_classLoader); // BUG #2814844
				return (IPluginInterface) CPluginData.class.getClassLoader().loadClass(m_sClassName).newInstance();
			}
			catch (Exception e)
			{
				throw new PluginInstantiationException("UNABLE TO INSTANTIATE PLUGIN " + m_sPluginName, e);
			}
		}

		public COptions getOptions()
		{
			return m_opts;
		}
		
		public String getDescription()
		{
		    return m_sDescription;
		}
	}

	/**
	 * Returns an instance of the plugin factory
	 * @return
	 */
	public static synchronized CPluginFactory getInstance()
	{
		if (m_Instance == null)
		{
			m_Instance = new CPluginFactory();
			m_Instance.configure();
		}

		return m_Instance;
	}

	/**
	 * This method has been created to facilitate plugin testing.
	 * Just call this method, passing the path to your plugin.xml files to get a factory.
	 * Then call the getPlugin method to obtain your plugin instance and call the 
	 * execute method to execute it.
	 * 
	 * @param sPluginXMLPath The path to your XML file
	 * @return a factory instance
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static CPluginFactory getInstance(String sPluginXMLPath) throws FileNotFoundException, IOException, SAXException
	{
		if (m_Instance == null)
		{
			CStreamManager streamMgr = new CStreamManager();
			try
			{
				m_Instance = new CPluginFactory();
				m_Instance.parsePluginXmlFile(streamMgr.getInputStream(new File(sPluginXMLPath)));
			}
			finally
			{
				streamMgr.closeAll();
			}
		}

		return m_Instance;
	}
	
	
	/**
	 * Configures the factory.
	 * 
	 * A "jnrpe.plugins.xml" file must be available at classloader's root in order to fetch
         * plugin definitions.
	 */
	private void configure()
	{
            try
            {
                m_Logger.trace("CONFIGURING PLUGIN FACTORY");
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("jnrpe.plugins.xml");

                if ( is != null )
                    parsePluginXmlFile(is);
                else
                    m_Logger.warn("PLUGIN DEFINITION FILE NOT FOUND.");
            }
            catch ( IOException ex )
            {
                m_Logger.error("Error " + ex.getMessage(), ex);
            }
            catch ( SAXException ex )
            {
                m_Logger.error("Error " + ex.getMessage(), ex);
            }
	}

        /**
         * @deprecated
         */
	private void configurePlugins(File fDir)
	{
                m_Logger.trace("READING PLUGIN CONFIGURATION FROM DIRECTORY " + fDir.getName());
                CStreamManager streamMgr = new CStreamManager();
                File[] vfJars = fDir.listFiles(new FileFilter()
                {

                    public boolean accept(File f)
                    {
                        return f.getName().endsWith(".jar");
                    }

                });

                // Initializing classloader
                URL[] urls = new URL[vfJars.length];
                URLClassLoader ul = null;

                for (int j = 0; j < vfJars.length; j++)
                {
                    try
                    {
                        urls[j] = vfJars[j].toURI().toURL();
                    }
                    catch (MalformedURLException e)
                    {
                        // should never happen
                    }
                }

                ul = URLClassLoader.newInstance(urls);

                for (int i = 0; i < vfJars.length; i++)
                {
                    File file = vfJars[i];


                    try
                    {
                        m_Logger.info("READING PLUGINS DATA IN FILE '" + file.getName() + "'");

                        ZipInputStream jin = (ZipInputStream) streamMgr.handle(new ZipInputStream(new FileInputStream(file)));
                        ZipEntry ze = null;

                        while ((ze = jin.getNextEntry()) != null)
                        {
                            if (ze.getName().equals("plugin.xml"))
                            {
				parsePluginXmlFile(jin);
                                break;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        m_Logger.error("UNABLE TO READ DATA FROM FILE '"
                            + file.getName() + "'. THE FILE WILL BE IGNORED.", e);
                    }
                    finally
                    {
                        streamMgr.closeAll();
                    }

                }
	}
	
	private void parsePluginXmlFile(InputStream in) throws IOException, SAXException
	{
		CStreamManager streamMgr = new CStreamManager();
		
		m_Logger.trace("PARSING FILE plugin.xml IN JAR FILE.");

		Digester digester = DigesterLoader
				.createDigester(new InputSource(
						streamMgr
								.handle(this.getClass().getClassLoader().getResourceAsStream("plugin-digester.xml"))));
		CPluginFactoryConfiguration oConf = (CPluginFactoryConfiguration) digester.parse(in);

		List vPluginDefs = oConf.getPluginDefinitions();

		//for (CPluginDefinition pluginDef : vPluginDefs)
		for (Iterator iter = vPluginDefs.iterator(); iter.hasNext(); )
		{
		    CPluginDefinition pluginDef = (CPluginDefinition) iter.next();
			m_Logger.debug("FOUND PLUGIN "
					+ pluginDef.getName()
					+ " IMPLEMENTED BY CLASS "
					+ pluginDef.getPluginClass());
			m_mPlugins.put(pluginDef.getName(), new CPluginData(pluginDef.getName(), pluginDef.getDescription(), pluginDef.getPluginClass(), pluginDef.getOptions()));
		}
	}
	
	
	/**
	 * Instantiates the plugin
	 * @param sPluginName The name of the plugin to be instantiated (as described
	 * in the plugin.xml file).
	 * @return The plugin
	 */	
	public CPluginProxy getPlugin(String sPluginName) throws PluginInstantiationException
	{
		CPluginData pluginData = (CPluginData) m_mPlugins.get(sPluginName);
		if (pluginData != null)
			return new CPluginProxy(sPluginName, pluginData.getDescription(), pluginData.getPluginInstance(), pluginData.getOptions());
		
		return null;
	}
	
	public Set getPluginList()
	{
		return m_mPlugins.keySet();
	}
}
