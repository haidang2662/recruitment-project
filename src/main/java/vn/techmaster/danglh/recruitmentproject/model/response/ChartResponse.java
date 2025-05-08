package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {

    List<ChartDetailResponse> data;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDetailResponse {
        String title; // tháng mấy ("12/2024", "01/2025", ...)
        Integer quantity;
    }

    Long numberPassedCandidatesT12;
    Long numberPassedCandidatesT1;
    Long numberPassedCandidatesT2;
    Long numberPassedCandidatesT3;
    Long numberPassedCandidatesT4;

}
