package vn.techmaster.danglh.recruitmentproject.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewType;
import vn.techmaster.danglh.recruitmentproject.constant.JobLevel;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchInterviewDto {

    Long id;
    String candidateName;
    String jobTitle;
    LocalDateTime interviewEmailSentAt;
    LocalDateTime interviewAt;
    InterviewType type;
    InterviewStatus status;

    Long candidateId;
    Long jobId;

    Long totalRecord;


}
