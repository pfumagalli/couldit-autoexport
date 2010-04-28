package it.it.could.confluence.autoexport;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.ConfluenceWebTester;
import com.atlassian.confluence.plugin.functest.JWebUnitConfluenceWebTester;
import com.atlassian.confluence.plugin.functest.TesterConfiguration;
import junit.framework.Assert;

import java.io.IOException;
import java.util.Properties;

public class AbstractIntegrationTestCase extends AbstractConfluencePluginWebTestCase
{

    @Override
    protected JWebUnitConfluenceWebTester createConfluenceWebTester()
    {
        try
        {
            Properties props = new Properties();
            props.setProperty("confluence.webapp.port", System.getProperty("http.port", "1990"));
            props.setProperty("confluence.webapp.context.path", System.getProperty("context.path", "/confluence"));
            return new JWebUnitConfluenceWebTester(new TesterConfiguration(props));
        }
        catch (IOException ioe)
        {
            Assert.fail("Unable to create tester: " + ioe.getMessage());
            return null;
        }
        //tester.getTestContext().setBaseUrl(tester.getBaseUrl());
        //tester.setScriptingEnabled(false);

    }
}
