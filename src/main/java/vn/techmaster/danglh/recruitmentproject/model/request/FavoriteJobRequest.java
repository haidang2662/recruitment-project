package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteJobRequest {

    @NotNull(message = "Id job is required")
    private Long jobId;

}
