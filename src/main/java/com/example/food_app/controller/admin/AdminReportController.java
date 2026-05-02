package com.example.food_app.controller.admin;

import com.example.food_app.service.admin.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<byte[]> exportMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {

        ByteArrayInputStream file =
                adminReportService.exportMonthlyFullReport(year, month);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition",
                "attachment; filename=report_" + month + "_" + year + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(file.readAllBytes());
    }
}