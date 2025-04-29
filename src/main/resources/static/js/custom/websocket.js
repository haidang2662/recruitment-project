const socket = new SockJS('/ws');

const stompClient = Stomp.over(socket);

// Thay bằng username thật nếu có từ backend (thông qua Thymeleaf hoặc JS token)
// Ví dụ:
// let currentUsername = [[${#authentication.name}]];  // nếu dùng Thymeleaf và Spring Security
// Hoặc nếu đã set qua JS

stompClient.connect({}, function (frame) {
    console.log('🔔 WebSocket Connected: ' + frame);

    // Log thêm để kiểm tra kết nối
    console.log("WebSocket connection established.");

    const account = JSON.parse(localStorage.getItem("account"));
    let email = account?.email;
    // Subscribe vào kênh riêng
    if (!email || email?.trim()?.length === 0) {
        return;
    }

    const keys = Object.keys(WEBSOCKET_DESTINATIONS);
    for (let i = 0; i < keys.length; i++) {
        stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS[keys[i]]}`, function (notification) {
            let notificationData = JSON.parse(notification.body);
            console.log("Notification Data : ", notificationData);
            let message = notificationData?.content
            showNotification(message)
        });
    }
});

function showNotification(message) {
    // Cập nhật badge notification trên chuông
    // let notificationBadge = document.getElementById("notification-badge");
    //
    // // Thêm thông báo mới vào badge
    // notificationBadge.textContent = '•'; // hoặc một dấu chấm nhỏ để chỉ có thông báo
    // notificationBadge.style.display = "block"; // Hiển thị badge

    // Hiển thị toast notification
    const toast = document.createElement("div");
    toast.innerText = message;
    toast.className = "custom-toast";
    document.body.append(toast);

    // Tự động ẩn toast sau 5 giây
    setTimeout(() => toast.remove(), 5000);
}
