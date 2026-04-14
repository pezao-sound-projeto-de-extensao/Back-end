package sound.pezao.backend.exception.handler;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityExistsException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import sound.pezao.backend.exception.*;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status, "Campos inválidos."
        );
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(EntityExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "E-mail já está em uso");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(409).body(problemDetail);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(EntityNomeJaExisteException.class)
    public ResponseEntity<ProblemDetail> handleNomeJaExiste(EntityNomeJaExisteException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(EntityInativaException.class)
    public ResponseEntity<ProblemDetail> handleInativa(EntityInativaException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(LoginInvalidoException.class)
    public ResponseEntity<ProblemDetail> handleDadosInvalidos(LoginInvalidoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(PrimeiroAcessoException.class)
    public ResponseEntity<ProblemDetail> handleSenhaPadrao(PrimeiroAcessoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAcessoNegado(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ProblemDetail> handleTokenExpirado(ExpiredJwtException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage()
        );
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("TimeStamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }


}