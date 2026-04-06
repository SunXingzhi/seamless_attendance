package tech.xuexinglab.seamless_attendance.service.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.xuexinglab.seamless_attendance.entity.*;
import tech.xuexinglab.seamless_attendance.mapper.StatisticsMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import tech.xuexinglab.seamless_attendance.mapper.studioMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class StatisticsTestDataService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    @Autowired
    private userMapper userMapper;
    
    @Autowired
    private studioMapper studioMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Random random = new Random();
    
    /**
     * 生成完整的测试统计数据
     */
    @Transactional
    public void generateCompleteTestData() {
        System.out.println("开始生成统计测试数据...");
        
        // 1. 生成用户每日统计
        generateUserDailyStats();
        
        // 2. 生成用户每周统计
        generateUserWeeklyStats();
        
        // 3. 生成用户每月统计
        generateUserMonthlyStats();
        
        // 4. 生成用户每年统计
        generateUserYearlyStats();
        
        // 5. 生成工作室统计
        generateStudioStats();
        
        System.out.println("统计测试数据生成完成！");
    }
    
    /**
     * 生成用户每日统计测试数据
     */
    private void generateUserDailyStats() {
        System.out.println("生成用户每日统计测试数据...");
        
        // 获取所有用户
        List<user> allUsers = userMapper.getAllUserInfo();
        if (allUsers.isEmpty()) {
            System.out.println("没有找到用户，跳过每日统计数据生成");
            return;
        }
        
        LocalDate today = LocalDate.now();
        List<UserDailyStats> statsList = new ArrayList<>();
        
        for (user u : allUsers) {
            // 为每个用户生成最近7天的数据
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(DATE_FORMATTER);
                
                // 随机生成考勤数据
                boolean isPresent = random.nextDouble() > 0.2; // 80%出勤率
                boolean isLate = isPresent && random.nextDouble() > 0.7; // 30%迟到率
                boolean isEarlyLeave = isPresent && random.nextDouble() > 0.8; // 20%早退率
                
                double workHours = isPresent ? 7.5 + random.nextDouble() * 2.0 : 0.0; // 7.5-9.5小时
                int lateMinutes = isLate ? random.nextInt(60) + 1 : 0; // 1-60分钟
                int earlyLeaveMinutes = isEarlyLeave ? random.nextInt(120) + 1 : 0; // 1-120分钟
                
                String attendanceStatus;
                if (!isPresent) {
                    attendanceStatus = random.nextDouble() > 0.5 ? "absent" : "excused";
                } else if (isLate) {
                    attendanceStatus = "late";
                } else if (isEarlyLeave) {
                    attendanceStatus = "leave";
                } else {
                    attendanceStatus = "active";
                }
                
                double activityScore = calculateActivityScore(workHours, attendanceStatus, lateMinutes, earlyLeaveMinutes);
                
                String checkInTime = isPresent ? generateRandomTime(8, 10) : null;
                String checkOutTime = isPresent ? generateRandomTime(16, 18) : null;
                
                UserDailyStats stats = new UserDailyStats();
                stats.setUserNumber(u.getUserNumber());
                stats.setDate(dateStr);
                stats.setWorkHours(workHours);
                stats.setActivityScore(activityScore);
                stats.setAttendanceStatus(attendanceStatus);
                stats.setCheckInTime(checkInTime);
                stats.setCheckOutTime(checkOutTime);
                stats.setLateMinutes(lateMinutes);
                stats.setEarlyLeaveMinutes(earlyLeaveMinutes);
                
                String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
                stats.setCreateTime(currentTime);
                stats.setUpdateTime(currentTime);
                
                statsList.add(stats);
            }
        }
        
        // 批量插入
        if (!statsList.isEmpty()) {
            for (UserDailyStats stats : statsList) {
                // 检查是否已存在
                UserDailyStats existing = statisticsMapper.getUserDailyStats(stats.getUserNumber(), stats.getDate());
                if (existing == null) {
                    statisticsMapper.insertUserDailyStats(stats);
                }
            }
            System.out.println("已生成 " + statsList.size() + " 条用户每日统计数据");
        }
    }
    
    /**
     * 生成用户每周统计测试数据
     */
    private void generateUserWeeklyStats() {
        System.out.println("生成用户每周统计测试数据...");
        
        List<user> allUsers = userMapper.getAllUserInfo();
        if (allUsers.isEmpty()) {
            System.out.println("没有找到用户，跳过每周统计数据生成");
            return;
        }
        
        LocalDate today = LocalDate.now();
        
        for (user u : allUsers) {
            // 为每个用户生成最近4周的数据
            for (int week = 3; week >= 0; week--) {
                LocalDate weekStart = today.minusWeeks(week).with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                String weekStartStr = weekStart.format(DATE_FORMATTER);
                String weekEndStr = weekEnd.format(DATE_FORMATTER);
                
                // 随机生成周统计
                int totalWorkDays = random.nextInt(5) + 3; // 3-7天
                double totalWorkHours = totalWorkDays * (7.5 + random.nextDouble() * 2.0);
                double attendanceRate = 70.0 + random.nextDouble() * 25.0; // 70-95%
                double activityScore = 60.0 + random.nextDouble() * 35.0; // 60-95%
                int lateCount = random.nextInt(3);
                int earlyLeaveCount = random.nextInt(2);
                int absentCount = random.nextInt(2);
                
                UserWeeklyStats stats = new UserWeeklyStats();
                stats.setUserNumber(u.getUserNumber());
                stats.setWeekStartDate(weekStartStr);
                stats.setWeekEndDate(weekEndStr);
                stats.setTotalWorkDays(totalWorkDays);
                stats.setTotalWorkHours(totalWorkHours);
                stats.setAttendanceRate(attendanceRate);
                stats.setActivityScore(activityScore);
                stats.setLateCount(lateCount);
                stats.setEarlyLeaveCount(earlyLeaveCount);
                stats.setAbsentCount(absentCount);
                
                String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
                stats.setCreateTime(currentTime);
                stats.setUpdateTime(currentTime);
                
                // 检查是否已存在
                UserWeeklyStats existing = statisticsMapper.getUserWeeklyStats(u.getUserNumber(), weekStartStr);
                if (existing == null) {
                    statisticsMapper.insertUserWeeklyStats(stats);
                }
            }
        }
        System.out.println("用户每周统计测试数据生成完成");
    }
    
    /**
     * 生成用户每月统计测试数据
     */
    private void generateUserMonthlyStats() {
        System.out.println("生成用户每月统计测试数据...");
        
        List<user> allUsers = userMapper.getAllUserInfo();
        if (allUsers.isEmpty()) {
            System.out.println("没有找到用户，跳过每月统计数据生成");
            return;
        }
        
        LocalDate today = LocalDate.now();
        
        for (user u : allUsers) {
            // 为每个用户生成最近3个月的数据
            for (int monthOffset = 2; monthOffset >= 0; monthOffset--) {
                LocalDate monthDate = today.minusMonths(monthOffset);
                int month = monthDate.getMonthValue();
                int year = monthDate.getYear();
                
                // 随机生成月统计
                int totalWorkDays = random.nextInt(15) + 15; // 15-30天
                double totalWorkHours = totalWorkDays * (7.5 + random.nextDouble() * 2.0);
                double attendanceRate = 75.0 + random.nextDouble() * 20.0; // 75-95%
                double activityScore = 65.0 + random.nextDouble() * 30.0; // 65-95%
                int lateCount = random.nextInt(5);
                int earlyLeaveCount = random.nextInt(4);
                int absentCount = random.nextInt(3);
                int leaveDays = random.nextInt(3);
                double overtimeHours = random.nextDouble() * 20.0; // 0-20小时
                
                UserMonthlyStats stats = new UserMonthlyStats();
                stats.setUserNumber(u.getUserNumber());
                stats.setMonth(month);
                stats.setYear(year);
                stats.setTotalWorkDays(totalWorkDays);
                stats.setTotalWorkHours(totalWorkHours);
                stats.setAttendanceRate(attendanceRate);
                stats.setActivityScore(activityScore);
                stats.setLateCount(lateCount);
                stats.setEarlyLeaveCount(earlyLeaveCount);
                stats.setAbsentCount(absentCount);
                stats.setLeaveDays(leaveDays);
                stats.setOvertimeHours(overtimeHours);
                
                String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
                stats.setCreateTime(currentTime);
                stats.setUpdateTime(currentTime);
                
                // 检查是否已存在
                UserMonthlyStats existing = statisticsMapper.getUserMonthlyStats(u.getUserNumber(), month, year);
                if (existing == null) {
                    statisticsMapper.insertUserMonthlyStats(stats);
                }
            }
        }
        System.out.println("用户每月统计测试数据生成完成");
    }
    
    /**
     * 生成用户每年统计测试数据
     */
    private void generateUserYearlyStats() {
        System.out.println("生成用户每年统计测试数据...");
        
        List<user> allUsers = userMapper.getAllUserInfo();
        if (allUsers.isEmpty()) {
            System.out.println("没有找到用户，跳过每年统计数据生成");
            return;
        }
        
        int currentYear = LocalDate.now().getYear();
        
        for (user u : allUsers) {
            // 为每个用户生成最近3年的数据
            for (int yearOffset = 2; yearOffset >= 0; yearOffset--) {
                int year = currentYear - yearOffset;
                
                // 随机生成年统计
                int totalWorkDays = random.nextInt(100) + 200; // 200-300天
                double totalWorkHours = totalWorkDays * (7.5 + random.nextDouble() * 2.0);
                double attendanceRate = 80.0 + random.nextDouble() * 15.0; // 80-95%
                double activityScore = 70.0 + random.nextDouble() * 25.0; // 70-95%
                int lateCount = random.nextInt(20);
                int earlyLeaveCount = random.nextInt(15);
                int absentCount = random.nextInt(10);
                int leaveDays = random.nextInt(15);
                double overtimeHours = random.nextDouble() * 100.0; // 0-100小时
                double performanceScore = 75.0 + random.nextDouble() * 20.0; // 75-95%
                
                UserYearlyStats stats = new UserYearlyStats();
                stats.setUserNumber(u.getUserNumber());
                stats.setYear(year);
                stats.setTotalWorkDays(totalWorkDays);
                stats.setTotalWorkHours(totalWorkHours);
                stats.setAttendanceRate(attendanceRate);
                stats.setActivityScore(activityScore);
                stats.setLateCount(lateCount);
                stats.setEarlyLeaveCount(earlyLeaveCount);
                stats.setAbsentCount(absentCount);
                stats.setLeaveDays(leaveDays);
                stats.setOvertimeHours(overtimeHours);
                stats.setPerformanceScore(performanceScore);
                
                String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
                stats.setCreateTime(currentTime);
                stats.setUpdateTime(currentTime);
                
                // 检查是否已存在
                UserYearlyStats existing = statisticsMapper.getUserYearlyStats(u.getUserNumber(), year);
                if (existing == null) {
                    statisticsMapper.insertUserYearlyStats(stats);
                }
            }
        }
        System.out.println("用户每年统计测试数据生成完成");
    }
    
    /**
     * 生成工作室统计测试数据
     */
    private void generateStudioStats() {
        System.out.println("生成工作室统计测试数据...");
        
        List<studio> allStudios = studioMapper.getAllStudios();
        if (allStudios.isEmpty()) {
            System.out.println("没有找到工作室，跳过工作室统计数据生成");
            return;
        }
        
        // 这里简化处理，只生成第一个工作室的数据
        if (!allStudios.isEmpty()) {
            studio firstStudio = allStudios.get(0);
            generateStudioDailyStats(firstStudio);
            generateStudioWeeklyStats(firstStudio);
            generateStudioMonthlyStats(firstStudio);
            generateStudioYearlyStats(firstStudio);
        }
    }
    
    /**
     * 生成工作室每日统计
     */
    private void generateStudioDailyStats(studio studio) {
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            
            int totalUsers = 3; // 假设3个用户
            int presentUsers = random.nextInt(totalUsers + 1);
            double attendanceRate = totalUsers > 0 ? (double) presentUsers / totalUsers * 100 : 0.0;
            double activityRate = 60.0 + random.nextDouble() * 35.0;
            double averageWorkHours = 6.0 + random.nextDouble() * 3.0;
            int lateCount = random.nextInt(3);
            int earlyLeaveCount = random.nextInt(2);
            
            StudioDailyStats stats = new StudioDailyStats();
            stats.setStudioId(studio.getId().longValue());
            stats.setDate(dateStr);
            stats.setTotalUsers(totalUsers);
            stats.setPresentUsers(presentUsers);
            stats.setAttendanceRate(attendanceRate);
            stats.setActivityRate(activityRate);
            stats.setAverageWorkHours(averageWorkHours);
            stats.setLateCount(lateCount);
            stats.setEarlyLeaveCount(earlyLeaveCount);
            
            String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
            stats.setCreateTime(currentTime);
            stats.setUpdateTime(currentTime);
            
            // 检查是否已存在
            StudioDailyStats existing = statisticsMapper.getStudioDailyStats(studio.getId().longValue(), dateStr);
            if (existing == null) {
                statisticsMapper.insertStudioDailyStats(stats);
            }
        }
    }
    
    /**
     * 生成工作室每周统计（类似逻辑，省略详细实现）
     */
    private void generateStudioWeeklyStats(studio studio) {
        // 实现类似generateStudioDailyStats的逻辑
    }
    
    /**
     * 生成工作室每月统计（类似逻辑，省略详细实现）
     */
    private void generateStudioMonthlyStats(studio studio) {
        // 实现类似generateStudioDailyStats的逻辑
    }
    
    /**
     * 生成工作室每年统计（类似逻辑，省略详细实现）
     */
    private void generateStudioYearlyStats(studio studio) {
        // 实现类似generateStudioDailyStats的逻辑
    }
    
    /**
     * 计算活跃度分数
     */
    private double calculateActivityScore(double workHours, String status, int lateMinutes, int earlyLeaveMinutes) {
        double score = 0.0;
        
        // 基础分数：工作时间（假设8小时满分）
        score += Math.min(workHours / 8.0 * 60, 60);
        
        // 状态加成
        if ("active".equals(status)) {
            score += 20;
        } else if ("excused".equals(status)) {
            score += 10;
        } else if ("holiday".equals(status)) {
            score += 5;
        }
        
        // 扣分：迟到早退
        score -= Math.min(lateMinutes + earlyLeaveMinutes, 20);
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * 生成随机时间
     */
    private String generateRandomTime(int startHour, int endHour) {
        int hour = startHour + random.nextInt(endHour - startHour + 1);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
    
    /**
     * 手动触发测试数据生成（通过HTTP接口或直接调用）
     */
    public String triggerTestDataGeneration() {
        try {
            generateCompleteTestData();
            return "测试数据生成成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "测试数据生成失败：" + e.getMessage();
        }
    }
}