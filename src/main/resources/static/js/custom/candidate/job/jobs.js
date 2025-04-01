$(document).ready(async function () {

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 20;

    const candidateJobSearchObj = loadSearchCondition();
    await getJobs(candidateJobSearchObj);

    await $.ajax({
        url: '/api/v1/locations',
        method: 'GET',
        success: function (response) {
            const locationSelect = $('#location');
            locationSelect.empty();
            response.forEach(function (location) {
                // Thêm từng địa điểm vào thẻ <select> dưới dạng thẻ <option>
                locationSelect.append(`<option value="${location.id}">${location.name}</option>`);
            });

            if ($.fn.chosen) { //xem plugin Chosen đã được tích hợp trong dự án chưa.
                locationSelect.trigger("chosen:updated"); // Thông báo cho thẻ select đã thay đổi , plugin sẽ tự động làm mới giao diện
            }
        },
        error: function (err) {
            showToast(err.responseJSON.message, ERROR_TOAST);
        }
    });

    await $.ajax({
        url: '/api/v1/job-categories',
        method: 'GET',
        success: function (response) {
            const jobCategorySelect = $('#job-categories');
            jobCategorySelect.empty();
            jobCategorySelect.append(`<option value="-1">All</option>`);
            response.forEach(function (category) {
                jobCategorySelect.append(`<option value="${category.id}">${category.name}</option>`);
            });

            if ($.fn.chosen) {
                jobCategorySelect.trigger("chosen:updated");
            }
        },
        error: function (error) {
            showToast(error.responseJSON.message, ERROR_TOAST);
        }
    });

    function loadSearchCondition() {
        const candidateJobSearchObjStr = localStorage.getItem("candidateJobSearchObj");
        const candidateJobSearchObj = candidateJobSearchObjStr ? JSON.parse(candidateJobSearchObjStr) : {};
        $(".filters-outer input[name='name']").val(candidateJobSearchObj?.name);
        $(".filters-outer #location").val(candidateJobSearchObj?.locationIds).trigger("chosen:updated");
        $(".filters-outer #job-categories").val(candidateJobSearchObj?.categoryId).trigger("chosen:updated");
        if (candidateJobSearchObj.workingTypes
            && candidateJobSearchObj.workingTypes?.length > 0
            && candidateJobSearchObj.workingTypes?.includes("ONLINE")) {
            $("#workingType-ONLINE").prop('checked', true);
        }
        if (candidateJobSearchObj.workingTypes
            && candidateJobSearchObj.workingTypes?.length > 0
            && candidateJobSearchObj.workingTypes?.includes("OFFLINE")) {
            $("#workingType-OFFLINE").prop('checked', true);
        }

        if (candidateJobSearchObj.workingTimeTypes
            && candidateJobSearchObj.workingTimeTypes?.length > 0
            && candidateJobSearchObj.workingTimeTypes?.includes("FULL_TIME")) {
            $("#workingTimeType-FULL_TIME").prop('checked', true);
        }
        if (candidateJobSearchObj.workingTimeTypes
            && candidateJobSearchObj.workingTimeTypes?.length > 0
            && candidateJobSearchObj.workingTimeTypes?.includes("PART_TIME")) {
            $("#workingTimeType-PART_TIME").prop('checked', true);
        }
        if (candidateJobSearchObj.yearOfExperience
            && candidateJobSearchObj.yearOfExperience?.length > 0) {
            $("*[name='yearOfExperience'][value='" + candidateJobSearchObj.yearOfExperience + "']").attr('checked', true);
        }
        if (candidateJobSearchObj.salaryFrom !== undefined && candidateJobSearchObj.salaryFrom !== null) {
            $("input[name='salaryFrom']").val(candidateJobSearchObj.salaryFrom);
        }

        if (candidateJobSearchObj.salaryTo !== undefined && candidateJobSearchObj.salaryTo !== null) {
            $("input[name='salaryTo']").val(candidateJobSearchObj.salaryTo);
        }
        console.log(candidateJobSearchObj)
        return candidateJobSearchObj;
    }

    // call ajax search job va render du lieu (Co phan trang)
    // sẽ call đúng vào API search job của company luôn (thêm điều kiện để phân biệt company và candidate)
    async function getJobs(request) {
        // Disable nút search và hiển thị spinner
        $("input").prop("disabled", true);
        $("select").prop("disabled", true);
        $(".page-item .page-link").addClass('disabled');

        // Hiển thị spinner ở khối div bên phải
        $("#candidate-jobs").html(`<div class="text-center">
                            <div class="spinner-border" role="status" style="width: 3rem; height: 3rem;">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        </div>`);

        await $.ajax({
            url: "/api/v1/jobs",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize,
                ...request
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
        $("select").prop("disabled", false);
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
                    showToast((favorite == 1 ? 'Remove from' : 'Add to') + " favorite successfully", SUCCESS_TOAST);
                    const filterValues = getFilterValues();
                    await getJobs(filterValues);
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
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getJobs(filterValues);
        });

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getJobs(filterValues);
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getJobs(filterValues);
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getJobs(filterValues);
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getJobs(filterValues);
        });
    }

    // bắt sự kiện khi ngươi dùng thay đổi bất kỳ thành phần nào ở cột filter => đi search
    $("input[name='name']").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("select[name='locationIds']").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("select[name='categoryId']").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("#workingType-ONLINE").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("#workingType-OFFLINE").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("#workingTimeType-FULL_TIME").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("#workingTimeType-PART_TIME").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("input[name='yearOfExperience']").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });
    $("input[name='salaryFrom']").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });

    $("input[name='salaryTo']").change(async function () {
        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });

    $("input[name='salaryFrom'], input[name='salaryTo']").change(async function () {
        if (!validateSalaryRange()) return;

        const filterValues = getFilterValues();
        await getJobs(filterValues);
    });

    function getFilterValues() {
        const workingTypes = [];
        if ($("#workingType-ONLINE").is(":checked")) {
            workingTypes.push('ONLINE');
        }
        if ($("#workingType-OFFLINE").is(":checked")) {
            workingTypes.push('OFFLINE');
        }

        const workingTimeTypes = [];
        if ($("#workingTimeType-FULL_TIME").is(":checked")) {
            workingTimeTypes.push('FULL_TIME');
        }
        if ($("#workingTimeType-PART_TIME").is(":checked")) {
            workingTimeTypes.push('PART_TIME');
        }
        const filterValue = {
            name: $("input[name='name']").val(),
            locationIds: $("select[name='locationIds']").chosen().val(),
            categoryId: $("select[name='categoryId']").find(":selected").val(),
            workingTypes,
            workingTimeTypes,
            yearOfExperience: $("input[name='yearOfExperience']:checked", "#search-job-form").val(),
            salaryFrom: $("input[name='salaryFrom']").val(),
            salaryTo: $("input[name='salaryTo']").val()
        }

        localStorage.setItem("candidateJobSearchObj", JSON.stringify(filterValue));
        return filterValue;
    }

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

