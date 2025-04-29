package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.constant.NotificationStatus;
import vn.techmaster.danglh.recruitmentproject.constant.NotificationType;
import vn.techmaster.danglh.recruitmentproject.dto.NotificationDto;
import vn.techmaster.danglh.recruitmentproject.entity.Notification;
import vn.techmaster.danglh.recruitmentproject.entity.NotificationTarget;
import vn.techmaster.danglh.recruitmentproject.repository.NotificationRepository;
import vn.techmaster.danglh.recruitmentproject.repository.NotificationTargetRepository;

import java.time.LocalDate;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    ObjectMapper objectMapper;
    SimpMessagingTemplate messagingTemplate;
    NotificationRepository notificationRepository;
    NotificationTargetRepository notificationTargetRepository;

    @Async
    @Transactional
    public void pushNotification(NotificationDto dto) throws JsonProcessingException {
        Notification notification = Notification.builder()
                .sender(dto.getSender())
                .title(dto.getTitle())
                .content(dto.getContent())
                .topic(dto.getDestination().getValue())
                .status(NotificationStatus.SENT)
                .type(NotificationType.SINGLE)
                .startAt(LocalDate.now())
                .metadata(dto.getMetadata())
                .build();

        NotificationTarget target = NotificationTarget.builder()
                .notification(notification)
                .target(dto.getTarget())
                .type(dto.getTargetType())
                .seen(false)
                .build();
        notificationRepository.save(notification);
        notificationTargetRepository.save(target);

        String topic = "/topic/" + target.getTarget().getEmail() + "/" + dto.getDestination().getValue();
        messagingTemplate.convertAndSend(
                topic,
                objectMapper.writeValueAsString(notification)
        );
    }


}
