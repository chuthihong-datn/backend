package com.example.food_app.service.admin;

import com.example.food_app.dto.response.admin.*;
import com.example.food_app.repository.OrderDetailRepository;
import com.example.food_app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatisticService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    // Theo ngày
    public List<DailyStatsResponse> getDailyStats() {

        return orderRepository.getRevenueByDay()
                .stream()
                .map(r -> new DailyStatsResponse(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        (BigDecimal) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    // Theo giờ
    public List<HourlyStatsResponse> getHourlyStats() {

        return orderRepository.getRevenueByHour()
                .stream()
                .map(r -> new HourlyStatsResponse(
                        ((Number) r[0]).intValue(),
                        (BigDecimal) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    // theo tháng
    public List<MonthlyStatsResponse> getMonthlyStats() {

        return orderRepository.getRevenueByMonth()
                .stream()
                .map(r -> new MonthlyStatsResponse(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).intValue(),
                        (BigDecimal) r[2],
                        ((Number) r[3]).longValue()
                ))
                .toList();
    }

    public List<TodayMenuStatisticResponse> getTodayMenuStatistic() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        return orderDetailRepository.getTodaySoldMenu(start, end)
                .stream()
                .map(o -> new TodayMenuStatisticResponse(
                        ((Number) o[0]).longValue(),
                        (String) o[1],
                        ((Number) o[2]).longValue()
                ))
                .toList();
    }

    public List<TodayMenuStatisticResponse> getMonthlyMenuStatistic(int year, int month) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);

        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay
                .withDayOfMonth(firstDay.lengthOfMonth())
                .atTime(23, 59, 59);

        return orderDetailRepository.getSoldMenuByRange(start, end)
                .stream()
                .map(o -> new TodayMenuStatisticResponse(
                        ((Number) o[0]).longValue(),
                        (String) o[1],
                        ((Number) o[2]).longValue()
                ))
                .toList();
    }
}
