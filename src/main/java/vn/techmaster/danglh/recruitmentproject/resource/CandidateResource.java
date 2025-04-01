package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.CandidateSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.JobSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CandidateCompanyResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CandidateResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.JobResponse;
import vn.techmaster.danglh.recruitmentproject.service.CandidateService;

@RestController
@RequestMapping("/api/v1/candidates")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandidateResource {

    CandidateService candidateService;

    @GetMapping
    public CommonSearchResponse<?> searchCandidates(CandidateSearchRequest request)  {
        return candidateService.searchCandidate(request);
    }

    @GetMapping("/{id}")
    public CandidateCompanyResponse getCandidateDetails(@PathVariable("id") Long idCandidate) throws ObjectNotFoundException {
        return candidateService.getCandidateDetails(idCandidate);
    }

}
