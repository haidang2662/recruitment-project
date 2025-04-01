package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;
import vn.techmaster.danglh.recruitmentproject.constant.Constant;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.dto.SearchApplicationDto;
import vn.techmaster.danglh.recruitmentproject.entity.*;
import vn.techmaster.danglh.recruitmentproject.exception.ExistedJobApplicationException;
import vn.techmaster.danglh.recruitmentproject.exception.InvalidFileExtensionException;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.ApplicationRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.ApplicationSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.JobApplicationRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.*;
import vn.techmaster.danglh.recruitmentproject.repository.*;
import vn.techmaster.danglh.recruitmentproject.repository.custom.ApplicationCustomRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;
import vn.techmaster.danglh.recruitmentproject.security.SecurityUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationService {

    FileService fileService;
    JobRepository jobRepository;
    CandidateCvRepository cvRepository;
    CandidateRepository candidateRepository;
    ApplicationRepository applicationRepository;
    AccountRepository accountRepository;
    ObjectMapper objectMapper;
    CandidateCvRepository candidateCvRepository;
    CompanyRepository companyRepository;
    ApplicationCustomRepository applicationCustomRepository;
    InterviewRepository interviewRepository;

    @Transactional
    public ApplicationResponse applyJob(JobApplicationRequest request, MultipartFile uploadedCv)
            throws InvalidFileExtensionException, ObjectNotFoundException, IOException, ExistedJobApplicationException {
        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ObjectNotFoundException("Account is not found"));
        Candidate candidate = candidateRepository.findByAccount(account)
                .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ObjectNotFoundException("Job not found"));

        Optional<Application> applicationOptional = applicationRepository.findFirstByCandidateAndJob(candidate, job);
        if (applicationOptional.isPresent()) {
            throw new ExistedJobApplicationException("Candidate had applied for this job already");
        }

        CandidateCv cv;
        if (request.getCvId() != null) {
            cv = cvRepository.findById(request.getCvId())
                    .orElseThrow(() -> new ObjectNotFoundException("Cv not found"));
        } else {
            if (!fileService.validateMultipartFile(uploadedCv, Constant.ALLOWED_FILE_EXTENSION.CV_FILE_EXTENSIONS)) {
                throw new InvalidFileExtensionException("CV file extension not allowed");
            }

            String fileName = fileService.saveFile(uploadedCv, CvService.CV_PATH);
            cv = new CandidateCv();
            cv.setCandidate(candidate);
            cv.setCvUrl(fileName);
            cv.setMain(false);
            candidateCvRepository.save(cv);
        }

        if (cv == null) {
            throw new FileNotFoundException("CV file invalid");
        }


        String applicationDescription = StringUtils.isBlank(request.getApplicationDescription()) ? null : request.getApplicationDescription();
        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .cv(cv)
                .applicationDescription(applicationDescription)
                .status(ApplicationStatus.APPLIED)
                .build();
        applicationRepository.save(application);

        return ApplicationResponse.builder()
                .job(objectMapper.convertValue(job, JobResponse.class))
                .cv(objectMapper.convertValue(cv, CvResponse.class))
                .candidate(objectMapper.convertValue(candidate, CandidateResponse.class))
                .status(ApplicationStatus.APPLIED)
                .applicationDescription(applicationDescription)
                .build();
    }

    public CommonSearchResponse<?> searchApplications(ApplicationSearchRequest request) {
        Long companyId = null;
        try {
            CustomUserDetails authentication = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Role role = authentication.getAccount().getRole();
            if (role == Role.COMPANY) {
                Optional<Company> companyOptional = companyRepository.findByAccount(authentication.getAccount());
                if (companyOptional.isEmpty()) {
                    return CommonSearchResponse.<ApplicationResponse>builder()
                            .totalRecord(0L)
                            .totalPage(1)
                            .data(Collections.emptyList())
                            .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                            .build();
                }
                companyId = companyOptional.get().getId();
            }
        } catch (Exception ignored) {
            return CommonSearchResponse.<ApplicationResponse>builder()
                    .totalRecord(0L)
                    .totalPage(1)
                    .data(Collections.emptyList())
                    .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                    .build();
        }
        List<SearchApplicationDto> result = applicationCustomRepository.searchApplicationForCompany(request, companyId);

        Long totalRecord = 0L;
        List<ApplicationResponse> applicationResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            applicationResponses = result
                    .stream()
                    .map(s ->
                            ApplicationResponse.builder()
                                    .id(s.getId())
                                    .status(s.getStatus())
                                    .job(JobResponse.builder().id(s.getJobId()).name(s.getJobName()).build())
                                    .candidate(CandidateResponse.builder().name(s.getCandidateName()).id(s.getCandidateId()).build())
                                    .cv(CvResponse.builder().name(s.getCvName()).url(s.getCvUrl()).id(s.getCvId()).build())
                                    .appliedDate(s.getAppliedDate())
                                    .build()
                    )
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<ApplicationResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(applicationResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }

    public ApplicationResponse applicationDetails(Long applicationId) throws ObjectNotFoundException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy application có id : " + applicationId));

        // candidate
        Candidate candidate = application.getCandidate();

        // tên , mức lương mong muôn, expectedWorkingTimeType, expectedWorkingType , position
        Job job = application.getJob();

        // interview
        Interview interview = interviewRepository.findByApplication(application);
        // cv
        CandidateCv candidateCv = application.getCv();
        // lấy id phục vụ download
        ApplicationResponse response = ApplicationResponse.builder()
                .id(applicationId)
                // tên, sdt, ngày sinh, giới tính, năm kinh nghiệm, trình độ học vấn (literacy), graduatedAt,
                .candidate(CandidateResponse.builder()
                        .id(candidate.getId())
                        .name(candidate.getName())
                        .phone(candidate.getPhone())
                        .dob(candidate.getDob())
                        .gender(candidate.getGender())
                        .yearOfExperience(candidate.getYearOfExperience())
                        .literacy(candidate.getLiteracy())
                        .graduatedAt(candidate.getGraduatedAt())
                        .build())
                .job(JobResponse.builder()
                        // tên , mức lương mong muôn, expectedWorkingTimeType, expectedWorkingType , position
                        .id(job.getId())
                        .name(job.getName())
                        .salaryFrom(job.getSalaryFrom())
                        .salaryTo(job.getSalaryTo())
                        .workingType(job.getWorkingType())
                        .workingTimeType(job.getWorkingTimeType())
                        .position(job.getPosition())
                        .build())
                .cv(CvResponse.builder()
                        // lấy ra interview thông qua application (invitationEmailSentAt, interviewAt, status, interviewType, interviewAddress, note)
                        .id(candidateCv.getId())
                        .build())
                .status(application.getStatus())
                .appliedDate(application.getCreatedAt())
                .applicationDescription(application.getApplicationDescription())
                .build();

        if (interview != null) {
            InterviewResponse interviewResponse = InterviewResponse.builder()
                    .id(interview.getId())
                    .invitationEmailSentAt(interview.getInvitationEmailSentAt())
                    .interviewAt(interview.getInterviewAt())
                    .status(interview.getStatus())
                    .interviewType(interview.getInterviewType())
                    .interviewAddress(interview.getInterviewAddress())
                    .note(interview.getNote())
                    .build();
            response.setInterview(interviewResponse);
        }

        return response;
    }

    public ApplicationResponse changeStatus(Long applicationId, ApplicationRequest request) throws ObjectNotFoundException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy application có id : " + applicationId));

        // Logic chuyển trạng thái
        switch (application.getStatus()) {
            case APPLIED:
                if (!ApplicationStatus.APPLICATION_ACCEPTED.equals(request.getStatus())
                        && !ApplicationStatus.APPLICATION_REJECTED.equals(request.getStatus())
                        && !ApplicationStatus.CANCELLED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case CANCELLED:
                throw new IllegalArgumentException("Cannot change status of CANCELLED");
            case APPLICATION_ACCEPTED:
                if (!ApplicationStatus.APPLICATION_REJECTED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case APPLICATION_REJECTED:
                if (!ApplicationStatus.APPLICATION_ACCEPTED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case WAIT_FOR_INTERVIEW:
                if (!ApplicationStatus.CANDIDATE_ACCEPTED.equals(request.getStatus())
                        && !ApplicationStatus.CANDIDATE_REJECTED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case CANDIDATE_ACCEPTED:
                if (!ApplicationStatus.CANDIDATE_REJECTED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case CANDIDATE_REJECTED:
                if (!ApplicationStatus.CANDIDATE_ACCEPTED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
        }

        application.setStatus(request.getStatus());
        applicationRepository.save(application);
        return objectMapper.convertValue(application, ApplicationResponse.class);
    }
}
