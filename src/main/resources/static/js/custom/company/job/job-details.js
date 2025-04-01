$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    const jobId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng

    if (!jobId) {
        window.href = '/404';
    }


    await loadJobDetails(jobId);
});

const UPDATE_BUTTON = `
    <button type="button" class="btn btn-primary me-2 btn-update" data-id="job-id">
        <i class="fa-solid fa-pencil"></i> Update
    </button>
`;
const DELETE_BUTTON = `
    <button type="button" class="btn btn-danger me-2 btn-delete" data-id="job-id">
        <i class="fa-solid fa-trash"></i> Delete
    </button>
`;
const PUBLISH_BUTTON = `
    <button type="button" class="btn btn-success me-2 btn-publish" data-id="job-id">
        <i class="fa-solid fa-check"></i> Publish
    </button>
`;
const UNPUBLISHED_BUTTON = `
    <button type="button" class="btn btn-secondary me-2 btn-unpublish" data-id="job-id">
        <i class="fa-solid fa-x"></i> Unpublished
    </button>
`;
const EXPIRE_BUTTON = `
    <button type="button" class="btn btn-warning me-2 btn-expire" data-id="job-id">
        <i class="fa-solid fa-clock"></i> Expire
    </button>
`;

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
    // Xóa các nút cũ trong .job-buttons
    $(".job-buttons").empty();

    let buttons = "";
    switch (job.status) {
        case "DRAFT":
            buttons = UPDATE_BUTTON + DELETE_BUTTON + PUBLISH_BUTTON;
            break;
        case "PUBLISH":
            buttons = UNPUBLISHED_BUTTON + EXPIRE_BUTTON;
            break;
        case "UNPUBLISHED":
            buttons = UPDATE_BUTTON + PUBLISH_BUTTON;
            break;
        case "EXPIRED":
            buttons = UPDATE_BUTTON + PUBLISH_BUTTON;
            break;
    }
    buttons = buttons.replaceAll("job-id", job.id);
    $(".job-buttons").append($(buttons));

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
    $("#status").text(decodeJobStatus(job.status));
    $("#level").text(decodeJobLevel(job.level));
    $("#category").text(job.category.name);
    $("#workingCity").text(job.workingCity.name);
    $("#job-urgent").toggleClass(job.urgent ? 'd-block' : 'd-none');

    //  EXPIRED
    $(".btn-expire").off("click").click(async function (event) {
        const toggleInput = $(event.currentTarget);
        const jobId = toggleInput.attr("data-id");
        try {
            await $.ajax({
                url: '/api/v1/jobs/' + jobId + '/status',
                type: 'PATCH',
                data: JSON.stringify({status: JOB_STATUS.EXPIRED}),
                contentType: 'application/json; charset=utf-8',
            });
            showToast("Job marked as expired successfully", SUCCESS_TOAST);
            await loadJobDetails(jobId); // Cập nhật lại giao diện
        } catch (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });

    //  PUBLISH
    $(".btn-publish").off("click").click(async function (event) {
        const toggleInput = $(event.currentTarget);
        const jobId = toggleInput.attr("data-id");

        try {
            await $.ajax({
                url: '/api/v1/jobs/' + jobId + '/status',
                type: 'PATCH',
                data: JSON.stringify({status: JOB_STATUS.PUBLISH}),
                contentType: 'application/json; charset=utf-8',
            });
            showToast("Job marked as publish successfully", SUCCESS_TOAST);
            await loadJobDetails(jobId); // Cập nhật lại giao diện
        } catch (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });

    // UNPUBLISHED
    $(".btn-unpublish").off("click").click(async function (event) {
        const toggleInput = $(event.currentTarget);
        const jobId = toggleInput.attr("data-id");

        try {
            await $.ajax({
                url: '/api/v1/jobs/' + jobId + '/status',
                type: 'PATCH',
                data: JSON.stringify({status: JOB_STATUS.UNPUBLISHED}),
                contentType: 'application/json; charset=utf-8',
            });
            showToast("Job marked as unpublished successfully", SUCCESS_TOAST);
            await loadJobDetails(jobId); // Cập nhật lại giao diện
        } catch (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });

    // DELETE
    $(".btn-delete").off("click").click(async function (event) {
        const jobId = $(event.currentTarget).attr("data-id"); // Lấy job ID từ thuộc tính id

        const confirmResult = confirm("Do you want to delete this job?");
        if (!confirmResult) {
            return;
        }

        try {
            await $.ajax({
                url: '/api/v1/jobs/' + jobId,
                type: 'DELETE',
                contentType: "application/json; charset=utf-8",
            });
            showToast("Delete job successfully", SUCCESS_TOAST);
            // Gọi lại hàm để cập nhật giao diện sau khi xóa
            await loadJobDetails(jobId); // Cập nhật lại giao diện
        } catch (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });

    // Update
    $(".btn-update").off("click").click(async function (event) {
        const jobId = $(event.currentTarget).attr("data-id");

        window.location.href = `/companies/jobs/job-updating/${jobId}`;
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

function decodeJobStatus(status) {
    switch (status) {
        case "DRAFT":
            return "Draft";
        case "PUBLISH":
            return "Publish";
        case "UNPUBLISHED":
            return "Unpublished";
        case "EXPIRED":
            return "Expired";
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