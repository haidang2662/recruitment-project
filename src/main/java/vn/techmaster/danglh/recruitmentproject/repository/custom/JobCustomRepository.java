package vn.techmaster.danglh.recruitmentproject.repository.custom;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.dto.SearchJobDto;
import vn.techmaster.danglh.recruitmentproject.model.request.JobSearchRequest;
import vn.techmaster.danglh.recruitmentproject.repository.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JobCustomRepository extends BaseRepository {

    public List<SearchJobDto> searchJob(JobSearchRequest request, Long creatorId, Long candidateId, Role role) {
        String query = "with raw_data as (\n" +
                "    select j.id, j.name, j.position, " +
                "       j.year_of_experience_from yearOfExperienceFrom, " +
                "       j.year_of_experience_to yearOfExperienceTo, " +
                "       j.working_type workingType, j.working_time_type workingTimeType, " +
                "       j.working_address workingAddress, j.literacy, j.level, j.recruiting_quantity recruitingQuantity, " +
                "       j.expired_date expiredDate, j.salary_from salaryFrom, j.salary_to salaryTo, j.status,\n" +
                "       j.created_at createdAt, c.avatar_url companyAvatarUrl, j.urgent,\n" +
                "       c.name companyName, c.alias, a.email companyEmail, c.created_at companyCreatedAt,\n" +
                "       c.head_quarter_address headQuarterAddress, c.website, l.name workingCity\n" +
                "       {{favorite_job_select_fields}}\n" +
                "    from jobs j\n" +
                "    left join companies c on j.company_id = c.id\n" +
                "    left join accounts a on c.account_id = a.id\n" +
                "    left join locations l on j.working_city = l.id\n" +
                "    {{join_favorite}} \n" +
                "    where 1 = 1\n" +
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
        String searchCondition = "";
        if (request.getName() != null && !request.getName().isBlank()) {
            parameters.put("p_name", "%" + request.getName().toLowerCase() + "%");
            searchCondition += "and lower(j.name) like :p_name\n";
        }
        if (!CollectionUtils.isEmpty(request.getLocationIds())) {
            parameters.put("p_working_cities", request.getLocationIds());
            searchCondition += "and j.working_city in (:p_working_cities)\n";
        }
        if (request.getPosition() != null && !request.getPosition().isBlank()) {
            parameters.put("p_position", "%" + request.getPosition().toLowerCase() + "%");
            searchCondition += "and lower(j.position) like :p_position\n";
        }
//        if (request.getStatus() != null) {
//            parameters.put("p_status", "PUBLISH");
//            searchCondition += "and j.status = :p_status\n";
//        }

        if (request.getCategoryId() != null && request.getCategoryId() != -1) {
            parameters.put("p_category_id", request.getCategoryId());
            searchCondition += "and j.category_id = :p_category_id\n";
        }
        if (!CollectionUtils.isEmpty(request.getWorkingTypes())) {
            parameters.put("p_working_types", request.getWorkingTypes());
            searchCondition += "and j.working_type in (:p_working_types)\n";
        }
        if (!CollectionUtils.isEmpty(request.getWorkingTimeTypes())) {
            parameters.put("p_working_time_types", request.getWorkingTimeTypes());
            searchCondition += "and j.working_time_type in (:p_working_time_types)\n";
        }
        if (request.getYearOfExperience() != null && !request.getYearOfExperience().isBlank()) {
            searchCondition = buildYearOfExperienceSearchCondition(searchCondition, parameters, request.getYearOfExperience());
        }
        if (request.getSalaryFrom() != null) {
            parameters.put("p_salary_from", request.getSalaryFrom());
            parameters.put("p_salary_to", request.getSalaryTo());
            searchCondition += "and (j.salary_from = :p_salary_from or j.salary_to = :p_salary_from or (j.salary_from < :p_salary_from and j.salary_to > :p_salary_from))" +
                    "or (j.salary_from = :p_salary_to or j.salary_to = :p_salary_to or (j.salary_from < :p_salary_to and j.salary_to > :p_salary_to))" +
                    "or (:p_salary_from < j.salary_from and :p_salary_to > j.salary_to)\n";
        }
        // ý tưởng để giải quyết vấn đề salary from và salary to là dùng or thay and để nối điều kiện giữa 2 bên
//        if (request.getSalaryTo() != null) {
//            parameters.put("p_salary_to", request.getSalaryTo());
//            searchCondition += "and (j.salary_from = :p_salary_to or j.salary_to = :p_salary_to or (j.salary_from < :p_salary_to and j.salary_to > :p_salary_to))\n";
//        }


        // nếu role của user hiện tại đang tìm job là company => chỉ show các job của company đấy thôi
        if (role != null && role.equals(Role.COMPANY)) {
            parameters.put("p_company", creatorId);
            searchCondition += "and j.company_id = :p_company\n";
            query = query.replace("{{favorite_job_select_fields}}", "")
                    .replace("{{join_favorite}}", "");
        }

        // nếu role của user hiện tại đang tìm job là candidate
        //      => show tất cả các job của tất cả các company nhưng chưa hết hạn và đang ở trạng thái PUBLISH
        if (role == null || role.equals(Role.CANDIDATE)) {
            searchCondition += "and j.status = 'PUBLISH'\n";

            if (role != null && role.equals(Role.CANDIDATE)) {
                String joinCondition = "left join favourite_jobs fj on j.id = fj.job_id and fj.candidate_id = " + candidateId + "\n";
                String selectField = ", case when fj.id is not null then true else false end favorite\n";
                query = query.replace("{{favorite_job_select_fields}}", selectField)
                        .replace("{{join_favorite}}", joinCondition);
                if (request.getFavorite() != null) {
                    parameters.put("p_favorite", request.getFavorite());
                    searchCondition += request.getFavorite() ? "and fj.id is not null\n" : "";
                }
            } else {
                query = query.replace("{{favorite_job_select_fields}}", "")
                        .replace("{{join_favorite}}", "");
            }
        }

        query = query.replace("{{search_condition}}", searchCondition);
        parameters.put("p_page_size", request.getPageSize());
        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());
        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchJobDto.class));
    }

    private String buildYearOfExperienceSearchCondition(String searchCondition, Map<String, Object> parameters, String yearOfExperience) {
        if (yearOfExperience.equals("all")) {
            return searchCondition;
        }
        switch (yearOfExperience) {
            case "not_required":
                searchCondition += "and j.year_of_experience_to = 0\n";
                break;
            case "less_than_1":
                searchCondition += "and j.year_of_experience_to <= 1\n";
                break;
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
                parameters.put("p_year_of_experience", Integer.valueOf(yearOfExperience));
                searchCondition += "and (j.year_of_experience_from = :p_year_of_experience or j.year_of_experience_to = :p_year_of_experience or (j.year_of_experience_from < :p_year_of_experience and j.year_of_experience_to > :p_year_of_experience))\n";
                break;
            case "greater_than_5":
                searchCondition += "and j.year_of_experience_from >= 5\n";
                break;
        }
        return searchCondition;
    }

