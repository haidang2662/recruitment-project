package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.Company;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company , Long> {
    Optional<Company> findByAccount(Account account);
}
