$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    interviewId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng

    if (!interviewId) {
        window.href = '/404';
    }
    await loadInterviewDetails(interviewId);
});

let interviewId;

let interview = null;

let ACCEPT_CANDIDATE_BUTTON =
    "<span role='button' class='btn btn-success me-2 btn-accept-candidate' data-id='{{interview.id}}' data-bs-toggle='tooltip' title='Accept candidate after interview'>" +
    "    <i class='fa-solid fa-square-check'></i> Accept candidate" +
    "</span>";
let REJECT_CANDIDATE_BUTTON =
    "<span role='button' class='btn btn-warning me-2 btn-reject-candidate text-danger' data-id='{{interview.id}}' data-bs-toggle='tooltip' title='Reject candidate after interview'>" +
    "    <i class='fa-solid fa-square-xmark'></i> Reject candidate" +
    "</span>";

let CANDIDATE_ABSENCE_BUTTON =
    "<span role='button' class='btn btn-warning me-2 btn-candidate-absence text-danger' data-id='{{interview.id}}' data-bs-toggle='tooltip' title='Candidate absence'>" +
    "    <i class=\"fa-solid fa-user-minus\" style='font-size: 12.5px'></i> Candidate absence" +
    "</span>";
let CANCEL_INTERVIEW =
    "<span role='button' class='btn btn-danger btn-cancel-interview me-2 btn-cancel' data-bs-toggle='tooltip' title='Cancel Interview' data-id='{{interview.id}}'>" +
    "    <i class='fa-solid fa-x'></i> Cancel interview" +
    "</span>";

