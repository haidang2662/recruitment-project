package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.dto.SearchCompanyDto;
import vn.techmaster.danglh.recruitmentproject.entity.Company;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.CompanySearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CompanyResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CompanySearchResponse;
import vn.techmaster.danglh.recruitmentproject.repository.CompanyRepository;
import vn.techmaster.danglh.recruitmentproject.repository.custom.CompanyCustomRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyService {

    CompanyCustomRepository companyCustomRepository;
    ObjectMapper objectMapper;
    CompanyRepository companyRepository;

    public CommonSearchResponse<?> searchRandomCompany(CompanySearchRequest request) {
        List<SearchCompanyDto> result = companyCustomRepository.searchRandomCompany(request);

        Long totalRecord = 0L;
        List<CompanySearchResponse> companyResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            companyResponses = result
                    .stream()
                    .map(s -> objectMapper.convertValue(s, CompanySearchResponse.class))
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<CompanySearchResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(companyResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }

    public CompanyResponse getCompanyDetails(Long id) throws ObjectNotFoundException {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay company co id la :" + id));
        CompanyResponse companyResponse = objectMapper.convertValue(company, CompanyResponse.class);
        companyResponse.setEmail(company.getAccount().getEmail());
        return companyResponse;
    }
}
