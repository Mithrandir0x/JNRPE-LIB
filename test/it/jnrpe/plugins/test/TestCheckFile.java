package it.jnrpe.plugins.test;

import java.io.File;

import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.CPluginProxy;
import it.jnrpe.plugins.CReturnValue;
import it.jnrpe.plugins.factory.CPluginFactory;
import junit.framework.TestCase;

public class TestCheckFile extends TestCase
{
    public void testCheckFileContains()
    {
        try
        {
            CPluginFactory f = CPluginFactory.getInstance("./plugins/plugin.xml");

            CPluginProxy pp = f.getPlugin("CHECK_FILE");
            
            
            String[] vArgs = new String[]
            { "--file", "./test/data/check_file.txt", 
              "--contains", "test string,:0,:0"};

            CReturnValue v = pp.execute(vArgs);

            assertNotNull("Check returned a null value", v);
            assertFalse("Check returned and UNKNOWN state", v.getReturnCode() == IJNRPEConstants.STATE_UNKNOWN);

            System.out.println("ASSILEA LU62 EXIT CODE : " + v.getReturnCode());
            System.out.println("ASSILEA LU62 EXIT MESSAGE : " + v.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
}
