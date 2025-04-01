package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
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
@Table(name = "candidate_cvs")
public class CandidateCv extends BaseEntity {

    @JoinColumn(name = "candidate_id")
    @ManyToOne(targetEntity = Candidate.class)
    Candidate candidate;

    String cvUrl;

    @Column(columnDefinition = "boolean default false")
    boolean main;

}
