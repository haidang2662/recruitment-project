package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.objenesis.ObjenesisException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.constant.JobStatus;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.dto.SearchJobDto;
import vn.techmaster.danglh.recruitmentproject.entity.*;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.exception.UnprocessableEntityException;
import vn.techmaster.danglh.recruitmentproject.model.request.JobRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.JobSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.JobStatusChangeRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.*;
import vn.techmaster.danglh.recruitmentproject.repository.*;
import vn.techmaster.danglh.recruitmentproject.repository.custom.JobCustomRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;
import vn.techmaster.danglh.recruitmentproject.security.SecurityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor

public class JobService {

    ObjectMapper objectMapper;
    JobRepository jobRepository;
    CompanyRepository companyRepository;
    JobCustomRepository jobCustomRepository;
    JobCategoryRepository jobCategoryRepository;
    CandidateRepository candidateRepository;
    LocationRepository locationRepository;
    FavoriteJobRepository favoriteJobRepository;
    ApplicationRepository applicationRepository;


    public JobResponse postJob(JobRequest request) throws ObjectNotFoundException {
        Job job = objectMapper.convertValue(request, Job.class);
        job.setStatus(JobStatus.DRAFT);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Company company = companyRepository.findByAccount(userDetails.getAccount())
                .orElseThrow(() -> new ObjectNotFoundException("Company not found"));
        job.setCompany(company);

        JobCategory category = jobCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        job.setCategory(category);

        jobRepository.save(job);
        return objectMapper.convertValue(job, JobResponse.class);
    }

