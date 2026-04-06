package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tech.xuexinglab.seamless_attendance.entity.user;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import tech.xuexinglab.seamless_attendance.entity.attendanceRecord;
import java.util.List;

@Mapper
public interface userMapper {

	@Select("select * from user")
	public List<user> getAllUserInfo();
	
	@Select("select * from user where id = #{id}")
	public user getUserInfoById(@Param("id") int id);

	@Select("select * from user where user_number = #{userNumber}")
	public user getUserInfoByUserNumber(@Param("userNumber") String userNumber);

	@Select("select * from user where name = #{name}")
	public user getUserInfoByName(@Param("name") String name);

	@Select("select * from user where phone = #{phone}")
	public user getUserInfoByPhone(@Param("phone") String phone);

        @Insert("insert into user (name, contact_type, contact_value, user_number, role, job_title, work_content, studio_id, avatar, status, join_date, device_id) values (#{name}, #{contactType}, #{contactValue}, #{userNumber}, #{role}, #{jobTitle}, #{workContent}, #{studioId}, #{avatar}, #{status}, #{joinDate}, #{deviceId})")
        public int addUserInfo( 
                                        @Param("name") String name, 
                                        @Param("contactType") String contactType, 
                                        @Param("userNumber") String userNumber,
                                        @Param("role") String role,
                                        @Param("jobTitle") String jobTitle,
                                        @Param("workContent") String workContent,
                                        @Param("studioId") String studioId,
                                        @Param("avatar") String avatar,
                                        @Param("status") String status,
                                        @Param("joinDate") String joinDate,
                                        @Param("deviceId") Integer deviceId
                                );      


        @Update("update user set name = #{name}, contact_type = #{contactType}, contact_value = #{contactValue}, user_number = #{userNumber}, role = #{role}, job_title = #{jobTitle}, work_content = #{workContent}, studio_id = #{studioId}, avatar = #{avatar}, status = #{status}, join_date = #{joinDate}, device_id = #{deviceId} where id = #{id}")  
        public int updateUserInfo( 
                                        @Param("id") Integer id,
                                        @Param("name") String name, 
                                        @Param("contactType") String contactType, 
                                        @Param("contactValue") String contactValue, 
                                        @Param("userNumber") String userNumber,
                                        @Param("role") String role,
                                        @Param("jobTitle") String jobTitle,
                                        @Param("workContent") String workContent,
                                        @Param("studioId") String studioId,
                                        @Param("avatar") String avatar,
                                        @Param("status") String status,
                                        @Param("joinDate") String joinDate,
                                        @Param("deviceId") Integer deviceId
                                );
        // 更新用户状态
        @Update("update user set status = #{status}, update_time = NOW() where user_number = #{userNumber}")
        public int updateUserStatus(@Param("userNumber") String userNumber, @Param("status") String status);

        @Delete("delete from user where id = #{id}")
        public int deleteUserInfo(@Param("id") int id);

        // 重新编号用户ID，保持连续性
	@Update("UPDATE user, (SELECT @i:=0) AS temp SET id = @i:=@i+1 ORDER BY id")
	public int renumberUserIds();
        
        // 更新用户设备ID
	@Update("update user set device_id = #{deviceId}, update_time = NOW() where user_number = #{userNumber}")
	public int updateUserDeviceId(@Param("userNumber") String userNumber, @Param("deviceId") Integer deviceId);

        // 重置用户表自增计数器
        @Update("ALTER TABLE user AUTO_INCREMENT = (SELECT IFNULL(MAX(id), 0) + 1 FROM user);")
        public int resetUserAutoIncrement();

        // 需要额外计算工作时间(work_hours)
        @Select("select ar.*, u.status, u.name, u.user_number from attendance_record as ar left join user as u on ar.user_number = u.user_number")
        public List<attendanceRecord> getUserAttendanceRecord();
        
        // 更新人员配对状态
        @Update("update user set pairing_status = #{pairingStatus}, device_id = #{deviceId}, update_time = NOW() where user_number = #{userNumber}")
        public int updateUserPairingStatus(@Param("userNumber") String userNumber, 
                                          @Param("pairingStatus") String pairingStatus, 
                                          @Param("deviceId") Integer deviceId);
        
        // 检查人员是否已与其他设备绑定
        @Select("select count(*) from user where user_number = #{userNumber} and pairing_status = 'paired' and device_id != #{deviceId}")
        public int checkUserPaired(@Param("userNumber") String userNumber, @Param("deviceId") Integer deviceId);
        
        // 登录验证：根据用户名和密码查询用户
        @Select("select * from user where user_name = #{userName} and password = #{password}")
        public user login(@Param("userName") String userName, @Param("password") String password);
        
        // 更新用户密码
        @Update("update user set password = #{password}, update_time = NOW() where user_name = #{userName}")
        public int updatePassword(@Param("userName") String userName, @Param("password") String password);
}
