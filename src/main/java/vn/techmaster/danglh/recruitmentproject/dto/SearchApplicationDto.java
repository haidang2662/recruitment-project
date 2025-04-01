package vn.techmaster.danglh.recruitmentproject.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchApplicationDto {

    Long id;

    String jobName;
    String candidateName;
    LocalDateTime appliedDate;
    ApplicationStatus status;

    String cvName;
    String cvUrl;

    Long totalRecord;

    Long cvId;
    Long jobId;
    Long candidateId;
}
