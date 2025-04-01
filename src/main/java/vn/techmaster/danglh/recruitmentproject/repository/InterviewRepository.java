package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.techmaster.danglh.recruitmentproject.entity.Application;
import vn.techmaster.danglh.recruitmentproject.entity.Interview;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Interview findByApplication(Application application);
}
