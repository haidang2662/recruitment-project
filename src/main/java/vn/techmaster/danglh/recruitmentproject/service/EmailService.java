package vn.techmaster.danglh.recruitmentproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.entity.*;
import vn.techmaster.danglh.recruitmentproject.repository.InterviewRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailService {

    @Value("${spring.mail.username}")
    String fromEmail;

    @Value("${application.account.activation.activationUrl}")
    String activationLink;


    @Value("${application.account.passwordForgotten.url}")
    String passwordForgottenLink;

    @Value("${interview.acceptUrl}")
    String acceptLink;

    @Value("${interview.refuseUrl}")
    String refuseLink;

    final JavaMailSender javaMailSender;

    final InterviewRepository interviewRepository;

    @Async // nôm na là cho phép chạy bất đồng bộ dẫn đến nhiều trường hợp giúp tăng hiệu năng
    public void sendActivationMail(Account account) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(fromEmail);
        mimeMessageHelper.setTo(account.getEmail());

        String url = activationLink.replace("{id}", account.getId().toString());
        String content = "<div>" +
                "<h1>Welcome to our website</h1>" +
                "<div>Please click <a href='" + url + "'>this link</a> to activate your account</div>" +
                "</div>";

        mimeMessageHelper.setText(content, true);
        mimeMessageHelper.setSubject("Registration Confirmation");

        javaMailSender.send(mimeMessage);
    }

    @Async
    public void sendForgotPasswordMail(Account account) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(fromEmail);
        mimeMessageHelper.setTo(account.getEmail());

        String url = passwordForgottenLink.replace("{id}", account.getId().toString());
        String content = "<div>" +
                "<h1>Forgot password</h1>" +
                "<div>Please click <a href='" + url + "'>this link</a> to change your password</div>" +
                "</div>";

        mimeMessageHelper.setText(content, true);
        mimeMessageHelper.setSubject("Password Forgotten");

        javaMailSender.send(mimeMessage);
    }

    @Async
    public void sendNotifyToInterviewMail(Candidate candidate, Company company, Interview interview, Application application) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(fromEmail);
        mimeMessageHelper.setTo(candidate.getAccount().getEmail());

        LocalDateTime interviewAt = interview.getInterviewAt();
        String interviewAtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(interviewAt);
        String interviewAtTime = DateTimeFormatter.ofPattern("HH:mm").format(interviewAt);

//        String acceptLink = "http://localhost:8080/companies/interviews/acceptance/" + interview.getId();
//        String refuseLink = "http://localhost:8080/companies/interviews/refusal/" + interview.getId();

        String acceptUrl = acceptLink.replace("{id}" , interview.getId().toString());
        String refuseUrl = refuseLink.replace("{id}" , interview.getId().toString());

        String content = "<div>" +
                "<h2> Dear Mr./Ms. <b>" + candidate.getName() + "</b>, </h2>" +
                "<div><b>" + company.getName() + "</b> is very pleased and honored to receive your application for the position <b>" + application.getJob().getName() + "</b><br>. " +
                "We have received your CV and would like to have an interview to discuss directly about your knowledge and the job you have applied for.</div><br><br>" +
                "<div>The expected interview time is at " + interviewAtTime + " on " + interviewAtDate + " " +
                "<div>If you agree to participate in our interview, please <a href='" + acceptUrl + "'>click here</a>. If not, please <a href='" + refuseUrl + "'>click here</a>.</div><br><br><br>" +
                "<div>Thanks & best regards,</div><br><br>" +
                "<div>Head office: " + company.getHeadQuarterAddress() + "</div>" +
                "<div>Hotline: " + company.getPhone() + "</div>";

        mimeMessageHelper.setText(content, true);
        mimeMessageHelper.setSubject("Notify To Interview");
        javaMailSender.send(mimeMessage);

        interview.setInvitationEmailSentAt(LocalDateTime.now());
        interviewRepository.save(interview);
    }
}
