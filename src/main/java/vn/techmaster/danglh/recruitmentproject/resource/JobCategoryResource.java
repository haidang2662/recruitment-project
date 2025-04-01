package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.entity.JobCategory;
import vn.techmaster.danglh.recruitmentproject.service.JobCategoryService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/job-categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobCategoryResource {

    JobCategoryService jobCategoryService;

    @GetMapping
    public List<JobCategory> getAllCategories(){
        return jobCategoryService.getAllCategories();
    }

}
