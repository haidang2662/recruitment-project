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

    $.validator.addMethod("greaterThanSalaryFrom", function (value, element) {
        const salaryFrom = parseFloat($("input[name='salaryFrom']").val());
        const salaryTo = parseFloat(value);

        if (!salaryFrom || !salaryTo) return true; // Nếu 1 trong 2 chưa nhập thì không kiểm tra

        return salaryTo >= salaryFrom;
    }, "Salary to must be greater than to salary from");


    $("#post-job-form").validate({
        onfocusout: false,
        onkeyup: false,
        onclick: false,
        rules: {
            "name": {
                required: true,
                maxlength: 50,
            },
            "position": {
                required: true,
                maxlength: 50,
            },
            "yearOfExperienceFrom": {
                required: true,
                min: 1,
            },
            "workingAddress": {
                required: true,
                maxlength: 50,
            },
            "expiredDate": {
                required: true,
                futureDate: true,
            },
            "recruitingQuantity": {
                required: true,
                min: 1,
            },
            "skills": {
                required: true,
                maxlength: 1000,
            },
            "benefit": {
                required: true,
                maxlength: 5000,
            },
            "requirement": {
                required: true,
                maxlength: 5000,
            },
            "salaryFrom": {
                required: true,
                min: 1,
            },
            "salaryTo": {
                required: true,
                min: 1,
                greaterThanSalaryFrom: true ,
            },
            "description": {
                required: true,
                maxlength: 5000,
            },
        },
        messages: {
            "name": {
                required: "Job title is required.",
                maxlength: "Job title must not exceed 50 characters.",
            },
            "position": {
                required: "Position is required.",
                maxlength: "Position must not exceed 50 characters.",
            },
            "yearOfExperienceFrom": {
                required: "Year Of Experience From is required.",
                min: "Year Of Experience From must be greater than or equal to 1"
            },
            "workingAddress": {
                required: "Working Address is required.",
                maxlength: "Working Address must not exceed 50 characters.",
            },
            "recruitingQuantity": {
                maxlength: "Recruiting Quantity must not exceed 50 characters.",
                min: "Recruiting Quantity must be greater than or equal to 1",
            },
            "expiredDate": {
                required: "Expired date is required.",
                futureDate: "The expiration date must be in the future",
            },
            "skills": {
                required: "Skills is required.",
                maxlength: "Skills must not exceed 1000 characters.",
            },
            "benefit": {
                required: "Benefit is required.",
                maxlength: "Benefit must not exceed 5000 characters.",
            },
            "requirement": {
                required: "Requirement is required.",
                maxlength: "Requirement must not exceed 5000 characters.",
            },
            "salaryFrom": {
                maxlength: "Salary from must not exceed 50 characters.",
                min: "Salary from must be greater than or equal to 1",
                greaterThanSalaryFrom: "Salary to must be greater than to Salary from",
            },
            "salaryTo": {
                maxlength: "Salary to must not exceed 50 characters.",
                min: "Salary to must be greater than or equal to 1",
            },
            "description": {
                required: "Description is required.",
                maxlength: "Description must not exceed 5000 characters.",
            },
        }
    });

    renderJobCategories();

    renderLocations();

    // Lấy jobId từ URL path
    const path = window.location.pathname;
    let jobId = null;
    if (path.startsWith("/companies/jobs/job-updating")) {
        const pathParts = window.location.pathname.split('/');
        jobId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng
    }

    // Thay đổi tiêu đề form dựa trên jobId
    if (jobId) {
        // Chế độ cập nhật
        await getJobDetails(jobId);
        $("#form-title").text("Update Job");
    } else {
        // Chế độ tạo mới
        $("#form-title").text("Post Job");
    }

    // Xử lý sự kiện khi nhấn nút Save
    $("#save-job-btn").click(async function () {
        const isValidForm = $("#post-job-form").valid();
        if (!isValidForm) {
            return;
        }

        $("#save-job-btn").prop("disabled", true);
        $("#job-saving-spinner").toggleClass("d-none");

        // Thu thập dữ liệu từ form
        const formData = $("#post-job-form").serializeArray();
        const jobData = {};
        formData.forEach(item => jobData[item.name] = item.value?.trim());
        jobData['categoryId'] = jobData['category'];
        jobData['workingCityId'] = jobData['locations'];
        jobData['urgent'] = $("#post-job-form input[name='urgent']").is(":checked");

        if (jobId) {
            try {
                await $.ajax({
                    url: `/api/v1/jobs/${jobId}`,
                    type: "PUT",
                    data: JSON.stringify(jobData),
                    contentType: "application/json; charset=utf-8",
                });
                showToast("Job updated successfully", SUCCESS_TOAST);
                await getJobDetails(jobId); // Tải lại dữ liệu
                window.location.href = `/companies/jobs/${jobId}`; // Chuyển về chi tiết công việc
            } catch (error) {
                showToast("Failed to update job", ERROR_TOAST);
            }
        } else {
            // Nếu không có jobId -> Tạo mới công việc
            try {
                await $.ajax({
                    url: "/api/v1/jobs",
                    type: "POST",
                    data: JSON.stringify(jobData),
                    contentType: "application/json; charset=utf-8",
                });
                showToast("Job posted successfully", SUCCESS_TOAST);
                window.location.href = "/companies/jobs"; // Chuyển về danh sách công việc
            } catch (error) {
                showToast("Failed to post job", ERROR_TOAST);
            }
        }

        $("#save-job-btn").prop("disabled", false);
        $("#job-saving-spinner").toggleClass("d-none");
    });

});

