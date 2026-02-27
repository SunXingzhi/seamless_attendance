package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import tech.xuexinglab.seamless_attendance.entity.attendanceRecord;
import tech.xuexinglab.seamless_attendance.entity.userStatus;
import tech.xuexinglab.seamless_attendance.entity.leaveRecord;
import tech.xuexinglab.seamless_attendance.entity.holidayRecord;
import java.util.List;

@Mapper
public interface attendanceMapper {
	// 考勤记录相关操作
	@Select("select * from attendance_record")
	public List<attendanceRecord> getAllAttendanceRecords();

	@Select("select * from attendance_record where user_number = #{user_number}")
	public List<attendanceRecord> getAttendanceRecordsByUserNumber(@Param("user_number") String user_number);

	@Select("select * from attendance_record where user_number = #{user_number} and date = #{date}")
	public attendanceRecord getAttendanceRecordByUserNumberAndDate(@Param("user_number") String user_number,
			@Param("date") String date);

	@Insert("insert into attendance_record (user_number, date, check_in_time, check_out_time, work_hours, status, create_time, update_time) values (#{user_number}, #{date}, #{check_in_time}, #{check_out_time}, #{work_hours}, #{status}, #{create_time}, #{update_time})")
	public int addAttendanceRecord(attendanceRecord record);

	@Update("update attendance_record set check_in_time = #{check_in_time}, check_out_time = #{check_out_time}, work_hours = #{work_hours}, status = #{status}, update_time = #{update_time} where user_number = #{user_number} and date = #{date}")
	public int updateAttendanceRecord(attendanceRecord record);

	// // 用户状态相关操作
	@Select("select * from user_status where user_number = #{user_number}")
	public userStatus getUserStatusByUserNumber(@Param("user_number") String user_number);
	// // 获取用户状态
	// @Select("select * from lot_record where user_number = #{user_number}")
	// public userStatus getUserStatusByUserNumber(@Param("user_number") String user_number);

	@Insert("insert into user_status (user_number, current_status, last_active_time, last_absent_time, today_work_hours, check_in_time, check_out_time, create_time, update_time) values (#{user_number}, #{current_status}, #{last_active_time}, #{last_absent_time}, #{today_work_hours}, #{check_in_time}, #{check_out_time}, #{create_time}, #{update_time})")
	public int addUserStatus(userStatus status);

	@Update("update user_status set current_status = #{current_status}, last_active_time = #{last_active_time}, last_absent_time = #{last_absent_time}, today_work_hours = #{today_work_hours}, check_in_time = #{check_in_time}, check_out_time = #{check_out_time}, update_time = #{update_time} where user_number = #{user_number}")
	public int updateUserStatus(userStatus status);

	@Select("select * from user_status")
	public List<userStatus> getAllUserStatuses();

	// 请假记录相关操作
	@Select("select * from leave_record where user_number = #{user_number}")
	public List<leaveRecord> getLeaveRecordsByUserNumber(@Param("user_number") String user_number);

	@Select("select * from leave_record where user_number = #{user_number} and start_date <= #{date} and end_date >= #{date} and status = 'approved'")
	public leaveRecord getLeaveRecordByUserNumberAndDate(@Param("user_number") String user_number,
			@Param("date") String date);

	@Insert("insert into leave_record (user_number, start_date, end_date, leave_type, reason, status, create_time, update_time) values (#{user_number}, #{start_date}, #{end_date}, #{leave_type}, #{reason}, #{status}, #{create_time}, #{update_time})")
	public int addLeaveRecord(leaveRecord record);

	// 假期记录相关操作
	@Select("select * from holiday_record where holiday_date = #{date}")
	public holidayRecord getHolidayRecordByDate(@Param("date") String date);

	@Insert("insert into holiday_record (holiday_name, holiday_date, description, create_time, update_time) values (#{holiday_name}, #{holiday_date}, #{description}, #{create_time}, #{update_time})")
	public int addHolidayRecord(holidayRecord record);

	// 初始化每日数据
	@Update("update user_status set today_work_hours = 0, check_in_time = null, check_out_time = null, update_time = #{update_time}")
	public int resetDailyUserStatus(@Param("update_time") String update_time);
}
