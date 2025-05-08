$(document).ready(function () {
    const loadMoreBtn = `
            <div class="load-more-block d-flex justify-content-center pt-3">
                <button id="load-more-btn" class="btn btn-outline-success px-3 py-0 fs-6">Load more</button>
            </div>
    `;
    let totalPage;
    let totalRecord;
    let pageIndex = 0;
    let pageSize = 20;

    function updateNotificationBadge() {
        $.ajax({
            url: "/api/v1/notifications/statistical-quantity",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                const unreadNotificationQuantity = data.quantity;
                const badgeElement = $("#notification-badge");
                if (unreadNotificationQuantity && unreadNotificationQuantity > 0) {
                    badgeElement.text(unreadNotificationQuantity);
                    badgeElement.show();
                } else {
                    badgeElement.hide();
                }
            },
            error: function () {
                console.error("Failed to fetch notifications for badge.");
            }
        });
    }

    function renderNotificationTarget(data) {
        const notifications = data.data;
        totalPage = data.totalPage;
        totalRecord = data.totalRecord;
        $(".load-more-block").remove();

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
                    <button class="text-success me-1 btn-accept-cv ${notification.seen ? 'd-none' : ''}" data-application-id="${applicationId}" data-bs-toggle='tooltip' title='Accept CV'>
                        <i class='fa-solid fa-check'></i>
                    </button>
                    <button class="text-danger btn-delete me-1 btn-reject-cv ${notification.seen ? 'd-none' : ''}"  data-application-id="${applicationId}" data-bs-toggle='tooltip' title='Reject CV'>
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
                    <button class="text-success me-1 btn-accept-candidate ${notification.seen ? 'd-none' : ''}" data-id="${interviewId}" data-bs-toggle='tooltip' title='Accept candidate after interview'>
                        <i class='fa-solid fa-square-check'></i>
                    </button>
                    <button class="text-danger me-1 btn-reject-candidate ${notification.seen ? 'd-none' : ''}" data-id="${interviewId}" data-bs-toggle='tooltip' title='Reject candidate after interview'>
                        <i class='fa-solid fa-square-xmark'></i>
                    </button>
                `;
                    redirectPath = '/companies/interviews/' + interviewId;
                    break;
                // để nguyên k cần làm gì cả
                case WEBSOCKET_DESTINATIONS.INTERVIEW_REFUSAL_NOTIFICATION:
                    redirectPath = '/companies/interviews/' + interviewId;
                    break;
                // xem lại thông tin job đã ứng tuyển và thông báo check mail (candidate)
                case WEBSOCKET_DESTINATIONS.NEW_INTERVIEW_NOTIFICATION:

                    buttons = `
                    <button class="text-primary me-1 btn-information-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Job Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                `;
                    redirectPath = '/jobs/' + jobId;
                    break;
                // xem lại thông tin job đã ứng tuyển (candidate)
                case WEBSOCKET_DESTINATIONS.CV_ACCEPTANCE_NOTIFICATION:
                    buttons = `
                    <button class="text-primary me-1 btn-information-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Job Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                `;
                    redirectPath = '/jobs/' + jobId;
                    break;
                // để nguyên k cần làm gì cả
                case WEBSOCKET_DESTINATIONS.CV_REFUSAL_NOTIFICATION:
                    buttons = `
                    <button class="text-primary me-1 btn-information-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Job Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                `;
                    redirectPath = '/jobs/' + jobId;
                    break;
                // để nguyên k cần làm gì cả
                case WEBSOCKET_DESTINATIONS.EXPIRED_FAVORITE_JOB_NOTIFICATION:
                    buttons = `
                    <button class="text-primary me-1 btn-information-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Job Information'>
                        <i class="fa-solid fa-eye"></i>
                    </button>
                `;
                    redirectPath = '/jobs/' + jobId;
                    break;
                case WEBSOCKET_DESTINATIONS.EXPIRED_JOB_NOTIFICATION:
                    buttons = `
                    <button class="text-success me-1 btn-update-job ${notification.seen ? 'd-none' : ''}" data-id="${jobId}" data-bs-toggle='tooltip' title='Update job'>
                        <i class="fa-solid fa-pencil"></i>
                    </button>
                    <button class="text-danger me-1 btn-unPublish ${notification.seen ? 'd-none' : ''}" data-id="${jobId}" data-bs-toggle='tooltip' title='Unpublish job'>
                        <i class="fa-solid fa-x"></i>
                    </button>
                `;

                    redirectPath = '/companies/jobs/' + jobId;
                    break;
                case WEBSOCKET_DESTINATIONS.ENOUGH_PASSED_CANDIDATE_NOTIFICATION:
                    buttons = `
                    <button class="text-success me-1 btn-update-job" data-id="${jobId}" data-bs-toggle='tooltip' title='Update job'>
                        <i class="fa-solid fa-pencil"></i>
                    </button>
                    <button class="text-danger me-1 btn-unPublish" data-id="${jobId}" data-bs-toggle='tooltip' title='Unpublish job'>
                        <i class="fa-solid fa-x"></i>
                    </button>
                `;
                    redirectPath = '/companies/jobs/' + jobId;
                    break;
            }

            const element = `
        <li class="notification-item pb-3" notification-target-id="${notification.id}" }">
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

            $container.infiniteScroll('appendItems', $(element));

            $(".dropdown-item").off("click").click(async function (event) {
                event.stopPropagation();

                // đánh dấu đã đọc
                const target = $(event.currentTarget);
                const notificationTargetId = target.attr("notification-target-id");
                markAsSeen(notificationTargetId);

                // chuyển trang
                location.href = target.attr("data-href");
            });

            // Xử lý sự kiện tải CV khi nhấn nút Download cv
            $(".download-cv-btn").off("click").click(async function (event) {
                event.stopPropagation();

                const target = $(event.currentTarget);
                const cvId = target.attr("data-cv-id");
                if (!cvId) {
                    showToast("CV ID not found", ERROR_TOAST);
                    return;
                }

                // Lấy notificationTargetId ngay trước khi thực hiện các thay đổi
                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();

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

            $(".btn-accept-cv").off("click").click(async function (event) {
                event.stopPropagation();

                const target = $(event.currentTarget);
                const applicationId = target.attr("data-application-id");
                if (!applicationId) {
                    showToast("ApplicationId ID not found", ERROR_TOAST);
                    return;
                }

                // Lấy notificationTargetId ngay trước khi thực hiện các thay đổi
                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();
                $(".btn-accept-cv").hide();
                $(".btn-reject-cv").hide();


                await changeApplicationStatus(applicationId, "APPLICATION_ACCEPTED", "Are you sure to accept this cv?");
            });

            $(".btn-reject-cv").off("click").click(async function (event) {
                event.stopPropagation()

                const target = $(event.currentTarget);
                const applicationId = target.attr("data-application-id");
                if (!applicationId) {
                    showToast("ApplicationId ID not found", ERROR_TOAST);
                    return;
                }

                $(".btn-accept-cv").hide();
                $(".btn-reject-cv").hide();

                // Lấy notificationTargetId ngay trước khi thực hiện các thay đổi
                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();


                await changeApplicationStatus(applicationId, "APPLICATION_REJECTED", "Are you sure to reject this cv?");
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
                event.stopPropagation();

                const target = $(event.currentTarget);
                const interviewId = target.attr("data-id");
                if (!interviewId) {
                    showToast("Interview ID not found", ERROR_TOAST);
                    return;
                }

                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();

                location.href = "/companies/interviews/" + interviewId;
            });

            $(".btn-accept-candidate").off("click").click(async function (event) {
                event.stopPropagation();

                const target = $(event.currentTarget);
                const interviewId = target.attr("data-id");
                if (!interviewId) {
                    showToast("Interview id not found", ERROR_TOAST);
                    return;
                }

                $(".btn-accept-candidate").hide();
                $(".btn-reject-candidate").hide();
                // Lấy notificationTargetId ngay trước khi thực hiện các thay đổi
                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();


                await changeInterviewStatus(interviewId, "PASSED", "Are you sure to accept this candidate?");
            });

            $(".btn-reject-candidate").off("click").click(async function (event) {
                event.stopPropagation();

                const target = $(event.currentTarget);
                const interviewId = target.attr("data-id");
                if (!interviewId) {
                    showToast("Interview id not found", ERROR_TOAST);
                    return;
                }

                $(".btn-accept-candidate").hide();
                $(".btn-reject-candidate").hide();

                // Lấy notificationTargetId ngay trước khi thực hiện các thay đổi
                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();

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
                event.stopPropagation();

                const target = $(event.currentTarget);
                const jobId = target.attr("data-id");
                if (!jobId) {
                    showToast("Job ID not found", ERROR_TOAST);
                    return;
                }

                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();

                location.href = "/jobs/" + jobId;
            });

            $(document).on("click", ".btn-update-job", function (event) {
                event.stopPropagation()

                const target = $(event.currentTarget);
                const jobId = target.attr("data-id");
                if (!jobId) {
                    showToast("Job ID not found", ERROR_TOAST);
                    return;
                }

                $(".btn-update-job").hide();
                $(".btn-unPublish").hide();

                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();

                window.location.href = `/companies/jobs/job-updating/${jobId}`;
            });

            // UNPUBLISHED
            $(".btn-unPublish").off("click").click(async function (event) {
                event.stopPropagation();

                const target = $(event.currentTarget);
                const jobId = target.attr("data-id");

                if (!jobId) {
                    showToast("Job ID not found", ERROR_TOAST);
                    return;
                }

                try {
                    await $.ajax({
                        url: '/api/v1/jobs/' + jobId + '/status',
                        type: 'PATCH',
                        data: JSON.stringify({status: JOB_STATUS.UNPUBLISHED}),
                        contentType: 'application/json; charset=utf-8',
                    });
                    showToast("Job marked as unPublished successfully", SUCCESS_TOAST);
                } catch (err) {
                    showToast(err.responseJSON.message, ERROR_TOAST);
                }

                $(".btn-update-job").hide();
                $(".btn-unPublish").hide();

                const notificationTargetId = target.closest('.dropdown-item').attr("notification-target-id");
                markAsSeen(notificationTargetId);
                // Ẩn dấu tròn "chưa đọc"
                target.closest('.dropdown-item').find('.seen-icon').hide();

            });

        }
        pageIndex++;
        if (totalPage > pageIndex) {
            $container.infiniteScroll('appendItems', $(loadMoreBtn));
            $('#load-more-btn').off("click").click(function (event) {
                event.stopPropagation();
                $container.infiniteScroll('loadNextPage');
            });
        } else {
            $container.infiniteScroll('appendItems', $(`<div class="d-flex justify-content-center pt-3 text-success fw-bold">NO MORE NOTIFICATIONS</div>`));
        }
    }

    function markAsSeen(id) {
        $.ajax({
            url: '/api/v1/notification-targets/' + id + "/seen",
            type: 'PATCH',
            contentType: "application/json; charset=utf-8",
            success: function () {
                // showToast("Successfully mark as seen", SUCCESS_TOAST)
            },
            error: function () {
                showToast("Failed mark as seen", ERROR_TOAST);
            }
        });
    }

    const token = localStorage.getItem("accessToken"); // Hoặc từ cookie
    const customFetchOptions = {
        headers: token ? {'Authorization': `Bearer ${token}`} : {}
    };
    const $container = $(".notification-box").infiniteScroll({
        path() {
            console.log(pageIndex);
            return `/api/v1/notifications?pageIndex=${pageIndex}&pageSize=${pageSize}`;
        },
        fetchOptions: customFetchOptions, // Gửi custom fetchOptions với header Authorization
        responseBody: 'json',
        status: '.scroll-status',
        history: false,
        scrollThreshold: false, // tắt tự động cuộn
        loadOnScroll: false   // kiểm soát việc gọi load bằng tay
    });

    $container.on('load.infiniteScroll', function (event, response) {
        renderNotificationTarget(response);
    });
    
    updateNotificationBadge(); // goi lan dau
    setInterval(() => {
        updateNotificationBadge();
    }, 30000); // 30s goi 1 lan

    $container.infiniteScroll('loadNextPage'); // load lần đầu;
});