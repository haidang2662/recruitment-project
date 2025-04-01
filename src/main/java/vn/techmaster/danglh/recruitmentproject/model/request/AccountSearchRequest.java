package vn.techmaster.danglh.recruitmentproject.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountSearchRequest extends BaseSearchRequest {

    String email;

    String gender;

}
