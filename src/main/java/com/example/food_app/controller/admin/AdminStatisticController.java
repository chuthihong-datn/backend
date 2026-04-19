package com.example.food_app.controller.admin;

import com.example.food_app.dto.response.admin.*;
import com.example.food_app.service.admin.AdminStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatisticController {

    private final AdminStatisticService adminStatisticService;

    @GetMapping("/daily")
    public List<DailyStatsResponse> daily() {
        return adminStatisticService.getDailyStats();
    }

    @GetMapping("/hourly")
    public List<HourlyStatsResponse> hourly() {
        return adminStatisticService.getHourlyStats();
    }

    @GetMapping("/monthly")
    public List<MonthlyStatsResponse> monthly() {
        return adminStatisticService.getMonthlyStats();
    }

    @GetMapping("/today-menu")
    public List<TodayMenuStatisticResponse> getTodayMenu() {
        return adminStatisticService.getTodayMenuStatistic();
    }

    @GetMapping("/monthly-menu")
    public List<TodayMenuStatisticResponse> getMonthlyMenu(
            @RequestParam int year,
            @RequestParam int month) {

        return adminStatisticService.getMonthlyMenuStatistic(year, month);
    }
}