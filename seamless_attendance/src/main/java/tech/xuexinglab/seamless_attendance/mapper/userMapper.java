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

	@Select("select * from user where user_number = #{user_number}")
	public user getUserInfoByUserNumber(@Param("user_number") String user_number);

	@Select("select * from user where name = #{name}")
	public user getUserInfoByName(@Param("name") String name);

	@Select("select * from user where phone = #{phone}")
	public user getUserInfoByPhone(@Param("phone") String phone);

        @Insert("insert into user (name, contact_value, user_number, role, job_title, work_content, studio_id, avatar, status, join_date, device_id) values (#{name}, #{contact_type}, #{contact_value}, #{user_number}, #{role}, #{job_title}, #{work_content}, #{studio_id}, #{avatar}, #{status}, #{join_date}, #{device_id})")
        public int addUserInfo( 
                                        @Param("name") String name, 
                                        @Param("contact_value") String contact_value, 
                                        @Param("user_number") String user_number,
                                        @Param("role") String role,
                                        @Param("job_title") String job_title,
                                        @Param("work_content") String work_content,
                                        @Param("studio_id") String studio_id,
                                        @Param("avatar") String avatar,
                                        @Param("status") String status,
                                        @Param("join_date") String join_date,
                                        @Param("device_id") Integer device_id
                                );      


        @Update("update user set name = #{name}, "+"contact_type = #{contact_type}, "+"contact_value = #{contact_value}, "+"user_number = #{user_number}, role = #{role}, job_title = #{job_title}, work_content = #{work_content}, studio_id = #{studio_id}, avatar = #{avatar}, status = #{status}, join_date = #{join_date}, device_id = #{device_id} where id = #{id}")  
        public int updateUserInfo( 
                                        @Param("id") Integer id,
                                        @Param("name") String name, 
                                        @Param("contact_type") String contact_type, 
                                        @Param("contact_value") String contact_value, 
                                        @Param("user_number") String user_number,
                                        @Param("role") String role,
                                        @Param("job_title") String job_title,
                                        @Param("work_content") String work_content,
                                        @Param("studio_id") String studio_id,
                                        @Param("avatar") String avatar,
                                        @Param("status") String status,
                                        @Param("join_date") String join_date,
                                        @Param("device_id") Integer device_id
                                );
        // 更新用户状态
        @Update("update user set status = #{status}, update_time = NOW() where user_number = #{user_number}")
        public int updateUserStatus(@Param("user_number") String user_number, @Param("status") String status);

        @Delete("delete from user where id = #{id}")
        public int deleteUserInfo(@Param("id") int id);

        // 重新编号用户ID，保持连续性
	@Update("UPDATE user, (SELECT @i:=0) AS temp SET id = @i:=@i+1 ORDER BY id")
	public int renumberUserIds();
        
        // 更新用户设备ID
	@Update("update user set device_id = #{device_id}, update_time = NOW() where user_number = #{user_number}")
	public int updateUserDeviceId(@Param("user_number") String user_number, @Param("device_id") Integer device_id);

        // 重置用户表自增计数器
        @Update("ALTER TABLE user AUTO_INCREMENT = (SELECT IFNULL(MAX(id), 0) + 1 FROM user);")
        public int resetUserAutoIncrement();

        // 需要额外计算工作时间(work_hours)
        @Select("select ar.*, u.status, u.name, u.user_number from attendance_record as ar left join user as u on ar.user_number = u.user_number")
        public List<attendanceRecord> getUserAttendanceRecord();
        
        // 更新人员配对状态
        @Update("update user set pairing_status = #{pairing_status}, device_id = #{device_id}, update_time = NOW() where user_number = #{user_number}")
        public int updateUserPairingStatus(@Param("user_number") String user_number, 
                                          @Param("pairing_status") String pairing_status, 
                                          @Param("device_id") Integer device_id);
        
        // 检查人员是否已与其他设备绑定
        @Select("select count(*) from user where user_number = #{user_number} and pairing_status = 'paired' and device_id != #{device_id}")
        public int checkUserPaired(@Param("user_number") String user_number, @Param("device_id") Integer device_id);
        
        // 登录验证：根据用户名和密码查询用户
        @Select("select * from user where user_name = #{user_name} and password = #{password}")
        public user login(@Param("user_name") String user_name, @Param("password") String password);
        
        // 更新用户密码
        @Update("update user set password = #{password}, update_time = NOW() where user_name = #{user_name}")
        public int updatePassword(@Param("user_name") String user_name, @Param("password") String password);
}
