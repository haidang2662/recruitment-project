$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    const applicationId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng

    if (!applicationId) {
        window.href = '/404';
    }

    await loadApplicationDetails(applicationId);
});

let application = null;

let DOWNLOAD_BUTTON = `
    <span role="button" class="btn btn-primary download-btn m-0 me-2" 
          data-cv-id="{{cvId}}" data-bs-toggle="tooltip" 
          title="Download CV" data-id="{{application.id}}">
        <i class="fa-solid fa-download" style="font-size: 10px"></i> Download CV
    </span>`;
let REJECT_CV_BUTTON =
    "<span role='button' class='btn btn-danger btn-delete me-2 btn-reject' data-bs-toggle='tooltip' title='Reject CV' data-id='{{application.id}}'>" +
    "    <i class='fa-solid fa-x'></i> Reject CV" +
    "</span>";
let ACCEPT_CV_BUTTON =
    "<span role='button' class='btn btn-success me-2 btn-accept' data-id='{{application.id}}' data-bs-toggle='tooltip' title='Accept CV'>" +
    "    <i class='fa-solid fa-check'></i> Accept CV" +
    "</span>";
let REJECT_AFTER_APPOINTMENT_BUTTON =
    "<span role='button' class='btn btn-warning me-2 btn-interview-reject' data-id='{{application.id}}' data-bs-toggle='tooltip' title='Reject after interview'>" +
    "    <i class='fa-solid fa-square-xmark'></i> Reject candidate" +
    "</span>";
let APPOINTMENT_BUTTON =
    "<span role='button' class='btn btn-secondary text-warning me-2 btn-interview-schedule' data-id='{{application.id}}' data-bs-toggle='tooltip' title='Make an interview'>" +
    "    <i class='fa-solid fa-clock'></i> Schedule interview" +
    "</span>";
let ACCEPT_CANDIDATE_BUTTON =
    "<span role='button' class='btn btn-success me-2 btn-interview-accept' data-id='{{application.id}}' data-bs-toggle='tooltip' title='Accept candidate after interview'>" +
    "    <i class='fa-solid fa-square-check'></i> Accept candidate" +
    "</span>";

