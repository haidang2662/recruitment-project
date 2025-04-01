$(document).ready(function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    candidateId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng

    if (!candidateId) {
        window.href = '/404';
    }
    loadCandidateDetails(candidateId);
    getApplications(candidateId);
});

let candidateId;
let candidate = null;
let totalRecord;
let paging;
let pageIndex = 0;
let pageSize = 10;

async function loadCandidateDetails(candidateId) {
    try {
        candidate = await $.ajax({
            url: `/api/v1/candidates/${candidateId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
        });
        renderCandidateDetails(candidate);
    } catch (error) {
        if (error.status === 404) {
            window.location.href = "/404";
            return;
        }
        showToast(error.responseJSON.message, ERROR_TOAST);
    }
}

function renderCandidateDetails(candidate) {

    $("#name").text(candidate.name);
    $("#phone").text(candidate.phone);
    $("#skills").text(candidate.skills);
    $("#yearOfExperience").text(candidate.yearOfExperience);
    $("#address").text(candidate.address);
    $("#currentJobPosition").text(candidate.currentJobPosition);
    $("#dob").text(candidate.dob);
    $("#email").text(candidate.email);
    $("#salary").text(candidate.expectedSalaryTo ? `${candidate.expectedSalaryFrom} - ${candidate.expectedSalaryTo}` : candidate.expectedSalaryFrom);
    $("#expectedWorkingTimeType").text(decodeJobWorkingTimeType(candidate.expectedWorkingTimeType));
    $("#expectedWorkingType").text(decodeJobWorkingType(candidate.expectedWorkingType));
    $("#gender").text(decodeGender(candidate.gender));
    $("#graduatedAt").text(candidate.graduatedAt);
    $("#literacy").text(decodeJobLiteracy(candidate.literacy));
}

async function getApplications(candidateId) {
    // Disable nút search và hiển thị spinner
    $("#search-job-btn").prop("disabled", true);
    $("#spinner-search").removeClass('d-none');
    $(".page-item .page-link").addClass('disabled');

    // Hiển thị spinner ở bảng
    $("#job-table tbody").html('<tr><td colspan="9" class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></td></tr>');

    await $.ajax({
        url: "/api/v1/applications",
        type: "GET",
        data: {
            pageIndex: pageIndex,
            pageSize: pageSize,
            candidateId
        },
        contentType: "application/json; charset=utf-8",
        success: function (data) {

            renderApplicationTable(data);

        },
        error: function () {
            showToast("Get application failed", ERROR_TOAST);
        }
    });

    // Ẩn spinner và kích hoạt lại nút search
    $("#spinner-search").addClass('d-none');
    $("#search-job-btn").prop("disabled", false);
    $(".page-item .page-link").removeClass('disabled');
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

function renderApplicationTable(data) {
    const paginationHtml = $("#application-paging .pagination");
    const tableContent = $("#application-table tbody");
    const totalRecordHtml = $(".total-record");

    tableContent.empty();
    paginationHtml.empty();
    totalRecordHtml.empty();
    if (!data) {
        return;
    }

    const applications = data.data;
    totalPage = data.totalPage;
    totalRecord = data.totalRecord;
    paging = data.pageInfo;
    pageIndex = paging.pageNumber;

    if (!applications || applications.length === 0) {
        return;
    }

    for (let i = 0; i < applications.length; i++) {
        applications[i]['stt'] = pageIndex * pageSize + i + 1;
    }


    for (let i = 0; i < applications.length; i++) {
        const application = applications[i];
        // application?.job?.name

        let tr = "<tr>" +
            "<td>" + application.stt + "</td>" +
            "<td><a href='/companies/jobs/" + application?.job?.id + "'>" + application?.job?.name + "</a></td>" +
            "<td>" + application?.candidate?.name + "</td>" +
            "<td>" + dateTimeFormat(new Date(application?.appliedDate)) + "</td>" +
            "<td>" + decodeApplicationStatus(application?.status) + "</td>" +
            "<td>" +
            "<div class='action-icons d-flex align-items-center'>" + // Thêm container Flexbox

            getApplicationActionButtons(application) +
            "</div>" +
            "</td>" +
            "</tr>";

        tableContent.append(tr);

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
            location.href = "/companies/interviews/creation";
        });

        $(".btn-detail").off("click").click(async function (event) {
            const target = $(event.currentTarget);
            const applicationId = target.attr("data-id");
            if (!applicationId) {
                showToast("ApplicationId ID not found", ERROR_TOAST);
                return;
            }
            location.href = "/companies/applications/" + applicationId;
        });

    }

    paginationHtml.append("<li class=\"page-item go-to-first-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-angles-left\"></i></a></li>");
    paginationHtml.append("<li class=\"page-item previous-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-chevron-left\"></i></a></li>");
    for (let i = 1; i <= totalPage; i++) {
        const page = "<li class='page-item " + (i === paging.pageNumber + 1 ? "active" : '') + "' page='" + (i - 1) + "'><a class='page-link' href='#'>" + i + "</a></li>";
        paginationHtml.append(page);
    }

    paginationHtml.append("<li class=\"page-item next-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-chevron-right\"></i></a></li>");
    paginationHtml.append("<li class=\"page-item go-to-last-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-angles-right\"></i></a></li>");


    totalRecordHtml.append("<span><span class='fw-bold'>Total records</span>: " + totalRecord + "</span>")

    // Xóa sự kiện cũ trước khi thêm sự kiện mới
    $(".page-item").off("click").click(async function (event) {
        const newPageIndex = $(event.currentTarget).attr("page");
        if (!newPageIndex || isNaN(newPageIndex)) {
            return;
        }
        pageIndex = parseInt(newPageIndex);
        await getApplications({});
    });

    $(".go-to-first-page").click(async function () {
        pageIndex = 0;
        await getApplications({});
    });

    $(".go-to-last-page").click(async function () {
        pageIndex = totalPage - 1;
        await getApplications({});
    });

    $(".previous-page").click(async function () {
        if (pageIndex === 0) {
            return;
        }
        pageIndex = pageIndex - 1;
        await getApplications({});
    });

    $(".next-page").click(async function () {
        if (pageIndex === totalPage - 1) {
            return;
        }
        pageIndex = pageIndex + 1;
        await getApplications({});
    });
}

function getApplicationActionButtons(application) {
    const cvId = application.cv.id;
    const DOWNLOAD_BUTTON = `
    <span role="button" class="text-primary me-2 download-btn" 
          data-cv-id="${cvId}" data-bs-toggle="tooltip" 
          title="Download CV" data-id="${application.id}">
        <i class="fa-solid fa-download"></i>
    </span>`;

    const REJECT_CV_BUTTON =
        "<span role='button' class='text-danger btn-delete me-2 btn-reject' data-bs-toggle='tooltip' title='Reject CV' data-id='" + application.id + "'>" +
        "    <i class='fa-solid fa-x'></i>" +
        "</span>";
    const ACCEPT_CV_BUTTON =
        "<span role='button' class='text-success me-2 btn-accept' data-id='" + application.id + "' data-bs-toggle='tooltip' title='Accept CV'>" +
        "    <i class='fa-solid fa-check'></i> " +
        "</span>";
    const REJECT_AFTER_APPOINTMENT_BUTTON =
        "<span role='button' class='text-warning me-2 btn-interview-reject text-danger' data-id='" + application.id + "' data-bs-toggle='tooltip' title='Reject after interview'>" +
        "    <i class='fa-solid fa-square-xmark'></i> " +
        "</span>";
    const APPOINTMENT_BUTTON =
        "<span role='button' class='text-secondary text-warning me-2 btn-interview-schedule' data-id='" + application.id + "' data-bs-toggle='tooltip' title='Make an interview'>" +
        "    <i class='fa-solid fa-clock'></i> " +
        "</span>";
    const ACCEPT_CANDIDATE_BUTTON =
        "<span role='button' class='text-secondary me-2 btn-interview-accept text-success' data-id='" + application.id + "' data-bs-toggle='tooltip' title='Accept candidate after interview'>" +
        "    <i class='fa-solid fa-square-check'></i> " +
        "</span>";
    const INFORMATION_APPLICATION_BUTTON =
        "<span role='button' class='text-secondary me-2 btn-detail' data-cv-id=\"${cvId}\" data-id='" + application.id + "' data-bs-toggle='tooltip' title='Information application'>" +
        "    <i class=\"fa-solid fa-eye\"></i> " +
        "<a href='/api/v1/applications" + application.id + "'></a>" +
        "</span>";

    let buttons = INFORMATION_APPLICATION_BUTTON + DOWNLOAD_BUTTON;
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

    return buttons;
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
        success: function () {
            showToast("Successfully", SUCCESS_TOAST);
            getApplications({});
        },
        error: function () {
            showToast("Failed", ERROR_TOAST);
        }
    });
}



