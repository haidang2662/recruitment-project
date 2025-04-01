package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewType;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewSearchResponse {

    Long id;
    CandidateResponse candidate;
    JobResponse job;
    LocalDateTime interviewEmailSentAt;
    LocalDateTime interviewAt;
    InterviewType type;
    InterviewStatus status;

}
