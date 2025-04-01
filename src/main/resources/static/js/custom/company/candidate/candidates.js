$(document).ready(async function () {

    checkRoleAccountForCompany(JSON.parse(localStorage.getItem("account")));

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 10;
    await getCandidatesData({});

    async function getCandidatesData(request) {
        // Disable nút search và hiển thị spinner
        $("#search-interview-btn").prop("disabled", true);
        $("#spinner-search").removeClass('d-none');
        $(".page-item .page-link").addClass('disabled');

        // Hiển thị spinner ở bảng
        $("#candidate-table tbody").html('<tr><td colspan="9" class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></td></tr>');

        await $.ajax({
            url: "/api/v1/candidates",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize,
                ...request
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderCandidateTable(data);

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

    function renderCandidateTable(data) {
        const paginationHtml = $("#candidate-paging .pagination");
        const tableContent = $("#candidate-table tbody");
        const totalRecordHtml = $(".total-record");

        tableContent.empty();
        paginationHtml.empty();
        totalRecordHtml.empty();
        if (!data) {
            return;
        }

        const candidates = data.data;
        totalPage = data.totalPage;
        totalRecord = data.totalRecord;
        paging = data.pageInfo;
        pageIndex = paging.pageNumber;

        if (!candidates || candidates.length === 0) {
            return;
        }

        for (let i = 0; i < candidates.length; i++) {
            candidates[i]['stt'] = pageIndex * pageSize + i + 1;
        }


        for (let i = 0; i < candidates.length; i++) {
            const candidate = candidates[i];
            let tr = "<tr>" +
                "<td>" + candidate.stt + "</td>" +
                "<td><a href='/companies/candidates/" + candidate.id + "'>" + candidate.name + "</a></td>" +
                "<td>" + candidate.phone + "</td>" +
                "<td>" + decodeGender(candidate.gender) + "</td>" +
                "<td>" + decodeCandidateLiteracy(candidate.literacy) + "</td>" +
                "<td>" + candidate?.totalAppliedJob || 0 + "</td>" +
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
            await getCandidatesData({});
        });

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            await getCandidatesData({});
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            await getCandidatesData({});
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            await getCandidatesData({});
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            await getCandidatesData({});
        });
    }

    $("#search-interview-btn").click(async function () {

        // Lấy dữ liệu từ form
        const formData = $("#search-interview-form").serializeArray();
        let searchInterview = {};
        for (let i = 0; i < formData.length; i++) {
            searchInterview[formData[i].name] = formData[i].value;
        }

        await getCandidatesData(searchInterview);

    });

    $("#reset-search-interview-btn").click(async function () {
        // Reset form
        $("#search-interview-form").trigger("reset");

        // Lấy lại dữ liệu
        await getCandidatesData({});
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
                getCandidatesData({});
            },
            error: function () {
                showToast("Failed", ERROR_TOAST);
            }
        });
    }


});

function decodeCandidateLiteracy(literacy) {
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
