package vn.techmaster.danglh.recruitmentproject.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.techmaster.danglh.recruitmentproject.exception.*;
import vn.techmaster.danglh.recruitmentproject.model.response.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ExistedAccountException.class,
            InvalidRefreshTokenException.class,
            ObjectNotFoundException.class,
            ExpiredEmailActivationUrlException.class,
            ExpiredPasswordForgottenUrlException.class,
            MessagingException.class,
            UnprocessableEntityException.class,
            IllegalArgumentException.class,
            InvalidFileExtensionException.class,
            ExistedJobApplicationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .build();

        if (
                ex instanceof ExistedAccountException
                        || ex instanceof PasswordNotMatchedException
                        || ex instanceof IllegalArgumentException
                        || ex instanceof InvalidFileExtensionException
        ) {
            errorResponse.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } else if (
                ex instanceof InvalidRefreshTokenException
                        || ex instanceof ObjectNotFoundException
        ) {
            errorResponse.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } else if (
                ex instanceof ExpiredEmailActivationUrlException
                        || ex instanceof ExpiredPasswordForgottenUrlException
                        || ex instanceof MessagingException
                        || ex instanceof UnprocessableEntityException
                        || ex instanceof ExistedJobApplicationException
        ) {
            errorResponse.setCode(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        }

        log.error(ex.getMessage(), ex);
        errorResponse.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
