package vn.techmaster.danglh.recruitmentproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMetadataDto {

    private Long applicationId;
    private Long cvId;
    private Long interviewId;
    private Long jobId;

}
