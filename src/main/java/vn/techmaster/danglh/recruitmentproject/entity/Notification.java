package vn.techmaster.danglh.recruitmentproject.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.NotificationStatus;
import vn.techmaster.danglh.recruitmentproject.constant.NotificationType;

import java.time.LocalDate;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @JoinColumn(name = "account_id")
    @ManyToOne(targetEntity = Account.class)
    Account senderId;

    String title;
    String content;

    @Enumerated(EnumType.STRING)
    NotificationStatus status;

    @Enumerated(EnumType.STRING)
    NotificationType type;

    LocalDate startAt;
    LocalDate finishAt;

}
