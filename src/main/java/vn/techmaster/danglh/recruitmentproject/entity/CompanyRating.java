package vn.techmaster.danglh.recruitmentproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.techmaster.danglh.recruitmentproject.constant.CompanyRatingMode;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "company_ratings")
public class CompanyRating extends BaseEntity {

    Integer rate;
    String comment;

    @Enumerated(EnumType.STRING)
    CompanyRatingMode ratingMode;

    @JoinColumn(name = "account_id")
    @ManyToOne(targetEntity = Account.class)
    Account account;

}
