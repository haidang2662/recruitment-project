package vn.techmaster.danglh.recruitmentproject.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommonSearchResponse<T> {

    Integer totalPage;

    Long totalRecord;

    List<T> data;

    CommonPagingResponse pageInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CommonPagingResponse {
        int pageSize;
        int pageNumber;
    }

}
