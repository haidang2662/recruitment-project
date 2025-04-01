package vn.techmaster.danglh.recruitmentproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/companies/candidates")
public class CompanyCandidateController {

    @GetMapping
    public String getCandidates() {
        return "company/candidate/candidates";
    }

    @GetMapping("/{id}")
    public String getCandidateDetails() {
        return "company/candidate/candidate-details";
    }

}
