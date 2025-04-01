package vn.techmaster.danglh.recruitmentproject.model.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.techmaster.danglh.recruitmentproject.constant.RegistrationType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrationRequest {

    @NotBlank(message = "Email không được bỏ trống")
    @Size(max = 50, message = "Email không được quá 50 ký tụ")
    @Email(message = "Phải đúng định dạng email")
    String email;

    @NotBlank(message = "Password không được bỏ trống")
    @Size(max = 50, message = "Password không được quá 50 ký tự")
//    @Pattern(
//            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
//            message = "Password phải có ít nhất 1 chữ hoa, 1 chữ thường, 1 số và tối thiểu 8 ký tự"
//    )
    String password;

    @NotBlank(message = "Tên không được bỏ trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tụ")
    String name;

    @Size(max = 50, message = "Tên trụ sở chính không được quá 50 ký tụ")
    String headQuarterAddress;

    @Min(value = 1, message = "Số nhân viên phải lớn hơn 0")
    Integer employeeQuantity;

    @Size(max = 50, message = "Tên website không được quá 50 ký tụ")
    String website;

    @NotNull(message = "Loại tài khoản không được trống")
    RegistrationType type;

}
