package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.NotificationTarget;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.BaseSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.NotificationResponse;
import vn.techmaster.danglh.recruitmentproject.repository.AccountRepository;
import vn.techmaster.danglh.recruitmentproject.repository.NotificationTargetRepository;
import vn.techmaster.danglh.recruitmentproject.security.SecurityUtils;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationTargetService {

    AccountRepository accountRepository;
    NotificationTargetRepository notificationTargetRepository;

    public CommonSearchResponse<?> getNotifications(BaseSearchRequest request) throws ObjectNotFoundException {
        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ObjectNotFoundException("Account is not found"));

        Page<NotificationTarget> notificationTargetPage = notificationTargetRepository.findByTarget(
                account,
                PageRequest.of(request.getPageIndex(), request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"))
        );


        List<NotificationResponse> data = notificationTargetPage.getContent()
                .stream()
                .map(noti ->
                        NotificationResponse.builder()
                                .id(noti.getId())
                                .title(noti.getNotification().getTitle())
                                .content(noti.getNotification().getContent())
                                .seen(noti.isSeen())
                                .topic(noti.getNotification().getTopic())
                                .createdAt(noti.getCreatedAt())
                                .metadata(noti.getNotification().getMetadata())
                                .build()
                )
                .toList();

        return CommonSearchResponse.<NotificationResponse>builder()
                .data(data)
                .totalPage(notificationTargetPage.getTotalPages())
                .totalRecord(notificationTargetPage.getTotalElements())
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }
}
