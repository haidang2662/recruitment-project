$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 10;
    await getInterviewsData({});

    async function getInterviewsData(request) {
        // Disable nút search và hiển thị spinner
        $("#search-interview-btn").prop("disabled", true);
        $("#spinner-search").removeClass('d-none');
        $(".page-item .page-link").addClass('disabled');

        // Hiển thị spinner ở bảng
        $("#interview-table tbody").html('<tr><td colspan="9" class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></td></tr>');

        await $.ajax({
            url: "/api/v1/interviews",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize,
                ...request
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderInterviewTable(data);

            },
            error: function () {
                showToast("Get job failed", ERROR_TOAST);
            }
        });

        // Ẩn spinner và kích hoạt lại nút search
        $("#spinner-search").addClass('d-none');
        $("#search-interview-btn").prop("disabled", false);
        $(".page-item .page-link").removeClass('disabled');
    }

    function renderInterviewTable(data) {
        const paginationHtml = $("#interview-paging .pagination");
        const tableContent = $("#interview-table tbody");
        const totalRecordHtml = $(".total-record");

        tableContent.empty();
        paginationHtml.empty();
        totalRecordHtml.empty();
        if (!data) {
            return;
        }

        const interviews = data.data;
        totalPage = data.totalPage;
        totalRecord = data.totalRecord;
        paging = data.pageInfo;
        pageIndex = paging.pageNumber;

        if (!interviews || interviews.length === 0) {
            return;
        }

        for (let i = 0; i < interviews.length; i++) {
            interviews[i]['stt'] = pageIndex * pageSize + i + 1;
        }


        for (let i = 0; i < interviews.length; i++) {
            const interview = interviews[i];
            // interview.job.name
            let tr = "<tr>" +
                "<td>" + interview.stt + "</td>" +
                "<td><a href='/companies/candidates/" + interview.candidate.id + "'>" + interview.candidate.name + "</a></td>" +
                "<td><a href='/companies/jobs/" + interview.job.id + "'>" + interview.job.name + "</a></td>" +
                "<td>" + dateTimeFormat(new Date(interview.interviewEmailSentAt)) + "</td>" +
                "<td>" + dateTimeFormat(new Date(interview.interviewAt)) + "</td>" +
                "<td>" + decodeInterviewType(interview.type) + "</td>" +
                "<td>" + decodeInterviewStatus(interview.status) + "</td>" +
                "<td>" +
                "<div class='action-icons d-flex align-items-center'>" + // Thêm container Flexbox
                getInterviewActionButtons(interview) +
                "</div>" +
                "</td>" +
                "</tr>";

            tableContent.append(tr);



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

            $(".btn-candidate-absence").off("click").click(async function (event) {
                const target = $(event.currentTarget);
                const interviewId = target.attr("data-id");
                if (!interviewId) {
                    showToast("Interview id not found", ERROR_TOAST);
                    return;
                }
                await changeInterviewStatus(interviewId, "CANDIDATE_ABSENCE", "Are you sure this candidate is absence?");
            });

            $(".btn-cancel-interview").off("click").click(async function (event) {
                const target = $(event.currentTarget);
                const interviewId = target.attr("data-id");
                if (!interviewId) {
                    showToast("Interview id not found", ERROR_TOAST);
                    return;
                }
                await changeInterviewStatus(interviewId, "CANCELLED", "Are you sure to cancel this interview?");
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
            await getInterviewsData({});
        });

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            await getInterviewsData({});
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            await getInterviewsData({});
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            await getInterviewsData({});
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            await getInterviewsData({});
        });
    }

    function getInterviewActionButtons(interview) {
        const INFORMATION_INTERVIEW_BUTTON =
            "<span role='button' class='text-secondary me-2 btn-information-interview' data-cv-id=\"${cvId}\" data-id='" + interview.id + "' data-bs-toggle='tooltip' title=' Interview information '>" +
            "    <i class=\"fa-solid fa-eye\"></i> " +
            "<a href='/api/v1/interviews" + interview.id + "'></a>" +
            "</span>";
        const ACCEPT_CANDIDATE_BUTTON =
            "<span role='button' class='text-secondary me-2 btn-accept-candidate text-success' data-id='" + interview.id + "' data-bs-toggle='tooltip' title='Accept candidate after interview'>" +
            "    <i class='fa-solid fa-square-check'></i> " +
            "</span>";
        const REJECT_CANDIDATE_BUTTON =
            "<span role='button' class='text-warning me-2 btn-reject-candidate text-danger' data-id='" + interview.id + "' data-bs-toggle='tooltip' title='Reject candidate after interview'>" +
            "    <i class='fa-solid fa-square-xmark'></i> " +
            "</span>";
        const CANDIDATE_ABSENCE_BUTTON =
            "<span role='button' class='text-warning me-2 btn-candidate-absence text-danger' data-id='" + interview.id + "' data-bs-toggle='tooltip' title='Candidate absence'>" +
            "    <i class=\"fa-solid fa-user-minus\"></i> " +
            "</span>";
        const CANCEL_INTERVIEW =
            "<span role='button' class='text-danger btn-cancel-interview me-2 btn-reject' data-bs-toggle='tooltip' title='Cancel Interview' data-id='" + interview.id + "'>" +
            "    <i class='fa-solid fa-x'></i>" +
            "</span>";

        let buttons = INFORMATION_INTERVIEW_BUTTON;
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
        return buttons;
    }

    $("#search-interview-btn").click(async function () {

        // Lấy dữ liệu từ form
        const formData = $("#search-interview-form").serializeArray();
        let searchInterview = {};
        for (let i = 0; i < formData.length; i++) {
            searchInterview[formData[i].name] = formData[i].value;
        }

        await getInterviewsData(searchInterview);

    });

    $("#reset-search-interview-btn").click(async function () {
        // Reset form
        $("#search-interview-form").trigger("reset");

        // Lấy lại dữ liệu
        await getInterviewsData({});
    });

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

    function decodeInterviewType(workingType) {
        switch (workingType) {
            case "OFFLINE":
                return "Offline";
            case "ONLINE":
                return "Online";
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
            success: function () {
                showToast("Successfully", SUCCESS_TOAST);
                getInterviewsData({});
            },
            error: function () {
                showToast("Failed", ERROR_TOAST);
            }
        });
    }



});
