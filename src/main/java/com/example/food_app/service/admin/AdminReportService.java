package com.example.food_app.service.admin;

import com.example.food_app.dto.response.admin.MonthlyStatsResponse;
import com.example.food_app.dto.response.admin.TodayMenuStatisticResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    private final AdminStatisticService adminStatisticService;

    public ByteArrayInputStream exportMonthlyFullReport(int year, int month) {

        List<MonthlyStatsResponse> monthlyStats = adminStatisticService.getMonthlyStats()
                .stream()
                .filter(s -> s.getYear() == year && s.getMonth() == month)
                .toList();

        List<TodayMenuStatisticResponse> menuStats =
                adminStatisticService.getMonthlyMenuStatistic(year, month);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    IndexedColors.GREY_25_PERCENT.getIndex()
            );
            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            CellStyle totalStyle = workbook.createCellStyle();

            Font totalFont = workbook.createFont();
            totalFont.setBold(true);

            totalStyle.setFont(totalFont);

            Sheet sheet1 = workbook.createSheet("Revenue");

            Row titleRow1 = sheet1.createRow(0);
            titleRow1.createCell(0)
                    .setCellValue("REVENUE REPORT - " + month + "/" + year);

            Row header1 = sheet1.createRow(2);

            Cell h1 = header1.createCell(0);
            h1.setCellValue("Year");
            h1.setCellStyle(headerStyle);

            Cell h2 = header1.createCell(1);
            h2.setCellValue("Month");
            h2.setCellStyle(headerStyle);

            Cell h3 = header1.createCell(2);
            h3.setCellValue("Revenue (VND)");
            h3.setCellStyle(headerStyle);

            Cell h4 = header1.createCell(3);
            h4.setCellValue("Order Count");
            h4.setCellStyle(headerStyle);

            int row1 = 3;

            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalOrders = 0;

            for (MonthlyStatsResponse s : monthlyStats) {

                Row row = sheet1.createRow(row1++);

                row.createCell(0).setCellValue(s.getYear());
                row.createCell(1).setCellValue(s.getMonth());

                row.createCell(2).setCellValue(
                        s.getRevenue() != null
                                ? s.getRevenue().doubleValue()
                                : 0
                );

                row.createCell(3).setCellValue(s.getOrderCount());

                if (s.getRevenue() != null) {
                    totalRevenue = totalRevenue.add(s.getRevenue());
                }

                totalOrders += s.getOrderCount();
            }

            Row totalRow1 = sheet1.createRow(row1);

            Cell totalCell1 = totalRow1.createCell(0);
            totalCell1.setCellValue("TOTAL");
            totalCell1.setCellStyle(totalStyle);

            Cell totalRevenueCell = totalRow1.createCell(2);
            totalRevenueCell.setCellValue(totalRevenue.doubleValue());
            totalRevenueCell.setCellStyle(totalStyle);

            Cell totalOrderCell = totalRow1.createCell(3);
            totalOrderCell.setCellValue(totalOrders);
            totalOrderCell.setCellStyle(totalStyle);

            for (int i = 0; i < 4; i++) {
                sheet1.autoSizeColumn(i);
            }

            Sheet sheet2 = workbook.createSheet("Sold Menu");

            Row titleRow2 = sheet2.createRow(0);
            titleRow2.createCell(0)
                    .setCellValue("SOLD MENU REPORT - " + month + "/" + year);

            Row header2 = sheet2.createRow(2);

            Cell s1 = header2.createCell(0);
            s1.setCellValue("Product ID");
            s1.setCellStyle(headerStyle);

            Cell s2 = header2.createCell(1);
            s2.setCellValue("Product Name");
            s2.setCellStyle(headerStyle);

            Cell s3 = header2.createCell(2);
            s3.setCellValue("Quantity Sold");
            s3.setCellStyle(headerStyle);

            int row2 = 3;
            long totalItems = 0;

            for (TodayMenuStatisticResponse m : menuStats) {

                Row row = sheet2.createRow(row2++);

                row.createCell(0).setCellValue(m.getMenuId().longValue());
                row.createCell(1).setCellValue(m.getMenuName());
                row.createCell(2).setCellValue(m.getTotalQuantity());

                totalItems += m.getTotalQuantity();
            }

            Row totalRow2 = sheet2.createRow(row2);

            Cell totalMenuCell = totalRow2.createCell(0);
            totalMenuCell.setCellValue("TOTAL");
            totalMenuCell.setCellStyle(totalStyle);

            Cell totalItemCell = totalRow2.createCell(2);
            totalItemCell.setCellValue(totalItems);
            totalItemCell.setCellStyle(totalStyle);

            for (int i = 0; i < 3; i++) {
                sheet2.autoSizeColumn(i);
            }

            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }
}