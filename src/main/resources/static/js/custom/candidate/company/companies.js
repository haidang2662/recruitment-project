$(document).ready(async function () {

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 4;

    await getCompanies();

    // call ajax search job va render du lieu (Co phan trang)
    // sẽ call đúng vào API search job của company luôn (thêm điều kiện để phân biệt company và candidate)
    async function getCompanies(request) {
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
                random: false,
                ...request
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
                                <h4>
                                    <a href="/public/companies/${company.id}" class="company-name">${company.name}</a>
                                    <span class="fa-solid fa-square-up-right company-arrow" style="cursor: pointer; margin-left: 10px;"></span>
                                </h4>
                                    <ul class="row">
                                        <li class="col"><span class="icon flaticon-user"></span> ${company.employeeQuantity}</li>
                                        <li class="col"><span class="icon flaticon-briefcase text-dark"></span> ${new Date(company.foundAt).getFullYear()}</li>
                                    </ul>
                                    <ul class="row">
                                        <li class="col"><span class="icon flaticon-phone"></span> ${company.phone}</li>
                                        <li class="col"><span class="icon flaticon-email"></span> ${company.email}</li>
                                    </ul>
                                    <ul class="company-other-info">
                                        <li class="privacy"><span class="icon flaticon-map-locator"></span> ${company.headQuarterAddress}</li>
                                    </ul>
                                </div>
                            </div>
                        </div>`;

            tableContent.append(companyBlock);
            $(".company-arrow").last().click(function () {
                window.location.href = company.website; // Chuyển hướng đến trang web của công ty
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

        // Ấn vào các nút page là con số cụ thể vd : 2 , 3 , 4
        $(".page-item").off("click").click(async function (event) {
            const newPageIndex = $(event.currentTarget).attr("page");
            if (!newPageIndex || isNaN(newPageIndex)) {
                return;
            }
            pageIndex = parseInt(newPageIndex);
            await getCompanies();
        });

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            await getCompanies();
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            await getCompanies();
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            await getCompanies();
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            await getCompanies();
        });
    }

    $("#search-company-btn").click(async function () {

        // Lấy dữ liệu từ form
        const formData = $("#search-company-form").serializeArray();
        let searchCompany = {};
        for (let i = 0; i < formData.length; i++) {
            searchCompany[formData[i].name] = formData[i].value;
        }

        pageIndex = 0;
        await getCompanies(searchCompany);

    });


    $("#reset-search-company-btn").click(async function () {
        // Reset form
        $("#search-company-form").trigger("reset");

        // Lấy lại dữ liệu
        await getCompanies({});
    });

});
