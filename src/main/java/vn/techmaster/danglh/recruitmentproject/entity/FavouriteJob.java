package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "favourite_jobs")
public class FavouriteJob extends BaseEntity {

    @JoinColumn(name = "candidate_id")
    @ManyToOne(targetEntity = Candidate.class)
    Candidate candidate;

    @JoinColumn(name = "job_id")
    @ManyToOne(targetEntity = Job.class)
    Job job;

}
