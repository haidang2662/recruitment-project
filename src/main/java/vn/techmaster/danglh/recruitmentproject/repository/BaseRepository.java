package vn.techmaster.danglh.recruitmentproject.repository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.techmaster.danglh.recruitmentproject.dto.SearchCompanyDto;

import java.util.List;

@Repository
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseRepository {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

}