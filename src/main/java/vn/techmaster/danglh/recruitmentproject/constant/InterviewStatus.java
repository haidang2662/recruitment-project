package vn.techmaster.danglh.recruitmentproject.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterviewStatus {
    CREATED,
    PASSED,
    FAILED,
    CANDIDATE_ABSENCE,
    CANCELLED
}
