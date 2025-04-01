package vn.techmaster.danglh.recruitmentproject.resource;

import vn.techmaster.danglh.recruitmentproject.exception.ExistedAccountException;
import vn.techmaster.danglh.recruitmentproject.exception.InvalidRefreshTokenException;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.LoginRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.RefreshTokenRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.RegistrationRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.JwtResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.AccountResponse;
import vn.techmaster.danglh.recruitmentproject.service.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/authentications")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationResource {

    AuthenticationService authenticateService;

    @PostMapping("/login")
    public JwtResponse authenticateUser(@Valid @RequestBody LoginRequest request) throws ObjectNotFoundException {
        return authenticateService.authenticate(request);
    }

    @PostMapping("/registration")
    public ResponseEntity<AccountResponse> registerAccount(@Valid @RequestBody RegistrationRequest request)
            throws ExistedAccountException, MessagingException {
        AccountResponse accountResponse = authenticateService.registerAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @PostMapping("/refresh_token")
    public JwtResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request)
            throws InvalidRefreshTokenException {
        return authenticateService.refreshToken(request);
    }

    @PostMapping("/logout")
    public void logout() {
        authenticateService.logout();
    }

}
