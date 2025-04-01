package vn.techmaster.danglh.recruitmentproject.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.Gender;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingTimeType;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingType;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateModel {

    String name;
    LocalDate dob;

    Gender gender;

    String phone;
    String address;
    String avatarUrl;
    String skills;
    Double yearOfExperience;
    String currentJobPosition;

    Literacy literacy;

    String graduatedAt;
    Integer expectedSalaryFrom;
    Integer expectedSalaryTo;

    WorkingTimeType expectedWorkingTimeType;

    WorkingType expectedWorkingType;

}
