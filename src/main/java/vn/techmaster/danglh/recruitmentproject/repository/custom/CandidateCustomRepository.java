package vn.techmaster.danglh.recruitmentproject.repository.custom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.dto.SearchCandidateDto;
import vn.techmaster.danglh.recruitmentproject.model.request.CandidateSearchRequest;
import vn.techmaster.danglh.recruitmentproject.repository.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CandidateCustomRepository extends BaseRepository {

    public List<SearchCandidateDto> searchCandidateForCompany(CandidateSearchRequest request, Long companyId) {
        String query = "with count_raw_data as (\n" +
                "    select ca.id candidate_id, count(j.id) total_applied_jobs\n" +
                "    from candidates ca\n" +
                "    left join applications a on a.candidate_id = ca.id\n" +
                "    left join jobs j on j.id = a.job_id\n" +
                "    left join companies c on c.id = j.company_id\n" +
                "    where j.company_id = 1\n" +
                "       {{search_condition}}\n" +
                "    group by ca.id\n" +
                "), raw_data as (\n" +
                "    select ca.id, ca.name , ca.phone , ca.gender , ca.literacy , ca.skills , \n" +
                "       ca.graduated_at graduatedAt , ca.expected_working_time_type expectedWorkingTimeType , " +
                "       ca.expected_working_type expectedWorkingType, cd.total_applied_jobs totalAppliedJob\n" +
                "    from candidates ca\n" +
                "    join count_raw_data cd on ca.id = cd.candidate_id\n" +
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
        if (StringUtils.isNotBlank(request.getName())) {
            searchCondition += " and lower(ca.name) like :name";
            parameters.put("name", "%" + request.getName().toLowerCase() + "%");
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            searchCondition += " and lower(ca.phone) like :phone";
            parameters.put("phone", "%" + request.getPhone().toLowerCase() + "%");
        }

        if (request.getGender() != null) {
            searchCondition += " and ca.gender = :gender";
            parameters.put("gender", request.getGender().name());
        }
        if (request.getLiteracy() != null) {
            searchCondition += " and ca.literacy = :literacy";
            parameters.put("literacy", request.getLiteracy().name());
        }

        if (StringUtils.isNotBlank(request.getSkills())) {
            searchCondition += " and lower(ca.skills) like :skills";
            parameters.put("skills", "%" + request.getSkills().toLowerCase() + "%");
        }

        if (request.getExpectedWorkingTimeType() != null) {
            searchCondition += " and ca.expectedWorkingTimeType = :expectedWorkingTimeType";
            parameters.put("expectedWorkingTimeType", request.getExpectedWorkingTimeType().name());
        }
        if (request.getExpectedWorkingType() != null) {
            searchCondition += " and ca.expectedWorkingType = :expectedWorkingType";
            parameters.put("expectedWorkingType", request.getExpectedWorkingType().name());
        }
//
        query = query.replace("{{search_condition}}", searchCondition);
        parameters.put("p_page_size", request.getPageSize());
        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());

        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchCandidateDto.class));
    }
}