    public void deleteJob(Long id) throws UnprocessableEntityException {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ObjenesisException("Không tìm thấy job có id : " + id));
        if (job.getStatus() != JobStatus.DRAFT) {
            throw new UnprocessableEntityException("Không được xóa khi status khác DRAFT");
        }
        jobRepository.deleteById(id);
    }

    public JobResponse getJobDetails(Long idJob) throws ObjectNotFoundException {
        Job job = jobRepository.findById(idJob)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy job có id : " + idJob));

        Company company = companyRepository.findById(job.getCompany().getId())
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy company có id : " + job.getCompany().getId()));

        JobResponse response = objectMapper.convertValue(job, JobResponse.class);

        if (SecurityUtils.isAuthenticated()) {
//        if (authentication != null && authentication.isAuthenticated()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Candidate> candidateOptional = candidateRepository.findByAccount(userDetails.getAccount());
            if (candidateOptional.isPresent()) {
                Optional<FavouriteJob> favouriteJobOptional = favoriteJobRepository.findByCandidateAndJob(candidateOptional.get(), job);
                if (favouriteJobOptional.isPresent()) {
                    response.setFavorite(true);
                }

                Optional<Application> applicationOptional = applicationRepository.findFirstByCandidateAndJob(candidateOptional.get(), job);
                if (applicationOptional.isPresent()) {
                    Application application = applicationOptional.get();
                    ApplicationResponse applicationResponse = ApplicationResponse.builder()
                            .id(application.getId())
                            .candidate(objectMapper.convertValue(application.getCandidate(), CandidateResponse.class))
                            .cv(objectMapper.convertValue(application.getCv(), CvResponse.class))
                            .status(application.getStatus())
                            .build();
                    response.setApplication(applicationResponse);
                }
            }
        }

        CompanyResponse companyResponse = objectMapper.convertValue(company, CompanyResponse.class);
        companyResponse.setEmail(company.getAccount().getEmail());
        response.setCompany(companyResponse);

        return response;
    }

    public JobResponse updateJob(Long idJob, JobRequest request) throws ObjectNotFoundException {
        Job entity = jobRepository.findById(idJob)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy job có id : " + idJob));
        Job job = objectMapper.convertValue(request, Job.class);
        job.setId(idJob);
        job.setStatus(entity.getStatus());
        job.setCreatedAt(entity.getCreatedAt());
        job.setCreatedBy(entity.getCreatedBy());
        job.setCompany(entity.getCompany());

        JobCategory category = jobCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        job.setCategory(category);

        Location location = locationRepository.findById(request.getWorkingCityId())
                .orElseThrow(() -> new ObjectNotFoundException("Working city not found"));
        job.setWorkingCity(location);

        jobRepository.save(job);
        return objectMapper.convertValue(job, JobResponse.class);
    }

    public CommonSearchResponse<?> searchJob(JobSearchRequest request) {
        Role role = null;
        Long creatorId = null;
        Long candidateId = null;
        try {
            CustomUserDetails authentication = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            role = authentication.getAccount().getRole();
            if (role == Role.COMPANY) {
                Optional<Company> companyOptional = companyRepository.findByAccount(authentication.getAccount());
                if (companyOptional.isEmpty()) {
                    return CommonSearchResponse.<JobSearchResponse>builder()
                            .totalRecord(0L)
                            .totalPage(1)
                            .data(Collections.emptyList())
                            .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                            .build();
                }
                creatorId = companyOptional.get().getId();
            }
            if (role == Role.CANDIDATE) {
                Optional<Candidate> candidateOptional = candidateRepository.findByAccount(authentication.getAccount());
                if (candidateOptional.isEmpty()) {
                    return CommonSearchResponse.<JobSearchResponse>builder()
                            .totalRecord(0L)
                            .totalPage(1)
                            .data(Collections.emptyList())
                            .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                            .build();
                }
                candidateId = candidateOptional.get().getId();
            }
        } catch (Exception ignored) {
            if (role != null) {
                return CommonSearchResponse.<JobSearchResponse>builder()
                        .totalRecord(0L)
                        .totalPage(1)
                        .data(Collections.emptyList())
                        .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                        .build();
            }
        }
        List<SearchJobDto> result = jobCustomRepository.searchJob(request, creatorId, candidateId, role);

        Long totalRecord = 0L;
        List<JobSearchResponse> jobResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            jobResponses = result
                    .stream()
                    .map(s -> {
                        JobSearchResponse response = objectMapper.convertValue(s, JobSearchResponse.class);

                        CompanyResponse companyResponse = new CompanyResponse();
                        companyResponse.setName(s.getCompanyName());
                        companyResponse.setAlias(s.getAlias());
                        companyResponse.setEmail(s.getCompanyEmail());
                        companyResponse.setHeadQuarterAddress(s.getHeadQuarterAddress());
                        companyResponse.setWebsite(s.getWebsite());
                        companyResponse.setCreatedAt(s.getCompanyCreatedAt());

                        response.setCompany(companyResponse);
                        return response;
                    })
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<JobSearchResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(jobResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }

    public void changeJobStatus(Long jobId, JobStatusChangeRequest request) throws ObjectNotFoundException {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ObjectNotFoundException("Job not found with id: " + jobId));

        // Logic chuyển trạng thái
        switch (job.getStatus()) {
            case DRAFT:
                if (!JobStatus.PUBLISH.equals(request.getStatus())) {
                    throw new IllegalArgumentException("DRAFT can only transition to PUBLISH.");
                }
                break;
            case PUBLISH:
                if (!JobStatus.UNPUBLISHED.equals(request.getStatus()) && !JobStatus.EXPIRED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("PUBLISH can only transition to UNPUBLISHED or EXPIRED.");
                }
                break;
            case EXPIRED:
                if (!JobStatus.PUBLISH.equals(request.getStatus())) {
                    throw new IllegalArgumentException("EXPIRED can only transition to PUBLISH.");
                }
                break;
            case UNPUBLISHED:
                if (!JobStatus.PUBLISH.equals(request.getStatus())) {
                    throw new IllegalArgumentException("UNPUBLISHED can only transition to PUBLISH.");
                }
                break;
        }

        job.setStatus(request.getStatus());
        jobRepository.save(job);
    }

    public CommonSearchResponse<?> searchApplicationJob(JobSearchRequest request) {
        Role role = null;
        Long creatorId = null;
        Long candidateId = null;
        try {
            CustomUserDetails authentication = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            role = authentication.getAccount().getRole();
            if (role == Role.COMPANY) {
                Optional<Company> companyOptional = companyRepository.findByAccount(authentication.getAccount());
                if (companyOptional.isEmpty()) {
                    return CommonSearchResponse.<JobSearchResponse>builder()
                            .totalRecord(0L)
                            .totalPage(1)
                            .data(Collections.emptyList())
                            .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                            .build();
                }
                creatorId = companyOptional.get().getId();
            }
            if (role == Role.CANDIDATE) {
                Optional<Candidate> candidateOptional = candidateRepository.findByAccount(authentication.getAccount());
                if (candidateOptional.isEmpty()) {
                    return CommonSearchResponse.<JobSearchResponse>builder()
                            .totalRecord(0L)
                            .totalPage(1)
                            .data(Collections.emptyList())
                            .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                            .build();
                }
                candidateId = candidateOptional.get().getId();
            }
        } catch (Exception ignored) {
            return null;
        }
        List<SearchJobDto> result = jobCustomRepository.searchApplicationJob(request, creatorId, candidateId, role);

        Long totalRecord = 0L;
        List<JobSearchResponse> jobResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            jobResponses = result
                    .stream()
                    .map(s -> {
                        JobSearchResponse response = objectMapper.convertValue(s, JobSearchResponse.class);

                        CompanyResponse companyResponse = new CompanyResponse();
                        companyResponse.setName(s.getCompanyName());
                        companyResponse.setAlias(s.getAlias());
                        companyResponse.setEmail(s.getCompanyEmail());
                        companyResponse.setHeadQuarterAddress(s.getHeadQuarterAddress());
                        companyResponse.setWebsite(s.getWebsite());
                        companyResponse.setCreatedAt(s.getCompanyCreatedAt());

                        response.setCompany(companyResponse);
                        return response;
                    })
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<JobSearchResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(jobResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }
}
