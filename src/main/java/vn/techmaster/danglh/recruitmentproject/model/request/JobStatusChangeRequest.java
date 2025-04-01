package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;

@Data
public class JobStatusChangeRequest {

    @NotNull(message = "Job status cannot be null")
    JobStatus status;

}
