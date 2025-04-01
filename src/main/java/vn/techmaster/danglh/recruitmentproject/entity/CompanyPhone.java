package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.PhoneType;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "company_phones")
public class CompanyPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "company_id")
    @ManyToOne(targetEntity = Company.class)
    Company company;

    String phone;

    @Enumerated(EnumType.STRING)
    PhoneType type;

    @Column(columnDefinition = "boolean default false")
    boolean main;
}
