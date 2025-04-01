package vn.techmaster.danglh.recruitmentproject.resource;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.exception.UnprocessableEntityException;
import vn.techmaster.danglh.recruitmentproject.model.request.JobRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.JobSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.JobStatusChangeRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.JobResponse;
import vn.techmaster.danglh.recruitmentproject.service.JobService;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/jobs")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobResource {

    JobService jobService;

    @PostMapping
    public JobResponse postJob(@RequestBody @Valid JobRequest request) throws ObjectNotFoundException {
        return jobService.postJob(request);
    }

    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) throws UnprocessableEntityException {
        jobService.deleteJob(id);
    }

    @GetMapping("/{id}")
    public JobResponse getJobDetails(@PathVariable("id") Long idJob) throws ObjectNotFoundException {
        return jobService.getJobDetails(idJob);
    }

    @PutMapping("{id}")
    public JobResponse updateJob(@PathVariable("id") Long idJob, @RequestBody JobRequest request)
            throws ObjectNotFoundException {
        return jobService.updateJob(idJob, request);
    }

    @GetMapping
    public CommonSearchResponse<?> searchJobs(JobSearchRequest request)  {
        return jobService.searchJob(request);
    }

    @PatchMapping("/{jobId}/status")
    public void changeJobStatus(@PathVariable Long jobId, @RequestBody @Valid JobStatusChangeRequest request)
            throws ObjectNotFoundException {
        jobService.changeJobStatus(jobId, request);
    }

    @GetMapping("/application")
    public CommonSearchResponse<?> searchApplicationJobs(JobSearchRequest request) {
        return jobService.searchApplicationJob(request);
    }

}
