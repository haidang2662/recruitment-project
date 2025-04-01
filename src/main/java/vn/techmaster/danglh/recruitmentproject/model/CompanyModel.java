package vn.techmaster.danglh.recruitmentproject.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyModel {

    String name;
    String alias; // Viết tắt
    String phone;
    LocalDate foundAt;
    String taxCode;
    String headQuarterAddress;
    Integer employeeQuantity;
    String website;
    String avatarUrl;
    String coverImageUrl;
    String description;
    Double rating;

}
