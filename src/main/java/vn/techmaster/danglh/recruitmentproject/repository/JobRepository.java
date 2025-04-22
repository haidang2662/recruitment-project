package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;
import vn.techmaster.danglh.recruitmentproject.entity.Job;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusAndExpiredDateBefore(JobStatus status, LocalDate expiredDate);

    List<Job> findByStatusAndExpiredDateBetween(JobStatus status, LocalDate fromDate, LocalDate toDate);

    @Query("select j from Job j " +
            "where j.status = :status " +
            "and j.expiredDate between :fromDate and :toDate " +
            "and j.passedQuantity < j.recruitingQuantity")
    List<Job> findExpiredRecruitingJobs(JobStatus status, LocalDate fromDate, LocalDate toDate);

}
