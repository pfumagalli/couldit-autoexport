package it.it.could.confluence.autoexport;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.ConfluenceWebTester;

public class AbstractIntegrationTestCase extends AbstractConfluencePluginWebTestCase
{
    @Override
    protected ConfluenceWebTester createConfluenceWebTester()
    {
        ConfluenceWebTester tester = new ConfluenceWebTester();
        tester.setAdminUserName("admin");
        tester.setAdminPassword("admin");
        tester.setHostName("localhost");
        tester.setPort(Integer.parseInt(System.getProperty("http.port", "1990")));
        tester.setContextPath(System.getProperty("context.path", "/confluence"));
        tester.setProtocol("http");
        tester.setSoapServicePath("/rpc/soap-axis/confluenceservice-v1?wsdl");
        tester.getTestContext().setBaseUrl(tester.getBaseUrl());
        tester.setScriptingEnabled(false);

        return tester;
    }
}
