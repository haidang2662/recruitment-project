package vn.techmaster.danglh.recruitmentproject.repository;

import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByAccountAndRefreshTokenAndInvalidated(Account account, String refreshToken, boolean invalidated);

    @Modifying
    @Query("update RefreshToken r set r.invalidated = true where r.account.id = :accountId")
    void logOut(Long accountId);

}
