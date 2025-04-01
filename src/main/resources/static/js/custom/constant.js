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