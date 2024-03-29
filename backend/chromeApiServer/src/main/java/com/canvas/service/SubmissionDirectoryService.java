package com.canvas.service;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.helperServices.FileService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.submission.Deletion;
import com.canvas.service.models.submission.Submission;
import com.canvas.service.models.submission.SubmissionFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubmissionDirectoryService {

    private final FileService fileService;
    private final CanvasClientService canvasClientService;

    public static final String MAKEFILE = "makefile";
    private static final Pattern MAKEFILE_REGEX_PATTERN = Pattern.compile("makefile(\\.dms)?|Makefile(\\.dms)?");

    /**
     * Constructor for creating our instances of FileService and CanvasClientService
     *
     * @param fileService         File service instance
     * @param canvasClientService Canvas client service instance
     */
    @Autowired
    public SubmissionDirectoryService(
            FileService fileService,
            CanvasClientService canvasClientService
    ) {
        this.fileService = fileService;
        this.canvasClientService = canvasClientService;
    }

    /**
     * Generates the student submission and directory and writes the submission files to the directory.
     *
     * @param user Extension user
     * @return Submission model response
     * @throws CanvasAPIException
     */
    public ResponseEntity<Submission> generateSubmissionDirectory(ExtensionUser user) throws CanvasAPIException {
        String submissionDirectory = generateUniqueDirectoryName(user.getCourseId(), user.getAssignmentId(), user.getStudentId());

        Submission submission = canvasClientService.fetchStudentSubmission(user);
        Map<String, byte[]> submissionFileBytes = submission.getSubmissionFileBytes();

        SubmissionFile[] submissionFiles = writeSubmissionFiles(submission.getSubmissionFileBytes(), submissionDirectory);

        // If student was not required to submit makefile, we need to get the instructor
        // uploaded makefile from Canvas
        if (!studentSubmissionContainsMakefile(submissionFileBytes)) {
            writeMakefileFromCanvas(user, submissionDirectory);
        }

        submission.setSubmissionFiles(submissionFiles);
        submission.setSubmissionDirectory(submissionDirectory);

        return new ResponseEntity<>(submission, HttpStatus.OK);
    }

    /**
     * Write the makefile retrieved from the student submission
     * @param makefileBytes bytes of the makefile
     * @param directory directory to write makefile to
     */
    public void writeMakefileFromStudent(byte[] makefileBytes, String directory) {
        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, directory);
    }

    /**
     * Write the makefile retrieved from the Canvas assignment folder.
     * @param user User object the make file is associated with
     * @param directory directory to write makefile to
     * @throws CanvasAPIException
     */
    public void writeMakefileFromCanvas(ExtensionUser user, String directory) throws CanvasAPIException {
        // Retrieve makefile from Canvas API
        byte[] makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, MAKEFILE);
        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, directory);
    }

    /**
     * Writes submission files to the submission files directly from Multipart files.
     *
     * @param files array of Multipart files
     * @param directory directory to write files to
     */
    public void writeSubmissionFiles(MultipartFile[] files, String directory) {
        for (MultipartFile file : files) {
            fileService.writeFileFromMultipart(file, directory);
        }
    }

    /**
     * Writes submission files to the submission directory from a map of file names to their file bytes.
     *
     * @param submissionFilesBytes submission file byte map
     * @param fileDirectory directory to write files to
     * @return array of submission files
     */
    private SubmissionFile[] writeSubmissionFiles(Map<String, byte[]> submissionFilesBytes, String fileDirectory) {
        List<SubmissionFile> submissionFiles = new ArrayList<>();

        for (Map.Entry<String, byte[]> entry : submissionFilesBytes.entrySet()) {
            String key = entry.getKey();
            String fileName = (isMakefile(key)) ? SubmissionDirectoryService.MAKEFILE : key;
            byte[] fileBytes = entry.getValue();
            fileService.writeFileFromBytes(fileName, fileBytes, fileDirectory);
            String[] fileContent = fileService.parseLinesFromFile(fileName, fileDirectory);
            submissionFiles.add(new SubmissionFile(fileName, fileContent));
        }

        return submissionFiles.toArray(new SubmissionFile[0]);
    }

    public ResponseEntity<Deletion> deleteSubmissionDirectory(ExtensionUser user) {
        String submissionDirectory = generateUniqueDirectoryName(user.getCourseId(), user.getAssignmentId(), user.getStudentId());
        String description = String.format(
                "courdeId:%s,\nassignmentId:%s,\nstudentId:%s,\nsubmissionDirectory:%s",
                user.getCourseId(), user.getAssignmentId(), user.getStudentId(), submissionDirectory
        );

        // Now delete
        boolean success = deleteDirectory(submissionDirectory);

        Deletion deletion = Deletion.builder()
                .success(success)
                .description(description)
                .build();

        return new ResponseEntity<>(deletion, HttpStatus.OK);
    }

    /**
     * Deletes the submission directory
     *
     * @param directory name of directory to delete
     */
    public boolean deleteDirectory(String directory) {
        return fileService.deleteDirectory(directory);
    }

    /**
     * Gets the submission directory name from the given id.
     *
     * @param id ID associated with the directory name
     * @return submission directory name
     */
    public String getSubmissionDirectory(String id) {
        return fileService.getFileDirectory(id);
    }

    /**
     * Creates a unique directory name by hashing the arguments provided.
     * @param idArgs IDs to hash
     * @return unique hash value
     */
    protected String generateUniqueDirectoryName(String ... idArgs) {
        String[] idList = Arrays.copyOf(idArgs, idArgs.length);
        return String.valueOf(Arrays.hashCode(idList));
    }

    /**
     * Regex matcher for detecting if the key is a makefile
     * @param key key to match
     * @return true if matches the makefile regex pattern
     */
    private boolean isMakefile(String key) {
        return MAKEFILE_REGEX_PATTERN.matcher(key).matches();
    }

    /**
     * Returns whether the student submitted files contains a makefile.
     * @param submissionFileBytes
     * @return
     */
    private boolean studentSubmissionContainsMakefile(Map<String, byte[]> submissionFileBytes) {
        for (String filename : submissionFileBytes.keySet()) {
            if (isMakefile(filename)) {
                return true;
            }
        }
        return false;
    }

}
