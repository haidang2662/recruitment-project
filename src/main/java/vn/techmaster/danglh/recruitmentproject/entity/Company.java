package vn.techmaster.danglh.recruitmentproject.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "companies")
public class Company extends BaseEntity {

    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL)
    Account account;

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
