package vn.techmaster.danglh.recruitmentproject.repository.custom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.dto.SearchApplicationDto;
import vn.techmaster.danglh.recruitmentproject.model.request.ApplicationSearchRequest;
import vn.techmaster.danglh.recruitmentproject.repository.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ApplicationCustomRepository extends BaseRepository {

    public List<SearchApplicationDto> searchApplicationForCompany(ApplicationSearchRequest request, Long companyId) {
        String query = "with raw_data as (\n" +
                "    select a.id, j.name jobName, c.name candidateName, a.created_at appliedDate, a.status, cv.cv_url cvUrl, cv.id cvId, j.id jobId, c.id candidateId \n" +
                "    from applications a\n" +
                "    left join jobs j on a.job_id = j.id\n" +
                "    left join candidates c on c.id = a.candidate_id\n" +
                "    left join candidate_cvs cv on cv.id = a.candidate_cv_id" +
                "    where j.company_id = :p_company_id\n" +
                "   {{search_condition}}\n" +
                "), count_data as(\n" +
                "    select count(*) totalRecord\n" +
                "    from raw_data\n" +
                ")\n" +
                "select r.*, c.totalRecord\n" +
                "from raw_data r, count_data c\n" +
                "limit :p_page_size\n" +
                "offset :p_offset";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_company_id", companyId);

        String searchCondition = "";
        if (StringUtils.isNotBlank(request.getJobName())) {
            searchCondition += " and lower(j.name) like :job_name";
            parameters.put("job_name", "%" + request.getJobName().toLowerCase() + "%");
        }
        if (StringUtils.isNotBlank(request.getCandidateName())) {
            searchCondition += " and lower(c.name) like :candidate_name";
            parameters.put("candidate_name", "%" + request.getCandidateName().toLowerCase() + "%");
        }
        if (request.getCandidateId() != null) {
            searchCondition += " and c.id = :candidate_id";
            parameters.put("candidate_id", request.getCandidateId());
        }
        if (request.getStatus() != null) {
            searchCondition += " and a.status = :status";
            parameters.put("status", request.getStatus().name());
        }

        query = query.replace("{{search_condition}}", searchCondition);
        parameters.put("p_page_size", request.getPageSize());
        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());

        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchApplicationDto.class));
    }
}