package vn.techmaster.danglh.recruitmentproject.resource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.CompanySearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CompanyResponse;
import vn.techmaster.danglh.recruitmentproject.service.CompanyService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/companies")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyResource {

    CompanyService companyService;

    @GetMapping
    public CommonSearchResponse<?> searchRandomCompany(CompanySearchRequest request) {
        return companyService.searchRandomCompany(request);
    }

    @GetMapping("/{id}")
    public CompanyResponse getCompanyDetails(@PathVariable Long id) throws ObjectNotFoundException {
        return companyService.getCompanyDetails(id);
    }
}
