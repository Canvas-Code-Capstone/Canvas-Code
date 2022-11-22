package com.canvas.service.helperServices;

import com.canvas.service.models.ExtensionUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Service
public class CanvasClientService {

    // seattleU host: https://seattleu.instructure.com/api/v1
    public static final String CANVAS_URL = "https://canvas.instructure.com/api/v1";
    public static final String AUTH_HEADER = "Authorization";

    private final OkHttpClient okHttpClient;

    public CanvasClientService() {
        this.okHttpClient = new OkHttpClient();
    }

    private JsonNode parseResponseToJsonNode(Response response) throws IOException {
        String r = response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(r);
    }

    private String getMyFilesFolderId(String userId, String folderName, String accessToken) throws IOException {
        JsonNode response = fetchFoldersUnderStudent(userId, accessToken);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    private String getCanvasCodeFolderId(String folderId, String folderName, String accessToken) throws IOException {
        JsonNode response = fetchFolders(folderId, accessToken);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    private String getFileId(String folderId, String fileName, String bearerToken) throws IOException {
        JsonNode response = fetchFilesUnderFolder(folderId, bearerToken);
        return getFileIdFromFilesResponse(response, fileName);
    }

    private String getFolderIdFromFoldersResponse(JsonNode response, String folderName) throws IOException {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("name").asText().equals(folderName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    private String getFileIdFromFilesResponse(JsonNode response, String fileName) throws IOException {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("filename").asText().equals(fileName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    private String getFilesRequestUrlFromAssignmentFolder(JsonNode response, String folderName) throws IOException {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("name").asText().equals(folderName)) {
                return folder.get("files_url").asText();
            }
        }
        return null;
    }

    public byte[] fetchFile(String fileId, String bearerToken) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/files/" + fileId)
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        String url = resp.get("url").asText();

        Request fileRequest = new Request.Builder().url(url).get().build();
        Response fileResp = this.okHttpClient.newCall(fileRequest).execute();
        return Objects.requireNonNull(fileResp.body()).bytes();
    }

    public byte[] fetchFileUnderCourseAssignmentFolder(ExtensionUser user, String fileName) throws IOException {
        JsonNode foldersResp = fetchFoldersUnderCourse(user.getCourseId(), user.getBearerToken());
        // assignmentId is the folder name
        String filesRequestUrl = getFilesRequestUrlFromAssignmentFolder(foldersResp, user.getAssignmentId());

        Request filesRequest = new Request.Builder()
                .url(filesRequestUrl)
                .get()
                .addHeader(AUTH_HEADER, user.getBearerToken())
                .build();

        JsonNode filesResponse = parseResponseToJsonNode(this.okHttpClient.newCall(filesRequest).execute());
        String fileId = getFileIdFromFilesResponse(filesResponse, fileName + ".dms");
        return fetchFile(fileId, user.getBearerToken());
    }

    public String fetchUserId(String bearerToken) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/self")
                .get()
                .addHeader(AUTH_HEADER,bearerToken)
                .build();
        JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        String userId = resp.get("id").asText();
        return userId;

    }

    public JsonNode fetchFoldersUnderCourse(String courseId,String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/courses/" + courseId + "/folders")
                .get()
                .addHeader(AUTH_HEADER, accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }

    public JsonNode fetchFoldersUnderStudent(String userId, String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/" + userId + "/folders/by_path")
                .get()
                .addHeader(AUTH_HEADER, accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }

    public JsonNode fetchFolders(String folderId,String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/folders/" + folderId + "/folders")
                .get()
                .addHeader(AUTH_HEADER, accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }

    public JsonNode fetchFilesUnderFolder(String folderId, String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/folders/" + folderId + "/files")
                .get()
                .addHeader(AUTH_HEADER, accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }

    public Map<String, byte[]> fetchSubmissionFilesFromStudent(String courseId, String assignmentId, String studentId, String accessToken) throws IOException {
        Map<String, byte[]> submissionFilesBytes = new HashMap<>();

        Request request = new Request.Builder()
                .url(CANVAS_URL + "/courses/" + courseId + "/assignments/" + assignmentId + "/submissions/" + studentId)
                .get()
                .addHeader(AUTH_HEADER, accessToken)
                .build();

        JsonNode submissionResp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        JsonNode filesAttachment = submissionResp.get("attachments");

        for (JsonNode fileJson : filesAttachment) {
            String fileId = fileJson.get("id").asText();
            byte[] fileBytes = fetchFile(fileId, accessToken);
            String fileName = fileJson.get("filename").asText();
            submissionFilesBytes.put(fileName, fileBytes);
        }

        return submissionFilesBytes;
    }

    public void fetchSubmissionFromMyFilesAndSave(String fileName, String accessToken) throws IOException {
        String userId = this.fetchUserId(accessToken);
        String myFilesId = this.getMyFilesFolderId(userId, "my files", accessToken);
        String canvasCodeFolderId = this.getCanvasCodeFolderId(myFilesId, "CanvasCode", accessToken);
        String submissionFileId = this.getFileId(canvasCodeFolderId, fileName, accessToken);
        byte[] fileBytes = this.fetchFile(submissionFileId, accessToken);

        FileService fs = new FileService();
        // default stores file in /tmp
        fs.writeFileFromBytes(fileName, fileBytes, "1");
    }

    //Test
    public static void main(String[] args) throws IOException {
        String accessToken = "testToken";
        CanvasClientService canvasClientServices = new CanvasClientService();
        String userId = canvasClientServices.fetchUserId(accessToken);
        String myFilesId = canvasClientServices.getMyFilesFolderId(userId, "my files", accessToken);
        // This will change to Submissions folder
        String canvasCodeFolderId = canvasClientServices.getCanvasCodeFolderId(myFilesId, "CanvasCode", accessToken);
        String submissionFileId = canvasClientServices.getFileId(canvasCodeFolderId, "sample.cpp", accessToken);
        byte[] fileBytes = canvasClientServices.fetchFile(submissionFileId, accessToken);

        FileService fs = new FileService();
        fs.writeFileFromBytes("sample1.cpp", fileBytes, "1");
    }
}