package com.paravai.communities.adapter.in.rest;

import com.paravai.communities.application.CommunityNotFoundException;
import com.paravai.communities.application.CommunityStorageException;
import com.paravai.communities.application.PolicyConflictException;
import com.paravai.communities.domain.InvalidCommunityException;
import com.paravai.communities.domain.NotAdministratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public final class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidCommunityException.class)
    ProblemDetail invalid(InvalidCommunityException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DemoAuthenticationException.class)
    ResponseEntity<ProblemDetail> unauthenticated(DemoAuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Demo realm=\"communities\"")
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage()));
    }

    @ExceptionHandler(NotAdministratorException.class)
    ProblemDetail forbidden(NotAdministratorException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(CommunityNotFoundException.class)
    ProblemDetail notFound(CommunityNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(PolicyConflictException.class)
    ProblemDetail conflict(PolicyConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CommunityStorageException.class)
    ProblemDetail unavailable(CommunityStorageException exception) {
        LOG.warn("Community persistence operation failed", exception.getCause());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception) {
        LOG.error("Unexpected community request failure", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
