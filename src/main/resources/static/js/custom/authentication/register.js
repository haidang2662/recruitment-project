$(document).ready(function () {

    // Thêm phương thức "pattern" vào jQuery Validation
    $.validator.addMethod("pattern", function (value, element, regex) {
        // Nếu trường không bắt buộc (optional) hoặc giá trị khớp regex thì hợp lệ
        return this.optional(element) || regex.test(value);
    }, "Invalid format."); // Thông báo lỗi mặc định

    let registrationType = "CANDIDATE";

    $("#success-register").hide();
    $("#failed-register").hide();
    $("#address").hide();
    $("#quantity").hide();
    $("#website").hide();

    const registerValidator = $("#register-form").validate({
        onfocusout: false,
        onkeyup: false,
        onclick: false,
        rules: {
            "email": {
                required: true,
                maxlength: 50,
                email:true
            },
            "password": {
                required: true,
                maxlength: 50,
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/ // Regex: Ít nhất 1 chữ hoa, 1 chữ thường, 1 số
            },
            "name": {
                required: true,
                maxlength: 50
            },
            "headQuarterAddress": {

                maxlength: 50
            },
            "employeeQuantity": {

                min: 1
            },
            "website": {

                maxlength: 50
            }
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
            },
            "name": {
                required: "Name is required.",
                maxlength: "Name must not exceed 50 characters."
            },
            "headQuarterAddress": {
                maxlength: "headQuarterAddress must not exceed 50 characters."
            },
            "employeeQuantity": {
                min: "Number of employees must be greater than or equal to 1"
            },
            "website": {
                maxlength: "website must not exceed 50 characters."
            },
        }
    });


    $("#register-btn").click(async function () {
        const isValidForm = $("#register-form").valid();
        if (!isValidForm) {
            return;
        }

        //disable nút đăng ký
        $("#register-btn").prop("disabled", true);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

        //b1 : Lay du lieu tu form
        const formData = $("#register-form").serializeArray();
        const register = {};
        for (let i = 0; i < formData.length; i++) {
            register[formData[i].name] = formData[i].value;
        }

        register["type"] = registrationType;
        register["password"] = md5(register["password"]);

        //b2 : call ajax
        await $.ajax({
            url: "/api/v1/authentications/registration",
            type: "POST",
            data: JSON.stringify(register),
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                //b3 : Hien thi thong bao thanh cong va yeu cau xac thuc tai khoan
                $("#resend-email").attr("account-id", data.id);
                $("#failed-register").hide();
                $("#success-register").show();
            },
            error: function (err) {
                handleResponseError(err, null);
                $("#success-register").hide();
                $("#failed-register").show();
            }
        });

        //Nhả nút đăng ký
        $("#register-btn").prop("disabled", false);
        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');
    });

    $("#resend-email").click(function () {
        const id = $("#resend-email").attr("account-id");
        $.ajax({
            url: "/api/v1/accounts/" + id + "/activation_emails",
            type: "POST",
            contentType: "application/json; charset=utf-8",
            success: function (response) {
                showToast("Resent email successfully", SUCCESS_TOAST);
            },
            error: function (err) {
                handleResponseError(err, "Resent email failed");
            }
        })
    });

    $("#register-company").click(function () {
        $("#address").show();
        $("#quantity").show();
        $("#website").show();
        registerValidator.resetForm();
        $("#register-form").trigger('reset');

        $('#id-company').css('background-color', '#34A853');
        $('#icon-company').css('color', 'white');
        $('#id-company').css('color', 'white');

        $('#id-candidate').css('background-color', '#E1F2E5');
        $('#icon-candidate').css('color', '#34A853');
        $('#id-candidate').css('color', '#34A853');

        registrationType = "COMPANY";
    });

    $("#register-candidate").click(function () {
        $("#address").hide();
        $("#quantity").hide();
        $("#website").hide();
        registerValidator.resetForm();
        $("#register-form").trigger('reset');

        $('#id-company').css('background-color', '#E1F2E5');
        $('#icon-company').css('color', '#34A853');
        $('#id-company').css('color', '#34A853');

        $('#id-candidate').css('background-color', '#34A853');
        $('#icon-candidate').css('color', 'white');
        $('#id-candidate').css('color', 'white');

        registrationType = "CANDIDATE";
    });

});