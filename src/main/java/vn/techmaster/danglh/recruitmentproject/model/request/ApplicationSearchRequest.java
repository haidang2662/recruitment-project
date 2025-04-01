package vn.techmaster.danglh.recruitmentproject.model.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationSearchRequest extends BaseSearchRequest {
    String jobName;
    String candidateName;
    ApplicationStatus status;
    Long candidateId;
}
