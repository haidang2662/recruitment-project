package vn.techmaster.danglh.recruitmentproject.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "applications")
public class Application extends BaseEntity {

    @JoinColumn(name = "job_id")
    @ManyToOne(targetEntity = Job.class)
    Job job;

    @JoinColumn(name = "candidate_cv_id")
    @ManyToOne(targetEntity = CandidateCv.class)
    CandidateCv cv;

    @JoinColumn(name = "candidate_id")
    @ManyToOne(targetEntity = Candidate.class)
    Candidate candidate;

    String applicationDescription;

    @Enumerated(EnumType.STRING)
    ApplicationStatus status;

    String recruiterComment;
}
