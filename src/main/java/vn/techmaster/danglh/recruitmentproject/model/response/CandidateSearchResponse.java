package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.Gender;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingTimeType;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateSearchResponse {

    Long id;

    String name;
    String phone;
    Gender gender;
    Literacy literacy;
    String skills;
    String graduatedAt;
    WorkingTimeType expectedWorkingTimeType;
    WorkingType expectedWorkingType;
    Integer totalAppliedJob;

}
