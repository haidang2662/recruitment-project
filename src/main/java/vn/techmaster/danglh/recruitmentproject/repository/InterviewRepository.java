package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.techmaster.danglh.recruitmentproject.entity.Application;
import vn.techmaster.danglh.recruitmentproject.entity.Interview;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Interview findByApplication(Application application);

    @Query("SELECT COUNT(i) FROM Interview i " +
            "JOIN i.application a " +
            "JOIN a.job j " +
            "JOIN j.company c " +
            "WHERE c.account.id = :accountId")
    int countInterviewsByAccountId(@Param("accountId") Long accountId);
}
