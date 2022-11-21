package com.canvas.service.models;

/**
 * Data class model
 * Representation of the extension user in the domain layer
 *
 * @author Alicia G (agarcia3)
 */
public class ExtensionUser {
    private String bearerToken;
    private String userId;
    private String courseId;
    private String assignmentId;


    /**
     * Getter for authorization token
     *
     * @return the auth token
     */
    public String getBearerToken() {
        return bearerToken;
    }

    /**
     * Setter for the authorization token
     *
     * @param bearerToken the authorization token
     */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    /**
     * Getter for the userId
     *
     * @return the userId for the given User
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter for the userId from Canvas API
     *
     * @param userId the canvas user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter for the course Id from Canvas
     *
     * @return the canvas course ID
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Setter for the course ID from Canvas
     *
     * @param courseId Canvas course
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Getter for the Assignment id from Canvas
     *
     * @return the canvas assignment
     */
    public String getAssignmentId() {
        return assignmentId;
    }

    /**
     * Setter for the assignment ID from Canvas
     *
     * @param assignmentId Canvas assignment for a course
     */
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
}
