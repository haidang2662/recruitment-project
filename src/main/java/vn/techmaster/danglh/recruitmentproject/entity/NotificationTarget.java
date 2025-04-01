package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.TargetType;


@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notification_targets")
public class NotificationTarget extends BaseEntity {

    @JoinColumn(name = "account_id")
    @ManyToOne(targetEntity = Account.class)
    Account targetId;

    @JoinColumn(name = "notification_id")
    @ManyToOne(targetEntity = Notification.class)
    Notification notification;

    @Enumerated(EnumType.STRING)
    TargetType type;

    @Column(columnDefinition = "boolean default false")
    boolean seen;

}
