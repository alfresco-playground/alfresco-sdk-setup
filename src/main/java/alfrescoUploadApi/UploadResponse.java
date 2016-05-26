package alfrescoUploadApi;

import org.activiti.engine.impl.util.json.JSONObject;

/**
 * Created by recovery on 9/2/14.
 */
public class UploadResponse {

    private boolean success = false;
    private String error = "";
    private String uuid = "";

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("uuid", uuid);
        obj.put("success", success);
        obj.put("error", error);
        return obj.toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
