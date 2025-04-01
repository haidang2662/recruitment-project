$(document).ready(function () {

    // Thêm phương thức "pattern" vào jQuery Validation
    $.validator.addMethod("pattern", function (value, element, regex) {
        // Nếu trường không bắt buộc (optional) hoặc giá trị khớp regex thì hợp lệ
        return this.optional(element) || regex.test(value);
    }, "Invalid format."); // Thông báo lỗi mặc định

    $("#change-password-form").validate({
        onfocusout: false,
        onkeyup: false,
        onclick: false,
        rules: {
            "newPassword": {
                required: true,
                maxlength: 50,
                pattern: PASSWORD_PATTERN // Regex: Ít nhất 1 chữ hoa, 1 chữ thường, 1 số
            },
            "confirmPassword": {
                required: true,
                maxlength: 50,
                pattern: PASSWORD_PATTERN // Regex: Ít nhất 1 chữ hoa, 1 chữ thường, 1 số
            },
        },
        messages: {
            "newPassword": {
                required: "New password is required.",
                maxlength: "New password must not exceed 50 characters.",
                pattern: "New password must include at least 1 uppercase letter, 1 lowercase letter, 1 number and must be at least 8 characters."
            },
            "confirmPassword": {
                required: "Confirm password is required.",
                maxlength: "Confirm password must not exceed 50 characters.",
                pattern: "Confirm password must include at least 1 uppercase letter, 1 lowercase letter, 1 number and must be at least 8 characters."
            }
        },
        errorPlacement: function (error, element) {
            // Gắn thông báo lỗi ngay sau input và đổi màu thành đỏ
            error.css("color", "red");
            error.insertAfter(element);
        }
    });

    $("#change-password-btn").click(async function (event) {

        const isValidForm = $("#change-password-form").valid();
        if (!isValidForm) {
            return;
        }

        //disable nút update
        $("#change-password-btn").prop("disabled", true);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

        const account = JSON.parse(localStorage.getItem("account"));
        const accountId = account?.id;

        const newPassword = $("#newPassword").val();
        const confirmedPassword = $("#confirmPassword").val();

        if (newPassword !== confirmedPassword) {
            showToast("Password and Confirm Password do not match", ERROR_TOAST);
            return;
        }

        await $.ajax({
            url: `/api/v1/accounts/${accountId}/password`,
            type: "PATCH",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify({
                password: md5(newPassword),
                confirmedPassword: md5(confirmedPassword)
            }),
            success: function () {
                showToast("Change password successfully", SUCCESS_TOAST);

                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('account');

                setTimeout(function () {
                    location.href = "/";
                }, 3000);
            },
            error: function () {
                showToast("Change password failed", ERROR_TOAST)
            }
        });

        //nhả nút update
        $("#change-password-btn").prop("disabled", false);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

    })


});