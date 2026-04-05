package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.*;
import tech.xuexinglab.seamless_attendance.entity.*;
import java.util.List;

@Mapper
public interface StatisticsMapper {

	// ==================== User Daily Stats ====================

	@Insert("INSERT INTO user_daily_stats (user_number, date, work_hours, activity_score, attendance_status, check_in_time, check_out_time, late_minutes, early_leave_minutes, create_time, update_time) "
			+
			"VALUES (#{userNumber}, #{date}, #{workHours}, #{activityScore}, #{attendanceStatus}, #{checkInTime}, #{checkOutTime}, #{lateMinutes}, #{earlyLeaveMinutes}, #{createTime}, #{updateTime})")
	int insertUserDailyStats(UserDailyStats stats);

	@Update("UPDATE user_daily_stats SET work_hours = #{workHours}, activity_score = #{activityScore}, attendance_status = #{attendanceStatus}, "
			+
			"check_in_time = #{checkInTime}, check_out_time = #{checkOutTime}, late_minutes = #{lateMinutes}, early_leave_minutes = #{earlyLeaveMinutes}, "
			+
			"update_time = #{updateTime} WHERE user_number = #{userNumber} AND date = #{date}")
	int updateUserDailyStats(UserDailyStats stats);

	@Select("SELECT * FROM user_daily_stats WHERE user_number = #{userNumber} AND date = #{date}")
	UserDailyStats getUserDailyStats(@Param("userNumber") String userNumber, @Param("date") String date);

	@Select("SELECT * FROM user_daily_stats WHERE user_number = #{userNumber} ORDER BY date DESC LIMIT #{limit}")
	List<UserDailyStats> getUserDailyStatsList(@Param("userNumber") String userNumber, @Param("limit") int limit);

	@Select("SELECT * FROM user_daily_stats WHERE date = #{date}")
	List<UserDailyStats> getAllUserDailyStatsByDate(@Param("date") String date);

	// ==================== User Weekly Stats ====================

	@Insert("INSERT INTO user_weekly_stats (user_number, week_start_date, week_end_date, total_work_days, total_work_hours, attendance_rate, activity_score, late_count, early_leave_count, absent_count, create_time, update_time) "
			+
			"VALUES (#{userNumber}, #{weekStartDate}, #{weekEndDate}, #{totalWorkDays}, #{totalWorkHours}, #{attendanceRate}, #{activityScore}, #{lateCount}, #{earlyLeaveCount}, #{absentCount}, #{createTime}, #{updateTime})")
	int insertUserWeeklyStats(UserWeeklyStats stats);

	@Update("UPDATE user_weekly_stats SET total_work_days = #{totalWorkDays}, total_work_hours = #{totalWorkHours}, attendance_rate = #{attendanceRate}, "
			+
			"activity_score = #{activityScore}, late_count = #{lateCount}, early_leave_count = #{earlyLeaveCount}, absent_count = #{absentCount}, "
			+
			"update_time = #{updateTime} WHERE user_number = #{userNumber} AND week_start_date = #{weekStartDate}")
	int updateUserWeeklyStats(UserWeeklyStats stats);

	@Select("SELECT * FROM user_weekly_stats WHERE user_number = #{userNumber} AND week_start_date = #{weekStartDate}")
	UserWeeklyStats getUserWeeklyStats(@Param("userNumber") String userNumber,
			@Param("weekStartDate") String weekStartDate);

	@Select("SELECT * FROM user_weekly_stats WHERE user_number = #{userNumber} ORDER BY week_start_date DESC LIMIT #{limit}")
	List<UserWeeklyStats> getUserWeeklyStatsList(@Param("userNumber") String userNumber, @Param("limit") int limit);

	// ==================== User Monthly Stats ====================

	@Insert("INSERT INTO user_monthly_stats (user_number, month, year, total_work_days, total_work_hours, attendance_rate, activity_score, late_count, early_leave_count, absent_count, leave_days, overtime_hours, create_time, update_time) "
			+
			"VALUES (#{userNumber}, #{month}, #{year}, #{totalWorkDays}, #{totalWorkHours}, #{attendanceRate}, #{activityScore}, #{lateCount}, #{earlyLeaveCount}, #{absentCount}, #{leaveDays}, #{overtimeHours}, #{createTime}, #{updateTime})")
	int insertUserMonthlyStats(UserMonthlyStats stats);

