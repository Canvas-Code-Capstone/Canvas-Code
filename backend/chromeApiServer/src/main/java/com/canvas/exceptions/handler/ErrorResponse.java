package com.canvas.exceptions.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Date;


/**
 * Error response object that will be seen by the client (as a JSON).
 */
@Getter
@Setter
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date timestamp;

    private int code;

    private HttpStatus status;

    private String message;

    /**
     * Error Response containing the timestamp, http status,
     * http code, and custom message
     *
     * @param httpStatus HTTP Status type
     * @param message    custom message string to display
     */
    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.timestamp = new Date();
        this.status = httpStatus;
        this.code = httpStatus.value();
        this.message = message;
    }
}
