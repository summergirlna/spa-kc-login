package com.example.spakclogin.passwordreset;

import com.example.spakclogin.passwordreset.dto.*;
import com.example.spakclogin.registration.RegistrationException;
import com.example.spakclogin.registration.dto.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/email")
    public StartPasswordResetResponse startPasswordReset(@Valid @RequestBody StartPasswordResetRequest request) {
        return passwordResetService.startPasswordReset(request.getEmail());
    }

    @PostMapping("/confirm-code")
    public ConfirmPasswordResetCodeResponse confirmCode(
            @Valid @RequestBody ConfirmPasswordResetCodeRequest request
    ) {
        passwordResetService.confirmCode(request.getResetId(), request.getCode());
        return new ConfirmPasswordResetCodeResponse("Password reset code verified.");
    }

    @PostMapping("/complete")
    public CompletePasswordResetResponse completePasswordReset(
            @Valid @RequestBody CompletePasswordResetRequest request
    ) {
        return passwordResetService.completePasswordReset(
                request.getResetId(),
                request.getPassword()
        );
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRegistrationException(RegistrationException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}