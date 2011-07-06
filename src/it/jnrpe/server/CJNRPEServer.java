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

import it.jnrpe.plugins.CPluginProxy;
import it.jnrpe.plugins.factory.COption;
import it.jnrpe.plugins.factory.COptions;
import it.jnrpe.plugins.factory.CPluginFactory;
import it.jnrpe.plugins.factory.PluginInstantiationException;
import it.jnrpe.server.config.CBinding;
import it.jnrpe.server.config.CJNRPEConfiguration;
import it.jnrpe.utils.CStreamManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Main server class
 * @author Massimilaino Ziccardi
 *
 */
public class CJNRPEServer
{
	private static Options m_Options = null;
	
	private static List m_vBindingThreads = new LinkedList();
	
//	private static ThreadTimeoutWatcher m_ThreadTimeoutWatcher = null;
	
	private static void printUsage()
	{
		printVersion();
        HelpFormatter hf = new HelpFormatter();
		hf.printHelp("JNRPE.jar", m_Options);
		System.exit(0);
	}
	
	private static void printHelp(String sPluginName)
	{
		try
		{
			CPluginProxy pp = CPluginFactory.getInstance().getPlugin(sPluginName);
			if (pp == null)
				System.out.println ("Plugin " + sPluginName + " does not exists.");
			else
			{
				pp.printHelp();
			}
		}
		catch (PluginInstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
	
    private static void printPluginList()
    {
        Set vPlugins = CPluginFactory.getInstance().getPluginList();

        System.out.println ("List of installed plugins : ");

        //for (String sPluginName : vPlugins)
        for (Iterator iter = vPlugins.iterator(); iter.hasNext(); )
            System.out.println ("  * " + iter.next());
        
        System.exit(0);
    }
    
    /**
     * Generates a simple configuration file
     * @param cl The command line
     */
    private static void generateScheletonConfig(String sFilePath)
    {
        CStreamManager mgr = new CStreamManager();
        try
        {
            PrintWriter w = (PrintWriter) mgr.handle(new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(sFilePath)))));
            w.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            w.println ("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:noNamespaceSchemaLocation=\"http://jnrpe.sourceforge.net/jnrpeConfig.xsd\">");
            w.println ("  <!-- Main Server Configuration -->");
            w.println ("  <server accept-params=\"true\">");
            w.println ("  <!-- The following configuration, will bind to");
            w.println ("  the 127.0.0.1 and will allow only local requests -->");
            w.println ("    <bind address=\"127.0.0.1:5666\" SSL=\"false\"/>");
            w.println ("    <allow ip=\"127.0.0.1\"/>");
            w.println ("    <!-- The directory where all plugins resides");
            w.println ("         is ./plugins - inside this directory you'll");
            w.println ("         have one directory for each plugin. -->");
            w.println ("    <plugin path=\"./plugins\"/>");
            w.println ("  </server>");
            w.println ("  <commands>");
            
            CPluginFactory factory = CPluginFactory.getInstance(); 
                
            Set vPlugins = factory.getPluginList();
    
            //for (String sPluginName : vPlugins)
            for (Iterator iter = vPlugins.iterator(); iter.hasNext(); )
            {
                String sPluginName = (String) iter.next();
                CPluginProxy pp = factory.getPlugin(sPluginName);
                w.println ("    <command name=\"" + sPluginName + "\" plugin_name=\"" + sPluginName + "\">");
                Collection vOptions = pp.getOptions();
                int i = 1;
                
                w.println("        <!-- WARNING!! -->");                
                w.println("        <!-- Generated argument list, won't take care of mutually exclusive arguments! -->");
                for (Iterator iterator = vOptions.iterator(); iterator.hasNext();)
                {
                    COption opt = (COption) iterator.next();
                    w.print("        <arg name=\"" + opt.getLongOpt() + "\" ");
                    if (opt.hasArgs())
                        w.print(" value=\"" + "$ARG" + i++ + "$\" ");
                    w.println ("/>");
                }
                w.println ("    </command>");
            }
            
            w.println ("  </commands>");
            w.println ("</config>");
        }
        catch (Exception e)
        {
            System.out.println ("ERROR GENERATING CONFIGURATION FILE : " + e.getMessage());
            System.exit(-1);            
        }
        finally
        {
            mgr.closeAll();
        }
        
