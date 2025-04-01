$(document).ready(async function () {

    const account = JSON.parse(localStorage.getItem("account"));

    // Ẩn các nút nếu chưa login hoặc là company
    if (!account || account.role === COMPANY_ROLE) {
        $(".apply-btn").addClass("d-none");
        $(".favorite-btn").addClass("d-none");
    }

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    const jobId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng

    if (!jobId) {
        window.href = '/404';
    }

    await loadJobDetails(jobId);

    $(".apply-btn").click(function () {
        const account = JSON.parse(localStorage.getItem("account"));
        if (!account) {
            location.href = "/login";
            return;
        }
        console.log(window.location.pathname);
        location.href = window.location.pathname + "/application";
    });

});

// Hàm load dữ liệu chi tiết của job từ API
async function loadJobDetails(jobId) {
    try {
        const job = await $.ajax({
            url: `/api/v1/jobs/${jobId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
        });
        renderJobDetails(job);
    } catch (error) {
        if (error.status === 404) {
            window.location.href = "/404";
            return;
        }
        showToast(error.responseJSON.message, ERROR_TOAST);
    }
}

// Render thông tin chi tiết của job vào HTML
function renderJobDetails(job) {

    job.description = job.description.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");
    job.requirement = job.requirement.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");
    job.benefit = job.benefit.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");
    job.skills = job.skills.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");

    for (let key in job) {
        $("#" + key).html(job[key]);
    }
    $("#yearOfExperience").text(job.yearOfExperienceTo ? `${job.yearOfExperienceFrom} - ${job.yearOfExperienceTo}` : job.yearOfExperienceFrom);
    $("#salary").text(job.salaryTo ? `${job.salaryFrom} - ${job.salaryTo}` : job.salaryFrom);
    $("#workingTimeType").text(decodeJobWorkingTimeType(job.workingTimeType));
    $("#workingType").text(decodeJobWorkingType(job.workingType));
    $("#literacy").text(decodeJobLiteracy(job.literacy));
    $("#level").text(decodeJobLevel(job.level));
    $("#category").text(job.category?.name);
    $("#workingCity").text(job.workingCity?.name);
    $("#job-urgent").toggleClass(job.urgent ? 'd-block' : 'd-none');


    $(".favorite-btn").toggleClass(job.favorite ? 'favorite-job' : '');
    $(".favorite-btn").attr("job-id", job.id);
    $(".favorite-btn").attr("favorite", job.favorite ? 1 : 0);

    // company info
    const company = job.company;
    const alias = company.alias ? " (" + company.alias + ")" : "";
    $(".company-widget .company-logo img").attr("src", company.avatarUrl ? "/api/v1/files/avatar/" + company.avatarUrl : DEFAULT_AVATAR_URL);
    $(".company-widget .company-name").text(company?.name + alias);
    $(".company-widget .profile-link").attr("href", "/companies/" + company.id);
    $(".company-widget .company-info .company-size span").text(company.employeeQuantity);
    $(".company-widget .company-info .company-created-at span").text(new Date(company.createdAt).getFullYear());
    $(".company-widget .company-info .phone span").text(company.phone);
    $(".company-widget .company-info .email span").text(company.email);
    $(".company-widget .company-info .location span").text(company.headQuarterAddress);
    $(".company-widget .company-website a").attr("href", company.website);
    $(".company-widget .company-website a").text(company.website);

    const application = job?.application;
    if (application) {
        $(".apply-btn").toggleClass("d-none");
        if (application?.status === 'APPLIED') {
            $(".cancel-application-btn").toggleClass("d-none");
        }
    }

    $(".cancel-application-btn").click(async function () {
        const confirmResult = confirm("Do you want to cancel this application?");
        if (!confirmResult) {
            return;
        }

        const account = JSON.parse(localStorage.getItem("account"));
        if (!account) {
            location.href = "/login";
            return;
        }
        const applicationId = job?.application.id;
        try {
            await $.ajax({
                url: `/api/v1/applications/${applicationId}/status`,
                type: "PATCH",
                data: JSON.stringify({
                    status: "CANCELLED"
                }),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                    $(".apply-btn").toggleClass("d-none");
                    $(".cancel-application-btn").toggleClass("d-none");
                    showToast("Cancel application successful", SUCCESS_TOAST);
                },
                error: function (err) {
                    showToast("Cancel application  failed", ERROR_TOAST);
                }
            });
        } catch (error) {
            console.error("Error cancel application:", error);
        }
    });


    //  favorite
    $(".favorite-btn").off("click").click(async function (event) {
        const account = JSON.parse(localStorage.getItem("account"));
        if (!account) {
            location.href = "/login";
            return;
        }

        const target = $(event.currentTarget);
        const jobId = target.attr("job-id");
        const favorite = target.attr("favorite"); // đã favorite chưa
        try {
            await $.ajax({
                url: '/api/v1/favourite-jobs',
                type: favorite == 1 ? 'DELETE' : 'POST',
                data: JSON.stringify({jobId}),
                contentType: 'application/json; charset=utf-8',
            });
            showToast((favorite == 1 ? 'Remove from' : 'Add to') + " favorite successfully", SUCCESS_TOAST);
            location.reload();
        } catch (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });

}

function decodeJobWorkingTimeType(workingTimeType) {
    switch (workingTimeType) {
        case "FULL_TIME":
            return "Full time";
        case "PART_TIME":
            return "Part time";
    }
}

function decodeJobWorkingType(workingType) {
    switch (workingType) {
        case "OFFLINE":
            return "Offline";
        case "ONLINE":
            return "Online";
    }
}

function decodeJobLiteracy(literacy) {
    switch (literacy) {
        case "PROFESSOR":
            return "Professor";
        case "DOCTOR":
            return "Doctor";
        case "MASTER":
            return "Master";
        case "UNIVERSITY":
            return "University";
        case "COLLEGE":
            return "College";
        case "HIGH_SCHOOL":
            return "High School";
    }
}

function decodeJobLevel(level) {
    switch (level) {
        case "INTERN":
            return "Intern";
        case "JUNIOR":
            return "Junior";
        case "FRESHER":
            return "Fresher";
        case "SENIOR":
            return "Senior";
        case "MASTER":
            return "Master";
    }
}