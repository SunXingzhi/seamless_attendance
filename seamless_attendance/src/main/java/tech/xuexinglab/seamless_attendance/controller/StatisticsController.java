package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.service.interfaces.userService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/seamless_attendance/api/statistics")
public class StatisticsController {

    @Autowired
    private userService userService;

    /**
     * 获取统计数据
     */
    @GetMapping
    public ResponseDTO<?> getStatistics(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String statisticsType,
            @RequestParam(required = false) String selectedPerson,
            @RequestParam(required = false) String statisticsContent) {
        
        // 模拟统计数据
        Map<String, Object> statisticsData = new HashMap<>();
        statisticsData.put("activeCount", 25);
        statisticsData.put("lateCount", 3);
        statisticsData.put("leaveCount", 2);
        statisticsData.put("absentCount", 1);
        statisticsData.put("holidayCount", 0);
        statisticsData.put("excusedCount", 0);
        statisticsData.put("attendanceRate", 85);
        
        return ResponseDTO.success(statisticsData);
    }

    /**
     * 获取活跃度统计
     */
    @GetMapping("/activity")
    public ResponseDTO<?> getActivityStatistics(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // 模拟活跃度数据
        List<Map<String, Object>> activityData = new ArrayList<>();
        Random random = new Random();
        String[] levels = {"high", "medium", "low", "none"};
        
        // 生成365天的活跃度数据
        for (int i = 0; i < 365; i++) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", getDateBefore(i));
            dayData.put("level", levels[random.nextInt(levels.length)]);
            activityData.add(dayData);
        }
        
        return ResponseDTO.success(activityData);
    }

    /**
     * 获取趋势分析
     */
    @GetMapping("/trend")
    public ResponseDTO<?> getTrendAnalysis(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String statisticsContent) {
        
        // 模拟趋势数据
        List<Integer> trendData = new ArrayList<>();
        Random random = new Random();
        
        // 生成12个数据点
        for (int i = 0; i < 12; i++) {
            trendData.add(random.nextInt(40) + 60); // 60-100之间的随机数
        }
        
        return ResponseDTO.success(trendData);
    }

    /**
     * 获取出勤率统计
     */
    @GetMapping("/attendance-rate")
    public ResponseDTO<?> getAttendanceRate(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // 模拟出勤率数据
        Map<String, Object> attendanceRateData = new HashMap<>();
        attendanceRateData.put("averageRate", 85);
        attendanceRateData.put("maxRate", 100);
        attendanceRateData.put("minRate", 60);
        
        return ResponseDTO.success(attendanceRateData);
    }

    /**
     * 获取满勤次数
     */
    @GetMapping("/full-attendance")
    public ResponseDTO<?> getFullAttendanceCount(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // 模拟满勤次数数据
        Map<String, Object> fullAttendanceData = new HashMap<>();
        fullAttendanceData.put("count", 15);
        
        return ResponseDTO.success(fullAttendanceData);
    }

    /**
     * 生成统计报告
     */
    @PostMapping("/report")
    public ResponseDTO<?> generateReport() {
        
        // 模拟报告生成
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("success", true);
        reportData.put("message", "报告生成成功");
        
        return ResponseDTO.success(reportData);
    }

    /**
     * 获取指定天数前的日期
     */
    private String getDateBefore(int days) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar.getTime());
    }
}
