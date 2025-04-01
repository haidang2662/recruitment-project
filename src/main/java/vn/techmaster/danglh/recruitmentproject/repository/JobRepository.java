package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;
import vn.techmaster.danglh.recruitmentproject.entity.Job;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusAndExpiredDateBefore(JobStatus status, LocalDate expiredDate);

}
