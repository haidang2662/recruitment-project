package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.AccountStatus;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.model.CandidateModel;
import vn.techmaster.danglh.recruitmentproject.model.CompanyModel;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {

    Long id;

    String email;

    String name;

    Role role;

    AccountStatus status;

    CandidateModel candidateModel;

    CompanyModel companyModel;

}
