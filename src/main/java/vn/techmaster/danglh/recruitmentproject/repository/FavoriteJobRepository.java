package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.FavouriteJob;
import vn.techmaster.danglh.recruitmentproject.entity.Job;

import java.util.Optional;

public interface FavoriteJobRepository extends JpaRepository<FavouriteJob, Long> {

    @Modifying
    @Query("delete from FavouriteJob f where f.candidate.id = :candidateId and f.job.id = :jobId")
    void deleteFavouriteJob(Long candidateId, Long jobId);

    Optional<FavouriteJob> findByCandidateAndJob(Candidate candidate, Job job);


}
