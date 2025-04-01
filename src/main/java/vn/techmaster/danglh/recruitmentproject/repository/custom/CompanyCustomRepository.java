package vn.techmaster.danglh.recruitmentproject.repository.custom;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.dto.SearchCompanyDto;
import vn.techmaster.danglh.recruitmentproject.model.request.CompanySearchRequest;
import vn.techmaster.danglh.recruitmentproject.repository.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CompanyCustomRepository extends BaseRepository {

    public List<SearchCompanyDto> searchRandomCompany(CompanySearchRequest request) {
        String query = "with raw_data as (\n" +
                "    select c.id, c.name, c.alias, c.phone,  " +
                "       c.found_at foundAt, " +
                "       c.tax_code taxCode, " +
                "       c.head_quarter_address  headQuarterAddress, c.employee_quantity employeeQuantity, " +
                "       c.website, c.avatar_url avatarUrl, " +
                "       c.description, c.rating,\n" +
                "       a.email email\n" +
                "    from companies c\n" +
                "    join accounts a on c.account_id = a.id\n" +
                "    where 1 = 1\n" +
                "), count_data as(\n" +
                "    select count(*) totalRecord\n" +
                "    from raw_data\n" +
                ")\n" +
                "select r.*, c.totalRecord\n" +
                "from raw_data r, count_data c\n" +
                "{{order_random}}\n" +
                "limit :p_page_size\n" +
                "offset :p_offset";

        query = query.replace("{{order_random}}", request.isRandom() ? "ORDER BY RAND()" : "order by name");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_page_size", request.getPageSize());
        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());
        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchCompanyDto.class));
    }

}
