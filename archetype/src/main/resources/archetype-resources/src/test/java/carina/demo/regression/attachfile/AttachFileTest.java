#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.attachfile;

import java.net.URL;
import java.nio.file.Path;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.gui.pages.desktop.AttachFilePage;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;

public class AttachFileTest implements IAbstractTest {

    @Test
    @MethodOwner(owner = "qpsdemo")
    public void testAttachFile() {
        AttachFilePage attachFilePage = new AttachFilePage(getDriver());
        attachFilePage.open();
        Assert.assertTrue(attachFilePage.isPageOpened(), "Attach file page has not been opened.");
        URL resourceURL = ClassLoader.getSystemClassLoader().getResource("files/icon.png");
        Assert.assertNotNull(resourceURL, "Resource should exists.");
        attachFilePage.uploadFile(Path.of(resourceURL.getPath()));
        attachFilePage.submit();
        Assert.assertTrue(attachFilePage.isFileUploaded(), "File has not been uploaded.");
    }

}
