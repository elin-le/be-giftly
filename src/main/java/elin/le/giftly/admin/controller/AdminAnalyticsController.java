package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AdminAnalyticsSummaryResponse;
import elin.le.giftly.admin.dto.AdminDailyRevenueResponse;
import elin.le.giftly.admin.service.AdminAnalyticsService;
import elin.le.giftly.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Analytics", description = "Sales and revenue analytics for administrators")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/overview")
    @Operation(summary = "Get revenue, average order value and order metrics")
    public ResponseEntity<ApiResponse<AdminAnalyticsSummaryResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getSummary(from, to)));
    }

    @GetMapping("/revenue/daily")
    @Operation(summary = "Get daily paid revenue time series")
    public ResponseEntity<ApiResponse<List<AdminDailyRevenueResponse>>> getDailyRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getDailyRevenue(from, to)));
    }
}