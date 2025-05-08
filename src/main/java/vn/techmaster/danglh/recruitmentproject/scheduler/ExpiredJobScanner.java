package vn.techmaster.danglh.recruitmentproject.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.constant.*;
import vn.techmaster.danglh.recruitmentproject.dto.NotificationMetadataDto;
import vn.techmaster.danglh.recruitmentproject.entity.*;
import vn.techmaster.danglh.recruitmentproject.exception.ExistedJobApplicationException;
import vn.techmaster.danglh.recruitmentproject.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpiredJobScanner {

    @Value("${application.account.admin.username}")
    String adminUsername;

    final JobRepository jobRepository;
    final FavoriteJobRepository favoriteJobRepository;
    final NotificationRepository notificationRepository;
    final NotificationTargetRepository notificationTargetRepository;
    final ObjectMapper objectMapper;
    final SimpMessagingTemplate messagingTemplate;
    final AccountRepository accountRepository;
    final ApplicationRepository applicationRepository;

    //    @Scheduled(fixedDelay = 1800000, initialDelay = 10000) // => cứ 30p chạy 1 phát (phát đầu tiên đợi chương trình khởi động xong 10s mới chạy)
    @Scheduled(cron = "0 0 0 * * *") // chạy lúc 0h hằng ngày => quét các job hết hạn
    @Transactional
    public void scanExpiredJob() {
        log.info("============ START SCAN EXPIRED JOBS =============");
        // thực hiện quét trong DB mà có giá trị của cột expired_date nhỏ hơn ngày hiện tại
        List<Job> jobList = jobRepository.findByStatusAndExpiredDateBefore(JobStatus.PUBLISH, LocalDate.now());
        if (jobList.isEmpty()) {
            log.info("============ NO EXPIRED JOBS TO UPDATE ==> SKIP =============");
            return;
        }
        // => lấy ra => chuyển trạng thái là hết hạn
        jobList.forEach(j -> j.setStatus(JobStatus.EXPIRED));

        jobRepository.saveAll(jobList);
        log.info("============ END SCAN EXPIRED JOBS with" + jobList.size() + " jobs =============");
    }


    @Scheduled(fixedDelay = 36000000, initialDelay = 30000)
    // => cứ 30p chạy 1 phát (phát đầu tiên đợi chương trình khởi động xong 10s mới chạy)
//    @Scheduled(cron = "0 0 0 * * *") // chạy lúc 0h hằng ngày => quét các job hết hạn
    @Transactional
    public void sendNotificationExpiredFavoriteJob() throws JsonProcessingException {
        log.info("============ START SEND NOTIFICATION EXPIRED FAVORITE JOBS =============");

        LocalDate now = LocalDate.now();
        LocalDate threeDaysLater = now.plusDays(3);

        // thực hiện quét trong DB mà có giá trị của cột expired_date nhỏ hơn ngày hiện tại
        List<Job> jobList = jobRepository.findByStatusAndExpiredDateBetween(JobStatus.PUBLISH, now, threeDaysLater);


        if (jobList.isEmpty()) {
            log.info("============ NO EXPIRED JOBS TO UPDATE ==> SKIP =============");
            return;
        }

        List<Long> ids = jobList.stream().map(BaseEntity::getId).toList();
        List<FavouriteJob> favouriteJobs = favoriteJobRepository.findByJobIdIn(ids);
        if (favouriteJobs.isEmpty()) {
            log.info("============ NO EXPIRED JOBS TO UPDATE ==> SKIP =============");
            return;
        }

        List<Notification> notificationList = new ArrayList<>();
        List<NotificationTarget> notificationTargetList = new ArrayList<>();

        // Xử lý send notification favorite job
        for (FavouriteJob favoriteJob : favouriteJobs) {
            try {
                Job job = favoriteJob.getJob();
                Company company = job.getCompany();
                Candidate candidate = favoriteJob.getCandidate();
                if (candidate == null || candidate.getAccount() == null) {
                    continue;
                }

                NotificationMetadataDto metadata = NotificationMetadataDto.builder()
                        .jobId(job.getId())
                        .build();

                Notification notification = Notification.builder()
                        .sender(company.getAccount())
                        .title("Công việc yêu thích sắp hết hạn")
                        .content("Công việc " + job.getName() + " (của công ty " + company.getName() + ") mà bạn yêu thích sắp hết hạn")
                        .topic(WebsocketDestination.EXPIRED_FAVORITE_JOB_NOTIFICATION.getValue())
                        .status(NotificationStatus.SENT)
                        .type(NotificationType.SINGLE)
                        .startAt(LocalDate.now())
                        .metadata(objectMapper.writeValueAsString(metadata))
                        .build();
                notificationList.add(notification);

                NotificationTarget target = NotificationTarget.builder()
                        .notification(notification)
                        .target(candidate.getAccount())
                        .type(TargetType.CANDIDATE)
                        .seen(false)
                        .build();
                notificationTargetList.add(target);

                String topic = "/topic/" + target.getTarget().getEmail() + "/" + WebsocketDestination.EXPIRED_FAVORITE_JOB_NOTIFICATION.getValue();
                log.info("Send websocket to topic: " + topic);

                messagingTemplate.convertAndSend(
                        topic,
                        objectMapper.writeValueAsString(notification)
                );
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        notificationRepository.saveAll(notificationList);
        notificationTargetRepository.saveAll(notificationTargetList);

        // save 1 bảng mới notificationWebSocket sau đó tạo quét 5s 1 lần rồi gửi notification , rồi đánh dấu đã gửi rồi . Để giải quyết các vấn đề nâng cao .

        log.info("============ END SCAN EXPIRED JOBS with" + jobList.size() + " jobs =============");
    }


    @Scheduled(fixedDelay = 36000000, initialDelay = 30000000)
//    @Scheduled(cron = "0 0 0 * * *") // chạy lúc 0h hằng ngày => quét các job hết hạn
    @Transactional
    public void sendNotificationExpiredJob() throws JsonProcessingException {
        log.info("============ START SEND NOTIFICATION EXPIRED JOBS =============");

        LocalDate now = LocalDate.now();
        LocalDate threeDaysLater = now.plusDays(3);

        // thực hiện quét trong DB mà có giá trị của cột expired_date nhỏ hơn ngày hiện tại
        List<Job> jobs = jobRepository.findExpiredRecruitingJobs(JobStatus.PUBLISH, now, threeDaysLater);

        if (jobs.isEmpty()) {
            log.info("============ NO EXPIRED JOBS TO UPDATE ==> SKIP =============");
            return;
        }

        List<Notification> notificationList = new ArrayList<>();
        List<NotificationTarget> notificationTargetList = new ArrayList<>();

        // Xử lý send notification expired job :
        Optional<Account> admin = accountRepository.findByEmail(adminUsername);

        if (admin.isEmpty()) {
            return;
        }
        for (Job job : jobs) {
            try {
                Company company = job.getCompany();
                NotificationMetadataDto metadata = NotificationMetadataDto.builder()
                        .jobId(job.getId())
                        .build();
                Notification notification = Notification.builder()
                        .sender(admin.get())
                        .title("Tin tuyển dụng chưa hoàn thành sắp hết hạn")
                        .content("Tin tuyển dụng " + job.getName() + " cho vị trí " + job.getPosition() + " sắp hết hạn.")
                        .topic(WebsocketDestination.EXPIRED_JOB_NOTIFICATION.getValue())
                        .status(NotificationStatus.SENT)
                        .type(NotificationType.SINGLE)
                        .startAt(LocalDate.now())
                        .metadata(objectMapper.writeValueAsString(metadata))
                        .build();
                notificationList.add(notification);

                NotificationTarget target = NotificationTarget.builder()
                        .notification(notification)
                        .target(company.getAccount())
                        .type(TargetType.COMPANY)
                        .seen(false)
                        .build();
                notificationTargetList.add(target);

//            // bắn noti
                messagingTemplate.convertAndSend(
                        "/topic/" + target.getTarget().getEmail() + "/" + WebsocketDestination.EXPIRED_JOB_NOTIFICATION.getValue(),
                        objectMapper.writeValueAsString(notification)
                );
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        notificationRepository.saveAll(notificationList);
        notificationTargetRepository.saveAll(notificationTargetList);

        log.info("============ END SCAN EXPIRED JOBS with " + jobs.size() + " jobs =============");
    }

}
