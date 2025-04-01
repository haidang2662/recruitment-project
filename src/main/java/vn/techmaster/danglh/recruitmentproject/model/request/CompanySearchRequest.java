package vn.techmaster.danglh.recruitmentproject.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanySearchRequest extends BaseSearchRequest{

    String name;
    String alias; // Viết tắt
    String phone;
    String taxCode;

    boolean random;

}



