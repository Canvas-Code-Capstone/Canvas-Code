package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.submission.Submission;
import com.canvas.service.models.submission.SubmissionFile;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper service for generating a student submission directory by using data from the Canvas submission JSON.
 */
@Service
public class SubmissionService {

    private final CanvasClientService canvasClientService;
    private final FileService fileService;
    private static final String MAKEFILE = "makefile";
    public SubmissionService(CanvasClientService canvasClientService) {
        this.canvasClientService = canvasClientService;
        this.fileService = new FileService();
    }

    /**
     * Gets the file bytes for each submission file and returns the names of each file mapped
     * to their corresponding file byte arrays.
     *
     * @param user extension user
     * @return map where key is the file name and value is the bytes for that file
     * @throws CanvasAPIException
     */
    public Map<String, byte[]> getSubmissionFileBytes(ExtensionUser user) throws CanvasAPIException {
        JsonNode canvasSubmissionResp = canvasClientService.fetchCanvasSubmissionJson(user);
        return getSubmissionFileBytes(canvasSubmissionResp, user.getBearerToken());
    }

    /**
     * Generates the submission model containing data for the student submission.
     *
     * @param user extension user
     * @return submission model
     * @throws CanvasAPIException
     */
    public Submission generateStudentSubmissionAndDirectory(ExtensionUser user) throws CanvasAPIException {
        // Get the Canvas submission JSON
        JsonNode submissionResponse = canvasClientService.fetchCanvasSubmissionJson(user);
        String submissionId = submissionResponse.get("id").asText();
        writeMakefile(user);

        // Map file name to bytes for each file
        Map<String, byte[]> submissionFilesBytes = getSubmissionFileBytes(submissionResponse, user.getBearerToken());

        String fileDirectory = fileService.getFileDirectory("12345"); // TODO hash of studentId, assignmentId, and courseId

        SubmissionFile[] submissionFiles = generateSubmissionFiles(submissionFilesBytes, fileDirectory);

        return new Submission(
                submissionId,
                user.getStudentId(),
                user.getAssignmentId(),
                submissionFiles,
                fileDirectory
        );
    }

    private SubmissionFile[] generateSubmissionFiles(Map<String, byte[]> submissionFilesBytes, String fileDirectory) {
        List<SubmissionFile> submissionFiles = new ArrayList<>();

        for (Map.Entry<String, byte[]> entry : submissionFilesBytes.entrySet()) {
            String fileName = entry.getKey();
            byte[] fileBytes = entry.getValue();
            fileService.writeFileFromBytes(fileName, fileBytes, fileDirectory);
            String[] fileContent = fileService.parseLinesFromFile(fileName, fileDirectory);
            submissionFiles.add(new SubmissionFile(fileName, fileContent));
        }

        return submissionFiles.toArray(new SubmissionFile[0]);
    }

    private Map<String, byte[]> getSubmissionFileBytes(JsonNode canvasSubmissionResp, String bearerToken)
            throws CanvasAPIException {
        Map<String, byte[]> submissionFilesBytes = new HashMap<>();
        JsonNode filesAttachment = canvasSubmissionResp.get("attachments");
        for (JsonNode fileJson : filesAttachment) {
            String fileId = fileJson.get("id").asText();
            byte[] fileBytes = canvasClientService.fetchFile(fileId, bearerToken);
            String fileName = fileJson.get("filename").asText();
            submissionFilesBytes.put(fileName, fileBytes);
        }

        return submissionFilesBytes;
    }

    // TODO duplicate code from evaluation service, need to refactor
    // TODO write makefile to the student submission directory (same directory as code files)
    private void writeMakefile(ExtensionUser user) throws CanvasAPIException {
        // Retrieve file json from Canvas
        byte[] makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, MAKEFILE);

        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, user.getUserId());
    }
}
