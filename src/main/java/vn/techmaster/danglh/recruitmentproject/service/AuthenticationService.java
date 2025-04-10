package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.techmaster.danglh.recruitmentproject.constant.*;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.Company;
import vn.techmaster.danglh.recruitmentproject.entity.RefreshToken;
import vn.techmaster.danglh.recruitmentproject.exception.CustomAuthenticationException;
import vn.techmaster.danglh.recruitmentproject.exception.ExistedAccountException;
import vn.techmaster.danglh.recruitmentproject.exception.InvalidRefreshTokenException;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.LoginRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.OAuth2LoginRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.RefreshTokenRequest;
import vn.techmaster.danglh.recruitmentproject.model.request.RegistrationRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.AccountResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.JwtResponse;
import vn.techmaster.danglh.recruitmentproject.repository.AccountRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CandidateRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CompanyRepository;
import vn.techmaster.danglh.recruitmentproject.repository.RefreshTokenRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;
import vn.techmaster.danglh.recruitmentproject.security.JwtService;
import vn.techmaster.danglh.recruitmentproject.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService {

    final JwtService jwtService;

    final AccountRepository accountRepository;

    final RefreshTokenRepository refreshTokenRepository;

    final AuthenticationManager authenticationManager;

    final CandidateRepository candidateRepository;

    final CompanyRepository companyRepository;


    final PasswordEncoder passwordEncoder;

    final ObjectMapper objectMapper;

    final EmailService emailService;

    @Value("${application.security.refreshToken.tokenValidityMilliseconds}")
    long refreshTokenValidityMilliseconds;

    @Value("${application.security.oauth2.google.client-id}")
    String googleClientId;

    @Transactional(rollbackFor = Exception.class)
    public AccountResponse registerAccount(RegistrationRequest registrationRequest)
            throws ExistedAccountException, MessagingException {
        // Kiểm tra nếu email đã tồn tại
        Optional<Account> userOptional = accountRepository.findByEmail(registrationRequest.getEmail());
        if (userOptional.isPresent()) {
            throw new ExistedAccountException("Username existed");
        }

        // Tạo tài khoản
        Account account = Account.builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .role(registrationRequest.getType() == RegistrationType.CANDIDATE ? Role.CANDIDATE : Role.COMPANY)
                .status(AccountStatus.CREATED)
                .createdBy(Constant.DEFAULT_CREATOR)
                .lastModifiedBy(Constant.DEFAULT_CREATOR)
                .activationMailSentAt(LocalDateTime.now())
                .activationMailSentCount(1)
                .build();
        accountRepository.save(account);

        if (registrationRequest.getType() == RegistrationType.CANDIDATE) {
            Candidate candidate = Candidate.builder()
                    .name(registrationRequest.getName())
                    .build();
            candidate.setAccount(account);
            candidateRepository.save(candidate);
        } else {
            Company company = Company.builder()
                    .name(registrationRequest.getName())
                    .headQuarterAddress(registrationRequest.getHeadQuarterAddress())
                    .employeeQuantity(registrationRequest.getEmployeeQuantity())
                    .website(registrationRequest.getWebsite())
                    .build();
            company.setAccount(account);
            companyRepository.save(company);
        }

        // Gửi email kích hoạt
        emailService.sendActivationMail(account);

        // Trả về AccountResponse
        return objectMapper.convertValue(account, AccountResponse.class);
    }


    public JwtResponse authenticate(LoginRequest request) throws ObjectNotFoundException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())); // đã login thành công

        SecurityContextHolder.getContext().setAuthentication(authentication); // lưu trữ thông tin thành công vào SecurityContextHolder
        String jwt = jwtService.generateJwtToken(authentication); // sinh ra jwt

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());  // Lấy ra quyền của thằng vừa login

        Account account = accountRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ObjectNotFoundException("Account not found"));

