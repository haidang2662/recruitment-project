package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {

    @NotBlank(message = "Email is required.")
    @Size(max = 50, message = "Email must not exceed 50 characters.")
    @Email(message = "Must have correct email format")
    String email;

    @NotBlank(message = "Password is required.")
    @Size(max = 50, message = "Password must not exceed 50 characters.")
    String password;
//Password must include at least 1 uppercase letter, 1 lowercase letter, 1 number and must be at least 8 characters.
}
