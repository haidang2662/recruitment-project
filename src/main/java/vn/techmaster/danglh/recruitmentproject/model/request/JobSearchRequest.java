package vn.techmaster.danglh.recruitmentproject.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.techmaster.danglh.recruitmentproject.constant.JobLevel;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobSearchRequest extends BaseSearchRequest {

    String name;

    List<Long> locationIds;
    Long categoryId;
    List<String> workingTypes;
    List<String> workingTimeTypes;
    String yearOfExperience;
    Long salaryFrom;
    Long salaryTo;

    String position;
    JobLevel level;
    String skills;
    LocalDate expiredDateFrom;
    LocalDate expiredDateTo;
    JobStatus status;

    Boolean favorite;
    Boolean application;

}
