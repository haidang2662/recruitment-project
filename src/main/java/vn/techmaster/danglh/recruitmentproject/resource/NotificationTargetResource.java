package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.service.NotificationTargetService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/notification-targets")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationTargetResource {

    NotificationTargetService notificationTargetService;

    @PatchMapping("/{id}/seen")
    public void markAsSeen(@PathVariable Long id) throws ObjectNotFoundException {
        notificationTargetService.markAsSeen(id);
    }


}
