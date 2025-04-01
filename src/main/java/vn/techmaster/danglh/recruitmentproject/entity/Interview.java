package vn.techmaster.danglh.recruitmentproject.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewType;

import java.time.LocalDateTime;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "interviews")
public class Interview extends BaseEntity {

    @JoinColumn(name = "application_id")
    @ManyToOne(targetEntity = Application.class)
    Application application;

    LocalDateTime invitationEmailSentAt;
    LocalDateTime interviewAt;
    String interviewAddress;

    @Enumerated(EnumType.STRING)
    InterviewType interviewType;

    Integer interviewStep;

    @Enumerated(EnumType.STRING)
    InterviewStatus status;

    String note;

}
