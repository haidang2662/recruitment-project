package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.entity.JobCategory;
import vn.techmaster.danglh.recruitmentproject.repository.JobCategoryRepository;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class JobCategoryService {

    JobCategoryRepository jobCategoryRepository;

    public List<JobCategory> getAllCategories() {
        return jobCategoryRepository.findAll();
    }
}
