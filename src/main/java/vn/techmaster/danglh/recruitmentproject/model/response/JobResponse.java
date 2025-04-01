package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.*;
import vn.techmaster.danglh.recruitmentproject.entity.JobCategory;
import vn.techmaster.danglh.recruitmentproject.entity.Location;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobResponse {

    Long id;
    String name;
    String position;
    Location workingCity;
    Integer yearOfExperienceFrom;
    Integer yearOfExperienceTo;

    WorkingType workingType;

    WorkingTimeType workingTimeType;

    String workingAddress;

    Literacy literacy;

    JobLevel level;

    Integer recruitingQuantity;
    LocalDate expiredDate;
    String skills;
    String benefit;
    String requirement;
    Integer salaryFrom;
    Integer salaryTo;

    JobStatus status;

    String description;

    JobCategory category;

    LocalDate createdAt;

    boolean urgent;
    boolean favorite;

    CompanyResponse company;

    ApplicationResponse application;

}
