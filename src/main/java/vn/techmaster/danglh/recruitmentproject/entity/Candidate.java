package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.Gender;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingTimeType;
import vn.techmaster.danglh.recruitmentproject.constant.WorkingType;

import java.time.LocalDate;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "candidates")
public class Candidate extends BaseEntity {

    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL)
    Account account;

    String name;
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    Gender gender;

    String currentJobPosition;

    String phone;
    String address;
    String avatarUrl;
    String skills;
    Double yearOfExperience;

    @Enumerated(EnumType.STRING)
    Literacy literacy;

    String graduatedAt;
    Integer expectedSalaryFrom;
    Integer expectedSalaryTo;

    @Enumerated(EnumType.STRING)
    WorkingTimeType expectedWorkingTimeType;

    @Enumerated(EnumType.STRING)
    WorkingType expectedWorkingType;

}
