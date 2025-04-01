package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.techmaster.danglh.recruitmentproject.constant.Gender;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingTimeType;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingType;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CandidateSearchRequest extends BaseSearchRequest {

    String name;
    String phone;

    Gender gender;
    Literacy literacy;

    String skills;
    String graduatedAt;

    WorkingTimeType expectedWorkingTimeType;
    WorkingType expectedWorkingType;

}