//    public List<SearchJobDto> searchApplicationJob(JobSearchRequest request, Long creatorId, Long candidateId, Role role) {
//        String query = "with raw_data as (\n" +
//                "    select j.id, j.name, j.position, " +
//                "       j.year_of_experience_from yearOfExperienceFrom, " +
//                "       j.year_of_experience_to yearOfExperienceTo, " +
//                "       j.working_type workingType, j.working_time_type workingTimeType, " +
//                "       j.working_address workingAddress, j.literacy, j.level, j.recruiting_quantity recruitingQuantity, " +
//                "       j.expired_date expiredDate, j.salary_from salaryFrom, j.salary_to salaryTo, j.status,\n" +
//                "       j.created_at createdAt, c.avatar_url companyAvatarUrl, j.urgent,\n" +
//                "       c.name companyName, c.alias, a.email companyEmail, c.created_at companyCreatedAt,\n" +
//                "       c.head_quarter_address headQuarterAddress, c.website, l.name workingCity\n" +
//                "       {{application_job_select_fields}}\n" +
//                "    from jobs j\n" +
//                "    left join companies c on j.company_id = c.id\n" +
//                "    left join accounts a on c.account_id = a.id\n" +
//                "    left join locations l on j.working_city = l.id\n" +
//                "    {{join_application}} \n" +
//                "    where 1 = 1\n" +
//                "   {{search_condition}}\n" +
//                "), count_data as(\n" +
//                "    select count(*) totalRecord\n" +
//                "    from raw_data\n" +
//                ")\n" +
//                "select r.*, c.totalRecord\n" +
//                "from raw_data r, count_data c\n" +
//                "limit :p_page_size\n" +
//                "offset :p_offset";
//
//        Map<String, Object> parameters = new HashMap<>();
//        String searchCondition = "";
//        if (request.getName() != null && !request.getName().isBlank()) {
//            parameters.put("p_name", "%" + request.getName().toLowerCase() + "%");
//            searchCondition += "and lower(j.name) like :p_name\n";
//        }
//
//        // nếu role của user hiện tại đang tìm job là company => chỉ show các job của company đấy thôi
//        if (role != null && role.equals(Role.COMPANY)) {
//            parameters.put("p_company", creatorId);
//            searchCondition += "and j.company_id = :p_company\n";
//            query = query.replace("{{application_job_select_fields}}", "")
//                    .replace("{{join_application}}", "");
//        }
//
//        // nếu role của user hiện tại đang tìm job là candidate
//        //      => show tất cả các job của tất cả các company nhưng chưa hết hạn và đang ở trạng thái PUBLISH
//        if (role == null || role.equals(Role.CANDIDATE)) {
//            searchCondition += "and j.status = 'PUBLISH'\n";
//
//            if (role != null && role.equals(Role.CANDIDATE)) {
//                String joinCondition = "left join applications app on j.id = app.job_id and app.candidate_id = " + candidateId + "\n";
//                String selectField = ", case when app.id is not null then true else false end application\n";
//                query = query.replace("{{application_job_select_fields}}", selectField)
//                        .replace("{{join_application}}", joinCondition);
//                if (request.getApplication() != null) {
//                    parameters.put("p_application", request.getApplication());
//                    searchCondition += request.getApplication() ? "and app.id is not null\n" : "";
//                }
//            } else {
//                query = query.replace("{{application_job_select_fields}}", "")
//                        .replace("{{join_application}}", "");
//            }
//        }
//
//        query = query.replace("{{search_condition}}", searchCondition);
//        parameters.put("p_page_size", request.getPageSize());
//        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());
//
//        System.out.println("Generated Query: " + query);
//        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchJobDto.class));
//    }

    public List<SearchJobDto> searchApplicationJob(JobSearchRequest request, Long creatorId, Long candidateId, Role role) {
        String query = "with raw_data as (\n" +
                "    select j.id, j.name, j.position, " +
                "       j.year_of_experience_from yearOfExperienceFrom, " +
                "       j.year_of_experience_to yearOfExperienceTo, " +
                "       j.working_type workingType, j.working_time_type workingTimeType, " +
                "       j.working_address workingAddress, j.literacy, j.level, j.recruiting_quantity recruitingQuantity, " +
                "       j.expired_date expiredDate, j.salary_from salaryFrom, j.salary_to salaryTo, j.status,\n" +
                "       j.created_at createdAt, c.avatar_url companyAvatarUrl, j.urgent,\n" +
                "       c.name companyName, c.alias, a.email companyEmail, c.created_at companyCreatedAt,\n" +
                "       c.head_quarter_address headQuarterAddress, c.website, l.name workingCity\n" +
                "       {{application_job_select_fields}}\n" +
                "    from jobs j\n" +
                "    left join companies c on j.company_id = c.id\n" +
                "    left join accounts a on c.account_id = a.id\n" +
                "    left join locations l on j.working_city = l.id\n" +
                "    {{join_application}} \n" +
                "    {{join_favorite}}\n" +  // Thêm join bảng favourite_jobs
                "    where 1 = 1\n" +
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
        String searchCondition = "";
        if (request.getName() != null && !request.getName().isBlank()) {
            parameters.put("p_name", "%" + request.getName().toLowerCase() + "%");
            searchCondition += "and lower(j.name) like :p_name\n";
        }

        // Nếu role là company, chỉ show job của công ty đó
        if (role != null && role.equals(Role.COMPANY)) {
            parameters.put("p_company", creatorId);
            searchCondition += "and j.company_id = :p_company\n";
            query = query.replace("{{application_job_select_fields}}", "")
                    .replace("{{join_application}}", "")
                    .replace("{{join_favorite}}", "");
        }

        // Nếu role là candidate, chỉ show job chưa hết hạn và đang ở trạng thái PUBLISH
        if (role == null || role.equals(Role.CANDIDATE)) {
            searchCondition += "and app.id is not null\n";

            if (role != null && role.equals(Role.CANDIDATE)) {
                String joinApplication = "left join applications app on j.id = app.job_id and app.candidate_id = " + candidateId + "\n";
                String joinFavorite = "left join favourite_jobs fj on j.id = fj.job_id and fj.candidate_id = " + candidateId + "\n";
                String selectFields = ", case when app.id is not null then true else false end application,\n" +
                        "  case when fj.id is not null then true else false end favorite\n";
                query = query.replace("{{application_job_select_fields}}", selectFields)
                        .replace("{{join_application}}", joinApplication)
                        .replace("{{join_favorite}}", joinFavorite);
            } else {
                query = query.replace("{{application_job_select_fields}}", "")
                        .replace("{{join_application}}", "")
                        .replace("{{join_favorite}}", "");
            }
        }

        query = query.replace("{{search_condition}}", searchCondition);
        parameters.put("p_page_size", request.getPageSize());
        parameters.put("p_offset", request.getPageSize() * request.getPageIndex());

        return getNamedParameterJdbcTemplate().query(query, parameters, new BeanPropertyRowMapper<>(SearchJobDto.class));
    }


}