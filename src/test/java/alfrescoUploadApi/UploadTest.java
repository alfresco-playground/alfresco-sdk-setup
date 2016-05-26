package alfrescoUploadApi;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Created by recovery on 9/2/14.
 */
public class UploadTest extends TestCase {
    public void testUpload() {
        UploadRequest request = new UploadRequest();
        request.setDescription("This is description");
        request.setTitle("This is title");
        request.setFileName("this-is-file-name.pdf");
        request.setPath("/Test/Upload/V2");
        request.setProperties(new HashMap<String, String>(){{

        }});
    }
}
