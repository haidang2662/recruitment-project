$(document).ready(async function () {

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    const companyId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng

    if (!companyId) {
        window.href = '/404';
    }
    await loadCompanyDetails(companyId);

});

// Hàm load dữ liệu chi tiết của job từ API
async function loadCompanyDetails(companyId) {
    try {
        const company = await $.ajax({
            url: `/api/v1/companies/${companyId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
        });
        renderCompanyDetails(company);
    } catch (error) {
        if (error.status === 404) {
            window.location.href = "/404";
            return;
        }
        showToast(error.responseJSON.message, ERROR_TOAST);
    }
}

// Render thông tin chi tiết của job vào HTML
function renderCompanyDetails(company) {

    company.description = company.description.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");

    for (let key in company) {
        $("#" + key).html(company[key]);
    }
    $("#name").text(company.name);
    $("#alias").text(company.alias);
    $("#phone").text(company.phone);
    $("#foundAt").text(new Date(company.foundAt).getFullYear());
    $("#taxCode").text(company.taxCode);
    $("#headQuarterAddress").text(company.headQuarterAddress);
    $("#employeeQuantity").text(company.employeeQuantity);
    $("#website").text(company.website);
    $("#website").attr("href", company.website);
    $("#rating").text(company.rating);
    $("#email").text(company.email);


}