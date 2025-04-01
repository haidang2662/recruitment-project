package vn.techmaster.danglh.recruitmentproject.repository;


import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.constant.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String username);

    Optional<Account> findByEmailAndStatus(String username, AccountStatus status);

}