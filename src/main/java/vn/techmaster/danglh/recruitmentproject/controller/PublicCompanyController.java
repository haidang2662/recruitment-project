package vn.techmaster.danglh.recruitmentproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public/companies")
public class PublicCompanyController {

    @GetMapping
    public String searchCompanies()  {
        return "/candidate/company/companies";
    }

    @GetMapping("/{id}")
    public String getCompanyDetails()  { return "/candidate/company/company-details"; }
}
