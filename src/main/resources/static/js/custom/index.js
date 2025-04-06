$(document).ready(async function () {

    if (account != null) {
        if (account.role === COMPANY_ROLE) {
            window.location.href = "/companies/dashboard";
        }
        $("#candidate-section").hide();
        $("#company-section").hide();
        if (account.role === CANDIDATE_ROLE) {
            $("#upload-cv").show();
            $("#header-job-posting-btn").hide();
        }
    }

    function renderLocations() {
        $.ajax({
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
    }

    renderLocations();

    function renderJobCategories() {
        $.ajax({
            url: '/api/v1/job-categories',
            method: 'GET',
            success: function (response) {
                const jobCategorySelect = $('#job-categories');
                jobCategorySelect.empty();
                jobCategorySelect.append(`<option value=""></option>`);
                response.forEach(function (category) {
                    jobCategorySelect.append(`<option value="${category.id}">${category.name}</option>`);
                });

                if ($.fn.chosen) {
                    jobCategorySelect.trigger("chosen:updated");
                }
            },
            error: function (error) {
                console.error('Error fetching job categories:', error);
            }
        });
    }

    renderJobCategories();

    $("#search-job-btn").click(function () {
        const formData = $("#search-job-candidate-form").serializeArray();
        const jobSearchObj = {};
        for (let i = 0; i < formData.length; i++) {
            if (formData[i].name === 'locations') {
                if (!jobSearchObj['locationIds']) {
                    jobSearchObj['locationIds'] = [formData[i].value];
                    continue;
                }
                jobSearchObj['locationIds'].push(formData[i].value);
                continue;
            }
            jobSearchObj[formData[i].name] = formData[i].value;
        }
        localStorage.setItem("candidateJobSearchObj", JSON.stringify(jobSearchObj));
        window.location.href = '/jobs';
    });

    await getCompanies();

    async function getCompanies(request) {

        await $.ajax({
            url: "/api/v1/companies",
            type: "GET",
            data: {
                pageIndex: 0,
                pageSize: 6,
                random: true,
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

    }

    function renderCompanies(data) {
        const tableContent = $("#companies");

        tableContent.empty();
        if (!data) {
            return;
        }

        const companies = data.data;
        console.log(companies)

        if (!companies || companies.length === 0) {
            return;
        }

        for (let i = 0; i < companies.length; i++) {
            const company = companies[i];
            const avatar = company?.avatarUrl ? `/api/v1/files/avatar/${company.avatarUrl}` : DEFAULT_AVATAR_URL;
            let companyBlock = `<div class="job-block col-lg-6 col-md-12 col-sm-12">
                            <div class="inner-box">
                                <div class="content w-100" style="padding-left: 68px">
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

            $(".company-arrow").last().click(function() {
                window.location.href = company.website; // Chuyển hướng đến trang web của công ty
            });

        }

    }
});
