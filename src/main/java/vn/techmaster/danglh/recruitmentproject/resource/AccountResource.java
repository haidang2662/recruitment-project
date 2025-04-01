package vn.techmaster.danglh.recruitmentproject.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.techmaster.danglh.recruitmentproject.exception.*;
import vn.techmaster.danglh.recruitmentproject.model.request.*;
import vn.techmaster.danglh.recruitmentproject.model.response.AccountResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.service.AccountService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/accounts")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountResource {

    AccountService accountService;

    ObjectMapper objectMapper;

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody @Valid PasswordChangingRequest request)
            throws ObjectNotFoundException, PasswordNotMatchedException {
        accountService.changePassword(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/activations")
    public ResponseEntity<?> activateAccount(@PathVariable Long id)
            throws ObjectNotFoundException, ExpiredEmailActivationUrlException {
        accountService.activateAccount(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/activation_emails")
    public ResponseEntity<?> sendActivationEmail(@PathVariable Long id)
            throws MessagingException {
        accountService.sendActivationEmail(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/password_forgotten_emails")
    public ResponseEntity<?> sendForgotPasswordEmail(@RequestBody @Valid ForgotPasswordEmailRequest request)
            throws MessagingException {
        accountService.sendForgotPasswordEmail(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/password_forgotten")
    public ResponseEntity<?> changeForgotPassword(@PathVariable Long id, @RequestBody @Valid PasswordChangingRequest request)
            throws ObjectNotFoundException, ExpiredPasswordForgottenUrlException, PasswordNotMatchedException {
        accountService.changeForgotPassword(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public CommonSearchResponse<?> search(AccountSearchRequest request) {
        return accountService.searchAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getDetail(@PathVariable Long id) throws ObjectNotFoundException {
        return accountService.getDetail(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateAccountRequest request) throws ExistedAccountException {
        AccountResponse accountResponse = accountService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(accountResponse);
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(
            @PathVariable Long id,
            @RequestPart("accountRequest") String updateAccountRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws ObjectNotFoundException, IOException, InvalidFileExtensionException {
        try {
            UpdateAccountRequest request = objectMapper.readValue(updateAccountRequest, UpdateAccountRequest.class);
            return accountService.updateAccount(id, avatar, cover, request);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Dữ liệu JSON không hợp lệ", e);
        }
    }

}
