package vn.quahoa.flowershop.dto.dashboard;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal currentMonthRevenue;
    private long totalOrdersThisMonth;
    private long totalProducts;
    private long totalUsers;
}
