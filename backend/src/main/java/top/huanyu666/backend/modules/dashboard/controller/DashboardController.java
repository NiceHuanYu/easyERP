package top.huanyu666.backend.modules.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.modules.inventory.entity.InvStock;
import top.huanyu666.backend.modules.inventory.mapper.InvStockMapper;
import top.huanyu666.backend.modules.sales.entity.SalesOrder;
import top.huanyu666.backend.modules.sales.mapper.SalesOrderMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SalesOrderMapper salesOrderMapper;
    private final InvStockMapper invStockMapper;

    @SaCheckLogin
    @GetMapping("/sales-trend")
    public ApiResponse<Map<String, Object>> salesTrend() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d.toString());
            LambdaQueryWrapper<SalesOrder> qw = new LambdaQueryWrapper<>();
            qw.eq(SalesOrder::getOrderDate, d);
            BigDecimal dayTotal = salesOrderMapper.selectList(qw).stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayTotal);
        }
        return ApiResponse.ok(Map.of("dates", dates, "amounts", amounts));
    }

    @SaCheckLogin
    @GetMapping("/order-status-dist")
    public ApiResponse<List<Map<String, Object>>> orderStatusDist() {
        List<SalesOrder> all = salesOrderMapper.selectList(null);
        Map<String, Long> dist = all.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN",
                        Collectors.counting()));
        List<Map<String, Object>> result = dist.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("status", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                }).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @SaCheckLogin
    @GetMapping("/stock-dist")
    public ApiResponse<List<Map<String, Object>>> stockDist() {
        List<InvStock> all = invStockMapper.selectList(null);
        Map<Long, BigDecimal> whMap = new LinkedHashMap<>();
        for (InvStock s : all) {
            whMap.merge(s.getWarehouseId(), s.getQuantity() != null ? s.getQuantity() : BigDecimal.ZERO, BigDecimal::add);
        }
        List<Map<String, Object>> result = whMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("warehouseId", e.getKey());
                    m.put("quantity", e.getValue());
                    return m;
                }).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }
}
