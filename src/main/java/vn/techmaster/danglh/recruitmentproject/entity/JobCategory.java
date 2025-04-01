package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.JobCategoryStatus;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "job_categories")
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Enumerated(EnumType.STRING)
    JobCategoryStatus status;

}