// Hàm tải dữ liệu job từ API và điền vào form
async function getJobDetails(jobId) {
    try {
        const job = await $.ajax({
            url: `/api/v1/jobs/${jobId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
        });

        // Lặp qua các key trong object job và gán giá trị trực tiếp vào form
        for (const key in job) {
            const field = $(`[name='${key}']`);
            // Nếu tìm thấy field tương ứng trong form
            if (field.length > 0 && key !== "workingCity" && key !== "category") {
                field.val(job[key]);
            }
        }

        // Xử lý riêng cho trường workingCity
        if (job.workingCity && job.workingCity.id) {
            setTimeout(() => {
                $("#location").val(job.workingCity.id).trigger("chosen:updated");
            }, 300);
        }
        // Xử lý riêng cho trường category
        if (job.category && job.category.id) {
            setTimeout(() => {
                $("#job-categories").val(job.category.id).trigger("chosen:updated");
            }, 300);
        }

    } catch (err) {
        if (err.status === 404) {
            window.location.href = "/404";
            return;
        }
        showToast("Failed to load job details", ERROR_TOAST);
    }
}

function renderJobCategories() {
    $.ajax({
        url: '/api/v1/job-categories',
        method: 'GET',
        success: function (response) {
            const jobCategorySelect = $('#job-categories');
            jobCategorySelect.empty();

            response.forEach(function (category) {
                jobCategorySelect.append(`<option value="${category.id}">${category.name}</option>`);
            });

            if ($.fn.chosen) {
                jobCategorySelect.trigger("chosen:updated");
            }
        },
        error: function (error) {
            showToast("Error fetching job categories", ERROR_TOAST);
        }
    });
}

function renderLocations() {
    $.ajax({
        url: '/api/v1/locations',
        method: 'GET',
        success: function (response) {
            const locationSelect = $('#location');
            locationSelect.empty();
            response.forEach(function (location) {
                // Thêm từng địa điểm vào thẻ <select> dưới dạng thẻ <option>
                locationSelect.append(`<option value="${location.id}">${location.name}</option>`);
            });

            if ($.fn.chosen) { //xem plugin Chosen đã được tích hợp trong dự án chưa.
                locationSelect.trigger("chosen:updated"); // Thông báo cho thẻ select đã thay đổi , plugin sẽ tự động làm mới giao diện
            }
        },
        error: function (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });
}
