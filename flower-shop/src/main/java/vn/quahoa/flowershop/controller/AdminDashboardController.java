package vn.quahoa.flowershop.controller;

import java.time.Year;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.quahoa.flowershop.dto.dashboard.DashboardMonthlyDetailResponse;
import vn.quahoa.flowershop.dto.dashboard.DashboardSummaryResponse;
import vn.quahoa.flowershop.dto.dashboard.RevenueChartResponse;
import vn.quahoa.flowershop.service.DashboardService;

@RestController
@RequestMapping("/api/admins/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/revenue-chart")
    public RevenueChartResponse getRevenueChart(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }
        return dashboardService.getRevenueChartByYear(year);
    }

    @GetMapping("/monthly-detail")
    public DashboardMonthlyDetailResponse getMonthlyDetail(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return dashboardService.getMonthlyDetail(year, month);
    }
}
