package com.example.spakclogin.account;

import com.example.spakclogin.account.dto.ConfirmEmailChangeRequest;
import com.example.spakclogin.account.dto.ConfirmEmailChangeResponse;
import com.example.spakclogin.account.dto.StartEmailChangeRequest;
import com.example.spakclogin.account.dto.StartEmailChangeResponse;
import com.example.spakclogin.registration.RegistrationException;
import com.example.spakclogin.registration.dto.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/email-change/start")
    public StartEmailChangeResponse startEmailChange(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StartEmailChangeRequest request
            ) {
        return accountService.startEmailChange(jwt.getSubject(), request.getNewEmail());
    }

    @PostMapping("/email-change/confirm")
    public ConfirmEmailChangeResponse confirmEmailChange(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConfirmEmailChangeRequest request
    ) {
        return accountService.confirmEmailChange(
                jwt.getSubject(),
                request.getChangeId(),
                request.getCode()
        );
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRegistrationException(RegistrationException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