	@Update("UPDATE user_monthly_stats SET total_work_days = #{totalWorkDays}, total_work_hours = #{totalWorkHours}, attendance_rate = #{attendanceRate}, "
			+
			"activity_score = #{activityScore}, late_count = #{lateCount}, early_leave_count = #{earlyLeaveCount}, absent_count = #{absentCount}, "
			+
			"leave_days = #{leaveDays}, overtime_hours = #{overtimeHours}, update_time = #{updateTime} " +
			"WHERE user_number = #{userNumber} AND month = #{month} AND year = #{year}")
	int updateUserMonthlyStats(UserMonthlyStats stats);

	@Select("SELECT * FROM user_monthly_stats WHERE user_number = #{userNumber} AND month = #{month} AND year = #{year}")
	UserMonthlyStats getUserMonthlyStats(@Param("userNumber") String userNumber, @Param("month") int month,
			@Param("year") int year);

	@Select("SELECT * FROM user_monthly_stats WHERE user_number = #{userNumber} ORDER BY year DESC, month DESC LIMIT #{limit}")
	List<UserMonthlyStats> getUserMonthlyStatsList(@Param("userNumber") String userNumber,
			@Param("limit") int limit);

	// ==================== User Yearly Stats ====================

	@Insert("INSERT INTO user_yearly_stats (user_number, year, total_work_days, total_work_hours, attendance_rate, activity_score, late_count, early_leave_count, absent_count, leave_days, overtime_hours, performance_score, create_time, update_time) "
			+
			"VALUES (#{userNumber}, #{year}, #{totalWorkDays}, #{totalWorkHours}, #{attendanceRate}, #{activityScore}, #{lateCount}, #{earlyLeaveCount}, #{absentCount}, #{leaveDays}, #{overtimeHours}, #{performanceScore}, #{createTime}, #{updateTime})")
	int insertUserYearlyStats(UserYearlyStats stats);

	@Update("UPDATE user_yearly_stats SET total_work_days = #{totalWorkDays}, total_work_hours = #{totalWorkHours}, attendance_rate = #{attendanceRate}, "
			+
			"activity_score = #{activityScore}, late_count = #{lateCount}, early_leave_count = #{earlyLeaveCount}, absent_count = #{absentCount}, "
			+
			"leave_days = #{leaveDays}, overtime_hours = #{overtimeHours}, performance_score = #{performanceScore}, update_time = #{updateTime} "
			+
			"WHERE user_number = #{userNumber} AND year = #{year}")
	int updateUserYearlyStats(UserYearlyStats stats);

	@Select("SELECT * FROM user_yearly_stats WHERE user_number = #{userNumber} AND year = #{year}")
	UserYearlyStats getUserYearlyStats(@Param("userNumber") String userNumber, @Param("year") int year);

	@Select("SELECT * FROM user_yearly_stats WHERE user_number = #{userNumber} ORDER BY year DESC LIMIT #{limit}")
	List<UserYearlyStats> getUserYearlyStatsList(@Param("userNumber") String userNumber, @Param("limit") int limit);

	// ==================== Studio Daily Stats ====================

	@Insert("INSERT INTO studio_daily_stats (studio_id, date, total_users, present_users, attendance_rate, activity_rate, average_work_hours, late_count, early_leave_count, create_time, update_time) "
			+
			"VALUES (#{studioId}, #{date}, #{totalUsers}, #{presentUsers}, #{attendanceRate}, #{activityRate}, #{averageWorkHours}, #{lateCount}, #{earlyLeaveCount}, #{createTime}, #{updateTime})")
	int insertStudioDailyStats(StudioDailyStats stats);

	@Update("UPDATE studio_daily_stats SET total_users = #{totalUsers}, present_users = #{presentUsers}, attendance_rate = #{attendanceRate}, "
			+
			"activity_rate = #{activityRate}, average_work_hours = #{averageWorkHours}, late_count = #{lateCount}, early_leave_count = #{earlyLeaveCount}, "
			+
			"update_time = #{updateTime} WHERE studio_id = #{studioId} AND date = #{date}")
	int updateStudioDailyStats(StudioDailyStats stats);

	@Select("SELECT * FROM studio_daily_stats WHERE studio_id = #{studioId} AND date = #{date}")
	StudioDailyStats getStudioDailyStats(@Param("studioId") Long studioId, @Param("date") String date);

