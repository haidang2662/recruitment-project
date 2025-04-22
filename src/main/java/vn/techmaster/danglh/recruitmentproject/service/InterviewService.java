package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.constant.*;
import vn.techmaster.danglh.recruitmentproject.dto.NotificationDto;
import vn.techmaster.danglh.recruitmentproject.dto.SearchInterviewDto;
import vn.techmaster.danglh.recruitmentproject.entity.*;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.InterviewRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.SearchInterviewRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.*;
import vn.techmaster.danglh.recruitmentproject.repository.*;
import vn.techmaster.danglh.recruitmentproject.repository.custom.InterviewCustomRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewService {

    @Value("${application.account.admin.username}")
    String adminUsername;

    final ApplicationRepository applicationRepository;
    final InterviewRepository interviewRepository;
    final ObjectMapper objectMapper;
    final EmailService emailService;
    final CompanyRepository companyRepository;
    final InterviewCustomRepository interviewCustomRepository;
    final NotificationService notificationService;
    final JobRepository jobRepository;
    final AccountRepository accountRepository;

    @Transactional
    public InterviewResponse createInterview(InterviewRequest request) throws ObjectNotFoundException, MessagingException, JsonProcessingException {
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

        // tạo entity notification và lưu db
        Job job = application.getJob();
        Company company = job.getCompany();
        Candidate candidate = application.getCandidate();

//        // bắn noti
        NotificationDto dto = NotificationDto.builder()
                .sender(company.getAccount())
                .title("Thư mời tham gia phỏng vấn")
                .content("Bạn có lời mời phỏng vấn từ công ty " + company.getName() + "cho vị trí " + job.getName() + ".")
                .target(candidate.getAccount())
                .targetType(TargetType.CANDIDATE)
                .destination(WebsocketDestination.NEW_INTERVIEW_NOTIFICATION)
                .build();

        notificationService.pushNotification(dto);

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

    @Transactional
    public InterviewResponse changeStatus(Long interviewId, InterviewRequest request) throws ObjectNotFoundException, JsonProcessingException {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay interview co id : " + interviewId));

        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status could not be null");
        }

        Application application = interview.getApplication();
        Job job = application.getJob();
        Candidate candidate = application.getCandidate();
        if (request.getStatus() == InterviewStatus.INTERVIEW_ACCEPTED) {
            // bắn noti
            NotificationDto dto = NotificationDto.builder()
                    .sender(candidate.getAccount())
                    .title("Ứng viên tham gia phỏng vấn")
                    .content("ứng viên " + candidate.getName() + " đã chấp nhận tham gia phỏng vấn cho vị trí " + job.getName() + " vào thời gian " + interview.getInterviewAt() + " . ")
                    .target(job.getCompany().getAccount())
                    .targetType(TargetType.COMPANY)
                    .destination(WebsocketDestination.INTERVIEW_ACCEPTANCE_NOTIFICATION)
                    .build();
            notificationService.pushNotification(dto);
        }

        if (request.getStatus() == InterviewStatus.INTERVIEW_REFUSED) {
            // bắn noti
            NotificationDto dto = NotificationDto.builder()
                    .sender(candidate.getAccount())
                    .title("Ứng viên từ chối tham gia phỏng vấn")
                    .content("Ứng viên " + candidate.getName() + " đã từ chối tham gia phỏng vấn cho vị trí " + job.getName() + " vào thời gian " + interview.getInterviewAt() + ".")
                    .target(job.getCompany().getAccount())
                    .targetType(TargetType.COMPANY)
                    .destination(WebsocketDestination.INTERVIEW_REFUSAL_NOTIFICATION)
                    .build();
            notificationService.pushNotification(dto);
        }

        if (request.getStatus() == InterviewStatus.PASSED) {
            job.setPassedQuantity(job.getPassedQuantity() + 1);
            jobRepository.save(job);

            // check xem tuyển đủ chưa
            if (job.getPassedQuantity() >= job.getRecruitingQuantity()) {
                sendCompletedRecruitmentJob(job);
            }
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

    private void sendCompletedRecruitmentJob(Job job) throws JsonProcessingException {
        Optional<Account> admin = accountRepository.findByEmail(adminUsername);

        if (admin.isEmpty()) {
            return;
        }

        NotificationDto dto = NotificationDto.builder()
                .sender(admin.get())
                .title("Tin tuyển dụng hoàn tất")
                .content("Tin tuyển dụng " + job.getName() + " cho vị trí " + job.getPosition() + " đã tuyển đủ người.")
                .target(job.getCompany().getAccount())
                .targetType(TargetType.COMPANY)
                .destination(WebsocketDestination.ENOUGH_PASSED_CANDIDATE_NOTIFICATION)
                .build();
        notificationService.pushNotification(dto);
    }

    public InterviewResponse changeNote(Long interviewId, InterviewRequest request) throws ObjectNotFoundException {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay interview co id la : " + interviewId));
        interview.setNote(request.getNote());
        interviewRepository.save(interview);
        return objectMapper.convertValue(interview, InterviewResponse.class);
    }
}
