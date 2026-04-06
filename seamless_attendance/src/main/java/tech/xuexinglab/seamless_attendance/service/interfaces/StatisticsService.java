package tech.xuexinglab.seamless_attendance.service.interfaces;

import tech.xuexinglab.seamless_attendance.entity.*;
import java.util.List;

public interface StatisticsService {

    // ==================== 用户每日统计 ====================

    /**
     * 计算并保存用户每日统计数据
     */
    void calculateAndSaveUserDailyStats(String userNumber, String date);

    /**
     * 批量计算并保存所有用户的每日统计数据
     */
    void calculateAndSaveAllUserDailyStats(String date);

    /**
     * 获取用户每日统计数据
     */
    UserDailyStats getUserDailyStats(String userNumber, String date);

    /**
     * 获取用户最近的每日统计数据列表
     */
    List<UserDailyStats> getUserDailyStatsList(String userNumber, int limit);

    /**
     * 获取指定日期的所有用户每日统计数据
     */
    List<UserDailyStats> getAllUserDailyStatsByDate(String date);

    // ==================== 用户每周统计 ====================

    /**
     * 计算并保存用户每周统计数据
     */
    void calculateAndSaveUserWeeklyStats(String userNumber, String weekStartDate);

    /**
     * 批量计算并保存所有用户的每周统计数据
     */
    void calculateAndSaveAllUserWeeklyStats(String weekStartDate);

    /**
     * 获取用户每周统计数据
     */
    UserWeeklyStats getUserWeeklyStats(String userNumber, String weekStartDate);

    /**
     * 获取用户最近的每周统计数据列表
     */
    List<UserWeeklyStats> getUserWeeklyStatsList(String userNumber, int limit);

    // ==================== 用户每月统计 ====================

    /**
     * 计算并保存用户每月统计数据
     */
    void calculateAndSaveUserMonthlyStats(String userNumber, int month, int year);

    /**
     * 批量计算并保存所有用户的每月统计数据
     */
    void calculateAndSaveAllUserMonthlyStats(int month, int year);

    /**
     * 获取用户每月统计数据
     */
    UserMonthlyStats getUserMonthlyStats(String userNumber, int month, int year);

    /**
     * 获取用户最近的每月统计数据列表
     */
    List<UserMonthlyStats> getUserMonthlyStatsList(String userNumber, int limit);

    // ==================== 用户每年统计 ====================

    /**
     * 计算并保存用户每年统计数据
     */
    void calculateAndSaveUserYearlyStats(String userNumber, int year);

    /**
     * 批量计算并保存所有用户的每年统计数据
     */
    void calculateAndSaveAllUserYearlyStats(int year);

    /**
     * 获取用户每年统计数据
     */
    UserYearlyStats getUserYearlyStats(String userNumber, int year);

    /**
     * 获取用户最近的每年统计数据列表
     */
    List<UserYearlyStats> getUserYearlyStatsList(String userNumber, int limit);

    // ==================== 工作室每日统计 ====================

    /**
     * 计算并保存工作室每日统计数据
     */
    void calculateAndSaveStudioDailyStats(Long studioId, String date);

    /**
     * 批量计算并保存所有工作室的每日统计数据
     */
    void calculateAndSaveAllStudioDailyStats(String date);

    /**
     * 获取工作室每日统计数据
     */
    StudioDailyStats getStudioDailyStats(Long studioId, String date);

    /**
     * 获取工作室最近的每日统计数据列表
     */
    List<StudioDailyStats> getStudioDailyStatsList(Long studioId, int limit);

    // ==================== 工作室每周统计 ====================

    /**
     * 计算并保存工作室每周统计数据
     */
    void calculateAndSaveStudioWeeklyStats(Long studioId, String weekStartDate);

    /**
     * 批量计算并保存所有工作室的每周统计数据
     */
    void calculateAndSaveAllStudioWeeklyStats(String weekStartDate);

    /**
     * 获取工作室每周统计数据
     */
    StudioWeeklyStats getStudioWeeklyStats(Long studioId, String weekStartDate);

    /**
     * 获取工作室最近的每周统计数据列表
     */
    List<StudioWeeklyStats> getStudioWeeklyStatsList(Long studioId, int limit);

    // ==================== 工作室每月统计 ====================

    /**
     * 计算并保存工作室每月统计数据
     */
    void calculateAndSaveStudioMonthlyStats(Long studioId, int month, int year);

    /**
     * 批量计算并保存所有工作室的每月统计数据
     */
    void calculateAndSaveAllStudioMonthlyStats(int month, int year);

    /**
     * 获取工作室每月统计数据
     */
    StudioMonthlyStats getStudioMonthlyStats(Long studioId, int month, int year);

    /**
     * 获取工作室最近的每月统计数据列表
     */
    List<StudioMonthlyStats> getStudioMonthlyStatsList(Long studioId, int limit);

    // ==================== 工作室每年统计 ====================

    /**
     * 计算并保存工作室每年统计数据
     */
    void calculateAndSaveStudioYearlyStats(Long studioId, int year);

    /**
     * 批量计算并保存所有工作室的每年统计数据
     */
    void calculateAndSaveAllStudioYearlyStats(int year);

    /**
     * 获取工作室每年统计数据
     */
    StudioYearlyStats getStudioYearlyStats(Long studioId, int year);

    /**
     * 获取工作室最近的每年统计数据列表
     */
    List<StudioYearlyStats> getStudioYearlyStatsList(Long studioId, int limit);

    // ==================== 综合查询 ====================

    /**
     * 获取工作室成员的每日统计数据
     */
    List<UserDailyStats> getStudioUserDailyStats(Long studioId, String date);

    /**
     * 获取工作室成员的月度统计数据
     */
    List<UserMonthlyStats> getStudioUserMonthlyStats(Long studioId, int month, int year);

    /**
     * 执行每日统计任务（定时任务调用）
     */
    void executeDailyStatisticsTask();

    /**
     * 执行每周统计任务（定时任务调用）
     */
    void executeWeeklyStatisticsTask();

    /**
     * 执行每月统计任务（定时任务调用）
     */
    void executeMonthlyStatisticsTask();

    /**
     * 执行每年统计任务（定时任务调用）
     */
    void executeYearlyStatisticsTask();
}