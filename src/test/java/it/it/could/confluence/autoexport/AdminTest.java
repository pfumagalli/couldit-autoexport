package it.it.could.confluence.autoexport;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class AdminTest extends AbstractIntegrationTestCase {
    File exportDir;
    AdminHelper admin = new AdminHelper();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File tmpDir = new File("target/it-temp").getAbsoluteFile();
        if (tmpDir.exists()) {
            FileUtils.cleanDirectory(tmpDir);
        }
        tmpDir.mkdirs();
        exportDir = new File(tmpDir, "exports");
        exportDir.mkdir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(exportDir);
    }

    public void testLinkAvailable() {
        gotoPage("/admin/console.action");
        assertLinkPresentWithExactText("Auto Export");
    }

    public void testChangeConfiguration() {
        admin.deleteConfiguration(this);
        admin.configurePlugin(this, exportDir);
        setWorkingForm("configform");
        assertTextFieldEquals("rootPath", exportDir.getAbsolutePath());
        assertTextFieldEquals("userName", "admin");
        assertTextPresent("Confluence user:");
        assertSelectedOptionEquals("encoding", "UTF-8");
    }

    public void testEditTemplate() {
        admin.configurePlugin(this, exportDir);
        admin.restoreTemplate(this, "ds");
        assertLinkNotPresentWithExactText("Restore Default");
        assertLinkNotPresentWithExactText("View Current");
        clickLinkWithText("Edit Template", 1);
        setWorkingForm("edittemplateform");
        String templateData = getElementTextByXPath("//textarea[@name='data']");
        assertFalse(templateData.contains("asdf"));
        setTextField("data", templateData.replace("</body>", "asdf</body>"));
        submit("save");
        assertTextPresent("saved successfully");
        admin.gotoConfiguration(this);
        assertLinkPresentWithExactText("Restore Default");
        assertLinkPresentWithExactText("View Current");
        clickLinkWithExactText("View Current");
        assertTextPresent("asdf");

    }
}