async function loadInterviewDetails(interviewId) {
    try {
        interview = await $.ajax({
            url: `/api/v1/interviews/${interviewId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
        });
        renderInterviewDetails(interview);
    } catch (error) {
        if (error.status === 404) {
            window.location.href = "/404";
            return;
        }
        showToast(error.responseJSON.message, ERROR_TOAST);
    }
}

function renderInterviewDetails(interview) {
    // Xóa các nút cũ trong .application-buttons
    $(".interview-buttons").empty();

    // render các button
    renderButtons(interview);

    // render interview
    renderInterview(interview);

    // render candidate
    renderCandidate(interview);

    // render job
    renderJob(interview);

    $(".btn-accept-candidate").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const applicationId = target.attr("data-id");
        if (!applicationId) {
            showToast("ApplicationId ID not found", ERROR_TOAST);
            return;
        }
        await changeInterviewStatus(applicationId, "PASSED", "Are you sure to accept this candidate?");
    });

    $(".btn-reject-candidate").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const interviewId = target.attr("data-id");
        if (!interviewId) {
            showToast("Interview ID not found", ERROR_TOAST);
            return;
        }
        await changeInterviewStatus(interviewId, "FAILED", "Are you sure to reject this candidate after interview?");
    });

    $(".btn-candidate-absence").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const interviewId = target.attr("data-id");
        if (!interviewId) {
            showToast("Interview ID not found", ERROR_TOAST);
            return;
        }
        await changeInterviewStatus(interviewId, "CANDIDATE_ABSENCE", "Are you sure the candidate is absence ?");
    });

    $(".btn-cancel-interview").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const interviewId = target.attr("data-id");
        if (!interviewId) {
            showToast("Interview ID not found", ERROR_TOAST);
            return;
        }
        await changeInterviewStatus(interviewId, "CANCELLED", "Are you sure to cancel this interview ?");
    });

}

function renderButtons(interview) {
    let buttons = "";
    switch (interview.status) {
        case "CREATED":
            buttons += ACCEPT_CANDIDATE_BUTTON + REJECT_CANDIDATE_BUTTON + CANDIDATE_ABSENCE_BUTTON + CANCEL_INTERVIEW;
            break;
        case "PASSED":
            buttons += REJECT_CANDIDATE_BUTTON;
            break;
        case "FAILED":
            buttons += ACCEPT_CANDIDATE_BUTTON;
            break;
        case "CANDIDATE_ABSENCE":
            buttons += "";
            break;
        case "CANCELLED":
            buttons += "";
            break;
    }
    buttons = buttons
        .replaceAll("{{interview.id}}", interview.id)
    $(".interview-buttons").append($(buttons));
}

function renderCandidate(interview) {
    const candidate = interview.application.candidate;

    $("#name").text(candidate.name);
    $("#candidateNameLink").attr("href", "/companies/candidates/" + candidate.id);
    $("#phone").text(candidate.phone);
    $("#dob").text(candidate.dob);
    $("#gender").text(decodeGender(candidate.gender));
    $("#yearOfExperience").text(candidate.yearOfExperience);
    $("#literacy").text(decodeJobLiteracy(candidate.literacy));
    $("#graduatedAt").text(candidate.graduatedAt);
}

function renderJob(interview) {
    const job = interview.application.job;

    $("#jobName").text(job.name);
    $("#jobNameLink").attr("href", "/companies/jobs/" + job.id);
    $("#salary").text(job.salaryTo ? `${job.salaryFrom} - ${job.salaryTo}` : job.salaryFrom);
    $("#workingType").text(decodeJobWorkingType(job.workingType));
    $("#workingTimeType").text(decodeJobWorkingTimeType(job.workingTimeType));
    $("#position").text(job.position);
}

function renderInterview(interview) {

    if (!interview) {
        $("#interview-block").hide();
        return;
    }

    $("#invitationEmailSentAt").text(dateTimeFormat(new Date(interview?.invitationEmailSentAt)));
    $("#interviewAt").text(dateTimeFormat(new Date(interview?.interviewAt)));
    $("#interviewStatus").text(decodeInterviewStatus(interview?.status));
    $("#interviewType").text(decodeJobWorkingType(interview?.interviewType));
    $("#interviewAddress").text(interview?.interviewAddress);
    $("#note").text(interview?.note);

    // Xử lý sự kiện click vào nút Save
    $("#save-interview-btn").off("click").click(async function (event) {
        const interviewId = interview?.id;
        const noteContent = $("#note").val().trim(); // Lấy nội dung note

        if (!interviewId) {
            showToast("Interview ID not found!", ERROR_TOAST);
            return;
        }

        $("#interview-saving-spinner").removeClass("d-none"); // Hiển thị spinner

        await $.ajax({
            url: "/api/v1/interviews/" + interviewId + "/note",
            type: "PATCH",
            data: JSON.stringify({
                note: noteContent
            }),
            contentType: "application/json; charset=utf-8",
            success: function () {
                showToast("Note saved successfully!", SUCCESS_TOAST);
                loadInterviewDetails(interviewId);
            },
            error: function (error) {
                showToast(error.responseJSON?.message || "Failed to save note!", ERROR_TOAST);
            },
        });

        $("#interview-saving-spinner").addClass("d-none"); // Ẩn spinner sau khi xử lý xong
    });
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

function decodeJobWorkingType(workingType) {
    switch (workingType) {
        case "OFFLINE":
            return "Offline";
        case "ONLINE":
            return "Online";
    }
}

function decodeJobWorkingTimeType(workingTimeType) {
    switch (workingTimeType) {
        case "FULL_TIME":
            return "Full time";
        case "PART_TIME":
            return "Part time";
    }
}

function decodeGender(gender) {
    switch (gender) {
        case "MALE":
            return "Male";
        case "FEMALE":
            return "Female";
        case "OTHER":
            return "Other";
    }
}

function decodeApplicationStatus(status) {
    switch (status) {
        case "APPLIED":
            return "Applied";
        case "CANCELLED":
            return "Cancelled";
        case "APPLICATION_ACCEPTED":
            return "Application accepted";
        case "APPLICATION_REJECTED":
            return "Application rejected";
        case "WAIT_FOR_INTERVIEW":
            return "Wait for interview";
        case "CANDIDATE_ACCEPTED":
            return "Candidate accepted";
        case "CANDIDATE_REJECTED":
            return "Candidate rejected";
    }
}

function decodeInterviewStatus(status) {
    switch (status) {
        case "CREATED":
            return "Created";
        case "PASSED":
            return "Passed";
        case "FAILED":
            return "Failed";
        case "CANDIDATE_ABSENCE":
            return "Candidate absence";
        case "CANCELLED":
            return "Cancelled";
    }
}

async function changeInterviewStatus(interviewId, targetStatus, confirmationMessage) {
    if (!confirm(confirmationMessage)) {
        return;
    }
    await $.ajax({
        url: "/api/v1/interviews/" + interviewId + "/status",
        type: "PATCH",
        data: JSON.stringify({
            status: targetStatus
        }),
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            showToast("Successfully", SUCCESS_TOAST);
            loadInterviewDetails(interview.id);
        },
        error: function () {
            showToast("Failed", ERROR_TOAST);
        }
    });
}