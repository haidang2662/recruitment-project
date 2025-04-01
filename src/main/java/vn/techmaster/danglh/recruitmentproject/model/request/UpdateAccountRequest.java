package vn.techmaster.danglh.recruitmentproject.model.request;

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
public class UpdateAccountRequest {

    String name;
    String avatarUrl;

    //////////////////////

    LocalDate dob;
    Gender gender;
    String phone;
    String address;
    String skills;
    Double yearOfExperience;
    Literacy literacy;
    String graduatedAt;
    Integer expectedSalaryFrom;
    Integer expectedSalaryTo;
    WorkingTimeType expectedWorkingTimeType;
    WorkingType expectedWorkingType;
    String currentJobPosition;

    /////////////////////////

    String alias;
    LocalDate foundAt;
    String taxCode;
    String headQuarterAddress;
    Integer employeeQuantity;
    String website;
    String coverImageUrl;
    String description;
    Double rating;

}
