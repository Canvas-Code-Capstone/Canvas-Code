package com.canvas.controllers.chromeApiServer;

import com.canvas.service.EvaluationService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
class ChromeApiControllerUnitTest {

    /**
     * automatically wire up the dependencies for the test class
     */
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ExtensionUser user;

    @MockBean
    CanvasClientService canvasClientService;

    @MockBean
    EvaluationService evaluationService;

    /**
     * Placeholder for action to take before running tests
     */
    @BeforeEach
    void setUp() {

    }

    /**
     * Placeholder for action to take after running tests
     */
    @AfterEach
    void tearDown() {

    }

    /**
     * Tests the ResponseEntity returns Ok with correct params:
     * <p>
     * Values passed through path:
     * assignmentID = cs5461321
     * courseID = cpsc5023
     * studentID = 1235464
     *
     * @throws Exception
     */
    @Test
    public void initiateInstructorCodeEvaluation_should_return_ResponseEntity_OK() throws Exception {
        // Set Up
        String authToken = "TESTStringToken";
        String userID = "4625132";

        UserType type = UserType.GRADER;
        CommandOutput commandOutput = new CommandOutput(true, "test");

        Mockito.when(canvasClientService.fetchUserId(authToken)).thenReturn(userID);
        Mockito.when(evaluationService.executeCodeFile(any())).thenReturn(new ResponseEntity<>(commandOutput, HttpStatus.OK));

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/execute/courses/cpsc5023/assignments/cs5461321/submissions/1235464")
                        .header("Authorization", authToken)
                        .param("userType", String.valueOf(type))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())  // assert we get OK response
                .andReturn();

        // Assert
        CommandOutput controllerResponse = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                CommandOutput.class);
        assertTrue(controllerResponse.isSuccess());
    }

    /**
     * Tests the initiateInstructorCodeEvaluation method return Bad_Request for wrong userTypes
     * <p>
     * Values passed through path:
     * assignmentID = cs5461321
     * courseID = cpsc5023
     * studentID = 1235464
     *
     * @param userType Enum UserType
     * @throws Exception exception
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/chromeAPI_userType_instructor_tests.csv", numLinesToSkip = 1)
    public void initiateInstructorCodeEvaluation_should_return_badRequest_when_userType_not_valid_enum(String userType) throws Exception {
        // Set Up
        String authToken = "TestStringToken";
        String type = userType;

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/execute/courses/cpsc5023/assignments/cs5461321/submissions/1235464")
                        .header("Authorization", authToken)
                        .param("userType", type)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())  // assert we get bad request response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNAUTHORIZED", "STUDENT"})
    public void initiateInstructorCodeEvaluation_should_return_unauthorized_when_userType_not_GRADER(String userType) throws Exception {
        // Set Up
        String authToken = "TestStringToken";
        String type = userType;

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/execute/courses/cpsc5023/assignments/cs5461321/submissions/1235464")
                        .header("Authorization", authToken)
                        .param("userType", type)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())  // assert we get unauthorized response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    /**
     * Tests ResponseEntity returns OK with correct params
     */
    @Test
    public void initiateStudentCodeEvaluation_should_return_responseEntity_OK() throws Exception {
        // Set Up
        String authToken = "Iamastringtoken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";
        UserType type = UserType.STUDENT;
        CommandOutput commandOutput = new CommandOutput(true, "test");

        Mockito.when(canvasClientService.fetchUserId(authToken)).thenReturn(userID);
        Mockito.when(evaluationService.compileStudentCodeFile(any(), any())).thenReturn(new ResponseEntity<>(commandOutput, HttpStatus.OK));

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .param("userType", String.valueOf(type))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())  // assert we get OK response
                .andReturn();

        // Assert
        CommandOutput controllerResponse = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                CommandOutput.class);
        assertTrue(controllerResponse.isSuccess());
    }

    /**
     * Tests the initiateStudentCodeEvaluation method return Bad_Request for wrong userTypes
     *
     * @param userType Enum UserType
     * @throws Exception exception
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/chromeAPI_userType_student_tests.csv", numLinesToSkip = 1)
    public void initiateStudentCodeEvaluation_should_return_badRequest_when_userType_not_valid_enum(String userType) throws Exception {
        // Set Up
        String authToken = "TestStringToken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";
        String type = userType;

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .param("userType", type)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())  // assert we get bad request response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNAUTHORIZED", "GRADER"})
    public void initiateStudentCodeEvaluation_should_return_unauthorized_when_userType_not_STUDENT(String userType) throws Exception {
        // Set Up
        String authToken = "TestStringToken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";
        String type = userType;

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .param("userType", type)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())  // assert we get unauthorized response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }


}