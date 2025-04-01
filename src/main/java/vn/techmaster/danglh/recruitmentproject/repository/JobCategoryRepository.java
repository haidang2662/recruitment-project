package vn.techmaster.danglh.recruitmentproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.techmaster.danglh.recruitmentproject.entity.JobCategory;

public interface JobCategoryRepository extends JpaRepository<JobCategory , Long> {
}
