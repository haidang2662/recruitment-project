package vn.techmaster.danglh.recruitmentproject.repository.custom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.dto.SearchApplicationDto;
import vn.techmaster.danglh.recruitmentproject.dto.SearchInterviewDto;
import vn.techmaster.danglh.recruitmentproject.model.request.SearchInterviewRequest;
import vn.techmaster.danglh.recruitmentproject.repository.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InterviewCustomRepository extends BaseRepository {

    public List<SearchInterviewDto> searchInterviewForCompany(SearchInterviewRequest request, Long companyId) {
        String query = "with raw_data as (\n" +
                "       SELECT i.id, cd.name candidateName, j.name jobTitle, i.invitation_email_sent_at interviewEmailSentAt, i.interview_at interviewAt, i.interview_type type, i.status, cd.id candidateId, j.id jobId\n" +
                "       FROM interviews i\n" +
                "       left join applications a on i.application_id = a.id\n" +
                "       left join jobs j on j.id = a.job_id\n" +
                "       left join candidates cd on cd.id = a.candidate_id\n" +
                "       left join candidate_cvs cv on cv.id = a.candidate_cv_id\n" +
                "       where j.company_id = :p_company_id\n" +
                "       {{search_condition}}\n" +
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
        if (StringUtils.isNotBlank(request.getJobTitle())) {
            searchCondition += " and lower(j.name) like :job_name";
            parameters.put("job_name", "%" + request.getJobTitle().toLowerCase() + "%");
        }
        if (StringUtils.isNotBlank(request.getCandidateName())) {
            searchCondition += " and lower(cd.name) like :candidate_name";
            parameters.put("candidate_name", "%" + request.getCandidateName().toLowerCase() + "%");
        }

        if (request.getInterviewDateFrom() != null) {
            parameters.put("p_interview_date_from", request.getInterviewDateFrom());
            searchCondition += "and i.interview_at >= :p_interview_date_from\n";
        }
        if (request.getInterviewDateTo() != null) {
            parameters.put("p_interview_date_to", request.getInterviewDateTo());
            searchCondition += "and i.interview_at <= :p_interview_date_to\n";
        }

        if (request.getInterviewType() != null) {
            searchCondition += " and i.interview_type = :type";
            parameters.put("type", request.getInterviewType().name());
        }

        if (request.getStatus() != null) {
            searchCondition += " and i.status = :status";
            parameters.put("status", request.getStatus().name());
        }

        query = query.replace("{{search_condition}}", searchCondition);
        parameters.put("p_page_size", request.getPageSize());
        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());

        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchInterviewDto.class));
    }
}