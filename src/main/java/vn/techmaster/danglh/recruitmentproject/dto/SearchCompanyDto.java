package vn.techmaster.danglh.recruitmentproject.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchCompanyDto {

    Long id;
    String name;
    String alias;
    String email;
    String phone;
    String headQuarterAddress;
    String website;
    LocalDate createdAt;
    LocalDate foundAt;
    String avatarUrl;
    String coverImageUrl;
    Integer employeeQuantity;

    Long totalRecord;
}
