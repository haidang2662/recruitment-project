package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtResponse {

    String jwt;

    String refreshToken;

    Long id;

    String username;

    Set<String> roles;

}
