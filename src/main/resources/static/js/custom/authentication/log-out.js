$(document).ready(function () {

    $("#log-out-btn").click(async function () {
        await $.ajax({
            url: "/api/v1/authentications/logout",
            type: "POST",
            contentType: "application/json; charset=utf-8",
        });

        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('account');
        location.href = "/";
    });

});