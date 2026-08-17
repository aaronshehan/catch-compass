package com.example.catchcompass.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns validation failures into a predictable JSON shape for the React client.
 *
 * <p>Uses {@link ProblemDetail} (RFC 9457), which Spring supports natively, with
 * one added property: a field-name to message map the frontend can render next
 * to the offending input. Without a fixed contract like this, each screen ends
 * up inventing its own error parsing.
 *
 * <p>This applies application-wide, but only fires for controllers that let the
 * exception escape. The Thymeleaf controllers take a BindingResult parameter,
 * which handles errors in place and redisplays the form, so they never reach here.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidationFailure(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        // Class-level rules such as the latitude/longitude pairing arrive here
        // rather than as field errors.
        exception.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setDetail("Some fields need correcting");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail onUploadTooLarge(MaxUploadSizeExceededException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        problem.setTitle("Photo too large");
        problem.setDetail("Choose an image under 10 MB");
        problem.setProperty("errors", Map.of("photo", "Choose an image under 10 MB"));
        return problem;
    }
}
