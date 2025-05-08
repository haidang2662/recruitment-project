package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.model.response.ChartResponse;
import vn.techmaster.danglh.recruitmentproject.repository.JobRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

    JobRepository jobRepository;

    public ChartResponse getNumbersPassedCandidate() {
        // Tính toán startDate và endDate cho mỗi tháng và chuyển sang LocalDateTime
        YearMonth december2024 = YearMonth.of(2024, Month.DECEMBER);
        LocalDateTime startDateT12 = december2024.atDay(1).atStartOfDay();
        LocalDateTime endDateT12 = december2024.atEndOfMonth().atTime(23, 59, 59);

        YearMonth january2025 = YearMonth.of(2025, Month.JANUARY);
        LocalDateTime startDateT1 = january2025.atDay(1).atStartOfDay();
        LocalDateTime endDateT1 = january2025.atEndOfMonth().atTime(23, 59, 59);

        YearMonth february2025 = YearMonth.of(2025, Month.FEBRUARY);
        LocalDateTime startDateT2 = february2025.atDay(1).atStartOfDay();
        LocalDateTime endDateT2 = february2025.atEndOfMonth().atTime(23, 59, 59);

        YearMonth march2025 = YearMonth.of(2025, Month.MARCH);
        LocalDateTime startDateT3 = march2025.atDay(1).atStartOfDay();
        LocalDateTime endDateT3 = march2025.atEndOfMonth().atTime(23, 59, 59);

        YearMonth april2025 = YearMonth.of(2025, Month.APRIL);
        LocalDateTime startDateT4 = april2025.atDay(1).atStartOfDay();
        LocalDateTime endDateT4 = april2025.atEndOfMonth().atTime(23, 59, 59);

        // Gọi repository với các ngày bắt đầu và kết thúc của mỗi tháng
        Long passedCandidatesT12 = jobRepository.getPassedCandidatesByMonth(startDateT12, endDateT12);
        Long passedCandidatesT1 = jobRepository.getPassedCandidatesByMonth(startDateT1, endDateT1);
        Long passedCandidatesT2 = jobRepository.getPassedCandidatesByMonth(startDateT2, endDateT2);
        Long passedCandidatesT3 = jobRepository.getPassedCandidatesByMonth(startDateT3, endDateT3);
        Long passedCandidatesT4 = jobRepository.getPassedCandidatesByMonth(startDateT4, endDateT4);

        // Trả về đối tượng ChartResponse với các giá trị lấy được
        return new ChartResponse(passedCandidatesT12, passedCandidatesT1, passedCandidatesT2, passedCandidatesT3, passedCandidatesT4);
    }
}
