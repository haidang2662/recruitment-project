$(document).ready(async function () {

    const account = JSON.parse(localStorage.getItem("account"));
    if (!account) {
        location.href = "/login";
    }

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 20;

    await getJobs();

    // call ajax search job va render du lieu (Co phan trang)
    // sẽ call đúng vào API search job của company luôn (thêm điều kiện để phân biệt company và candidate)
    async function getJobs() {
        // Disable nút search và hiển thị spinner
        $("input").prop("disabled", true);
        $(".page-item .page-link").addClass('disabled');

        // Hiển thị spinner ở khối div bên phải
        $("#candidate-jobs").html(`<div class="text-center">
                            <div class="spinner-border" role="status" style="width: 3rem; height: 3rem;">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        </div>`);

        await $.ajax({
            url: "/api/v1/jobs/application",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize,
                favorite: true,
                name: $("#name").val()
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderJobs(data);
            },
            error: function () {
                showToast("Get job failed", ERROR_TOAST);
            }
        });

        // Ẩn spinner và kích hoạt lại nút search
        $("input").prop("disabled", false);
        $(".filters-outer #location").trigger("chosen:updated");
        $(".filters-outer #job-categories").trigger("chosen:updated");
        $(".page-item .page-link").removeClass('disabled');
    }

    function renderJobs(data) {
        const paginationHtml = $("#job-paging .pagination");
        const tableContent = $("#candidate-jobs");
        const totalRecordHtml = $(".total-record");

        tableContent.empty();
        paginationHtml.empty();
        totalRecordHtml.empty();
        if (!data) {
            return;
        }

        const jobs = data.data;
        totalPage = data.totalPage;
        totalRecord = data.totalRecord;
        paging = data.pageInfo;
        pageIndex = paging.pageNumber;

        if (!jobs || jobs.length === 0) {
            return;
        }

        for (let i = 0; i < jobs.length; i++) {
            jobs[i]['stt'] = pageIndex * pageSize + i + 1;
        }

        for (let i = 0; i < jobs.length; i++) {
            const job = jobs[i];
            const avatar = job?.companyAvatarUrl ? `/api/v1/files/avatar/${job.companyAvatarUrl}` : DEFAULT_AVATAR_URL;
            const urgentHtml = job?.urgent ? '<li class="required">Urgent</li>' : '';
            let jobBlock = `<div class="job-block col-lg-6 col-md-12 col-sm-12">
                            <div class="inner-box">
                                <div class="content">
                                    <span class="company-logo"><img src='${avatar}' alt="" class="rounded rounded-circle" style="max-width:unset!important;width: 55px;height: 55px;"></span>
                                    <h4><a href="/jobs/${job.id}">${job.name}</a></h4>
                                    <ul class="job-info">
                                        <li><span class="icon flaticon-briefcase"></span> ${job.position}</li>
                                        <li><span class="icon flaticon-map-locator"></span> ${job.workingCity}</li>
                                        <li><span class="icon flaticon-clock-3"></span> ${getTimeDifferenceInDays(new Date(job.createdAt), new Date())}</li>
                                        <li><span class="icon flaticon-money"></span> ${job.salaryTo ? `${job.salaryFrom} - ${job.salaryTo}` : job.salaryFrom}</li>
                                    </ul>
                                    <ul class="job-other-info">
                                        <li class="time">${decodeJobWorkingTimeType(job?.workingTimeType || 'FULL_TIME')}</li>
                                        <li class="privacy">${decodeJobWorkingType(job?.workingType || 'OFFLINE')}</li>
                                        ${urgentHtml}
                                    </ul>
                                    <button class="bookmark-btn favorite-btn ${job.favorite ? 'favorite-job' : ''}" job-id="${job.id}" favorite="${job.favorite ? 1 : 0}"><i class="fa-regular fa-heart" style="color: #696969"></i></button>
                                </div>
                            </div>
                        </div>`;

            tableContent.append(jobBlock);

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
                    showToast((favorite == 1 ? 'Remove from' : 'Add to') +  " favorite successfully", SUCCESS_TOAST);
                    await getJobs();
                } catch (err) {
                    showToast(err.responseJSON.message, ERROR_TOAST);
                }
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
            await getJobs();
        });

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            await getJobs();
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            await getJobs();
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            await getJobs();
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            await getJobs();
        });
    }

    $(".favorite-job-search-btn").click(async function () {
        await getJobs();
    });

});


function decodeJobWorkingTimeType(workingTimeType) {
    switch (workingTimeType) {
        case "FULL_TIME":
            return "Full time";
        case "PART_TIME":
            return "Part time";
        default:
            return "Full time";
    }
}

function decodeJobWorkingType(workingType) {
    switch (workingType) {
        case "OFFLINE":
            return "Offline";
        case "ONLINE":
            return "Online";
        default:
            return "Offline";
    }
}