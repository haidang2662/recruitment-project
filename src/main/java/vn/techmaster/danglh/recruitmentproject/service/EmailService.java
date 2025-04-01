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

    final JavaMailSender javaMailSender;

    final InterviewRepository interviewRepository;

    @Async
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

        String content = "<div>" +
                "<h2> Dear Mr./Ms. <b>" + candidate.getName() + "</b>, </h2>" +
                "<div><b>" + company.getName() + "</b> is very pleased and honored to receive your application for the position <b>" + application.getJob().getName() + "</b><br>. " +
                "We have received your CV and would like to have an interview to discuss directly about your knowledge and the job you have applied for.</div><br><br>" +
                "<div>The expected interview time is at " + interviewAtTime + " on " + interviewAtDate + " " +
                "(we will send you the link after you confirm your agreement to the interview by replying to this email).</div><br>" +
                "<div>We hope for your early response and hope that we will be able to cooperate together in the future.</div><br><br><br>" +
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
