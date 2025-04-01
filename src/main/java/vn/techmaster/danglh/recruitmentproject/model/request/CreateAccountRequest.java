package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAccountRequest {

    @NotBlank
    @Length(max = 50, message = "email must be less than 50 characters")
    String email;

}
