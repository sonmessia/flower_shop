package vn.quahoa.flowershop.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartResponse {
    private List<String> labels; // The months, e.g. ["Tháng 1", "Tháng 2", ...]
    private List<BigDecimal> data; // The revenue for each month
    private int year; // The year this chart represents
}
