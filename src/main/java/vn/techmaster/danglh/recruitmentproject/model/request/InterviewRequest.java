package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewStatus;
import vn.techmaster.danglh.recruitmentproject.constant.InterviewType;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewRequest {

    @NotNull
    Long applicationId;

    @NotNull(message = "Thời gian phỏng vấn không được bỏ trống")
    @Future(message = "Thời gian phỏng vấn phải là ngày trong tương lai")
    LocalDateTime interviewAt;

    @NotNull(message = "Hình thức phỏng vấn không được bỏ trống")
    InterviewType interviewType;

//    @NotBlank(message = "Địa chỉ phỏng vấn không được bỏ trống")
    @Size(max = 200, message = "Địa chỉ phỏng vấn không được quá 200 ký tụ")
    String interviewAddress;

    InterviewStatus status;

    String note;

}

