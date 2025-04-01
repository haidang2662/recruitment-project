package vn.techmaster.danglh.recruitmentproject.model.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.Gender;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingTimeType;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateCompanyResponse {

    Long id;
    String name;
    Gender gender;
    String phone;
    LocalDate dob;
    Double yearOfExperience;
    Literacy literacy;
    String graduatedAt;

    String email;
    String currentJobPosition;

    String address;
    String avatarUrl;
    String skills;

    Integer expectedSalaryFrom;
    Integer expectedSalaryTo;
    WorkingTimeType expectedWorkingTimeType;
    WorkingType expectedWorkingType;



}
