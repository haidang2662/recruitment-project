package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangingRequest {

    @NotBlank
    String password;

    @NotBlank
    String confirmedPassword;

}
