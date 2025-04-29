package vn.techmaster.danglh.recruitmentproject.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebsocketDestination {

    NEW_APPLICATION_NOTIFICATION("new-application"), // down cv, accept cv, reject cv
    INTERVIEW_ACCEPTANCE_NOTIFICATION("interview-acceptance"),
    INTERVIEW_REFUSAL_NOTIFICATION("interview-refusal"),
    NEW_INTERVIEW_NOTIFICATION("new-interview"),
    CV_ACCEPTANCE_NOTIFICATION("cv-acceptance"),
    CV_REFUSAL_NOTIFICATION("cv-refusal"),
    EXPIRED_FAVORITE_JOB_NOTIFICATION("expired-favorite-job"),
    EXPIRED_JOB_NOTIFICATION("expired-job"),
    ENOUGH_PASSED_CANDIDATE_NOTIFICATION("enough-passed-candidate")
    ;

    private final String value;

}