//        String refreshToken = UUID.randomUUID().toString();
        String refreshToken = jwtService.generateJwtRefreshToken(authentication);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .refreshToken(refreshToken)
                .account(account)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return JwtResponse.builder()
                .jwt(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public JwtResponse refreshToken(RefreshTokenRequest request) throws InvalidRefreshTokenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        JwtResponse response = accountRepository.findById(userDetails.getId())
                .flatMap(user -> refreshTokenRepository
                        .findByAccountAndRefreshTokenAndInvalidated(user, request.getRefreshToken(), false)
                        .map(oldRefreshToken -> {
                            LocalDateTime createdDateTime = oldRefreshToken.getCreatedAt();
                            LocalDateTime expiryTime = createdDateTime.plusSeconds(refreshTokenValidityMilliseconds / 1000);
                            if (expiryTime.isBefore(LocalDateTime.now())) {
                                oldRefreshToken.setInvalidated(true);
                                refreshTokenRepository.save(oldRefreshToken);
                                return null;
                            }
                            String jwtToken = jwtService.generateJwtToken(authentication);
                            String refreshToken = jwtService.generateJwtRefreshToken(authentication);
                            RefreshToken refreshTokenEntity = RefreshToken.builder()
                                    .refreshToken(refreshToken)
                                    .account(user)
                                    .build();
                            refreshTokenRepository.save(refreshTokenEntity);
                            oldRefreshToken.setInvalidated(true);
                            refreshTokenRepository.save(oldRefreshToken);
                            return JwtResponse.builder()
                                    .jwt(jwtToken)
                                    .refreshToken(refreshToken)
                                    .id(userDetails.getId())
                                    .username(userDetails.getUsername())
                                    .roles(userDetails.getAuthorities().stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .collect(Collectors.toSet()))
                                    .build();
                        }))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (response == null) {
            throw new InvalidRefreshTokenException("Refresh token invalid or expired");
        }
        return response;
    }

    @Transactional
    public void logout() {
        Long userId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        refreshTokenRepository.logOut(userId);
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public JwtResponse authenticateByOAuth2(OAuth2LoginRequest request) throws CustomAuthenticationException {
        Oauth2Tenant tenant = request.getTenant();
        if (tenant.equals(Oauth2Tenant.GOOGLE)) {
            return authenticateByGoogle(request);
        }
        return null;
    }

    private JwtResponse authenticateByGoogle(OAuth2LoginRequest request) throws CustomAuthenticationException {
        GoogleIdToken.Payload payload = verifyToken(request.getCredential());
        if (payload == null) {
            throw new CustomAuthenticationException("Invalid credential");
        }

        // kiem tra xem email vua login da ton tai trong Db chua
        // => nêu rồi thì tạo jwt, xong
        // => nếu chưa => tạo account mới cho nó rồi tạo jwt
        String email = payload.getEmail();
        if (StringUtils.isBlank(email)) {
            throw new CustomAuthenticationException("Empty email");
        }

        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            return buildJwt(account, null);
        }

        // Tạo tài khoản
        Account account = Account.builder()
                .email(email)
                .password(null)
                .role(Role.CANDIDATE)
                .status(AccountStatus.CREATED)
                .createdBy(Constant.DEFAULT_CREATOR)
                .lastModifiedBy(Constant.DEFAULT_CREATOR)
                .build();
        accountRepository.save(account);
        Candidate candidate = Candidate.builder().name(email).account(account).build();
        candidateRepository.save(candidate);

        return buildJwt(account, null);
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? idToken.getPayload() : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private JwtResponse buildJwt(Account account, Object credentials) {
        CustomUserDetails userDetails = new CustomUserDetails(account);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, credentials);
        SecurityContextHolder.getContext().setAuthentication(authentication); // lưu trữ thông tin thành công vào SecurityContextHolder
        String jwt = jwtService.generateJwtToken(authentication); // sinh ra jwt

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());  // Lấy ra quyền của thằng vừa login

        String refreshToken = jwtService.generateJwtRefreshToken(authentication);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .refreshToken(refreshToken)
                .account(account)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return JwtResponse.builder()
                .jwt(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}
