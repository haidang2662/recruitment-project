package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompanyResponse {

    Long id;

    String name;
    String alias;
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

    LocalDate createdAt;
    String email;

}
