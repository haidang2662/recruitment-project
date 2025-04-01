$(document).ready(function () {

    const accessToken = localStorage.getItem("accessToken");
    // Kiểm tra token ngay khi trang được tải
    if (accessToken && isTokenExpired(accessToken)) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("account");
    }

    const account = JSON.parse(localStorage.getItem("account"));

    // login rồi
    if (account) {
        $("#btn-login").hide();

        $("#notification-btn").removeClass('d-none');
        $("#header-account-info").removeClass('d-none');

        if (account?.role === COMPANY_ROLE) {
            $("#header-job-posting-btn").show();
            $(".main-header #job-header").hide();
            $(".main-header #has-mega-menu").hide();
            $("#nav-mobile #job-header").hide();
            $("#nav-mobile #has-mega-menu").hide();
            $(".main-box").addClass("py-2");
            $("#header-account-info #avatar-image-header").attr("src", `/api/v1/files/avatar/${account.companyModel.avatarUrl}`);
        } else {
            $("#header-job-posting-btn").hide();
            $("#header-account-info #avatar-image-header").attr("src", `/api/v1/files/avatar/${account.candidateModel.avatarUrl}`);
        }

        $("#header-account-info .dropdown-toggle .name").text(`${account.name || "My account"}`);
    }

    function refreshToken() {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken || !accessToken) return;

        $.ajax({
            url: "/api/v1/authentications/refresh_token",
            method: "POST",
            data: JSON.stringify({refreshToken: refreshToken}),
            contentType: "application/json",
            success: function (data) {
                localStorage.setItem("accessToken", data?.jwt);
                localStorage.setItem("refreshToken", data?.refreshToken);
                const user = {
                    id: data?.id,
                    email: data?.email,
                    name: data?.name,
                    role: data?.roles?.[0]
                };
                localStorage.setItem("user", JSON.stringify(user));
                setRefreshTimer();
            }
        });
    }

    // Kiểm tra xem token có hết hạn không
    function isTokenExpired(token) {
        const decodedToken = parseJwt(token);
        if (!decodedToken || !decodedToken.exp) return true;
        const expTime = decodedToken.exp * 1000;
        return expTime < Date.now();
    }

    // Hàm giải mã JWT (token) để lấy payload
    function parseJwt(token) {
        if (!token) return null;
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    }

    function setRefreshTimer() {
        const accessToken = localStorage.getItem('accessToken');
        if (!accessToken) return;

        const decodedToken = parseJwt(accessToken);
        if (!decodedToken || !decodedToken.exp) return;

        const expTime = decodedToken.exp * 1000;  // Lấy thời gian hết hạn (convert sang milliseconds)
        const timeUntilExpiry = expTime - Date.now();  // Tính thời gian còn lại
        const refreshTime = timeUntilExpiry - 60000;  // Refresh trước 1 phút (60,000ms)

        if (refreshTime > 0) {
            setTimeout(() => {
                refreshToken();
            }, refreshTime);
        }
    }

    setRefreshTimer();

})