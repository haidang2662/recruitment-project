package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.JobLevel;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingTimeType;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingType;
import vn.techmaster.danglh.recruitmentproject.entity.JobCategory;
import vn.techmaster.danglh.recruitmentproject.entity.Location;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobRequest {

    @NotBlank(message = "Job title không được bỏ trống")
    @Size(max = 50, message = "Job title không được quá 50 ký tụ")
    String name;

    @NotBlank(message = "Position không được bỏ trống")
    @Size(max = 50, message = "Position không được quá 50 ký tụ")
    String position;

    @NotNull(message = "Working City không được bỏ trống")
    Long workingCityId;

    @NotNull(message = "Year Of Experience From không được bỏ trống")
    @Min(value = 1, message = "Year Of Experience From phải lớn hơn hoặc bằng 1")
    Integer yearOfExperienceFrom;

    @Min(value = 1, message = "Year Of Experience To phải lớn hơn hoặc bằng 1")
    Integer yearOfExperienceTo;

    @NotNull(message = "....")
    WorkingType workingType;

    WorkingTimeType workingTimeType;

    @NotBlank(message = "Working Address không được bỏ trống")
    @Size(max = 50, message = "Working Address không được quá 50 ký tụ")
    String workingAddress;

    Literacy literacy;

    JobLevel level;

    @NotNull(message = "Recruiting Quantity không được bỏ trống")
    @Min(value = 1, message = "Recruiting Quantity phải lớn hơn hoặc bằng 1")
    Integer recruitingQuantity;

    @NotNull(message = "Expired Date không được bỏ trống")
    @Future(message = "Expired Date phải là ngày trong tương lai")
    LocalDate expiredDate;

    @NotBlank(message = "Skills không được bỏ trống")
    @Size(max = 1000, message = "Skills không được quá 1000 ký tụ")
    String skills;

    @NotBlank(message = "Benefit không được bỏ trống")
    @Size(max = 5000, message = "Benefit không được quá 5000 ký tụ")
    String benefit;

    @NotBlank(message = "Requirement không được bỏ trống")
    @Size(max = 5000, message = "Requirement không được quá 5000 ký tụ")
    String requirement;

    @NotNull(message = "Salary From không được bỏ trống")
    @Min(value = 1, message = "Salary from phải lớn hơn hoặc bằng 1 ")
    Integer salaryFrom;

    @NotNull(message = "Salary To không được bỏ trống")
    @Min(value = 1, message = "Salary to phải lớn hơn hoặc bằng 1")
    Integer salaryTo;

    @NotBlank(message = "Description không được bỏ trống")
    @Size(max = 5000, message = "Description không được quá 5000 ký tụ")
    String description;

    @NotNull(message = "Category không được bỏ trống")
    Long categoryId;

    boolean urgent = false;

}
