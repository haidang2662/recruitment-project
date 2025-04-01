package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "company_working_fields")
public class CompanyWorkingField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "account_id")
    @ManyToOne(targetEntity = Account.class)
    Account account;

    String name;
}
