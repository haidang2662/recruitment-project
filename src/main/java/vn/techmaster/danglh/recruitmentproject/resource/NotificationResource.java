package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.BaseSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.NotificationStatisticalQuantityRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.NotificationStatisticalQuantityResponse;
import vn.techmaster.danglh.recruitmentproject.service.NotificationTargetService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/notifications")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationResource {

    NotificationTargetService notificationTargetService;

    @GetMapping
    public CommonSearchResponse<?> getNotifications(BaseSearchRequest request) throws ObjectNotFoundException {
        return notificationTargetService.getNotifications(request);
    }

    @GetMapping("/statistical-quantity")
    public NotificationStatisticalQuantityResponse statisticQuantity(NotificationStatisticalQuantityRequest request)
            throws ObjectNotFoundException {
        return notificationTargetService.statisticQuantity(request);
    }

}
