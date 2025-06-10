package vn.techmaster.danglh.recruitmentproject.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebsocketDestination {

    NEW_INTERVIEW_NOTIFICATION("new-interview"),// ok
    CV_ACCEPTANCE_NOTIFICATION("cv-acceptance"),// ok
    CV_REFUSAL_NOTIFICATION("cv-refusal"),// ok
    EXPIRED_FAVORITE_JOB_NOTIFICATION("expired-favorite-job"),//ok

    NEW_APPLICATION_NOTIFICATION("new-application"), // ok
    INTERVIEW_ACCEPTANCE_NOTIFICATION("interview-acceptance"),// ok
    INTERVIEW_REFUSAL_NOTIFICATION("interview-refusal"),//ok
    EXPIRED_JOB_NOTIFICATION("expired-job"),//ok
    ENOUGH_PASSED_CANDIDATE_NOTIFICATION("enough-passed-candidate")//ok
    ;

    private final String value;

}
