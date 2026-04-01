package vn.quahoa.flowershop.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.quahoa.flowershop.dto.order.OrderResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMonthlyDetailResponse {
    private List<String> labels;
    private List<BigDecimal> data;
    private int year;
    private int month;
    private List<OrderResponse> orders;
}
