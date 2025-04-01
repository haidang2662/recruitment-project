package vn.techmaster.danglh.recruitmentproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/companies/interviews")
public class CompanyInterviewController {

    @GetMapping("/creation")
    public String creatInterview() {
        return "company/interview/interview-creation";
    }

    @GetMapping
    public String getInterviews() {
        return "company/interview/interviews";
    }

    @GetMapping("/{id}")
    public String getInterviewDetails(@PathVariable Long id) {
        return "company/interview/interview-details";
    }

}
