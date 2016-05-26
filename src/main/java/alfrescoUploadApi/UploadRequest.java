package alfrescoUploadApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.stringtemplate.v4.compiler.STParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by recovery on 9/2/14.
 */
public class UploadRequest {

    private Map<String, String> properties = new HashMap<String, String>();
    private String fileName;
    private String path;
    private String title;
    private String description;

    public String toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("description", description);
        obj.put("title", title);
        obj.put("fileName", "fileName");
        obj.put("path", path);
        obj.put("properties", properties);
        return obj.toString();
    }

    public static UploadRequest fromJson(String json) throws JSONException {

        final JSONObject obj = new JSONObject(json);

        UploadRequest request = new UploadRequest() {{
            setDescription(obj.getString("description"));
            setTitle(obj.getString("title"));
            setFileName(obj.getString("fileName"));
            setPath(obj.getString("path"));
        }};

        JSONObject properties = obj.getJSONObject("properties");
        Iterator keys = properties.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = properties.getString(key);
            request.properties.put(key, value);
        }

        return request;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
