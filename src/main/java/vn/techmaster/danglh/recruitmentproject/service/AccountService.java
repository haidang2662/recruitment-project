package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.techmaster.danglh.recruitmentproject.constant.AccountStatus;
import vn.techmaster.danglh.recruitmentproject.constant.Constant;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.dto.SearchUserDto;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.Company;
import vn.techmaster.danglh.recruitmentproject.exception.*;
import vn.techmaster.danglh.recruitmentproject.model.CandidateModel;
import vn.techmaster.danglh.recruitmentproject.model.CompanyModel;
import vn.techmaster.danglh.recruitmentproject.model.request.*;
import vn.techmaster.danglh.recruitmentproject.model.response.AccountResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.AccountSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.repository.AccountRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CandidateRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CompanyRepository;
import vn.techmaster.danglh.recruitmentproject.repository.RefreshTokenRepository;
import vn.techmaster.danglh.recruitmentproject.repository.custom.AccountCustomRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountService {

    final AccountRepository accountRepository;
    final PasswordEncoder passwordEncoder;
    final EmailService emailService;
    final ObjectMapper objectMapper;
    final AccountCustomRepository accountCustomRepository;
    final CandidateRepository candidateRepository;
    final CompanyRepository companyRepository;
    final RefreshTokenRepository refreshTokenRepository;
    final FileService fileService;

    @Value("${application.account.activation.expiredDurationInMilliseconds}")
    long activationMailExpiredDurationInMilliseconds;

    @Value("${application.account.passwordForgotten.expiredDurationInMilliseconds}")
    long passwordForgottenExpiredDurationInMilliseconds;

    @Value("${application.account.activation.maxResendTimes}")
    int activationMailMaxSentCount;

    static final String AVATAR_PATH = System.getProperty("user.dir") + File.separator + Constant.FOLDER_NAME.FILE_FOLDER_NAME + File.separator + Constant.FOLDER_NAME.AVATAR_FOLDER_NAME;
    static final String COVER_PATH = System.getProperty("user.dir") + File.separator + Constant.FOLDER_NAME.FILE_FOLDER_NAME + File.separator + Constant.FOLDER_NAME.COVER_FOLDER_NAME;

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long id, PasswordChangingRequest request) throws ObjectNotFoundException, PasswordNotMatchedException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found"));

        if (!request.getPassword().equals(request.getConfirmedPassword())) {
            throw new PasswordNotMatchedException("Password not matched");
        }

        account.setPassword(passwordEncoder.encode(request.getPassword()));
        accountRepository.save(account);

        refreshTokenRepository.logOut(account.getId());
        SecurityContextHolder.clearContext();
    }

    public void activateAccount(Long userId) throws ObjectNotFoundException, ExpiredEmailActivationUrlException {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found"));

        // check xem link active het han chua
        LocalDateTime activationMailSentAt = account.getActivationMailSentAt();
        if (activationMailSentAt.plusSeconds(activationMailExpiredDurationInMilliseconds / 1000).isBefore(LocalDateTime.now())) {
            throw new ExpiredEmailActivationUrlException("Activation link expired");
        }
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    public void sendActivationEmail(Long id) throws MessagingException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        if (account.getActivationMailSentCount() >= activationMailMaxSentCount) {
            throw new MessagingException("Activation email has been sent over " + activationMailMaxSentCount + " times");
        }
        try {
            emailService.sendActivationMail(account);
        } catch (jakarta.mail.MessagingException e) {
            throw new MessagingException(e.getMessage());
        }
        account.setActivationMailSentCount(account.getActivationMailSentCount() + 1);
        account.setActivationMailSentAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    public void sendForgotPasswordEmail(@Valid ForgotPasswordEmailRequest request) throws MessagingException {
        // TODO - cần check so lần gửi tối đa trong 1 khoag thơi gian (vi du chi duoc gui toi da 3 mail trong vong 1h)

        // cần check xem email có tồn tại trong hệ thống không
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        // gửi mail
        try {
            emailService.sendForgotPasswordMail(account);
        } catch (jakarta.mail.MessagingException e) {
            throw new MessagingException(e.getMessage());
        }
        account.setForgotPasswordMailSentAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    public void changeForgotPassword(Long userId, PasswordChangingRequest request)
            throws ObjectNotFoundException, ExpiredPasswordForgottenUrlException, PasswordNotMatchedException {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));

        // check xem link active het han chua
        LocalDateTime forgotPasswordMailSentAt = account.getForgotPasswordMailSentAt();
        if (forgotPasswordMailSentAt.plusSeconds(passwordForgottenExpiredDurationInMilliseconds / 1000).isBefore(LocalDateTime.now())) {
            throw new ExpiredPasswordForgottenUrlException("Forgot password link expired");
        }

        if (!request.getPassword().equals(request.getConfirmedPassword())) {
            throw new PasswordNotMatchedException("Password not matched");
        }

        account.setPassword(passwordEncoder.encode(request.getPassword()));
        accountRepository.save(account);
    }

    public AccountResponse getDetail(Long id) throws ObjectNotFoundException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found"));
        AccountResponse accountResponse = objectMapper.convertValue(account, AccountResponse.class);
        if (account.getRole() == Role.CANDIDATE) {
            Candidate candidate = candidateRepository.findByAccount(account)
                    .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));
            CandidateModel candidateModel = objectMapper.convertValue(candidate, CandidateModel.class);
            accountResponse.setCandidateModel(candidateModel);
            accountResponse.setName(candidateModel.getName());
        } else if (account.getRole() == Role.COMPANY) {
            Company company = companyRepository.findByAccount(account)
                    .orElseThrow(() -> new ObjectNotFoundException("Company not found"));
            CompanyModel companyModel = objectMapper.convertValue(company, CompanyModel.class);
            accountResponse.setCompanyModel(companyModel);
            accountResponse.setName(companyModel.getName());
        }
        return accountResponse;
    }

    public AccountResponse createUser(CreateAccountRequest request) throws ExistedAccountException {
        Optional<Account> userOptional = accountRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            throw new ExistedAccountException("Username existed");
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode("123")) // TODO: change to random password
                .role(Role.CANDIDATE) // TODO : Đoạn này chưa hiểu lắm làm thế nào để chọn candidate hay company
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(account);
        return objectMapper.convertValue(account, AccountResponse.class);
    }

    public CommonSearchResponse<AccountSearchResponse> searchAccount(AccountSearchRequest request) {
        List<SearchUserDto> result = accountCustomRepository.searchAccount(request);

        Long totalRecord = 0L;
        List<AccountSearchResponse> studentResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            studentResponses = result
                    .stream()
                    .map(s -> objectMapper.convertValue(s, AccountSearchResponse.class))
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<AccountSearchResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(studentResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }

    public AccountResponse updateAccount(Long id, MultipartFile avatar, MultipartFile cover, UpdateAccountRequest request)
            throws ObjectNotFoundException, IOException, InvalidFileExtensionException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Account is not found"));

        AccountResponse response = objectMapper.convertValue(account, AccountResponse.class);
        if (account.getRole() == Role.CANDIDATE) {
            CandidateModel model = updateCandidate(account, avatar, request);
            response.setCandidateModel(model);
            response.setName(model.getName());
        } else if (account.getRole() == Role.COMPANY) {
            CompanyModel model = updateCompany(account, avatar, cover, request);

            response.setCompanyModel(model);
            response.setName(model.getName());
        }

        return response;
    }

    private CompanyModel updateCompany(Account account, MultipartFile avatar, MultipartFile cover, UpdateAccountRequest request)
            throws ObjectNotFoundException, IOException, InvalidFileExtensionException {
        if (!fileService.validateMultipartFile(avatar, Constant.ALLOWED_FILE_EXTENSION.IMAGE_FILE_EXTENSIONS)) {
            throw new InvalidFileExtensionException("Avatar extension not allowed");
        }
        if (!fileService.validateMultipartFile(cover, Constant.ALLOWED_FILE_EXTENSION.IMAGE_FILE_EXTENSIONS)) {
            throw new InvalidFileExtensionException("Cover extension not allowed");
        }
        Company company = companyRepository.findByAccount(account)
                .orElseThrow(() -> new ObjectNotFoundException("Company not found"));

        company.setName(request.getName());
        company.setAlias(request.getAlias());
        company.setPhone(request.getPhone());
        company.setFoundAt(request.getFoundAt());
        company.setTaxCode(request.getTaxCode());
        company.setHeadQuarterAddress(request.getHeadQuarterAddress());
        company.setEmployeeQuantity(request.getEmployeeQuantity());
        company.setWebsite(request.getWebsite());
        company.setDescription(request.getDescription());
        company.setRating(request.getRating());

        if (avatar != null && !avatar.isEmpty()) {
            String avatarFileName = fileService.saveFile(avatar, AVATAR_PATH);
            company.setAvatarUrl(avatarFileName);
        }

        if (cover != null && !cover.isEmpty()) {
            String coverFileName = fileService.saveFile(cover, COVER_PATH);
            company.setCoverImageUrl(coverFileName);
        }

        companyRepository.save(company);

        return objectMapper.convertValue(company, CompanyModel.class);
    }

    private CandidateModel updateCandidate(Account account, MultipartFile avatar, UpdateAccountRequest request)
            throws ObjectNotFoundException, IOException, InvalidFileExtensionException {
        if (!fileService.validateMultipartFile(avatar, Constant.ALLOWED_FILE_EXTENSION.IMAGE_FILE_EXTENSIONS)) {
            throw new InvalidFileExtensionException("Avatar extension not allowed");
        }

        Candidate candidate = candidateRepository.findByAccount(account)
                .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));

        candidate.setName(request.getName());
        candidate.setDob(request.getDob());
        candidate.setGender(request.getGender());
        candidate.setCurrentJobPosition(request.getCurrentJobPosition());
        candidate.setPhone(request.getPhone());
        candidate.setAddress(request.getAddress());
        candidate.setSkills(request.getSkills());
        candidate.setYearOfExperience(request.getYearOfExperience());
        candidate.setLiteracy(request.getLiteracy());
        candidate.setGraduatedAt(request.getGraduatedAt());
        candidate.setExpectedSalaryFrom(request.getExpectedSalaryFrom());
        candidate.setExpectedSalaryTo(request.getExpectedSalaryTo());
        candidate.setExpectedWorkingTimeType(request.getExpectedWorkingTimeType());
        candidate.setExpectedWorkingType(request.getExpectedWorkingType());

        if (avatar != null && !avatar.isEmpty()) {
            String fileName = fileService.saveFile(avatar, AVATAR_PATH);
            candidate.setAvatarUrl(fileName);
        }

        candidateRepository.save(candidate);

        return objectMapper.convertValue(candidate, CandidateModel.class);
    }

}
