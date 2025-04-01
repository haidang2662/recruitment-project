package vn.techmaster.danglh.recruitmentproject.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationStatus {
    APPLIED, // ứng viên đã ứng tuyển
    CANCELLED, // ứng viên hủy ứng tuyển
    APPLICATION_ACCEPTED, // nhà tuyển dụng đã duyệt CV và thấy oke => nhà tuyển dụng duyệt đơn ứng tuyển
    APPLICATION_REJECTED, // nhà tuyển dụng từ chối đơn ứng tuyển
    WAIT_FOR_INTERVIEW, // nhà tuyển dụng đã đặt lịch hẹn phỏng vấn ứng viên
    CANDIDATE_ACCEPTED, // phỏng vấn thành công, nhận ứng viên vào làm
    CANDIDATE_REJECTED // phỏng vấn thất bại, từ chối ứng viên
}
