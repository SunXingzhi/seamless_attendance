package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.entity.*;
import tech.xuexinglab.seamless_attendance.service.interfaces.StatisticsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seamless_attendance/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 用户每日统计 ====================

    /**
     * 获取用户每日统计数据
     */
    @GetMapping("/user/daily/{userNumber}")
    public ResponseDTO<?> getUserDailyStats(
            @PathVariable String userNumber,
            @RequestParam(required = false, defaultValue = "7") int limit) {

        try {
            List<UserDailyStats> stats = statisticsService.getUserDailyStatsList(userNumber, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每日统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定日期的每日统计
     */
    @GetMapping("/user/daily/{userNumber}/{date}")
    public ResponseDTO<?> getUserDailyStatsByDate(
            @PathVariable String userNumber,
            @PathVariable String date) {

        try {
            UserDailyStats stats = statisticsService.getUserDailyStats(userNumber, date);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定日期的统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每日统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 用户每周统计 ====================

    /**
     * 获取用户每周统计数据
     */
    @GetMapping("/user/weekly/{userNumber}")
    public ResponseDTO<?> getUserWeeklyStats(
            @PathVariable String userNumber,
            @RequestParam(required = false, defaultValue = "4") int limit) {

        try {
            List<UserWeeklyStats> stats = statisticsService.getUserWeeklyStatsList(userNumber, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每周统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定周的每周统计
     */
    @GetMapping("/user/weekly/{userNumber}/{weekStartDate}")
    public ResponseDTO<?> getUserWeeklyStatsByWeek(
            @PathVariable String userNumber,
            @PathVariable String weekStartDate) {

        try {
            UserWeeklyStats stats = statisticsService.getUserWeeklyStats(userNumber, weekStartDate);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定周的统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每周统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 用户每月统计 ====================

    /**
     * 获取用户每月统计数据
     */
    @GetMapping("/user/monthly/{userNumber}")
    public ResponseDTO<?> getUserMonthlyStats(
            @PathVariable String userNumber,
            @RequestParam(required = false, defaultValue = "12") int limit) {

        try {
            List<UserMonthlyStats> stats = statisticsService.getUserMonthlyStatsList(userNumber, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每月统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定月份的每月统计
     */
    @GetMapping("/user/monthly/{userNumber}/{year}/{month}")
    public ResponseDTO<?> getUserMonthlyStatsByMonth(
            @PathVariable String userNumber,
            @PathVariable int year,
            @PathVariable int month) {

        try {
            UserMonthlyStats stats = statisticsService.getUserMonthlyStats(userNumber, month, year);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定月份的统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每月统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 用户每年统计 ====================

    /**
     * 获取用户每年统计数据
     */
    @GetMapping("/user/yearly/{userNumber}")
    public ResponseDTO<?> getUserYearlyStats(
            @PathVariable String userNumber,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        try {
            List<UserYearlyStats> stats = statisticsService.getUserYearlyStatsList(userNumber, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每年统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定年份的每年统计
     */
    @GetMapping("/user/yearly/{userNumber}/{year}")
    public ResponseDTO<?> getUserYearlyStatsByYear(
            @PathVariable String userNumber,
            @PathVariable int year) {

        try {
            UserYearlyStats stats = statisticsService.getUserYearlyStats(userNumber, year);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定年份的统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取用户每年统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 工作室每日统计 ====================

    /**
     * 获取工作室每日统计数据
     */
    @GetMapping("/studio/daily/{studioId}")
    public ResponseDTO<?> getStudioDailyStats(
            @PathVariable Long studioId,
            @RequestParam(required = false, defaultValue = "7") int limit) {

        try {
            List<StudioDailyStats> stats = statisticsService.getStudioDailyStatsList(studioId, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每日统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取工作室指定日期的每日统计
     */
    @GetMapping("/studio/daily/{studioId}/{date}")
    public ResponseDTO<?> getStudioDailyStatsByDate(
            @PathVariable Long studioId,
            @PathVariable String date) {

        try {
            StudioDailyStats stats = statisticsService.getStudioDailyStats(studioId, date);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定日期的工作室统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每日统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 工作室每周统计 ====================

    /**
     * 获取工作室每周统计数据
     */
    @GetMapping("/studio/weekly/{studioId}")
    public ResponseDTO<?> getStudioWeeklyStats(
            @PathVariable Long studioId,
            @RequestParam(required = false, defaultValue = "4") int limit) {

        try {
            List<StudioWeeklyStats> stats = statisticsService.getStudioWeeklyStatsList(studioId, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每周统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取工作室指定周的每周统计
     */
    @GetMapping("/studio/weekly/{studioId}/{weekStartDate}")
    public ResponseDTO<?> getStudioWeeklyStatsByWeek(
            @PathVariable Long studioId,
            @PathVariable String weekStartDate) {

        try {
            StudioWeeklyStats stats = statisticsService.getStudioWeeklyStats(studioId, weekStartDate);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定周的工作室统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每周统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 工作室每月统计 ====================

    /**
     * 获取工作室每月统计数据
     */
    @GetMapping("/studio/monthly/{studioId}")
    public ResponseDTO<?> getStudioMonthlyStats(
            @PathVariable Long studioId,
            @RequestParam(required = false, defaultValue = "12") int limit) {

        try {
            List<StudioMonthlyStats> stats = statisticsService.getStudioMonthlyStatsList(studioId, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每月统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取工作室指定月份的每月统计
     */
    @GetMapping("/studio/monthly/{studioId}/{year}/{month}")
    public ResponseDTO<?> getStudioMonthlyStatsByMonth(
            @PathVariable Long studioId,
            @PathVariable int year,
            @PathVariable int month) {

        try {
            StudioMonthlyStats stats = statisticsService.getStudioMonthlyStats(studioId, month, year);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定月份的工作室统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每月统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 工作室每年统计 ====================

    /**
     * 获取工作室每年统计数据
     */
    @GetMapping("/studio/yearly/{studioId}")
    public ResponseDTO<?> getStudioYearlyStats(
            @PathVariable Long studioId,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        try {
            List<StudioYearlyStats> stats = statisticsService.getStudioYearlyStatsList(studioId, limit);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每年统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取工作室指定年份的每年统计
     */
    @GetMapping("/studio/yearly/{studioId}/{year}")
    public ResponseDTO<?> getStudioYearlyStatsByYear(
            @PathVariable Long studioId,
            @PathVariable int year) {

        try {
            StudioYearlyStats stats = statisticsService.getStudioYearlyStats(studioId, year);
            if (stats != null) {
                return ResponseDTO.success(stats);
            } else {
                return ResponseDTO.error("未找到指定年份的工作室统计数据");
            }
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室每年统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 综合统计查询 ====================

    /**
     * 获取工作室成员的每日统计数据
     */
    @GetMapping("/studio/{studioId}/members/daily/{date}")
    public ResponseDTO<?> getStudioUserDailyStats(
            @PathVariable Long studioId,
            @PathVariable String date) {

        try {
            List<UserDailyStats> stats = statisticsService.getStudioUserDailyStats(studioId, date);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室成员每日统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取工作室成员的月度统计数据
     */
    @GetMapping("/studio/{studioId}/members/monthly/{year}/{month}")
    public ResponseDTO<?> getStudioUserMonthlyStats(
            @PathVariable Long studioId,
            @PathVariable int year,
            @PathVariable int month) {

        try {
            List<UserMonthlyStats> stats = statisticsService.getStudioUserMonthlyStats(studioId, month, year);
            return ResponseDTO.success(stats);
        } catch (Exception e) {
            return ResponseDTO.error("获取工作室成员月度统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 统计概览 ====================

    /**
     * 获取统计概览数据
     */
    @GetMapping("/overview")
    public ResponseDTO<?> getStatisticsOverview(
            @RequestParam(required = false) String date) {

        try {
            final String targetDate = date != null ? date : LocalDate.now().format(DATE_FORMATTER);

            Map<String, Object> overview = new HashMap<>();

            // 获取今日所有用户的每日统计
            List<UserDailyStats> todayStats = statisticsService.getAllUserDailyStatsByDate(targetDate);

            // 计算概览数据
            int totalUsers = todayStats.size();
            int presentUsers = (int) todayStats.stream()
                    .filter(s -> !"absent".equals(s.getAttendanceStatus()))
                    .count();
            int lateUsers = (int) todayStats.stream()
                    .filter(s -> s.getLateMinutes() > 0)
                    .count();
            int earlyLeaveUsers = (int) todayStats.stream()
                    .filter(s -> s.getEarlyLeaveMinutes() > 0)
                    .count();

            double averageAttendanceRate = totalUsers > 0 ? (double) presentUsers / totalUsers * 100 : 0;
            double averageActivityScore = todayStats.stream()
                    .mapToDouble(UserDailyStats::getActivityScore)
                    .average().orElse(0.0);
            double averageWorkHours = todayStats.stream()
                    .mapToDouble(UserDailyStats::getWorkHours)
                    .average().orElse(0.0);

            overview.put("date", targetDate);
            overview.put("totalUsers", totalUsers);
            overview.put("presentUsers", presentUsers);
            overview.put("lateUsers", lateUsers);
            overview.put("earlyLeaveUsers", earlyLeaveUsers);
            overview.put("averageAttendanceRate", Math.round(averageAttendanceRate * 100.0) / 100.0);
            overview.put("averageActivityScore", Math.round(averageActivityScore * 100.0) / 100.0);
            overview.put("averageWorkHours", Math.round(averageWorkHours * 100.0) / 100.0);

            return ResponseDTO.success(overview);
        } catch (Exception e) {
            return ResponseDTO.error("获取统计概览数据失败: " + e.getMessage());
        }
    }

    // ==================== 手动触发统计计算 ====================

    /**
     * 手动触发每日统计计算
     */
    @PostMapping("/calculate/daily")
    public ResponseDTO<?> triggerDailyStatistics() {
        try {
            statisticsService.executeDailyStatisticsTask();
            return ResponseDTO.success("每日统计计算完成");
        } catch (Exception e) {
            return ResponseDTO.error("每日统计计算失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发每周统计计算
     */
    @PostMapping("/calculate/weekly")
    public ResponseDTO<?> triggerWeeklyStatistics() {
        try {
            statisticsService.executeWeeklyStatisticsTask();
            return ResponseDTO.success("每周统计计算完成");
        } catch (Exception e) {
            return ResponseDTO.error("每周统计计算失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发每月统计计算
     */
    @PostMapping("/calculate/monthly")
    public ResponseDTO<?> triggerMonthlyStatistics() {
        try {
            statisticsService.executeMonthlyStatisticsTask();
            return ResponseDTO.success("每月统计计算完成");
        } catch (Exception e) {
            return ResponseDTO.error("每月统计计算失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发每年统计计算
     */
    @PostMapping("/calculate/yearly")
    public ResponseDTO<?> triggerYearlyStatistics() {
        try {
            statisticsService.executeYearlyStatisticsTask();
            return ResponseDTO.success("每年统计计算完成");
        } catch (Exception e) {
            return ResponseDTO.error("每年统计计算失败: " + e.getMessage());
        }
    }
}
