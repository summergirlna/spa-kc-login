package com.example.spakclogin.registration;

import com.example.spakclogin.registration.dto.*;
import jakarta.validation.Valid;
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

    @PostMapping("/resend-code")
    public StartRegistrationResponse resendConfirmationCode(
            @Valid @RequestBody ResendConfirmationCodeRequest request
    ) {
        return registrationService.resendConfirmationCode(request.getRegistrationId());
    }

    @PostMapping("/confirm-code")
    public ConfirmCodeResponse confirmCode(@Valid @RequestBody ConfirmCodeRequest request) {
        registrationService.confirmCode(request.getRegistrationId(), request.getCode());
        return new ConfirmCodeResponse("Confirmation code verified");
    }

    @PostMapping("/complete")
    public CompleteRegistrationResponse completeRegistration(@Valid @RequestBody CompleteRegistrationRequest request) {
        return registrationService.completeRegistration(
                request.getRegistrationId(),
                request.getPassword(),
                request.getMfaType()
        );
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRegistrationException(RegistrationException exception) {
        return new ErrorResponse(exception.getMessage());
    }

}
