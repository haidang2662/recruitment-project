package vn.techmaster.danglh.recruitmentproject.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;
import vn.techmaster.danglh.recruitmentproject.entity.Job;
import vn.techmaster.danglh.recruitmentproject.repository.JobRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ExpiredJobScanner {

    JobRepository jobRepository;

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

}
