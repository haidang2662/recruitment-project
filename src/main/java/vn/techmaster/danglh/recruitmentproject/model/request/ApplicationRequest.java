package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;

@Data
public class ApplicationRequest {

    @NotNull(message = "Application status cannot be null")
    ApplicationStatus status;

}
