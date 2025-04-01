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
public class InterviewResponse {

    Long id;

    ApplicationResponse application;

    LocalDateTime invitationEmailSentAt;
    LocalDateTime interviewAt;
    String interviewAddress;

    InterviewType interviewType;

    Integer interviewStep;

    InterviewStatus status;

    String note;

}
