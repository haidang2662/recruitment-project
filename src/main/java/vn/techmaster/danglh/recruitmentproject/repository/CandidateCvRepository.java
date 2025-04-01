package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.CandidateCv;

@Repository
public interface CandidateCvRepository extends JpaRepository<CandidateCv, Long> {

    Page<CandidateCv> findByCandidate(Candidate candidate, Pageable pageable);

    @Modifying
    @Query("update CandidateCv cv set cv.main = false where cv.candidate.id = :candidateId")
    void resetMainCv(Long candidateId);

}
