$(document).ready(function () {

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 20;

    $("#notification-btn").click(function () {
        $.ajax({
            url: "/api/v1/notifications",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderNotificationTarget(data);
            },
            error: function () {
                // showToast("Get notification target failed", ERROR_TOAST);
            }
        });
    });
});

function renderNotificationTarget(data) {
    const notifications = data.data;
    const notificationList = $(".dropdown .notification-box");
    notificationList.empty();

    if (!notifications || notifications.length === 0) {
        if (pageIndex === 0) {
            notificationList.append("<div>Không có thông báo nào!</div>")
        }
        return;
    }

    // Duyệt qua các thông báo và tạo các thẻ a bằng vòng lặp for
    for (let i = 0; i < notifications.length; i++) {
        const notification = notifications[i];
        const metadata = JSON.parse(notification.metadata);
        let buttons = '';
        const cvId = metadata.cvId;
        const applicationId = metadata.applicationId;
        const interviewId = metadata.interviewId;
        const jobId = metadata.jobId;

        let redirectPath = '';

        switch (notification.topic) {
            // download cv , accept cv , reject cv (company)
            case WEBSOCKET_DESTINATIONS.NEW_APPLICATION_NOTIFICATION:
                buttons = `
                    <button class="text-primary me-1 download-cv-btn" data-cv-id="${cvId}" data-bs-toggle='tooltip' title='Download Cv'>
                        <i class=\"fa-solid fa-download\"></i>
                    </button>
                    <button class="text-success me-1 btn-accept" data-id="${applicationId}" data-bs-toggle='tooltip' title='Accept CV'>
                        <i class='fa-solid fa-check'></i>
                    </button>
                    <button class="text-danger btn-delete me-1 btn-interview-reject" data-id="${applicationId}" data-bs-toggle='tooltip' title='Reject CV'>
                        <i class='fa-solid fa-x'></i>
                    </button>
                `;
                redirectPath = '/companies/applications/' + applicationId;
                break;
            // xem lại thông tin buổi phỏng vấn , accept camdidate , reject candidate (company)
            case WEBSOCKET_DESTINATIONS.INTERVIEW_ACCEPTANCE_NOTIFICATION:
                buttons = `
                    <button class="text-primary me-1 btn-information-interview" data-id="${interviewId}" data-bs-toggle='tooltip' title='Interview Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                    <button class="text-success me-1 btn-accept-candidate" data-id="${interviewId}" data-bs-toggle='tooltip' title='Accept candidate after interview'>
                        <i class='fa-solid fa-square-check'></i>
                    </button>
                    <button class="text-danger me-1 btn-reject-candidate" data-id="${interviewId}" data-bs-toggle='tooltip' title='Reject candidate after interview'>
                        <i class='fa-solid fa-square-xmark'></i>
                    </button>
                `;
                break;
            // để nguyên k cần làm gì cả
            case WEBSOCKET_DESTINATIONS.INTERVIEW_REFUSAL_NOTIFICATION:
                break;
            // xem lại thông tin job đã ứng tuyển và thông báo check mail (candidate)
            case WEBSOCKET_DESTINATIONS.NEW_INTERVIEW_NOTIFICATION:
                buttons = `
                    <button class="text-primary me-1 btn-information-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Job Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                `;
                break;
            // xem lại thông tin job đã ứng tuyển (candidate)
            case WEBSOCKET_DESTINATIONS.CV_ACCEPTANCE_NOTIFICATION:
                buttons = `
                    <button class="text-primary me-1 btn-information-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Job Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                `;
                break;
            // để nguyên k cần làm gì cả
            case WEBSOCKET_DESTINATIONS.CV_REFUSAL_NOTIFICATION:
                break;
            // có nút application job (candidate)
            case WEBSOCKET_DESTINATIONS.EXPIRED_FAVORITE_JOB_NOTIFICATION:
                buttons = `
                    <button class="text-primary me-1 btn-apply-job" data-id="${jobId}" data-bs-toggle='tooltip' title=' Apply job '>
                        <i class="fa-solid fa-paper-plane"></i>
                    </button>
                `;
                break;
            case WEBSOCKET_DESTINATIONS.EXPIRED_JOB_NOTIFICATION:
                break;
            case WEBSOCKET_DESTINATIONS.ENOUGH_PASSED_CANDIDATE_NOTIFICATION:
                break;
        }

        const element = `
        <li class="notification-item pb-3" notification-target-id="${notification.id}">
            <a class="dropdown-item" data-href="${redirectPath}" notification-target-id="${notification.id}">
                <div class="row">
                    <div class="col">
                        <div class="row title" data-bs-toggle="tooltip" title="${notification.title}">
                            <div class="col text-truncate">
                                ${notification.title}  <!-- Sử dụng notification.title -->
                            </div>
                        </div>
                        <div class="row content text-secondary">
                            <div class="col-11">
                                ${notification.content}  <!-- Sử dụng notification.content -->
                            </div>
                            ${
            !notification.seen ?
                `<div class="col-1 seen-icon">
                                    <i class="fa-solid fa-circle text-primary"></i>
                                </div>` : ''
        }
                        </div>
                        <div class="row buttons">
                            <div class="col d-flex justify-content-end gap-2">
                                ${buttons}
                            </div>
                        </div>
                    </div>
                </div>
            </a>
        </li>
        `;

        notificationList.append(element);

        $(".dropdown-item").off("click").click(async function (event) {
            event.preventDefault();
            const target = $(event.currentTarget);

            // đánh dấu đã đọc


            // chuyển trang
            location.href = target.attr("data-href");
        });

        // Xử lý sự kiện tải CV khi nhấn nút Download cv
        $(".download-cv-btn").off("click").click(async function (event) {
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

        $(".btn-accept").off("click").click(async function (event) {
            event.preventDefault();
            const target = $(event.currentTarget);
            const applicationId = target.attr("data-id");
            if (!applicationId) {
                showToast("ApplicationId ID not found", ERROR_TOAST);
                return;
            }
            // đánh dấu đã đọc notification
            await changeApplicationStatus(applicationId, "APPLICATION_ACCEPTED", "Are you sure to accept this cv?");
        });

        $(".btn-interview-reject").off("click").click(async function (event) {
            event.preventDefault();
            const target = $(event.currentTarget);
            const applicationId = target.attr("data-id");
            if (!applicationId) {
                showToast("ApplicationId ID not found", ERROR_TOAST);
                return;
            }
            await changeApplicationStatus(applicationId, "APPLICATION_REJECTED", "Are you sure to reject this candidate after interview?");
        });

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

        $(".btn-information-interview").off("click").click(async function (event) {
            const target = $(event.currentTarget);
            const interviewId = target.attr("data-id");
            if (!interviewId) {
                showToast("Interview ID not found", ERROR_TOAST);
                return;
            }
            location.href = "/companies/interviews/" + interviewId;
        });

        $(".btn-accept-candidate").off("click").click(async function (event) {
            const target = $(event.currentTarget);
            const interviewId = target.attr("data-id");
            if (!interviewId) {
                showToast("Interview id not found", ERROR_TOAST);
                return;
            }
            await changeInterviewStatus(interviewId, "PASSED", "Are you sure to accept this candidate?");
        });

        $(".btn-reject-candidate").off("click").click(async function (event) {
            const target = $(event.currentTarget);
            const interviewId = target.attr("data-id");
            if (!interviewId) {
                showToast("Interview id not found", ERROR_TOAST);
                return;
            }
            await changeInterviewStatus(interviewId, "FAILED", "Are you sure to reject this candidate?");
        });

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
                success: function () {
                    showToast("Successfully", SUCCESS_TOAST);
                    getInterviewsData({});
                },
                error: function () {
                    showToast("Failed", ERROR_TOAST);
                }
            });
        }

        $(".btn-information-job").off("click").click(async function (event) {
            const target = $(event.currentTarget);
            const jobId = target.attr("data-id");
            if (!jobId) {
                showToast("Job ID not found", ERROR_TOAST);
                return;
            }
            location.href = "/jobs/" + jobId;
        });

        $(".btn-apply-job").off("click").click(async function (event) {
            const target = $(event.currentTarget);
            const jobId = target.attr("data-id");
            if (!jobId) {
                showToast("Job ID not found", ERROR_TOAST);
                return;
            }
            location.href = "/jobs/" + jobId + "/application";
        });


    }
}


