package com.canvas.chromeApiServer;
import com.canvas.dto.CommandOutput;
import com.canvas.service.CanvasClientService;
import com.canvas.service.JSONParsingService;
import com.canvas.service.ProcessExecutor;
import com.canvas.service.FileService;


import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@CrossOrigin
public class ChromeApiController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    //@Value("${canvas.mock.url}")
    @Value("${canvas.api.url}")
    private String CANVAS_HOST_URL;

    @Value("${canvas.api.auth.token}")
    private String CANVAS_AUTH_TOKEN;

    public ChromeApiController() {

    }

    @Bean
    public WebClient.Builder getWebClientBuilder(){
        return WebClient.builder();
    }

    @PostMapping(
            value = "/evaluate",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<CommandOutput> compileCodeFile(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("userId") String userId
    ) {
        // Retrieve file json from Canvas
        Publisher<DataBuffer> dataBuffer = getFileFromCanvas("68709717");

        // Write json to file
        String canvasFileJsonName = "canvas-file-response.json";
        FileService fileService = FileService.getFileService(userId);
        fileService.writeFileFromDataBufferPublisher(dataBuffer, canvasFileJsonName);

        // Write makefile from file URL
        JSONParsingService canvasFileJson = new JSONParsingService(fileService.getFileDirectory() + "/" + canvasFileJsonName);
        fileService.writeFileFromUrl(canvasFileJson.get("url"), "makefile");

        // Write submitted code files
        for (MultipartFile file : files) {
            fileService.writeFileFromMultipart(file);
        }

        System.out.println("File directory before exe: " + fileService.getFileDirectory());

        // Compile the files and grab output
        //fileService.getFileDirectory()
        //new String[] {"make", "-C", fileService.getFileDirectory()}
        System.out.println(fileService.getFileDirectory());
        ProcessExecutor processExecutor = new ProcessExecutor(new String[] {"make", "-C", fileService.getFileDirectory()}, "");
        boolean compileSuccess = processExecutor.executeProcess();
        String output = compileSuccess ? "Your program compiled successfully!" : processExecutor.getProcessOutput();

        // Cleanup
        //fileService.deleteDirectory();

        // Generate response
        CommandOutput commandOutput = new CommandOutput(compileSuccess, output);

        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }

    /**
     * Test Route to get student submission given access token and studentId
     * Submission is expected to be saved under my files/CanvasCode/sample.cpp
     * @param studentId
     * @param token
     * @return
     */

        @GetMapping(
            value = "/fetchStudentSubmission",
            produces = { "application/json" }
    )

    public ResponseEntity<String> fetchFileFromCanvasAndSave(@RequestParam("studentId") String studentId,
                                                  @RequestParam("accessToken") String token,
                                                  @RequestParam("fileName") String fileName  ) {
        CanvasClientService canvasClientService = new CanvasClientService("Bearer " + token);
        try {
        canvasClientService.fetchSubmissionFromMyFilesAndSave(fileName);
        }
        catch(Exception e) {
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("SAVED FILE", HttpStatus.OK);
    }
    private Publisher<DataBuffer> getFileFromCanvas(String fileId) {
        return webClientBuilder.build()
                .get()
                .uri(CANVAS_HOST_URL + "/files/" + fileId)
                .header("Authorization", "Bearer " + CANVAS_AUTH_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(DataBuffer.class);
    }
}