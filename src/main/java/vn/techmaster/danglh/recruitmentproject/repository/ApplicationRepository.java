package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.entity.Application;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.Job;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findFirstByCandidateAndJob(Candidate candidate, Job job);

}
