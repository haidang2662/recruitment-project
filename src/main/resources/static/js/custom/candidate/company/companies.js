$(document).ready(async function () {

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 10;

    await getCompanies();

    // call ajax search job va render du lieu (Co phan trang)
    // sẽ call đúng vào API search job của company luôn (thêm điều kiện để phân biệt company và candidate)
    async function getCompanies() {
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
            url: "/api/v1/companies",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize,
                random: false
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderCompanies(data);
            },
            error: function () {
                showToast("Get company failed", ERROR_TOAST);
            }
        });

        // Ẩn spinner và kích hoạt lại nút search
        $("input").prop("disabled", false);
        $("select").prop("disabled", false);
        $(".filters-outer #location").trigger("chosen:updated");
        $(".filters-outer #job-categories").trigger("chosen:updated");
        $(".page-item .page-link").removeClass('disabled');
    }

    function renderCompanies(data) {
        const paginationHtml = $("#company-paging .pagination");
        const tableContent = $("#candidate-companies");
        const totalRecordHtml = $(".total-record");

        tableContent.empty();
        paginationHtml.empty();
        totalRecordHtml.empty();
        if (!data) {
            return;
        }

        const account = JSON.parse(localStorage.getItem("account"));
        console.log(account)
        const companies = data.data;
        console.log(companies)
        totalPage = data.totalPage;
        totalRecord = data.totalRecord;
        paging = data.pageInfo;
        pageIndex = paging.pageNumber;

        if (!companies || companies.length === 0) {
            return;
        }

        for (let i = 0; i < companies.length; i++) {
            companies[i]['stt'] = pageIndex * pageSize + i + 1;
        }


        for (let i = 0; i < companies.length; i++) {
            const company = companies[i];
            const avatar = company?.avatarUrl ? `/api/v1/files/avatar/${company.avatarUrl}` : DEFAULT_AVATAR_URL;
            let companyBlock = `<div class="job-block col-lg-6 col-md-12 col-sm-12">
                            <div class="inner-box">
                                <div class="content">
                                    <span class="company-logo"><img src='${avatar}' alt="" class="rounded rounded-circle" style="max-width:unset!important;width: 55px;height: 55px;"></span>
                                    <h4><a href="/companies/${company.id}">${company.name}</a></h4>
                                    <ul class="job-info">
                                        <li><span class="icon flaticon-user"></span> ${company.employeeQuantity}</li>
                                        <li><span class="icon flaticon-briefcase"></span> ${new Date(company.foundAt).getFullYear()}</li>
                                        <li><span class="icon flaticon-phone"></span> ${company.phone}</li>
                                        <li><span class="icon flaticon-email"></span> ${company.email}</li>
                                    </ul>
                                    <ul class="job-other-info">
                                        <li class="privacy"><span class="icon flaticon-map-locator"></span> ${company.headQuarterAddress}</li>
                                        <li class="time">
                                            <span class="icon flaticon-web-programming"></span>
                                            <a href="${company.website}" target="_blank" rel="noopener noreferrer">${company.website}</a>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>`;

            tableContent.append(companyBlock);

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

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getCompanies(filterValues);
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getCompanies(filterValues);
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getCompanies(filterValues);
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            const filterValues = getFilterValues();  // Lấy lại các điều kiện tìm kiếm hiện tại
            await getCompanies(filterValues);
        });
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



