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

    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.NEW_APPLICATION_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        console.log("Nội dung message : " + message)
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.INTERVIEW_ACCEPTANCE_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.INTERVIEW_REFUSAL_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.NEW_INTERVIEW_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.CV_ACCEPTANCE_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.CV_REFUSAL_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.EXPIRED_FAVORITE_JOB_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.EXPIRED_JOB_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        showNotification(message);
    });
    stompClient.subscribe(`/topic/${email}/${WEBSOCKET_DESTINATIONS.ENOUGH_PASSED_CANDIDATE_NOTIFICATION}`, function (notification) {
        let notificationData = JSON.parse(notification.body);
        let message = notificationData?.content
        console.log("Nội dung Notification : " + notificationData)
        console.log("Nội dung message : " + message)
        showNotification(message);
    });
});

function showNotification(message) {
    // Cập nhật badge notification trên chuông
    let bellIcon = document.getElementById("notification-btn");
    let notificationBadge = document.getElementById("notification-badge");

    // Thêm thông báo mới vào badge
    notificationBadge.textContent = '•'; // hoặc một dấu chấm nhỏ để chỉ có thông báo
    notificationBadge.style.display = "block"; // Hiển thị badge

    // Hiển thị toast notification
    const toast = document.createElement("div");
    toast.innerText = message;
    toast.className = "custom-toast";
    document.body.appendChild(toast);

    // Tự động ẩn toast sau 5 giây
    setTimeout(() => toast.remove(), 5000);
}

// Khi người dùng nhấp vào biểu tượng chuông, hiển thị thông báo (popup toast)
document.getElementById("notification-btn").addEventListener("click", function () {
    // Lấy message từ localStorage
    const message = localStorage.getItem('latestMessage');

    if (message) {
        const toast = document.createElement("div");
        toast.innerText = message;  // Cập nhật nội dung của toast bằng message
        toast.className = "custom-toast";
        document.body.appendChild(toast);

        // Tự động ẩn toast sau 5 giây
        setTimeout(() => toast.remove(), 5000);

        // Ẩn badge khi người dùng đã đọc thông báo
        let notificationBadge = document.getElementById("notification-badge");
        notificationBadge.style.display = "none";
    }
});


// Thêm CSS vào document (nhúng vào trong JavaScript)
// const style = document.createElement("style");
// style.innerHTML = `
//     /* Style for notification badge */
//     .badge {
//         position: absolute;
//         top: 0;
//         right: 0;
//         background-color: red;
//         color: white;
//         font-size: 16px;
//         width: 12px;
//         height: 12px;
//         border-radius: 50%;
//         display: none; /* Ẩn badge khi không có thông báo */
//         text-align: center;
//         line-height: 12px;
//     }
//
//     /* Style for toast notification */
//     .custom-toast {
//         position: fixed;
//         top: 20px;
//         left: 50%;
//         transform: translateX(-50%);
//         background-color: rgba(0, 0, 0, 0.8);
//         color: white;
//         padding: 10px;
//         border-radius: 5px;
//         font-size: 14px;
//         z-index: 9999;
//         animation: fadeInOut 5s;
//     }
//
//     @keyframes fadeInOut {
//         0% { opacity: 0; }
//         20% { opacity: 1; }
//         80% { opacity: 1; }
//         100% { opacity: 0; }
//     }
// `;
// document.head.appendChild(style);  // Thêm phần CSS vào đầu trang

