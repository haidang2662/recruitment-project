$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    $.validator.addMethod(
        "futureDate",
        function (value, element) {
            if (!value) return true;
            const dob = new Date(value);
            const today = new Date();
            return dob > today;
        },
        "Date must be a date in the future"
    );

    $("#post-interview-form").validate({
        onfocusout: false,
        onkeyup: false,
        onclick: false,
        rules: {
            "interviewAt": {
                required: true,
                futureDate: true,
            },
            "interviewAddress": {
                // required: true,
                maxlength: 200,
            },
        },
        messages: {
            "interviewAt": {
                required: "Interview At is required.",
                futureDate: "Interview At must be in the future",
            },
            "interviewAddress": {
                // required: "Interview Address is required.",
                maxlength: "Interview Address must not exceed 200 characters.",
            },
        }
    });

    // Xử lý sự kiện khi nhấn nút Save
    $("#save-interview-btn").click(async function () {
        const isValidForm = $("#post-interview-form").valid();
        if (!isValidForm) {
            return;
        }

        // Thu thập dữ liệu từ form
        const formData = $("#post-interview-form").serializeArray();
        const applicationId = localStorage.getItem("applicationId");
        if (!applicationId) {
            showToast("Application ID is missing", ERROR_TOAST);
            return;
        }
        // Chuyển đổi mảng formData thành object
        const interviewDataObj = {};
        formData.forEach(item => {
            interviewDataObj[item.name] = item.value?.trim();
        });

        // nếu user chọn phỏng vấn online thì không quan tâm address,
        // nếu offline thi check xem address điền chưa, nếu chưa thì không cho lưu
        if (
            interviewDataObj.interviewType === 'OFFLINE'
            && (!interviewDataObj.interviewAddress || interviewDataObj.interviewAddress?.trim()?.length === 0)
        ) {
            $("#interviewAddress-error-custom").toggleClass("d-none");
            $("#interviewAddress-error-custom").toggleClass("error");
            // $("#interviewAddress-error-custom").css("display", "block !important");
            return;
        }

        $("#save-interview-btn").prop("disabled", true);
        $("#interview-saving-spinner").toggleClass("d-none");

        await $.ajax({
            url: "/api/v1/interviews",
            type: "POST",
            data: JSON.stringify({
                applicationId,
                ...interviewDataObj
            }),
            contentType: "application/json; charset=utf-8",
            success: function () {
                showToast("Interview posted successfully", SUCCESS_TOAST);
                window.location.href = "/companies/applications"; // Chuyển về danh sách công việc
                localStorage.removeItem("applicationId");
            },
            error: function () {
                showToast("Failed to post interview", ERROR_TOAST);
            }
        });

        $("#save-interview-btn").prop("disabled", false);
        $("#interview-saving-spinner").toggleClass("d-none");
    });

});


