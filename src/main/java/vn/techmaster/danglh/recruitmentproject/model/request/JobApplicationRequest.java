package vn.techmaster.danglh.recruitmentproject.model.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobApplicationRequest {

    Long jobId;
    Long cvId;
    String applicationDescription;

}
