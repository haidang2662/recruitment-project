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
        String title;
        Integer quantity;

        String subtitle; // dùng để sắp xếp thời gian các tháng trong biểu đồ theo thứ tự thời gian . VD : 06-2025 -> 202506 .
    }

}