        System.out.println ("FILE GENERATED SUCCESSFULLY");
        System.exit(0);
    }
	
	private static void printVersion()
    {
	    // TODO: this should be handled by ant...
	    System.out.println ("JNRPE version 1.05.1");
	    System.out.println ("Copyright (c) 2008 Massimiliano Ziccardi");
	    System.out.println ("Licensed under the Apache License, Version 2.0");
	    System.out.println ();
    }
    
    public static void main(String[] args)
	{
		CommandLine cl = parseCommandLine(args);

        if (cl.hasOption("help") && cl.getOptionValue("help") == null)
            printUsage();

		if (cl.hasOption("version"))
            printVersion();
		
		CJNRPEConfiguration.init(cl.getOptionValue("conf"));

		if (cl.hasOption("help") && cl.getOptionValue("help") != null)
			printHelp(cl.getOptionValue("help"));

		if (cl.hasOption("list"))
			printPluginList();

        if (cl.hasOption("generateConfig"))
            generateScheletonConfig(cl.getOptionValue("generateConfig"));


//        // Configure the timeout watcher
//        m_ThreadTimeoutWatcher = new ThreadTimeoutWatcher();
//        m_ThreadTimeoutWatcher.setThreadTimeout(CJNRPEConfiguration.getInstance().getThreadTimeout());
//        m_ThreadTimeoutWatcher.start();
        
		List vBindings = CJNRPEConfiguration.getInstance().getServerBindings();
		
		for (Iterator iterator = vBindings.iterator(); iterator.hasNext();)
		{
            CBinding binding = (CBinding) iterator.next();
			try
            {
    			CBindingThread bt = new CBindingThread(binding);
    			bt.setAcceptedHosts (CJNRPEConfiguration.getInstance().getAcceptedHosts());
    			bt.start();
    			m_vBindingThreads.add(bt);
            }
            catch (Exception e)
            {
                System.out.println ("ERROR BINDING TO ADDRESS " + binding.getIP() + ":" + binding.getPort() + " - " + e.getMessage());
                System.exit(-1);
            }
		}
	}

	public static void stop()
	{
//	    getThreadTimeoutWatcher().stopWatching();
//	    try
//	    {
//	        // Waits for the thread to stop.
//	        getThreadTimeoutWatcher().join(2000);
//	    }
//	    catch (InterruptedException ie)
//	    {
//	        // This should never happen...
//	    }
	    
	    for (Iterator iterator = m_vBindingThreads.iterator(); iterator.hasNext();)
        {
            CBindingThread t = (CBindingThread) iterator.next();
            t.close();
        }
	}
	
	private static CommandLine parseCommandLine(String[] vsArgs)
	{
		Digester d = DigesterLoader.createDigester(new InputSource(CJNRPEServer.class.getResourceAsStream("/it/jnrpe/server/command-line-digester.xml")));
		
		try
		{
			COptions opts= (COptions) d.parse(CJNRPEServer.class.getResourceAsStream("/it/jnrpe/server/jnrpe-command-line.xml"));
			m_Options = opts.toOptions();
			CommandLineParser clp = new PosixParser();
			return clp.parse(m_Options, vsArgs);
		}
		catch (IOException e)
		{
			// Should never happen...			
		}
		catch (SAXException e)
		{
			// Should never happen...			
			
		}
		catch (ParseException e)
		{
			printUsage();
		}
		return null;
	}
	
	/**
	 * Returns the thread timeout watcher. 
	 * @return
	 */
//	static ThreadTimeoutWatcher getThreadTimeoutWatcher()
//	{
//	    return m_ThreadTimeoutWatcher;
//	}
}
