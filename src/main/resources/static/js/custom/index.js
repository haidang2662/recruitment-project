$(document).ready(function () {

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

});
