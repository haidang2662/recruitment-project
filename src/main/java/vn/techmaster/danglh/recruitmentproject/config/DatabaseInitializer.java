package vn.techmaster.danglh.recruitmentproject.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.techmaster.danglh.recruitmentproject.constant.AccountStatus;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.repository.AccountRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatabaseInitializer implements CommandLineRunner {

    final AccountRepository accountRepository;

    final PasswordEncoder passwordEncoder;

    @Value("${application.account.admin.username}")
    String adminUsername;

    @Value("${application.account.admin.password}")
    String pass;

    @Override
    public void run(String... args) {
        Optional<Account> admin = accountRepository.findByEmail(adminUsername);
        if (admin.isEmpty()) {
            Account account = Account.builder()
                    .email(adminUsername)
                    .password(passwordEncoder.encode(pass))
                    .role(Role.ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .build();
            accountRepository.save(account);
        }
    }
}


