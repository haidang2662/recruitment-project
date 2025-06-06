const SUCCESS_TOAST = "SUCCESS";
const ERROR_TOAST = "ERROR";
const WARNING_TOAST = "WARING";


const CANDIDATE_ROLE = "CANDIDATE";
const COMPANY_ROLE = "COMPANY";
const ADMIN_ROLE = "ADMIN";

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

const MAX_AVATAR_FILE_SIZE = 5 * 1024 * 1024; // 5MB
const MAX_CV_FILE_SIZE = 10 * 1024 * 1024; // 10MB

const DEFAULT_AVATAR_URL = "/api/v1/files/avatar/default-avatar.jpg";
const DEFAULT_COVER_URL = "/api/v1/files/cover/default-cover.jpg";

const SIDEBAR_APPLICATION_PATHS = [
    {
        path: "/companies/dashboard",
        sidebarClass: "dashboard-menu",
        role: COMPANY_ROLE
    },
    {
        path: "/companies/jobs",
        sidebarClass: "jobs-dashboard-menu",
        role: COMPANY_ROLE
    },
    {
        path: "/companies/applications",
        sidebarClass: "applications-dashboard-menu",
        role: COMPANY_ROLE
    },
    {
        path: "/companies/interviews",
        sidebarClass: "interview-dashboard-menu",
        role: COMPANY_ROLE
    },
    {
        path: "/companies/candidates",
        sidebarClass: "candidate-dashboard-menu",
        role: COMPANY_ROLE
    }
];

const JOB_STATUS = {
    DRAFT: "DRAFT",
    PUBLISH: "PUBLISH",
    EXPIRED: "EXPIRED",
    UNPUBLISHED: "UNPUBLISHED"
}

const WEBSOCKET_DESTINATIONS = {
    NEW_APPLICATION_NOTIFICATION: "new-application", // download cv , accept cv , reject cv (company) ok ++
    INTERVIEW_ACCEPTANCE_NOTIFICATION: "interview-acceptance", // xem lại thông tin buổi phỏng vấn , accept camdidate , reject candidate (company) ok ++
    INTERVIEW_REFUSAL_NOTIFICATION: "interview-refusal", // để nguyên k cần làm gì cả vì đã chuyển sang status interview_refuse ok ++
    NEW_INTERVIEW_NOTIFICATION: "new-interview", // xem lại thông tin job đã ứng tuyển và thông báo check mail (candidate) ok ++
    CV_ACCEPTANCE_NOTIFICATION: "cv-acceptance", // xem lại thông tin job đã ứng tuyển (candidate) ok ++
    CV_REFUSAL_NOTIFICATION: "cv-refusal", // xem lại thông tin job đã ứng tuyển  (candidate) ok ++
    EXPIRED_FAVORITE_JOB_NOTIFICATION:"expired-favorite-job", // xem lại thông tin job đã ứng tuyển ok ++
    EXPIRED_JOB_NOTIFICATION:"expired-job", // có nút update job  , unPublish job (company) ok có sửa lại bên scheldule hỏi lại thầy ok+
    ENOUGH_PASSED_CANDIDATE_NOTIFICATION:"enough-passed-candidate" // có nút update job , unPublish job (company) // logic đoạn này bị thiếu bên application đã chỉnh lại và hỏi thầy . xem lại logic lúc accept candidate khi ở bên application và interview . VD : Nguyen van bao - tester
}