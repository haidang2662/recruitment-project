package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;
import vn.techmaster.danglh.recruitmentproject.entity.Application;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.Job;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findFirstByCandidateAndJob(Candidate candidate, Job job);

    List<Application> findByCandidateAndJob(Candidate candidate, Job job);


    Optional<Application> findFirstByCandidateAndJobAndStatus(Candidate candidate, Job job, ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a " +
            "JOIN a.job j " +
            "JOIN j.company c " +
            "WHERE c.account.id = :accountId")
    int countApplicationsByAccountId(@Param("accountId") Long accountId);
}