function validateSalaryRange() {
    const salaryFromStr = $("input[name='salaryFrom']").val();
    const salaryToStr = $("input[name='salaryTo']").val();

    const hasSalaryFrom = salaryFromStr !== "";
    const hasSalaryTo = salaryToStr !== "";

    const salaryFrom = hasSalaryFrom ? parseFloat(salaryFromStr) : null;
    const salaryTo = hasSalaryTo ? parseFloat(salaryToStr) : null;

    // Nếu Salary To có nhưng Salary From không có => báo lỗi
    if (!hasSalaryFrom) {
        showToast("Please fill 'Salary from'", ERROR_TOAST);
        return false;
    }

    if (!hasSalaryTo) {
        showToast("Please fill 'Salary to'", ERROR_TOAST);
        return false;
    }

    // Salary From phải là số không âm (nếu có điền)
    if (salaryFrom !== null && salaryFrom < 0) {
        showToast("Salary from must be a non-negative number", ERROR_TOAST);
        return false;
    }

    // Salary To phải là số không âm (nếu có điền)
    if (salaryTo !== null && salaryTo < 0) {
        showToast("Salary to must be a non-negative number", ERROR_TOAST);
        return false;
    }

    // Nếu cả 2 đều có, kiểm tra điều kiện từ < đến
    if (salaryFrom !== null && salaryTo !== null && salaryTo < salaryFrom) {
        showToast("Salary to must be greater than or equal to salary from", ERROR_TOAST);
        return false;
    }

    return true;
}

