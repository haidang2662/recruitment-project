package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "jobs")
public class Job extends BaseEntity {

    @JoinColumn(name = "company_id", referencedColumnName = "id")
    @ManyToOne(targetEntity = Company.class)
    Company company;

    String name;
    String position;
    Integer yearOfExperienceFrom;
    Integer yearOfExperienceTo;

    @Enumerated(EnumType.STRING)
    WorkingType workingType;

    @Enumerated(EnumType.STRING)
    WorkingTimeType workingTimeType;

    String workingAddress;

    @JoinColumn(name = "working_city")
    @ManyToOne(targetEntity = Location.class)
    Location workingCity;

    @Enumerated(EnumType.STRING)
    Literacy literacy;

    @Enumerated(EnumType.STRING)
    JobLevel level;

    Integer recruitingQuantity;
    LocalDate expiredDate;

    @Column(name = "skills", length = 1000)
    String skills;

    @Column(name = "description", length = 5000)
    String description;

    @Column(name = "benefit", length = 5000)
    String benefit;

    @Lob
    String requirement;

    Integer salaryFrom;
    Integer salaryTo;

    @Enumerated(EnumType.STRING)
    JobStatus status;

    @JoinColumn(name = "category_id")
    @ManyToOne(targetEntity = JobCategory.class)
    JobCategory category;

    @Column(columnDefinition = "boolean default false")
    boolean urgent;

}
