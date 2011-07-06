package it.jnrpe.plugins.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for it.jnrpe.plugins.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestCheckFile.class);
        //$JUnit-END$
        return suite;
    }

}
