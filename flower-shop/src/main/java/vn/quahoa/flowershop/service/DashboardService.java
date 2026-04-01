package vn.quahoa.flowershop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.quahoa.flowershop.dto.dashboard.DashboardMonthlyDetailResponse;
import vn.quahoa.flowershop.dto.dashboard.DashboardSummaryResponse;
import vn.quahoa.flowershop.dto.dashboard.RevenueChartResponse;
import vn.quahoa.flowershop.model.Order;
import vn.quahoa.flowershop.model.OrderStatus;
import vn.quahoa.flowershop.repository.OrderRepository;
import vn.quahoa.flowershop.repository.ProductRepository;
import vn.quahoa.flowershop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(OrderStatus.COMPLETED, OrderStatus.SHIPPED);

    public DashboardSummaryResponse getSummary() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        // Calculate Revenue for Current Month
        List<Order> currentMonthOrders = orderRepository.findByStatusInAndCreatedAtBetween(
            REVENUE_STATUSES, startOfMonth, endOfMonth);
        
        BigDecimal currentMonthRevenue = currentMonthOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Order count this month
        long totalOrdersThisMonth = orderRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);

        // Overall basic stats
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();

        return DashboardSummaryResponse.builder()
            .currentMonthRevenue(currentMonthRevenue)
            .totalOrdersThisMonth(totalOrdersThisMonth)
            .totalProducts(totalProducts)
            .totalUsers(totalUsers)
            .build();
    }

    public RevenueChartResponse getRevenueChartByYear(int year) {
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(23, 59, 59, 999999999);

        List<Order> ordersOfYear = orderRepository.findByStatusInAndCreatedAtBetween(
            REVENUE_STATUSES, startOfYear, endOfYear);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            labels.add("Tháng " + month);
            int finalMonth = month;
            BigDecimal monthRevenue = ordersOfYear.stream()
                .filter(o -> o.getCreatedAt().getMonthValue() == finalMonth)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.add(monthRevenue);
        }

        return RevenueChartResponse.builder()
            .labels(labels)
            .data(data)
            .year(year)
            .build();
    }

    public DashboardMonthlyDetailResponse getMonthlyDetail(int year, int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        List<Order> monthlyOrders = orderRepository.findByStatusInAndCreatedAtBetween(
            REVENUE_STATUSES, startOfMonth, endOfMonth);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        
        int lengthOfMonth = targetMonth.lengthOfMonth();
        for (int day = 1; day <= lengthOfMonth; day++) {
            labels.add(String.valueOf(day));
            int finalDay = day;
            BigDecimal dayRevenue = monthlyOrders.stream()
                .filter(o -> o.getCreatedAt().getDayOfMonth() == finalDay)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.add(dayRevenue);
        }

        List<vn.quahoa.flowershop.dto.order.OrderResponse> orderResponses = monthlyOrders.stream()
            .map(orderService::toOrderResponse)
            .toList();

        return DashboardMonthlyDetailResponse.builder()
            .labels(labels)
            .data(data)
            .year(year)
            .month(month)
            .orders(orderResponses)
            .build();
    }
}
