package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationResponse {

    Long id;

    JobResponse job;

    CvResponse cv;

    CandidateResponse candidate;

    String applicationDescription;

    ApplicationStatus status;

    String recruiterComment;

    LocalDateTime appliedDate;

    InterviewResponse interview;

}
