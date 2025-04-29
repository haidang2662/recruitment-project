package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.NotificationTarget;

public interface NotificationTargetRepository extends JpaRepository<NotificationTarget, Long> {

    Page<NotificationTarget> findByTarget(Account target, Pageable pageable);

}
