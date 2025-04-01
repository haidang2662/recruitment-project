package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CvResponse {

    private Long id;
    private String url;
    private String name;
    private LocalDateTime createdAt;
    private boolean main;

}
