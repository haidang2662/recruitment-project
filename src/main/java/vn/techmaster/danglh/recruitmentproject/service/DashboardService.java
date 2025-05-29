package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.Company;
import vn.techmaster.danglh.recruitmentproject.model.response.ChartResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.PieChartResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.StatisticsResponse;
import vn.techmaster.danglh.recruitmentproject.repository.*;
import vn.techmaster.danglh.recruitmentproject.repository.custom.DashboardCustomRepository;
import vn.techmaster.danglh.recruitmentproject.security.SecurityUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

    JobRepository jobRepository;
    ApplicationRepository applicationRepository;
    InterviewRepository interviewRepository;
    NotificationRepository notificationRepository;
    DashboardCustomRepository dashboardCustomRepository;
    CompanyRepository companyRepository;
    AccountRepository accountRepository;

    public ChartResponse getNumbersPassedCandidate(String timeRange) {
        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        Company company = companyRepository.findByAccount(account)
                .orElseThrow(() -> new UsernameNotFoundException("Company not found"));

        YearMonth currentYearMonth = YearMonth.now();
        int timeStep = Integer.parseInt(timeRange) - 1; // để lấy ra số tháng cần lùi lại . Vd tháng 6 thì lấy tháng 6 và 5 tháng trước đó

        LocalDateTime startDate = currentYearMonth.minusMonths(timeStep).atDay(1).atTime(0, 0, 0);
        LocalDateTime endDate = currentYearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<ChartResponse.ChartDetailResponse> data = dashboardCustomRepository.getPassedCandidateByMonth(startDate, endDate, company.getId());

        LocalDateTime temp = startDate;
        while (temp.isBefore(endDate)) {
            String monthStr = temp.getMonthValue() < 10 ? "0" + temp.getMonthValue() : temp.getMonthValue() + "";
            String label = monthStr + "-" + temp.getYear();
            if (data.stream().noneMatch(d -> d.getTitle().equals(label))) {
                String sublabel = temp.getYear() + "" + monthStr;
                data.add(new ChartResponse.ChartDetailResponse(label, 0, sublabel));
            }
            temp = temp.plusMonths(1);
        } // bù vào danh sách các tháng không có dữ liệu thì cho nó quantity bằng 0

        data.sort(Comparator.comparing(ChartResponse.ChartDetailResponse::getSubtitle));
        return new ChartResponse(data);
    }

    public PieChartResponse getNumbersJobEnoughPassedCandidate(String timeRange) {
        List<PieChartResponse.PieChartDetailResponse> list = new ArrayList<>();
        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Lấy mốc thời gian dựa vào giá trị timeRange
        YearMonth yearMonth = YearMonth.now().minusMonths(Integer.parseInt(timeRange));
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Lấy số lượng các công việc đã hoàn thành và chưa hoàn thành trong khoảng thời gian
        Integer finishedJob = jobRepository.countFinishedJobsByAccountIdAndDateRange(accountId, startDate, endDate);
        Integer unFinishedJob = jobRepository.countUnfinishedJobsByAccountIdAndDateRange(accountId, startDate, endDate);

        PieChartResponse.PieChartDetailResponse detailResponse = new PieChartResponse.PieChartDetailResponse();
        String formattedDate = yearMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));  // Định dạng thành "MM/yyyy"
        detailResponse.setTitle(formattedDate);
        detailResponse.setFinishedJob(finishedJob);
        detailResponse.setUnFinishedJob(unFinishedJob);
        list.add(detailResponse);

        return new PieChartResponse(list);
    }

    public StatisticsResponse getStatistics() {
        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        int publishedJobsCount = jobRepository.countPublishedJobsByAccountId(accountId);
        int applicationCount = applicationRepository.countApplicationsByAccountId(accountId);
        int interviewCount = interviewRepository.countInterviewsByAccountId(accountId);
        int notificationCount = notificationRepository.countNotificationsByAccountId(accountId);

        return new StatisticsResponse(publishedJobsCount, applicationCount, interviewCount, notificationCount);
    }
}
