async function getAccountDetail(accountId) {
    let account = null;
    await $.ajax({
        url: `/api/v1/accounts/${accountId}`,
        type: "GET",
        contentType: "application/json; charset=utf-8",
        headers: {
            "Authorization": `Bearer ${localStorage.getItem("accessToken")}` // Thêm AccessToken vào header
        },
        success: function (response) {
            account = response;
        },
        error: function (err) {
            handleResponseError(err, "Get account info failed");
        }
    });
    return account;
}
