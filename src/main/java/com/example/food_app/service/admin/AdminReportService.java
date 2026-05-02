package com.example.food_app.service.admin;

import com.example.food_app.dto.response.admin.MonthlyStatsResponse;
import com.example.food_app.dto.response.admin.TodayMenuStatisticResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

            // sheet 1: doanh thu
            Sheet sheet1 = workbook.createSheet("Revenue");

            Row header1 = sheet1.createRow(0);
            header1.createCell(0).setCellValue("Year");
            header1.createCell(1).setCellValue("Month");
            header1.createCell(2).setCellValue("Revenue");
            header1.createCell(3).setCellValue("Order Count");

            int row1 = 1;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalOrders = 0;

            for (MonthlyStatsResponse s : monthlyStats) {
                Row row = sheet1.createRow(row1++);

                row.createCell(0).setCellValue(s.getYear());
                row.createCell(1).setCellValue(s.getMonth());
                row.createCell(2).setCellValue(
                        s.getRevenue() != null ? s.getRevenue().doubleValue() : 0
                );
                row.createCell(3).setCellValue(s.getOrderCount());

                if (s.getRevenue() != null) {
                    totalRevenue = totalRevenue.add(s.getRevenue());
                }
                totalOrders += s.getOrderCount();
            }

            // TOTAL revenue sheet
            Row totalRow1 = sheet1.createRow(row1);
            totalRow1.createCell(1).setCellValue("TOTAL");
            totalRow1.createCell(2).setCellValue(totalRevenue.doubleValue());
            totalRow1.createCell(3).setCellValue(totalOrders);

            //sheet 2: số lượng bán
            Sheet sheet2 = workbook.createSheet("Sold Menu");

            Row header2 = sheet2.createRow(0);
            header2.createCell(0).setCellValue("Product ID");
            header2.createCell(1).setCellValue("Product Name");
            header2.createCell(2).setCellValue("Quantity Sold");

            int row2 = 1;
            long totalItems = 0;

            for (TodayMenuStatisticResponse m : menuStats) {

                Row row = sheet2.createRow(row2++);

                row.createCell(0).setCellValue(m.getMenuId().longValue());
                row.createCell(1).setCellValue(m.getMenuName());
                row.createCell(2).setCellValue(m.getTotalQuantity());

                totalItems += m.getTotalQuantity();
            }

            Row totalRow2 = sheet2.createRow(row2);
            totalRow2.createCell(1).setCellValue("TOTAL");
            totalRow2.createCell(2).setCellValue(totalItems);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }
}
