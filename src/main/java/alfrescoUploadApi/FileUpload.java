package alfrescoUploadApi;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * Created by recovery on 9/1/14.
 */
public class FileUpload extends AbstractWebScript {

    private final static Log logger = LogFactory.getLog(FileUpload.class);
    private final int random = hashCode();

    private Repository repository;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private ContentService contentService;
    private CheckOutCheckInService checkOutCheckInService;

    static {
        String prop = System.getProperty("log4j.configuration");
        if (prop == null) prop = "log4j.properties";
        URL log4jConfig = Loader.getResource(prop);
        if (log4jConfig.getProtocol().equalsIgnoreCase("file")) {
            int time = 1000 * 60 * 10;
            PropertyConfigurator.configureAndWatch(log4jConfig.getFile(), time);
            logger.info("|| check log4j.properties every | " + time);
        } else {
            logger.info("|| cannot cannot monitor log4j.properties");
        }
    }

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Find node.
     *
     * @param parent
     * @param sub
     * @return
     */
    private NodeRef getNode(NodeRef parent, List<String> sub) {
        try {
            NodeRef node = fileFolderService.resolveNamePath(parent, sub).getNodeRef();
            return node;
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    /**
     * Try to create folder if not exist.
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    private NodeRef createFolder(String path) throws FileNotFoundException {

        logger.info("|| check directory | " + path);
        final NodeRef companyHome = repository.getCompanyHome();
        final List<String> pathElements = Arrays.asList(path.split("/"));

        NodeRef currentNode = companyHome;

        for (int i = 1; i < pathElements.size(); i++) {
            final List<String> sub = pathElements.subList(i, i + 1);
            final NodeRef node = getNode(currentNode, sub);
            if (node == null) {
                String folderName = pathElements.get(i);
                logger.info("|| create sub directory | " + folderName);
                currentNode = fileFolderService.create(currentNode, folderName, ContentModel.TYPE_FOLDER).getNodeRef();
            } else {
                currentNode = node;
            }
        }
        return currentNode;
    }

    /**
     * Find specific field by giving name.
     *
     * @param form
     * @param name
     * @return
     */
    private FormData.FormField getField(FormData form, String name) {
        //logger.info("|| request parameters ...");
        FormData.FormField file = null;
        for (FormData.FormField field : form.getFields()) {
            //logger.info("|| name | " + field.getName());
            if (name.equals(field.getName()))
                file = field;
        }
        return file;
    }

    /**
     * Create new node if not exist.
     *
     * @param folderRef
     * @param fileName
     * @return
     */
    private NodeInfo checkoutOrCreate(NodeRef folderRef, final String fileName) {

        final NodeInfo nodeInfo = new NodeInfo();

        logger.info("|| try checkout or create file | " + fileName);
        final FileInfo newFile;

        final NodeRef targetFile = getNode(folderRef, new ArrayList<String>() {{
            add(fileName);
        }});

        if (targetFile != null) {
            logger.info("|| checkout exist file | " + fileName);
            final NodeRef newNode = checkOutCheckInService.checkout(targetFile);
            newFile = fileFolderService.getFileInfo(newNode);
            nodeInfo.setCheckout(true);
        } else {
            logger.info("|| create new file | " + fileName);
            newFile = fileFolderService.create(folderRef, fileName, ContentModel.TYPE_CONTENT);
        }

        nodeInfo.setFileInfo(newFile);
        return nodeInfo;
    }

    /**
     * Update meta data to file.
     *
     * @param newFile
     * @param request
     */
    private void updateMetaData(FileInfo newFile, UploadRequest request) {
        final String title = request.getTitle();
        final String description = request.getDescription();
        final Map<String, String> properties = request.getProperties();
        final NodeRef newRef = newFile.getNodeRef();

        for (String key : properties.keySet()) {
            final String value = properties.get(key);
            final QName qname = QName.createQName(key);
            nodeService.setProperty(newRef, qname, value);
        }
        nodeService.setProperty(newRef, QName.createQName(Keys.TITLE), title);
        nodeService.setProperty(newRef, QName.createQName(Keys.DESCRIPTION), description);
    }

    /**
     * White file content from given stream.
     *
     * @param newFile
     * @param inputStream
     * @param mimetype
     */
    private void writeContent(FileInfo newFile, InputStream inputStream, String mimetype) {
        final NodeRef newRef = newFile.getNodeRef();
        final ContentWriter writer = contentService.getWriter(newRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.putContent(inputStream);
    }

    /**
     * Write file content from FormData.FormField.
     *
     * @param newFile
     * @param file
     */
    private void writeContent(FileInfo newFile, FormData.FormField file) {
        final String mimetype = file.getMimetype();
        final InputStream inputStream = file.getInputStream();
        writeContent(newFile, inputStream, mimetype);
    }

    private void printProperties(FileInfo newFile) {
        for (Object key : newFile.getProperties().keySet()) {
            Object value = newFile.getProperties().get(key);
            System.out.printf("|| key | %s\n", key);
            System.out.printf("|| value | %s\n", value);
        }
    }

    /**
     * Web script entry point.
     *
     * @param req
     * @param res
     * @throws IOException
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        //synchronized (this) {
        synchronizedExecute(req, res);
        //}
    }

    private void synchronizedExecute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        long start = System.currentTimeMillis();
        logger.info("|| instance id | " + random);

        final FormData form = (FormData) req.parseContent();
        final FormData.FormField file = getField(form, "file");
        final FormData.FormField json = getField(form, "json");
        final String raw = json.getContent().getContent();
        final String body = java.net.URLDecoder.decode(raw, "UTF-8");
        final UploadResponse response = new UploadResponse();

        try {
            // request metadata.
            final UploadRequest request = UploadRequest.fromJson(body);
            final String folder = request.getPath();
            final String fileName = request.getFileName();
            final NodeRef folderRef = createFolder(folder);
            final NodeInfo nodeInfo = checkoutOrCreate(folderRef, fileName);
            final FileInfo newFile = nodeInfo.getFileInfo();

            // 1) update metadata to new file.
            // 2) write binary content and mimetype.
            // 3) check file in.
            updateMetaData(newFile, request);
            writeContent(newFile, file);
            if (nodeInfo.isCheckout()) {
                checkOutCheckInService.checkin(newFile.getNodeRef(), new HashMap<String, Serializable>());
            }

            final String uuid = newFile.getProperties().get(QName.createQName(Keys.UUID)).toString();
            response.setUuid(uuid);
            response.setSuccess(true);

        } catch (JSONException e) {
            response.setError(e.getMessage());
            e.printStackTrace();

        } catch (Exception ex) {
            response.setError(ex.getMessage());
            ex.printStackTrace();
        }

        long end = System.currentTimeMillis();
        logger.info(String.format("|| process time | %d ms.", end - start));

        res.getWriter().write(response.toString());
    }
}