package alfrescoUploadApi;

import junit.framework.TestCase;
import org.apache.log4j.helpers.Loader;

import java.net.URL;

/**
 * Created by recovery on 9/3/14.
 */
public class ResourceTest extends TestCase {
    public void testResource() {
        URL log4jConfig = Loader.getResource("log4j.properties");
        System.out.println(log4jConfig.getFile());
        System.out.println(log4jConfig.getFile().substring(1));
    }
}
