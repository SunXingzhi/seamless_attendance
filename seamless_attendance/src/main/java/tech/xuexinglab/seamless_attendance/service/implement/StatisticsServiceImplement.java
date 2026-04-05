package tech.xuexinglab.seamless_attendance.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.xuexinglab.seamless_attendance.service.interfaces.StatisticsService;
import tech.xuexinglab.seamless_attendance.entity.*;
import tech.xuexinglab.seamless_attendance.mapper.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImplement implements StatisticsService {

	@Autowired
	private StatisticsMapper statisticsMapper;

	@Autowired
	private attendanceMapper attendanceMapper;

	@Autowired
	private userMapper userMapper;

	@Autowired
	private studioMapper studioMapper;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

	// ==================== 用户每日统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveUserDailyStats(String userNumber, String date) {
		// 获取用户的考勤记录
		attendanceRecord record = attendanceMapper.getAttendanceRecordByUserNumberAndDate(userNumber, date);
		if (record == null) {
			return; // 没有考勤记录，跳过
		}

		// 计算工作时间（如果有打卡记录）
		double workHours = record.getWork_hours() != null ? record.getWork_hours() : 0.0;

		// 计算迟到和早退分钟数
		int lateMinutes = calculateLateMinutes(record.getCheck_in_time());
		int earlyLeaveMinutes = calculateEarlyLeaveMinutes(record.getCheck_out_time());

		// 计算活跃度分数（基于工作时间、状态等）
		double activityScore = calculateActivityScore(workHours, record.getStatus(), lateMinutes,
				earlyLeaveMinutes);

		// 确定出勤状态
		String attendanceStatus = determineAttendanceStatus(record.getStatus(), workHours, lateMinutes,
				earlyLeaveMinutes);

		UserDailyStats stats = new UserDailyStats();
		stats.setUserNumber(userNumber);
		stats.setDate(date);
		stats.setWorkHours(workHours);
		stats.setActivityScore(activityScore);
		stats.setAttendanceStatus(attendanceStatus);
		stats.setCheckInTime(record.getCheck_in_time());
		stats.setCheckOutTime(record.getCheck_out_time());
		stats.setLateMinutes(lateMinutes);
		stats.setEarlyLeaveMinutes(earlyLeaveMinutes);
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		// 检查是否已存在记录，如果存在则更新，否则插入
		UserDailyStats existing = statisticsMapper.getUserDailyStats(userNumber, date);
		if (existing != null) {
			statisticsMapper.updateUserDailyStats(stats);
		} else {
			statisticsMapper.insertUserDailyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllUserDailyStats(String date) {
                // 获取所有用户，计算并保存他们的每日统计
		List<user>              allUsers = userMapper.getAllUserInfo();
		List<UserDailyStats>    statsList = new ArrayList<>();
                double			workHours;
		int			lateMinutes;
		int			earlyLeaveMinutes;
		double			activityScore;
		String			attendanceStatus;



		for (user u : allUsers) {

			attendanceRecord record = attendanceMapper
					.getAttendanceRecordByUserNumberAndDate(u.getUser_number(), date);
			if (record != null) {
				workHours = record.getWork_hours() != null ? record.getWork_hours() : 0.0;
				lateMinutes = calculateLateMinutes(record.getCheck_in_time());
				earlyLeaveMinutes = calculateEarlyLeaveMinutes(record.getCheck_out_time());
				activityScore = calculateActivityScore(workHours, record.getStatus(),
						lateMinutes, earlyLeaveMinutes);
				attendanceStatus = determineAttendanceStatus(record.getStatus(), workHours,
						lateMinutes, earlyLeaveMinutes);

				UserDailyStats stats = new UserDailyStats();
				stats.setUserNumber(u.getUser_number());
				stats.setDate(date);
				stats.setWorkHours(workHours);
				stats.setActivityScore(activityScore);
				stats.setAttendanceStatus(attendanceStatus);
				stats.setCheckInTime(record.getCheck_in_time());
				stats.setCheckOutTime(record.getCheck_out_time());
				stats.setLateMinutes(lateMinutes);
				stats.setEarlyLeaveMinutes(earlyLeaveMinutes);
				stats.setCreateTime(LocalDateTime.now()
						.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				stats.setUpdateTime(stats.getCreateTime());

				statsList.add(stats);
			}
		}

		if (!statsList.isEmpty()) {
			statisticsMapper.batchInsertUserDailyStats(statsList);
		}
	}

	@Override
	public UserDailyStats getUserDailyStats(String userNumber, String date) {
		return statisticsMapper.getUserDailyStats(userNumber, date);
	}

	@Override
	public List<UserDailyStats> getUserDailyStatsList(String userNumber, int limit) {
		return statisticsMapper.getUserDailyStatsList(userNumber, limit);
	}

	// ==================== 用户每周统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveUserWeeklyStats(String userNumber, String weekStartDate) {
		LocalDate startDate = LocalDate.parse(weekStartDate, DATE_FORMATTER);
		LocalDate endDate = startDate.plusDays(6);

		// 获取本周的每日统计数据
		List<UserDailyStats> dailyStats = new ArrayList<>();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			UserDailyStats daily = statisticsMapper.getUserDailyStats(userNumber,
					date.format(DATE_FORMATTER));
			if (daily != null) {
				dailyStats.add(daily);
			}
		}

		if (dailyStats.isEmpty()) {
			return; // 没有数据，跳过
		}

		// 计算周统计
		int totalWorkDays = dailyStats.size();
		double totalWorkHours = dailyStats.stream().mapToDouble(UserDailyStats::getWorkHours).sum();
		double attendanceRate = calculateAttendanceRate(dailyStats);
		double activityScore = dailyStats.stream().mapToDouble(UserDailyStats::getActivityScore).average()
				.orElse(0.0);

		int lateCount = (int) dailyStats.stream().filter(s -> s.getLateMinutes() > 0).count();
		int earlyLeaveCount = (int) dailyStats.stream().filter(s -> s.getEarlyLeaveMinutes() > 0).count();
		int absentCount = 7 - totalWorkDays; // 假设一周7天

		UserWeeklyStats stats = new UserWeeklyStats();
		stats.setUserNumber(userNumber);
		stats.setWeekStartDate(weekStartDate);
		stats.setWeekEndDate(endDate.format(DATE_FORMATTER));
		stats.setTotalWorkDays(totalWorkDays);
		stats.setTotalWorkHours(totalWorkHours);
		stats.setAttendanceRate(attendanceRate);
		stats.setActivityScore(activityScore);
		stats.setLateCount(lateCount);
		stats.setEarlyLeaveCount(earlyLeaveCount);
		stats.setAbsentCount(absentCount);
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		UserWeeklyStats existing = statisticsMapper.getUserWeeklyStats(userNumber, weekStartDate);
		if (existing != null) {
			statisticsMapper.updateUserWeeklyStats(stats);
		} else {
			statisticsMapper.insertUserWeeklyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllUserWeeklyStats(String weekStartDate) {
		List<user> allUsers = userMapper.getAllUserInfo();
		for (user u : allUsers) {
			calculateAndSaveUserWeeklyStats(u.getUser_number(), weekStartDate);
		}
	}

	@Override
	public UserWeeklyStats getUserWeeklyStats(String userNumber, String weekStartDate) {
		return statisticsMapper.getUserWeeklyStats(userNumber, weekStartDate);
	}

	@Override
	public List<UserWeeklyStats> getUserWeeklyStatsList(String userNumber, int limit) {
		return statisticsMapper.getUserWeeklyStatsList(userNumber, limit);
	}

	// ==================== 用户每月统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveUserMonthlyStats(String userNumber, int month, int year) {
		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		// 获取本月的每日统计数据
		List<UserDailyStats> dailyStats = new ArrayList<>();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			UserDailyStats daily = statisticsMapper.getUserDailyStats(userNumber,
					date.format(DATE_FORMATTER));
			if (daily != null) {
				dailyStats.add(daily);
			}
		}

		if (dailyStats.isEmpty()) {
			return;
		}

		// 计算月统计
		int totalWorkDays = dailyStats.size();
		double totalWorkHours = dailyStats.stream().mapToDouble(UserDailyStats::getWorkHours).sum();
		double attendanceRate = calculateAttendanceRate(dailyStats);
		double activityScore = dailyStats.stream().mapToDouble(UserDailyStats::getActivityScore).average()
				.orElse(0.0);

		int lateCount = (int) dailyStats.stream().filter(s -> s.getLateMinutes() > 0).count();
		int earlyLeaveCount = (int) dailyStats.stream().filter(s -> s.getEarlyLeaveMinutes() > 0).count();
		int absentCount = startDate.lengthOfMonth() - totalWorkDays;

		// 计算请假天数和加班小时数（这里简化处理，实际应该从请假记录计算）
		int leaveDays = 0;
		double overtimeHours = Math.max(0, totalWorkHours - (totalWorkDays * 8.0)); // 假设每天8小时工作制

		UserMonthlyStats stats = new UserMonthlyStats();
		stats.setUserNumber(userNumber);
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
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		UserMonthlyStats existing = statisticsMapper.getUserMonthlyStats(userNumber, month, year);
		if (existing != null) {
			statisticsMapper.updateUserMonthlyStats(stats);
		} else {
			statisticsMapper.insertUserMonthlyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllUserMonthlyStats(int month, int year) {
		List<user> allUsers = userMapper.getAllUserInfo();
		for (user u : allUsers) {
			calculateAndSaveUserMonthlyStats(u.getUser_number(), month, year);
		}
	}

	@Override
	public UserMonthlyStats getUserMonthlyStats(String userNumber, int month, int year) {
		return statisticsMapper.getUserMonthlyStats(userNumber, month, year);
	}

	@Override
	public List<UserMonthlyStats> getUserMonthlyStatsList(String userNumber, int limit) {
		return statisticsMapper.getUserMonthlyStatsList(userNumber, limit);
	}

	// ==================== 用户每年统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveUserYearlyStats(String userNumber, int year) {
		// 获取本年的月度统计数据
		List<UserMonthlyStats> monthlyStats = new ArrayList<>();
		for (int month = 1; month <= 12; month++) {
			UserMonthlyStats monthly = statisticsMapper.getUserMonthlyStats(userNumber, month, year);
			if (monthly != null) {
				monthlyStats.add(monthly);
			}
		}

		if (monthlyStats.isEmpty()) {
			return;
		}

		// 计算年统计
		int totalWorkDays = monthlyStats.stream().mapToInt(UserMonthlyStats::getTotalWorkDays).sum();
		double totalWorkHours = monthlyStats.stream().mapToDouble(UserMonthlyStats::getTotalWorkHours).sum();
		double attendanceRate = monthlyStats.stream().mapToDouble(UserMonthlyStats::getAttendanceRate).average()
				.orElse(0.0);
		double activityScore = monthlyStats.stream().mapToDouble(UserMonthlyStats::getActivityScore).average()
				.orElse(0.0);

		int lateCount = monthlyStats.stream().mapToInt(UserMonthlyStats::getLateCount).sum();
		int earlyLeaveCount = monthlyStats.stream().mapToInt(UserMonthlyStats::getEarlyLeaveCount).sum();
		int absentCount = monthlyStats.stream().mapToInt(UserMonthlyStats::getAbsentCount).sum();
		int leaveDays = monthlyStats.stream().mapToInt(UserMonthlyStats::getLeaveDays).sum();
		double overtimeHours = monthlyStats.stream().mapToDouble(UserMonthlyStats::getOvertimeHours).sum();

		// 计算绩效分数（基于出勤率、活跃度等）
		double performanceScore = calculatePerformanceScore(attendanceRate, activityScore, lateCount,
				earlyLeaveCount);

		UserYearlyStats stats = new UserYearlyStats();
		stats.setUserNumber(userNumber);
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
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		UserYearlyStats existing = statisticsMapper.getUserYearlyStats(userNumber, year);
		if (existing != null) {
			statisticsMapper.updateUserYearlyStats(stats);
		} else {
			statisticsMapper.insertUserYearlyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllUserYearlyStats(int year) {
		List<user> allUsers = userMapper.getAllUserInfo();
		for (user u : allUsers) {
			calculateAndSaveUserYearlyStats(u.getUser_number(), year);
		}
	}

	@Override
	public UserYearlyStats getUserYearlyStats(String userNumber, int year) {
		return statisticsMapper.getUserYearlyStats(userNumber, year);
	}

	@Override
	public List<UserYearlyStats> getUserYearlyStatsList(String userNumber, int limit) {
		return statisticsMapper.getUserYearlyStatsList(userNumber, limit);
	}

	// ==================== 工作室每日统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveStudioDailyStats(Long studioId, String date) {
		// 获取工作室成员的每日统计
		List<UserDailyStats> memberStats = statisticsMapper.getStudioUserDailyStats(studioId, date);

		if (memberStats.isEmpty()) {
			return;
		}

		int totalUsers = memberStats.size();
		int presentUsers = (int) memberStats.stream()
				.filter(s -> !"absent".equals(s.getAttendanceStatus()))
				.count();

		double attendanceRate = totalUsers > 0 ? (double) presentUsers / totalUsers * 100 : 0.0;
		double activityRate = memberStats.stream()
				.mapToDouble(UserDailyStats::getActivityScore)
				.average().orElse(0.0);
		double averageWorkHours = memberStats.stream()
				.mapToDouble(UserDailyStats::getWorkHours)
				.average().orElse(0.0);

		int lateCount = (int) memberStats.stream()
				.filter(s -> s.getLateMinutes() > 0)
				.count();
		int earlyLeaveCount = (int) memberStats.stream()
				.filter(s -> s.getEarlyLeaveMinutes() > 0)
				.count();

		StudioDailyStats stats = new StudioDailyStats();
		stats.setStudioId(studioId);
		stats.setDate(date);
		stats.setTotalUsers(totalUsers);
		stats.setPresentUsers(presentUsers);
		stats.setAttendanceRate(attendanceRate);
		stats.setActivityRate(activityRate);
		stats.setAverageWorkHours(averageWorkHours);
		stats.setLateCount(lateCount);
		stats.setEarlyLeaveCount(earlyLeaveCount);
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		StudioDailyStats existing = statisticsMapper.getStudioDailyStats(studioId, date);
		if (existing != null) {
			statisticsMapper.updateStudioDailyStats(stats);
		} else {
			statisticsMapper.insertStudioDailyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllStudioDailyStats(String date) {
		List<studio> allStudios = studioMapper.getAllStudios();
		List<StudioDailyStats> statsList = new ArrayList<>();

		for (studio s : allStudios) {
			List<UserDailyStats> memberStats = statisticsMapper
					.getStudioUserDailyStats(s.getId().longValue(), date);
			if (!memberStats.isEmpty()) {
				int totalUsers = memberStats.size();
				int presentUsers = (int) memberStats.stream()
						.filter(stats -> !"absent".equals(stats.getAttendanceStatus()))
						.count();

				double attendanceRate = totalUsers > 0 ? (double) presentUsers / totalUsers * 100 : 0.0;
				double activityRate = memberStats.stream()
						.mapToDouble(UserDailyStats::getActivityScore)
						.average().orElse(0.0);
				double averageWorkHours = memberStats.stream()
						.mapToDouble(UserDailyStats::getWorkHours)
						.average().orElse(0.0);

				int lateCount = (int) memberStats.stream()
						.filter(stats -> stats.getLateMinutes() > 0)
						.count();
				int earlyLeaveCount = (int) memberStats.stream()
						.filter(stats -> stats.getEarlyLeaveMinutes() > 0)
						.count();

				StudioDailyStats stats = new StudioDailyStats();
				stats.setStudioId(s.getId().longValue());
				stats.setDate(date);
				stats.setTotalUsers(totalUsers);
				stats.setPresentUsers(presentUsers);
				stats.setAttendanceRate(attendanceRate);
				stats.setActivityRate(activityRate);
				stats.setAverageWorkHours(averageWorkHours);
				stats.setLateCount(lateCount);
				stats.setEarlyLeaveCount(earlyLeaveCount);
				stats.setCreateTime(LocalDateTime.now()
						.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				stats.setUpdateTime(stats.getCreateTime());

				statsList.add(stats);
			}
		}

		if (!statsList.isEmpty()) {
			statisticsMapper.batchInsertStudioDailyStats(statsList);
		}
	}

	@Override
	public StudioDailyStats getStudioDailyStats(Long studioId, String date) {
		return statisticsMapper.getStudioDailyStats(studioId, date);
	}

	@Override
	public List<StudioDailyStats> getStudioDailyStatsList(Long studioId, int limit) {
		return statisticsMapper.getStudioDailyStatsList(studioId, limit);
	}

	// ==================== 工作室每周统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveStudioWeeklyStats(Long studioId, String weekStartDate) {
		LocalDate startDate = LocalDate.parse(weekStartDate, DATE_FORMATTER);
		LocalDate endDate = startDate.plusDays(6);

		// 获取本周的每日统计数据
		List<StudioDailyStats> dailyStats = new ArrayList<>();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			StudioDailyStats daily = statisticsMapper.getStudioDailyStats(studioId,
					date.format(DATE_FORMATTER));
			if (daily != null) {
				dailyStats.add(daily);
			}
		}

		if (dailyStats.isEmpty()) {
			return;
		}

		int totalUsers = dailyStats.stream().mapToInt(StudioDailyStats::getTotalUsers).max().orElse(0);
		double averageAttendanceRate = dailyStats.stream().mapToDouble(StudioDailyStats::getAttendanceRate)
				.average().orElse(0.0);
		double averageActivityRate = dailyStats.stream().mapToDouble(StudioDailyStats::getActivityRate)
				.average().orElse(0.0);

		int totalLateCount = dailyStats.stream().mapToInt(StudioDailyStats::getLateCount).sum();
		int totalEarlyLeaveCount = dailyStats.stream().mapToInt(StudioDailyStats::getEarlyLeaveCount).sum();
		int totalAbsentCount = dailyStats.stream().mapToInt(s -> s.getTotalUsers() - s.getPresentUsers()).sum();

		StudioWeeklyStats stats = new StudioWeeklyStats();
		stats.setStudioId(studioId);
		stats.setWeekStartDate(weekStartDate);
		stats.setWeekEndDate(endDate.format(DATE_FORMATTER));
		stats.setTotalUsers(totalUsers);
		stats.setAverageAttendanceRate(averageAttendanceRate);
		stats.setAverageActivityRate(averageActivityRate);
		stats.setTotalLateCount(totalLateCount);
		stats.setTotalEarlyLeaveCount(totalEarlyLeaveCount);
		stats.setTotalAbsentCount(totalAbsentCount);
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		StudioWeeklyStats existing = statisticsMapper.getStudioWeeklyStats(studioId, weekStartDate);
		if (existing != null) {
			statisticsMapper.updateStudioWeeklyStats(stats);
		} else {
			statisticsMapper.insertStudioWeeklyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllStudioWeeklyStats(String weekStartDate) {
		List<studio> allStudios = studioMapper.getAllStudios();
		for (studio s : allStudios) {
			calculateAndSaveStudioWeeklyStats(s.getId().longValue(), weekStartDate);
		}
	}

	@Override
	public StudioWeeklyStats getStudioWeeklyStats(Long studioId, String weekStartDate) {
		return statisticsMapper.getStudioWeeklyStats(studioId, weekStartDate);
	}

	@Override
	public List<StudioWeeklyStats> getStudioWeeklyStatsList(Long studioId, int limit) {
		return statisticsMapper.getStudioWeeklyStatsList(studioId, limit);
	}

	// ==================== 工作室每月统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveStudioMonthlyStats(Long studioId, int month, int year) {
		// 获取本月的每日统计数据
		List<StudioDailyStats> dailyStats = statisticsMapper.getStudioDailyStatsList(studioId, 31); // 最多31天

		// 过滤出指定月份的数据
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

		List<StudioDailyStats> monthlyStats = dailyStats.stream()
				.filter(s -> {
					LocalDate date = LocalDate.parse(s.getDate());
					return !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth);
				})
				.collect(Collectors.toList());

		if (monthlyStats.isEmpty()) {
			return;
		}

		int totalUsers = monthlyStats.stream().mapToInt(StudioDailyStats::getTotalUsers).max().orElse(0);
		double averageAttendanceRate = monthlyStats.stream().mapToDouble(StudioDailyStats::getAttendanceRate)
				.average().orElse(0.0);
		double averageActivityRate = monthlyStats.stream().mapToDouble(StudioDailyStats::getActivityRate)
				.average().orElse(0.0);

		int totalLateCount = monthlyStats.stream().mapToInt(StudioDailyStats::getLateCount).sum();
		int totalEarlyLeaveCount = monthlyStats.stream().mapToInt(StudioDailyStats::getEarlyLeaveCount).sum();
		int totalAbsentCount = monthlyStats.stream().mapToInt(s -> s.getTotalUsers() - s.getPresentUsers())
				.sum();

		// 计算请假和加班数据（这里简化处理）
		int totalLeaveDays = 0;
		double totalOvertimeHours = 0.0;

		StudioMonthlyStats stats = new StudioMonthlyStats();
		stats.setStudioId(studioId);
		stats.setMonth(month);
		stats.setYear(year);
		stats.setTotalUsers(totalUsers);
		stats.setAverageAttendanceRate(averageAttendanceRate);
		stats.setAverageActivityRate(averageActivityRate);
		stats.setTotalLateCount(totalLateCount);
		stats.setTotalEarlyLeaveCount(totalEarlyLeaveCount);
		stats.setTotalAbsentCount(totalAbsentCount);
		stats.setTotalLeaveDays(totalLeaveDays);
		stats.setTotalOvertimeHours(totalOvertimeHours);
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		StudioMonthlyStats existing = statisticsMapper.getStudioMonthlyStats(studioId, month, year);
		if (existing != null) {
			statisticsMapper.updateStudioMonthlyStats(stats);
		} else {
			statisticsMapper.insertStudioMonthlyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllStudioMonthlyStats(int month, int year) {
		List<studio> allStudios = studioMapper.getAllStudios();
		for (studio s : allStudios) {
			calculateAndSaveStudioMonthlyStats(s.getId().longValue(), month, year);
		}
	}

	@Override
	public StudioMonthlyStats getStudioMonthlyStats(Long studioId, int month, int year) {
		return statisticsMapper.getStudioMonthlyStats(studioId, month, year);
	}

	@Override
	public List<StudioMonthlyStats> getStudioMonthlyStatsList(Long studioId, int limit) {
		return statisticsMapper.getStudioMonthlyStatsList(studioId, limit);
	}

	// ==================== 工作室每年统计 ====================

	@Override
	@Transactional
	public void calculateAndSaveStudioYearlyStats(Long studioId, int year) {
		// 获取本年的月度统计数据
		List<StudioMonthlyStats> monthlyStats = new ArrayList<>();
		for (int month = 1; month <= 12; month++) {
			StudioMonthlyStats monthly = statisticsMapper.getStudioMonthlyStats(studioId, month, year);
			if (monthly != null) {
				monthlyStats.add(monthly);
			}
		}

		if (monthlyStats.isEmpty()) {
			return;
		}

		int totalUsers = monthlyStats.stream().mapToInt(StudioMonthlyStats::getTotalUsers).max().orElse(0);
		double averageAttendanceRate = monthlyStats.stream()
				.mapToDouble(StudioMonthlyStats::getAverageAttendanceRate).average().orElse(0.0);
		double averageActivityRate = monthlyStats.stream()
				.mapToDouble(StudioMonthlyStats::getAverageActivityRate).average().orElse(0.0);

		int totalLateCount = monthlyStats.stream().mapToInt(StudioMonthlyStats::getTotalLateCount).sum();
		int totalEarlyLeaveCount = monthlyStats.stream().mapToInt(StudioMonthlyStats::getTotalEarlyLeaveCount)
				.sum();
		int totalAbsentCount = monthlyStats.stream().mapToInt(StudioMonthlyStats::getTotalAbsentCount).sum();
		int totalLeaveDays = monthlyStats.stream().mapToInt(StudioMonthlyStats::getTotalLeaveDays).sum();
		double totalOvertimeHours = monthlyStats.stream().mapToDouble(StudioMonthlyStats::getTotalOvertimeHours)
				.sum();

		// 计算绩效分数
		double performanceScore = calculateStudioPerformanceScore(averageAttendanceRate, averageActivityRate,
				totalLateCount, totalEarlyLeaveCount);

		StudioYearlyStats stats = new StudioYearlyStats();
		stats.setStudioId(studioId);
		stats.setYear(year);
		stats.setTotalUsers(totalUsers);
		stats.setAverageAttendanceRate(averageAttendanceRate);
		stats.setAverageActivityRate(averageActivityRate);
		stats.setTotalLateCount(totalLateCount);
		stats.setTotalEarlyLeaveCount(totalEarlyLeaveCount);
		stats.setTotalAbsentCount(totalAbsentCount);
		stats.setTotalLeaveDays(totalLeaveDays);
		stats.setTotalOvertimeHours(totalOvertimeHours);
		stats.setPerformanceScore(performanceScore);
		stats.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		stats.setUpdateTime(stats.getCreateTime());

		StudioYearlyStats existing = statisticsMapper.getStudioYearlyStats(studioId, year);
		if (existing != null) {
			statisticsMapper.updateStudioYearlyStats(stats);
		} else {
			statisticsMapper.insertStudioYearlyStats(stats);
		}
	}

	@Override
	@Transactional
	public void calculateAndSaveAllStudioYearlyStats(int year) {
		List<studio> allStudios = studioMapper.getAllStudios();
		for (studio s : allStudios) {
			calculateAndSaveStudioYearlyStats(s.getId().longValue(), year);
		}
	}

	@Override
	public StudioYearlyStats getStudioYearlyStats(Long studioId, int year) {
		return statisticsMapper.getStudioYearlyStats(studioId, year);
	}

	@Override
	public List<StudioYearlyStats> getStudioYearlyStatsList(Long studioId, int limit) {
		return statisticsMapper.getStudioYearlyStatsList(studioId, limit);
	}

	// ==================== 综合查询 ====================

	@Override
	public List<UserDailyStats> getStudioUserDailyStats(Long studioId, String date) {
		return statisticsMapper.getStudioUserDailyStats(studioId, date);
	}

	@Override
	public List<UserMonthlyStats> getStudioUserMonthlyStats(Long studioId, int month, int year) {
		return statisticsMapper.getStudioUserMonthlyStats(studioId, month, year);
	}

	// ==================== 定时任务 ====================

	@Override
	@Transactional
	public void executeDailyStatisticsTask() {
		String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);

		// 计算所有用户的每日统计
		calculateAndSaveAllUserDailyStats(yesterday);

		// 计算所有工作室的每日统计
		calculateAndSaveAllStudioDailyStats(yesterday);
	}

	@Override
	@Transactional
	public void executeWeeklyStatisticsTask() {
		// 获取上周的开始日期（周一）
		LocalDate today = LocalDate.now();
		LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
		String weekStartDate = lastMonday.format(DATE_FORMATTER);

		// 计算所有用户的每周统计
		calculateAndSaveAllUserWeeklyStats(weekStartDate);

		// 计算所有工作室的每周统计
		calculateAndSaveAllStudioWeeklyStats(weekStartDate);
	}

	@Override
	@Transactional
	public void executeMonthlyStatisticsTask() {
		// 获取上个月
		LocalDate today = LocalDate.now();
		LocalDate lastMonth = today.minusMonths(1);
		int month = lastMonth.getMonthValue();
		int year = lastMonth.getYear();

		// 计算所有用户的每月统计
		calculateAndSaveAllUserMonthlyStats(month, year);

		// 计算所有工作室的每月统计
		calculateAndSaveAllStudioMonthlyStats(month, year);
	}

	@Override
	@Transactional
	public void executeYearlyStatisticsTask() {
		// 获取去年
		int lastYear = LocalDate.now().getYear() - 1;

		// 计算所有用户的每年统计
		calculateAndSaveAllUserYearlyStats(lastYear);

		// 计算所有工作室的每年统计
		calculateAndSaveAllStudioYearlyStats(lastYear);
	}

	// ==================== 辅助方法 ====================

	/**
	 * 计算迟到分钟数（假设上班时间为9:00）
	 */
	private int calculateLateMinutes(String checkInTime) {
		if (checkInTime == null)
			return 0;

		try {
			LocalTime checkIn = LocalTime.parse(checkInTime, TIME_FORMATTER);
			LocalTime standardTime = LocalTime.of(9, 0); // 假设9:00上班

			if (checkIn.isAfter(standardTime)) {
				return (int) ChronoUnit.MINUTES.between(standardTime, checkIn);
			}
		} catch (Exception e) {
			// 解析失败，返回0
		}
		return 0;
	}

	/**
	 * 计算早退分钟数（假设下班时间为18:00）
	 */
	private int calculateEarlyLeaveMinutes(String checkOutTime) {
		if (checkOutTime == null)
			return 0;

		try {
			LocalTime checkOut = LocalTime.parse(checkOutTime, TIME_FORMATTER);
			LocalTime standardTime = LocalTime.of(18, 0); // 假设18:00下班

			if (checkOut.isBefore(standardTime)) {
				return (int) ChronoUnit.MINUTES.between(checkOut, standardTime);
			}
		} catch (Exception e) {
			// 解析失败，返回0
		}
		return 0;
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
			score += 5; // 假期少量加分
		}

		// 扣分：迟到早退
		score -= Math.min(lateMinutes + earlyLeaveMinutes, 20);

		return Math.max(0, Math.min(100, score));
	}

	/**
	 * 确定出勤状态
	 */
	private String determineAttendanceStatus(String recordStatus, double workHours, int lateMinutes,
			int earlyLeaveMinutes) {
		// 如果原始状态是absent、holiday、excused，直接返回
		if ("absent".equals(recordStatus) || "holiday".equals(recordStatus) || "excused".equals(recordStatus)) {
			return recordStatus;
		}

		// 处理迟到和早退情况
		if (lateMinutes > 0) {
			return "late";
		} else if (earlyLeaveMinutes > 0) {
			return "leave";
		}

		// 默认返回在勤状态
		return "active";
	}

	/**
	 * 计算出勤率
	 */
	private double calculateAttendanceRate(List<UserDailyStats> dailyStats) {
		if (dailyStats.isEmpty())
			return 0.0;

		long presentDays = dailyStats.stream()
				.filter(s -> !"absent".equals(s.getAttendanceStatus())
						&& !"excused".equals(s.getAttendanceStatus())
						&& !"holiday".equals(s.getAttendanceStatus()))
				.count();

		return (double) presentDays / dailyStats.size() * 100;
	}

	/**
	 * 计算个人绩效分数
	 */
	private double calculatePerformanceScore(double attendanceRate, double activityScore, int lateCount,
			int earlyLeaveCount) {
		double score = attendanceRate * 0.4 + activityScore * 0.4;
		score -= (lateCount + earlyLeaveCount) * 2; // 每次迟到早退扣2分
		return Math.max(0, Math.min(100, score));
	}

	/**
	 * 计算工作室绩效分数
	 */
	private double calculateStudioPerformanceScore(double attendanceRate, double activityRate, int lateCount,
			int earlyLeaveCount) {
		double score = attendanceRate * 0.5 + activityRate * 0.3;
		score -= (lateCount + earlyLeaveCount) * 0.5; // 团队迟到早退影响较小
		return Math.max(0, Math.min(100, score));
	}
}