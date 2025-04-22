package vn.techmaster.danglh.recruitmentproject.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.TargetType;
import vn.techmaster.danglh.recruitmentproject.constant.WebsocketDestination;
import vn.techmaster.danglh.recruitmentproject.entity.Account;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDto {

    Account sender;
    String title;
    String content;
    Account target;
    TargetType targetType;
    WebsocketDestination destination;

}