	@Select("SELECT * FROM studio_daily_stats WHERE studio_id = #{studioId} ORDER BY date DESC LIMIT #{limit}")
	List<StudioDailyStats> getStudioDailyStatsList(@Param("studioId") Long studioId, @Param("limit") int limit);

	@Select("SELECT * FROM studio_daily_stats WHERE date = #{date}")
	List<StudioDailyStats> getAllStudioDailyStatsByDate(@Param("date") String date);

	// ==================== Studio Weekly Stats ====================

	@Insert("INSERT INTO studio_weekly_stats (studio_id, week_start_date, week_end_date, total_users, average_attendance_rate, average_activity_rate, total_late_count, total_early_leave_count, total_absent_count, create_time, update_time) "
			+
			"VALUES (#{studioId}, #{weekStartDate}, #{weekEndDate}, #{totalUsers}, #{averageAttendanceRate}, #{averageActivityRate}, #{totalLateCount}, #{totalEarlyLeaveCount}, #{totalAbsentCount}, #{createTime}, #{updateTime})")
	int insertStudioWeeklyStats(StudioWeeklyStats stats);

	@Update("UPDATE studio_weekly_stats SET total_users = #{totalUsers}, average_attendance_rate = #{averageAttendanceRate}, average_activity_rate = #{averageActivityRate}, "
			+
			"total_late_count = #{totalLateCount}, total_early_leave_count = #{totalEarlyLeaveCount}, total_absent_count = #{totalAbsentCount}, "
			+
			"update_time = #{updateTime} WHERE studio_id = #{studioId} AND week_start_date = #{weekStartDate}")
	int updateStudioWeeklyStats(StudioWeeklyStats stats);

	@Select("SELECT * FROM studio_weekly_stats WHERE studio_id = #{studioId} AND week_start_date = #{weekStartDate}")
	StudioWeeklyStats getStudioWeeklyStats(@Param("studioId") Long studioId,
			@Param("weekStartDate") String weekStartDate);

	@Select("SELECT * FROM studio_weekly_stats WHERE studio_id = #{studioId} ORDER BY week_start_date DESC LIMIT #{limit}")
	List<StudioWeeklyStats> getStudioWeeklyStatsList(@Param("studioId") Long studioId, @Param("limit") int limit);

	// ==================== Studio Monthly Stats ====================

	@Insert("INSERT INTO studio_monthly_stats (studio_id, month, year, total_users, average_attendance_rate, average_activity_rate, total_late_count, total_early_leave_count, total_absent_count, total_leave_days, total_overtime_hours, create_time, update_time) "
			+
			"VALUES (#{studioId}, #{month}, #{year}, #{totalUsers}, #{averageAttendanceRate}, #{averageActivityRate}, #{totalLateCount}, #{totalEarlyLeaveCount}, #{totalAbsentCount}, #{totalLeaveDays}, #{totalOvertimeHours}, #{createTime}, #{updateTime})")
	int insertStudioMonthlyStats(StudioMonthlyStats stats);

	@Update("UPDATE studio_monthly_stats SET total_users = #{totalUsers}, average_attendance_rate = #{averageAttendanceRate}, average_activity_rate = #{averageActivityRate}, "
			+
			"total_late_count = #{totalLateCount}, total_early_leave_count = #{totalEarlyLeaveCount}, total_absent_count = #{totalAbsentCount}, "
			+
			"total_leave_days = #{totalLeaveDays}, total_overtime_hours = #{totalOvertimeHours}, update_time = #{updateTime} "
			+
			"WHERE studio_id = #{studioId} AND month = #{month} AND year = #{year}")
	int updateStudioMonthlyStats(StudioMonthlyStats stats);

	@Select("SELECT * FROM studio_monthly_stats WHERE studio_id = #{studioId} AND month = #{month} AND year = #{year}")
	StudioMonthlyStats getStudioMonthlyStats(@Param("studioId") Long studioId, @Param("month") int month,
			@Param("year") int year);

	@Select("SELECT * FROM studio_monthly_stats WHERE studio_id = #{studioId} ORDER BY year DESC, month DESC LIMIT #{limit}")
	List<StudioMonthlyStats> getStudioMonthlyStatsList(@Param("studioId") Long studioId, @Param("limit") int limit);

	// ==================== Studio Yearly Stats ====================

