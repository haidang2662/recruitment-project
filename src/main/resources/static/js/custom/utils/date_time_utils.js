function getTimeDifferenceInDays(fromDate, toDate) {
    const msPerDay = 24 * 60 * 60 * 1000; // Số mili giây trong một ngày
    const msPerMonth = msPerDay * 30; // Xấp xỉ số mili giây trong 1 tháng
    const diffInMs = Math.abs(toDate - fromDate); // Chênh lệch thời gian tính bằng mili giây
    const diffInDays = Math.floor(diffInMs / msPerDay); // Số ngày chênh lệch
    const diffInMonths = Math.floor(diffInMs / msPerMonth); // Số tháng chênh lệch

    // Xử lý kết quả theo định dạng mong muốn
    if (diffInDays < 1) {
        return "< 1 day ago";
    } else if (diffInMonths >= 1) {
        if (diffInMonths === 1) {
            return "1 month ago";
        } else {
            return `${diffInMonths} months ago`;
        }
    } else {
        if (diffInDays === 1) {
            return "1 day ago";
        } else {
            return `${diffInDays} days ago`;
        }
    }
}

function dateTimeFormat(dateTime) {
    return ("0" + dateTime.getDate()).slice(-2)
        + " - "
        + ("0" + (dateTime.getMonth() + 1)).slice(-2)
        + " - " +
        dateTime.getFullYear()
        + "    "
        + ("0" + dateTime.getHours()).slice(-2)
        + ":"
        + ("0" + dateTime.getMinutes()).slice(-2);
}

function checkRoleAccountForCompany(account) {

    if (!account) {
        location.href = "/login";
    }
    if (account.role !== COMPANY_ROLE) {
        location.href = "/403";
    }

}