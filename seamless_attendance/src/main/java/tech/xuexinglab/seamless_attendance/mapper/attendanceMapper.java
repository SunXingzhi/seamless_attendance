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

	@Select("select * from attendance_record where user_number = #{userNumber}")
	public List<attendanceRecord> getAttendanceRecordsByUserNumber(@Param("userNumber") String userNumber);

	@Select("select * from attendance_record where user_number = #{userNumber} and date = #{date}")
	public attendanceRecord getAttendanceRecordByUserNumberAndDate(@Param("userNumber") String userNumber,
			@Param("date") String date);

	@Insert("insert into attendance_record (user_number, date, check_in_time, check_out_time, work_hours, status, create_time, update_time) values (#{userNumber}, #{date}, #{checkInTime}, #{checkOutTime}, #{workHours}, #{status}, #{createTime}, #{updateTime})")
	public int addAttendanceRecord(attendanceRecord record);

	@Update("update attendance_record set check_in_time = #{checkInTime}, check_out_time = #{checkOutTime}, work_hours = #{workHours}, status = #{status}, update_time = #{updateTime} where user_number = #{userNumber} and date = #{date}")
	public int updateAttendanceRecord(attendanceRecord record);

	// // 用户状态相关操作
	@Select("select * from user_status where user_number = #{userNumber}")
	public userStatus getUserStatusByUserNumber(@Param("userNumber") String userNumber);
	// // 获取用户状态
	// @Select("select * from lot_record where user_number = #{user_number}")
	// public userStatus getUserStatusByUserNumber(@Param("user_number") String user_number);

	@Insert("insert into user_status (user_number, current_status, last_active_time, last_absent_time, today_work_hours, check_in_time, check_out_time, create_time, update_time) values (#{userNumber}, #{currentStatus}, #{lastActiveTime}, #{lastAbsentTime}, #{todayWorkHours}, #{checkInTime}, #{checkOutTime}, #{createTime}, #{updateTime})")
	public int addUserStatus(userStatus status);

	@Update("update user_status set current_status = #{currentStatus}, last_active_time = #{lastActiveTime}, last_absent_time = #{lastAbsentTime}, today_work_hours = #{todayWorkHours}, check_in_time = #{checkInTime}, check_out_time = #{checkOutTime}, update_time = #{updateTime} where user_number = #{userNumber}")
	public int updateUserStatus(userStatus status);

	@Select("select * from user_status")
	public List<userStatus> getAllUserStatuses();

	// 请假记录相关操作
	@Select("select * from leave_record where user_number = #{userNumber}")
	public List<leaveRecord> getLeaveRecordsByUserNumber(@Param("userNumber") String userNumber);

	@Select("select * from leave_record where user_number = #{userNumber} and start_date <= #{date} and end_date >= #{date} and status = 'approved'")
	public leaveRecord getLeaveRecordByUserNumberAndDate(@Param("userNumber") String userNumber,
			@Param("date") String date);

	@Insert("insert into leave_record (user_number, start_date, end_date, leave_type, reason, status, create_time, update_time) values (#{userNumber}, #{startDate}, #{endDate}, #{leaveType}, #{reason}, #{status}, #{createTime}, #{updateTime})")
	public int addLeaveRecord(leaveRecord record);

	// 假期记录相关操作
	@Select("select * from holiday_record where holiday_date = #{date}")
	public holidayRecord getHolidayRecordByDate(@Param("date") String date);

	@Insert("insert into holiday_record (holiday_name, holiday_date, description, create_time, update_time) values (#{holidayName}, #{holidayDate}, #{description}, #{createTime}, #{updateTime})")
	public int addHolidayRecord(holidayRecord record);

	// 初始化每日数据
	@Update("update user_status set today_work_hours = 0, check_in_time = null, check_out_time = null, update_time = #{updateTime}")
	public int resetDailyUserStatus(@Param("updateTime") String updateTime);
}
