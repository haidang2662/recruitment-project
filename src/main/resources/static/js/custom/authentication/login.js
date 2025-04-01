$(document).ready(function () {

    // Thêm phương thức "pattern" vào jQuery Validation
    $.validator.addMethod("pattern", function (value, element, regex) {
        // Nếu trường không bắt buộc (optional) hoặc giá trị khớp regex thì hợp lệ
        return this.optional(element) || regex.test(value);
    }, "Invalid format."); // Thông báo lỗi mặc định

    $("#login-form").validate({
        onfocusout: false,
        onkeyup: false,
        onclick: false,
        rules: {
            "email": {
                required: true,
                maxlength: 50,
                email: true
            },
            "password": {
                required: true,
                maxlength: 50,
                pattern: PASSWORD_PATTERN // Regex: Ít nhất 1 chữ hoa, 1 chữ thường, 1 số
            },
        },
        messages: {
            "email": {
                required: "Email is required.",
                maxlength: "Email must not exceed 50 characters.",
                email: "Must have correct email format"
            },
            "password": {
                required: "Password is required.",
                maxlength: "Password must not exceed 50 characters.",
                pattern: "Password must include at least 1 uppercase letter, 1 lowercase letter, 1 number and must be at least 8 characters."
            }
        },
        // errorPlacement: function (error, element) {
        //     // Gắn thông báo lỗi ngay sau input và đổi màu thành đỏ
        //     error.css("color", "red");
        //     error.insertAfter(element);
        // }
    });

    $("#login-btn").click(function () {

        const isValidForm = $("#login-form").valid();
        if (!isValidForm) {
            return;
        }

        //disable nút login
        $("#login-btn").prop("disabled", true);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

        // B1 : Lay du lieu tu form
        const formData = $("#login-form").serializeArray();
        let login = {};
        for (let i = 0; i < formData.length; i++) {
            login[formData[i].name] = formData[i].value;
        }
        login["password"] = md5(login["password"]);
        login["email"] = login["email"].trim();
        //B2 : Call ajax
        $.ajax({
            url: "/api/v1/authentications/login",
            type: "POST",
            data: JSON.stringify(login),
            contentType: "application/json; charset=utf-8",
            success: async function (data) {
                localStorage.setItem("accessToken", data?.jwt);
                localStorage.setItem("refreshToken", data?.refreshToken);

                // lấy thông tin chi tiết account
                const accountInfo = await getAccountDetail(data?.id);
                localStorage.setItem("account", JSON.stringify(accountInfo));

                const id = data?.id;

                if (accountInfo.role === CANDIDATE_ROLE) {
                    location.href = "/";
                } else if (accountInfo.role === COMPANY_ROLE) {
                    location.href = `/companies/dashboard`;
                }
            },
            error: function (err) {
                if (err?.responseJSON?.status === 401) {
                    showToast("Email or password incorrect", ERROR_TOAST);
                    return;
                }
                handleResponseError(err, "Login failed");
            }

        });

        //nhả nút login
        $("#login-btn").prop("disabled", false);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

    });

});