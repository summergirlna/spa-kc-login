package com.example.spakclogin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/email")
    public StartRegistrationResponse startRegistration(@Valid @RequestBody StartRegistrationRequest request) {
        return registrationService.startRegistration(request.getEmail());
    }

    @PostMapping("/confirm-code")
    public ConfirmCodeResponse confirmCode(@Valid @RequestBody ConfirmCodeRequest request) {
        registrationService.confirmCode(request.getRegistrationId(), request.getCode());
        return new ConfirmCodeResponse("Confirmation code verified");
    }

    @PostMapping("/complete")
    public CompleteRegistrationResponse completeRegistration(@Valid @RequestBody CompleteRegistrationRequest request) {
        return registrationService.completeRegistration(request.getRegistrationId(), request.getPassword());
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRegistrationException(RegistrationException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @Data
    public static class StartRegistrationRequest {

        @NotBlank
        @Email
        private String email;
    }

    @Data
    public static class ConfirmCodeRequest {

        @NotBlank
        private String registrationId;

        @NotBlank
        @Size(min = 6, max = 6)
        private String code;
    }

    @Data
    public static class CompleteRegistrationRequest {

        @NotBlank
        private String registrationId;

        @NotBlank
        @Size(min = 8, max = 100)
        private String password;
    }

    public record StartRegistrationResponse(
            String registrationId,
            String message
    ) {}

    public record ConfirmCodeResponse(
            String message
    ) {}

    public record CompleteRegistrationResponse(
            String userId,
            String email,
            String message
    ) {}

    public record ErrorResponse(
            String message
    ) {}
}
