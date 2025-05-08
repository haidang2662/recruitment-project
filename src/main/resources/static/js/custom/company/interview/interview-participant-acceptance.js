$(document).ready(async function () {

    const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
    const interviewId = pathParts[pathParts.length - 1]; // Lấy phần tử cuối cùng
    const status = pathParts[pathParts.length - 2]; // Lấy phần tử gần cuối cùng

    const content = status === "acceptance" ?
        "Congratulations on successfully registering for the interview. We look forward to meeting you soon."
        : "We deeply regret that you have declined to participate in our interview. Have a wonderful day !";
    await $.ajax({
        url: "/api/v1/interviews/" + interviewId + "/status",
        type: "PATCH",
        data: JSON.stringify({
            status: status === "acceptance" ? "INTERVIEW_ACCEPTED" : "INTERVIEW_REFUSED"
        }),
        contentType: "application/json; charset=utf-8",
        success: function () {
            $(".content").text(content);
        },
        error: function () {
            showToast("Failed", ERROR_TOAST);
        }
    });
})