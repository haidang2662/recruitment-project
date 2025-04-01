package vn.techmaster.danglh.recruitmentproject.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.JobLevel;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSearchResponse {

    Long id;
    String name;
    LocalDate expiredDate;
    LocalDateTime createdAt;
    JobStatus status;
    Integer yearOfExperienceFrom;
    Integer yearOfExperienceTo;
    String position;
    JobLevel level;
    Integer recruitingQuantity;
    String workingAddress;
    Integer salaryFrom;
    Integer salaryTo;
    String companyAvatarUrl;
    boolean urgent;
    String workingTimeType;
    String workingType;
    String workingCity;
    boolean favorite;

    CompanyResponse company;

}
