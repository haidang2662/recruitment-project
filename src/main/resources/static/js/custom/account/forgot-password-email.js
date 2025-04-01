$(document).ready(function () {

    $("#send-email-forget-password").validate({
        onfocusout: false,
        onkeyup: false,
        onclick: false,
        rules: {
            "email": {
                required: true,
                maxlength: 50,
                email: true
            },
        },
        messages: {
            "email": {
                required: "Email is required.",
                maxlength: "Email must not exceed 50 characters.",
                email: "Must have correct email format" // Regex: Ít nhất 1 chữ hoa, 1 chữ thường, 1 số
            },
        },
        errorPlacement: function (error, element) {
            // Gắn thông báo lỗi ngay sau input và đổi màu thành đỏ
            error.css("color", "red");
            error.insertAfter(element);
        }
    });

    $("#send-email-forget-password-btn").click(function () {

        const isValidForm = $("#send-email-forget-password").valid();
        if (!isValidForm) {
            return;
        }

        //disable nút update
        $("#send-email-forget-password-btn").prop("disabled", true);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

        const inputEmail = $("#input-email").val();

        $.ajax({
            url: "/api/v1/accounts/password_forgotten_emails",
            type: "POST",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify({
                email: inputEmail
            }),
            success: function () {
                showToast("Send email successfully", SUCCESS_TOAST);
                setTimeout(function () {
                    location.href = "/";
                }, 3000);
            },
            error: function () {
                showToast("Send email failed", ERROR_TOAST);
            }
        });

        //disable nút update
        $("#send-email-forget-password-btn").prop("disabled", false);

        //Chuyen doi an hien
        $("#spinner").toggleClass('d-none');

    });

});