	@Insert("INSERT INTO studio_yearly_stats (studio_id, year, total_users, average_attendance_rate, average_activity_rate, total_late_count, total_early_leave_count, total_absent_count, total_leave_days, total_overtime_hours, performance_score, create_time, update_time) "
			+
			"VALUES (#{studioId}, #{year}, #{totalUsers}, #{averageAttendanceRate}, #{averageActivityRate}, #{totalLateCount}, #{totalEarlyLeaveCount}, #{totalAbsentCount}, #{totalLeaveDays}, #{totalOvertimeHours}, #{performanceScore}, #{createTime}, #{updateTime})")
	int insertStudioYearlyStats(StudioYearlyStats stats);

	@Update("UPDATE studio_yearly_stats SET total_users = #{totalUsers}, average_attendance_rate = #{averageAttendanceRate}, average_activity_rate = #{averageActivityRate}, "
			+
			"total_late_count = #{totalLateCount}, total_early_leave_count = #{totalEarlyLeaveCount}, total_absent_count = #{totalAbsentCount}, "
			+
			"total_leave_days = #{totalLeaveDays}, total_overtime_hours = #{totalOvertimeHours}, performance_score = #{performanceScore}, "
			+
			"update_time = #{updateTime} WHERE studio_id = #{studioId} AND year = #{year}")
	int updateStudioYearlyStats(StudioYearlyStats stats);

	@Select("SELECT * FROM studio_yearly_stats WHERE studio_id = #{studioId} AND year = #{year}")
	StudioYearlyStats getStudioYearlyStats(@Param("studioId") Long studioId, @Param("year") int year);

	@Select("SELECT * FROM studio_yearly_stats WHERE studio_id = #{studioId} ORDER BY year DESC LIMIT #{limit}")
	List<StudioYearlyStats> getStudioYearlyStatsList(@Param("studioId") Long studioId, @Param("limit") int limit);

	// ==================== Complex Queries ====================

	// 获取工作室成员的每日统计
	@Select("SELECT uds.* FROM user_daily_stats uds " +
			"INNER JOIN user u ON uds.user_number COLLATE utf8mb4_unicode_ci = u.user_number COLLATE utf8mb4_unicode_ci " +
			"WHERE u.studio_id = #{studioId} AND uds.date = #{date}")
	List<UserDailyStats> getStudioUserDailyStats(@Param("studioId") Long studioId, @Param("date") String date);

	// 获取工作室成员的月度统计
	@Select("SELECT ums.* FROM user_monthly_stats ums " +
			"INNER JOIN user u ON ums.user_number COLLATE utf8mb4_unicode_ci = u.user_number COLLATE utf8mb4_unicode_ci " +
			"WHERE u.studio_id = #{studioId} AND ums.month = #{month} AND ums.year = #{year}")
	List<UserMonthlyStats> getStudioUserMonthlyStats(@Param("studioId") Long studioId, @Param("month") int month,
			@Param("year") int year);

	// 批量插入用户每日统计（用于定时任务）
	@Insert("<script>" +
			"INSERT INTO user_daily_stats (user_number, date, work_hours, activity_score, attendance_status, check_in_time, check_out_time, late_minutes, early_leave_minutes, create_time, update_time) "
			+
			"VALUES " +
			"<foreach collection='list' item='item' separator=','>" +
			"(#{item.userNumber}, #{item.date}, #{item.workHours}, #{item.activityScore}, #{item.attendanceStatus}, #{item.checkInTime}, #{item.checkOutTime}, #{item.lateMinutes}, #{item.earlyLeaveMinutes}, #{item.createTime}, #{item.updateTime})"
			+
			"</foreach>" +
			"</script>")
	int batchInsertUserDailyStats(@Param("list") List<UserDailyStats> statsList);

	// 批量插入工作室每日统计（用于定时任务）
	@Insert("<script>" +
			"INSERT INTO studio_daily_stats (studio_id, date, total_users, present_users, attendance_rate, activity_rate, average_work_hours, late_count, early_leave_count, create_time, update_time) "
			+
			"VALUES " +
			"<foreach collection='list' item='item' separator=','>" +
			"(#{item.studioId}, #{item.date}, #{item.totalUsers}, #{item.presentUsers}, #{item.attendanceRate}, #{item.activityRate}, #{item.averageWorkHours}, #{item.lateCount}, #{item.earlyLeaveCount}, #{item.createTime}, #{item.updateTime})"
			+
			"</foreach>" +
			"</script>")
	int batchInsertStudioDailyStats(@Param("list") List<StudioDailyStats> statsList);
}