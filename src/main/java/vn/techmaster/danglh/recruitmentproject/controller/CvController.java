package vn.techmaster.danglh.recruitmentproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cv")
public class CvController {

    @GetMapping("/upload")
    public String uploadCv() {
        return "candidate/cv/cv-uploading";
    }

    @GetMapping("")
    public String listCv() {
        return "candidate/cv/cv-list";
    }

}
