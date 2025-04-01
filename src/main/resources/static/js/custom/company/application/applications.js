$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 10;
    await getApplications({});

    async function getApplications(request) {
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
                ...request
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderTable(data);

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

    function renderTable(data) {
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
        // application?.job?.name

        for (let i = 0; i < applications.length; i++) {
            const application = applications[i];
            let tr = "<tr>" +
                "<td>" + application.stt + "</td>" +
                "<td><a href='/companies/jobs/" + application.job.id + "'>" + application.job.name + "</a></td>" +
                "<td><a href='/companies/candidates/" + application.candidate.id + "'>" + application.candidate.name + "</a></td>" +
                "<td>" + application?.appliedDate + "</td>" +
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


    $("#search-application-btn").click(async function () {

        // Lấy dữ liệu từ form
        const formData = $("#search-application-form").serializeArray();
        let searchJob = {};
        for (let i = 0; i < formData.length; i++) {
            searchJob[formData[i].name] = formData[i].value;
        }

        await getApplications(searchJob);
    });


    $("#reset-search-application-btn").click(async function () {
        // Reset form
        $("#search-application-form").trigger("reset");

        // Lấy lại dữ liệu
        await getApplications({});
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

});
