$(document).ready(function () {
    $("#confirm-role-btn").click(function () {
        const selectedRole = $("#oauth2-role").val();
        const credential = localStorage.getItem("oauth2Credential");

        if (!credential) {
            alert("Missing credential. Please login again.");
            location.href = "/login";
            return;
        }

        $.ajax({
            url: "/api/v1/authentications/login/oauth2",
            type: "POST",
            data: JSON.stringify({
                tenant: "GOOGLE",
                credential: credential,
                role: selectedRole
            }),
            contentType: "application/json; charset=utf-8",
            success: async function (data) {
                localStorage.removeItem("oauth2Credential");
                localStorage.setItem("accessToken", data?.jwt);
                localStorage.setItem("refreshToken", data?.refreshToken);

                const accountInfo = await getAccountDetail(data?.id);
                localStorage.setItem("account", JSON.stringify(accountInfo));

                if (accountInfo.role === CANDIDATE_ROLE) {
                    location.href = "/";
                } else if (accountInfo.role === COMPANY_ROLE) {
                    location.href = `/companies/dashboard`;
                }
            },
            error: function (err) {
                handleResponseError(err, "OAuth2 registration failed");
            }
        });
    });
});