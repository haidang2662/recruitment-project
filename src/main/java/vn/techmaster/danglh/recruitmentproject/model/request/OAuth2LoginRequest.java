package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.Oauth2Tenant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OAuth2LoginRequest {

    @NotNull(message = "OAuth2 tenant is required")
    Oauth2Tenant tenant;

    @NotBlank(message = "Credential is required.")
    String credential;

}
