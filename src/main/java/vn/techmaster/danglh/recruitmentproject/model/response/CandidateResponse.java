package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.Gender;
import vn.techmaster.danglh.recruitmentproject.constant.Literacy;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateResponse {

    Long id;
    String name;
    Gender gender;
    String phone;
    LocalDate dob;
    Double yearOfExperience;
    Literacy literacy;
    String graduatedAt;

}
