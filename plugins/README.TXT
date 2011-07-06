                         
                         	       JNRPE_PLUGINS
                         	
What is it?
-----------

In this project, you'll find some basic plugin for the JNRPE.
In this release, you'll find:
	* TEST plugin: this is just a JAVA TEST plugin (as its name implies).
		If you call this plugin, you obtain a OK return code and a 
		TEST OK message
	* EXEC plugin: this plugin allows the execution of external plugin commands.
		With this plugin you can execute native plugins.

The Latest Version
------------------

You can always get the latest version at the following link:

    http://sourceforge.net/project/showfiles.php?group_id=204486
    
Documentation
-------------

The TEST plugin usage is very simple.
=====================================

You just have to configure a command in the command section of the 
JNRPE configuration this way:

<command name="test" plugin_name="TEST" >
   <arg name="text" value="$ARG1$"/>
</command>

Then, you can call it with the NRPE check_nrpe command:
	check_nrpe -H your_host -c test
	
The EXEC plugin is very simple.
===============================

Just configure a command for every native plugin you want to execute:

<command name="check_disk" plugin_name="EXEC" params="C:\PROGRA~2\nagios\check_pdm.exe --disk --drive $ARG1$ -w $ARG2$ -c $ARG3$">
   <arg name="disk"/>
   <arg name="drive" value="$ARG1$"/>
   <arg name="warning" value="$ARG2$"/>
   <arg name="critical" value="$ARG3$"/>
</command>
 
Then call the command in the usual way:
	check_nrpe -H 193.42.233.137 -c check_disk -a '70!90!/'

OTHERS plugin
=============

Install the plugin, then run
	java -jar jnrpe.jar -c <YOUR_CONFIG_FILE> -h <PLUGIN-NAME>

where PLUGIN-NAME is the plugin you want to configure.

You'll get an exhaustive help about how to use the plugin.

For example:
	java -jar jnrpe.jar -c <YOUR_CONFIG_FILE> -h CHECK_ORACLE

Installation
------------

Create a directory for this plugin in the plugin directory of the JNRPE server,
then copy the jar inside that directory.
Restart the server.