async function loadApplicationDetails(applicationId) {
    try {
        application = await $.ajax({
            url: `/api/v1/applications/${applicationId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
        });

        renderApplicationDetails(application);
    } catch (error) {
        if (error.status === 404) {
            window.location.href = "/404";
            return;
        }
        showToast(error.responseJSON.message, ERROR_TOAST);
    }
}

function renderApplicationDetails(application) {
    // Xóa các nút cũ trong .application-buttons
    $(".application-buttons").empty();

    // render các button
    renderButtons(application);

    $("#application-status").text(decodeApplicationStatus(application.status));

    // render candidate
    renderCandidate(application);

    // render job
    renderJob(application);

    // render interview
    renderInterview(application);

    // Xử lý sự kiện tải CV khi nhấn nút Download
    $(".download-btn").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const cvId = target.attr("data-cv-id");
        if (!cvId) {
            showToast("CV ID not found", ERROR_TOAST);
            return;
        }
        $.ajax({
            url: '/api/v1/cv/' + cvId + "/download",
            type: 'GET',
            xhrFields: {
                responseType: 'blob' // to avoid binary data being mangled on charset conversion
            },
            success: function (blob, status, xhr) {
                let filename = "";
                const disposition = xhr.getResponseHeader('Content-Disposition');
                if (disposition && disposition.indexOf('attachment') !== -1) {
                    const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                    const matches = filenameRegex.exec(disposition);
                    if (matches != null && matches[1]) {
                        filename = matches[1].replace(/['"]/g, '');
                    }
                }
                const URL = window.URL || window.webkitURL;
                const downloadUrl = URL.createObjectURL(blob);

                if (filename) {
                    // use HTML5 a[download] attribute to specify filename
                    const a = document.createElement("a");
                    // safari doesn't support this yet
                    if (typeof a.download === 'undefined') {
                        window.location.href = downloadUrl;
                    } else {
                        a.href = downloadUrl;
                        a.download = filename;
                        document.body.appendChild(a);
                        a.click();
                        document.body.removeChild(a);
                        window.URL.revokeObjectURL(url);
                    }
                } else {
                    window.location.href = downloadUrl;
                }
            },
            error: function (err) {
                showToast("Download failed: " + error.message, ERROR_TOAST);
            }
        });
    });

    $(".btn-reject").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const applicationId = target.attr("data-id");
        if (!applicationId) {
            showToast("ApplicationId ID not found", ERROR_TOAST);
            return;
        }
        await changeApplicationStatus(applicationId, "APPLICATION_REJECTED", "Are you sure to reject this cv?");
    });

    $(".btn-accept").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const applicationId = target.attr("data-id");
        if (!applicationId) {
            showToast("ApplicationId ID not found", ERROR_TOAST);
            return;
        }
        await changeApplicationStatus(applicationId, "APPLICATION_ACCEPTED", "Are you sure to accept this cv?");
    });

    $(".btn-interview-reject").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const applicationId = target.attr("data-id");
        if (!applicationId) {
            showToast("ApplicationId ID not found", ERROR_TOAST);
            return;
        }
        await changeApplicationStatus(applicationId, "CANDIDATE_REJECTED", "Are you sure to reject this candidate after interview?");
    });

    $(".btn-interview-accept").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const applicationId = target.attr("data-id");
        if (!applicationId) {
            showToast("ApplicationId ID not found", ERROR_TOAST);
            return;
        }
        await changeApplicationStatus(applicationId, "CANDIDATE_ACCEPTED", "Are you sure to accept this candidate after interview?");
    });

    $(".btn-interview-schedule").off("click").click(async function (event) {
        const target = $(event.currentTarget);
        const applicationId = target.attr("data-id");
        if (!applicationId) {
            showToast("ApplicationId ID not found", ERROR_TOAST);
            return;
        }
        localStorage.setItem("applicationId", applicationId);
        location.href = "/companies/interviews";
    });

}

function renderButtons(application) {
    let buttons = DOWNLOAD_BUTTON;
    switch (application.status) {
        case "APPLIED":
            buttons += REJECT_CV_BUTTON + ACCEPT_CV_BUTTON;
            break;
        case "APPLICATION_ACCEPTED":
            buttons += APPOINTMENT_BUTTON + REJECT_CV_BUTTON;
            break;
        case "APPLICATION_REJECTED":
            buttons += ACCEPT_CV_BUTTON;
            break;
        case "WAIT_FOR_INTERVIEW":
            buttons += ACCEPT_CANDIDATE_BUTTON + REJECT_AFTER_APPOINTMENT_BUTTON;
            break;
        case "CANDIDATE_ACCEPTED":
            buttons += REJECT_AFTER_APPOINTMENT_BUTTON;
            break;
        case "CANDIDATE_REJECTED":
            buttons += ACCEPT_CANDIDATE_BUTTON;
            break;
    }
    buttons = buttons
        .replaceAll("{{application.id}}", application.id)
        .replaceAll("{{cvId}}", application.cv.id);
    $(".application-buttons").append($(buttons));
}

function renderCandidate(application) {
    const candidate = application.candidate;

    $("#name").text(candidate.name);
    $("#jobCandidateLink").attr("href", "/companies/candidates/" + candidate.id);
    $("#phone").text(candidate.phone);
    $("#dob").text(candidate.dob);
    $("#gender").text(decodeGender(candidate.gender));
    $("#yearOfExperience").text(candidate.yearOfExperience);
    $("#literacy").text(decodeJobLiteracy(candidate.literacy));
    $("#graduatedAt").text(candidate.graduatedAt);
}

function renderJob(application) {
    const job = application.job;

    $("#jobName").text(job.name);
    $("#jobNameLink").attr("href", "/companies/jobs/" + job.id);
    $("#salary").text(job.salaryTo ? `${job.salaryFrom} - ${job.salaryTo}` : job.salaryFrom);
    $("#workingType").text(decodeJobWorkingType(job.workingType));
    $("#workingTimeType").text(decodeJobWorkingTimeType(job.workingTimeType));
    $("#position").text(job.position);
}

function renderInterview(application) {
    const interview = application.interview;

    if (!interview) {
        $("#interview-block").hide();
        return;
    }
    console.log(application)
    console.log(interview)

    $("#invitationEmailSentAt").text(interview?.invitationEmailSentAt);
    $("#interviewLink").attr("href", "/companies/interviews/" + interview?.id);
    $("#interviewAt").text(interview?.interviewAt);
    $("#interviewStatus").text(decodeInterviewStatus(interview?.status));
    $("#interviewType").text(decodeJobWorkingType(interview?.interviewType));
    $("#interviewAddress").text(interview?.interviewAddress);
    $("#note").text(interview?.note);

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

async function changeApplicationStatus(applicationId, targetStatus, confirmationMessage) {
    if (!confirm(confirmationMessage)) {
        return;
    }
    await $.ajax({
        url: "/api/v1/applications/" + applicationId + "/status",
        type: "PATCH",
        data: JSON.stringify({
            status: targetStatus
        }),
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            showToast("Successfully", SUCCESS_TOAST);
            loadApplicationDetails(application.id);
        },
        error: function () {
            showToast("Failed", ERROR_TOAST);
        }
    });
}