package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.constant.ApplicationStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewType;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.dto.SearchInterviewDto;
import vn.techmaster.danglh.recruitmentproject.entity.Application;
import vn.techmaster.danglh.recruitmentproject.entity.Company;
import vn.techmaster.danglh.recruitmentproject.entity.Interview;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.InterviewRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.SearchInterviewRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.*;
import vn.techmaster.danglh.recruitmentproject.repository.ApplicationRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CompanyRepository;
import vn.techmaster.danglh.recruitmentproject.repository.InterviewRepository;
import vn.techmaster.danglh.recruitmentproject.repository.custom.InterviewCustomRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InterviewService {

    ApplicationRepository applicationRepository;
    InterviewRepository interviewRepository;
    ObjectMapper objectMapper;
    EmailService emailService;
    CompanyRepository companyRepository;
    InterviewCustomRepository interviewCustomRepository;

    @Transactional
    public InterviewResponse createInterview(InterviewRequest request) throws ObjectNotFoundException, MessagingException {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

        if (request.getInterviewType().equals(InterviewType.OFFLINE) && StringUtils.isBlank(request.getInterviewAddress())) {
            throw new IllegalArgumentException("Offline interview will require an address");
        }

        Interview interview = Interview.builder()
                .application(application)
                .interviewAt(request.getInterviewAt())
                .interviewType(request.getInterviewType())
                .interviewStep(1)
                .interviewAddress(request.getInterviewAddress())
                .status(InterviewStatus.CREATED)
                .invitationEmailSentAt(LocalDateTime.now())
                .build();
        interviewRepository.save(interview);

        application.setStatus(ApplicationStatus.WAIT_FOR_INTERVIEW);
        applicationRepository.save(application);

        emailService.sendNotifyToInterviewMail(application.getCandidate(), application.getJob().getCompany(), interview, application);

        return objectMapper.convertValue(interview, InterviewResponse.class);
    }

    public CommonSearchResponse<?> getInterviews(SearchInterviewRequest request) {
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
        List<SearchInterviewDto> result = interviewCustomRepository.searchInterviewForCompany(request, companyId);

        Long totalRecord = 0L;
        List<InterviewSearchResponse> interviewResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            interviewResponses = result
                    .stream()
                    .map(s ->
                            InterviewSearchResponse.builder()
                                    .id(s.getId())
                                    .candidate(CandidateResponse.builder().name(s.getCandidateName()).id(s.getCandidateId()).build())
                                    .job(JobResponse.builder().name(s.getJobTitle()).id(s.getJobId()).build())
                                    .interviewEmailSentAt(s.getInterviewEmailSentAt())
                                    .interviewAt(s.getInterviewAt())
                                    .type(s.getType())
                                    .status(s.getStatus())
                                    .build()
                    )
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<InterviewSearchResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(interviewResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }

    public InterviewResponse interviewDetails(Long interviewId) throws ObjectNotFoundException {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay interview co id la : " + interviewId));
        return objectMapper.convertValue(interview, InterviewResponse.class);
    }

    public InterviewResponse changeStatus(Long interviewId, InterviewRequest request) throws ObjectNotFoundException {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay interview co id : " + interviewId));

        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status could not be null");
        }

        // Logic chuyển trạng thái
        switch (interview.getStatus()) {
            case PASSED:
                if (!InterviewStatus.FAILED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case FAILED:
                if (!InterviewStatus.PASSED.equals(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status");
                }
                break;
            case CANDIDATE_ABSENCE:
                throw new IllegalArgumentException("Cannot change status of CANDIDATE_ABSENCE");
            case CANCELLED:
                throw new IllegalArgumentException("Cannot change status of CANCELLED");
        }
        interview.setStatus(request.getStatus());
        interviewRepository.save(interview);
        return objectMapper.convertValue(interview, InterviewResponse.class);
    }

    public InterviewResponse changeNote(Long interviewId, InterviewRequest request) throws ObjectNotFoundException {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay interview co id la : " + interviewId));
        interview.setNote(request.getNote());
        interviewRepository.save(interview);
        return objectMapper.convertValue(interview, InterviewResponse.class);
    }
}
