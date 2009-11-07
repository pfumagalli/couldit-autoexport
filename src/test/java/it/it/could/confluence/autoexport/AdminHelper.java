package it.it.could.confluence.autoexport;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;

import java.io.File;

/**
 *
 */
public class AdminHelper {
    public void configurePlugin(AbstractConfluencePluginWebTestCase test, File exportDir) {
        gotoConfiguration(test);
        test.setTextField("rootPath", exportDir.getAbsolutePath());
        test.setTextField("userName", "admin");
        test.selectOption("encoding", "UTF-8");
        test.submit("saveConfig");
        test.assertTextPresent("saved succesfully");
        test.clickLinkWithExactText("Back");
    }

    public void gotoConfiguration(AbstractConfluencePluginWebTestCase test) {
        test.gotoPage("/admin/console.action");
        test.clickLinkWithExactText("Auto Export");
        test.setWorkingForm("configform");
    }

    public void deleteConfiguration(AbstractConfluencePluginWebTestCase test) {
        test.gotoPage("/admin/autoexport/configuration-delete.action");
        test.assertTextPresent("deleted successfully");
        test.clickLinkWithExactText("Back");
    }

    public void restoreTemplate(AbstractConfluencePluginWebTestCase test, String spaceKey) {
        test.gotoPage("/admin/autoexport/template-restore.action?space=ds");
        test.assertTextPresent("deleted successfully");
        test.clickLinkWithExactText("Back");
    }
}
