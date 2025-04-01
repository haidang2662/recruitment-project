package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.FavouriteJob;
import vn.techmaster.danglh.recruitmentproject.entity.Job;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.FavoriteJobRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.FavoriteJobResponse;
import vn.techmaster.danglh.recruitmentproject.repository.CandidateRepository;
import vn.techmaster.danglh.recruitmentproject.repository.FavoriteJobRepository;
import vn.techmaster.danglh.recruitmentproject.repository.JobRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteJobService {

    JobRepository jobRepository;
    CandidateRepository candidateRepository;
    FavoriteJobRepository favoriteJobRepository;

    public FavoriteJobResponse favoriteJob(FavoriteJobRequest request) throws ObjectNotFoundException {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ObjectNotFoundException("Job not found"));

        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Candidate candidate = candidateRepository.findByAccount(user.getAccount())
                .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));

        FavouriteJob favouriteJob = new FavouriteJob(candidate, job);
        favoriteJobRepository.save(favouriteJob);

        return new FavoriteJobResponse(favouriteJob.getId());
    }

    @Transactional
    public void unfavoriteJob(FavoriteJobRequest request) throws ObjectNotFoundException {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Candidate candidate = candidateRepository.findByAccount(user.getAccount())
                .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));

        favoriteJobRepository.deleteFavouriteJob(candidate.getId(), request.getJobId());
    }
}
