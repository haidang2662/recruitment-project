package vn.techmaster.danglh.recruitmentproject.repository.custom;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.dto.SearchCompanyDto;
import vn.techmaster.danglh.recruitmentproject.model.response.ChartResponse;
import vn.techmaster.danglh.recruitmentproject.repository.BaseRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardCustomRepository extends BaseRepository {

    public List<ChartResponse.ChartDetailResponse> getPassedCandidateByMonth(LocalDateTime startDate,
                                                                             LocalDateTime endDate,
                                                                             Long companyId) {
        String query = "SELECT \n" +
                "    DATE_FORMAT(interview_at, '%m-%Y') AS title, \n" + // lấy ra dạng tháng năm để trực quan thị giác trên front end
                "    DATE_FORMAT(interview_at, '%Y%m') AS subtitle, \n" + // lấy ra dạng số để dễ sắp xếp theo thời gian
                "    COUNT(*) AS quantity\n" +
                "FROM interviews i\n" +
                "left join applications a on i.application_id = a.id\n" +
                "left join jobs j on a.job_id = j.id\n" +
                "left join companies c on c.id = j.company_id\n" +
                "where i.status = 'PASSED'\n" +
                "   and c.id = :p_company_id\n" + // chỉ lấy dữ liệu của công ty hiện tại
                "   and interview_at >= STR_TO_DATE(:p_start_time, '%Y-%m-%d %H:%i:%s')\n" +
                "   and interview_at <= STR_TO_DATE(:p_end_time, '%Y-%m-%d %H:%i:%s')\n" +
                "GROUP BY DATE_FORMAT(interview_at, '%m-%Y'), DATE_FORMAT(interview_at, '%Y%m')";

        Map<String, Object> parameters = new HashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        parameters.put("p_start_time", dateTimeFormatter.format(startDate));
        parameters.put("p_end_time", dateTimeFormatter.format(endDate));
        parameters.put("p_company_id", companyId);

        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(ChartResponse.ChartDetailResponse.class));
    }


}
