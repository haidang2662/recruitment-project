package vn.techmaster.danglh.recruitmentproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.dto.SearchCandidateDto;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.Company;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.CandidateSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.ApplicationResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CandidateCompanyResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CandidateSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.repository.CandidateRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CompanyRepository;
import vn.techmaster.danglh.recruitmentproject.repository.custom.CandidateCustomRepository;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandidateService {

    CompanyRepository companyRepository;
    CandidateCustomRepository candidateCustomRepository;
    ObjectMapper objectMapper;
    CandidateRepository candidateRepository;

    public CommonSearchResponse<?> searchCandidate(CandidateSearchRequest request) {
        Long companyId = null;
        try {
            CustomUserDetails authentication = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Role role = authentication.getAccount().getRole();
            if (role == Role.COMPANY) {
                Optional<Company> companyOptional = companyRepository.findByAccount(authentication.getAccount());
                if (companyOptional.isEmpty()) {
                    return CommonSearchResponse.<ApplicationResponse>builder()
                            .totalRecord(0L)
                            .totalPage(1)
                            .data(Collections.emptyList())
                            .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                            .build();
                }
                companyId = companyOptional.get().getId();
            }
        } catch (Exception ignored) {
            return CommonSearchResponse.<ApplicationResponse>builder()
                    .totalRecord(0L)
                    .totalPage(1)
                    .data(Collections.emptyList())
                    .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                    .build();
        }
        List<SearchCandidateDto> result = candidateCustomRepository.searchCandidateForCompany(request, companyId);

        Long totalRecord = 0L;
        List<CandidateSearchResponse> candidateResponses = new ArrayList<>();
        if (!result.isEmpty()) {
            totalRecord = result.get(0).getTotalRecord();
            candidateResponses = result
                    .stream()
                    .map(s -> objectMapper.convertValue(s, CandidateSearchResponse.class))
                    .toList();
        }

        int totalPage = (int) Math.ceil((double) totalRecord / request.getPageSize());

        return CommonSearchResponse.<CandidateSearchResponse>builder()
                .totalRecord(totalRecord)
                .totalPage(totalPage)
                .data(candidateResponses)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();
    }

    public CandidateCompanyResponse getCandidateDetails(Long idCandidate) throws ObjectNotFoundException {
        Candidate candidate = candidateRepository.findById(idCandidate)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay candidate co id" + idCandidate));
        CandidateCompanyResponse response = objectMapper.convertValue(candidate, CandidateCompanyResponse.class);
        response.setEmail(candidate.getAccount().getEmail());
        return response;
    }
}
