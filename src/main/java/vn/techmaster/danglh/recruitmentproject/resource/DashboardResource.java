package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.model.response.ChartResponse;
import vn.techmaster.danglh.recruitmentproject.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboards")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardResource {

    DashboardService dashboardService;

    @GetMapping("")
    public ChartResponse getNumbersPassedCandidate(){
        return dashboardService.getNumbersPassedCandidate();
    }

}
