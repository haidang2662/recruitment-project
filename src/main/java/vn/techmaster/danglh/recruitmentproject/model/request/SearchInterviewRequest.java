package vn.techmaster.danglh.recruitmentproject.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewType;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchInterviewRequest extends BaseSearchRequest{

    String jobTitle;
    String candidateName;
    LocalDate interviewDateFrom;
    LocalDate interviewDateTo;
    InterviewType interviewType;
    InterviewStatus status;

}
