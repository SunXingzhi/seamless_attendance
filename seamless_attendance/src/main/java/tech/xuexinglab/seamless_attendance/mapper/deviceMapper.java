package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tech.xuexinglab.seamless_attendance.entity.device;
import java.util.List;

@Mapper
public interface deviceMapper {
	// 新增设备
	@Insert("INSERT INTO device (device_name, device_id, studio_codes, personnels, status, create_time, update_time) "
			+
			"VALUES (#{deviceName}, #{deviceId}, #{studioCodes}, #{personnels}, #{status}, NOW(), NOW())")
	int insertDevice(device device);

	// 获取设备列表
	@Select("SELECT * FROM device")
	List<device> getAllDevices();

	// 根据ID获取设备详情
	@Select("SELECT * FROM device WHERE id = #{id}")
	device getDeviceById(@Param("id") Integer id);

	// 根据设备ID获取设备详情
	@Select("SELECT * FROM device WHERE device_id = #{deviceId}")
	device getDeviceByDeviceId(@Param("deviceId") String deviceId);
	// 根据设备名称获取设备
	@Select("SELECT * FROM device WHERE device_name = #{deviceName}")
	List<device> getDevicesByName(@Param("deviceName") String deviceName);

	// 根据设备名称获取单个设备
	@Select("SELECT * FROM device WHERE device_name = #{deviceName} LIMIT 1")
	device getDeviceByDeviceName(@Param("deviceName") String deviceName);

	// 更新设备
	@Update("UPDATE device SET device_name = #{deviceName}, device_id = #{deviceId}, studio_codes = #{studioCodes}, "
			+
			"personnels = #{personnels}, status = #{status}, update_time = NOW() " +
			"WHERE id = #{id}")
	int updateDevice(device device);

	// 删除设备
	@Delete("DELETE FROM device WHERE id = #{id}")
	int deleteDevice(@Param("id") Integer id);

	// 重新编号设备ID，保持连续性
	@Update("UPDATE device d JOIN (SELECT id, @i:=@i+1 as new_id FROM device, (SELECT @i:=0) init ORDER BY id) t ON d.id = t.id SET d.id = t.new_id")
	int renumberDeviceIds();

	// 获取最大设备ID
	@Select("SELECT IFNULL(MAX(id), 0) FROM device")
	int getMaxDeviceId();

        // 获取字模发送状态
        @Select("SELECT font_sent FROM device WHERE device_name = #{deviceName}")
        Boolean getFontSent(@Param("deviceName") String deviceName);

        // 更新字模发送状态
        @Update("UPDATE device SET font_sent = #{fontSent}, update_time = NOW() WHERE device_name = #{deviceName}")
        int updateFontSent(@Param("deviceName") String deviceName, @Param("fontSent") Boolean fontSent);
        
	// 重置设备表自增计数器
	@Update("ALTER TABLE device AUTO_INCREMENT = #{value}")
	int resetDeviceAutoIncrement(@Param("value") int value);

	// 更新设备状态
	@Update("UPDATE device SET status = #{status}, update_time = NOW() WHERE id = #{id}")
	int updateDeviceStatus(@Param("id") Integer id, @Param("status") String status);

	// 根据原始设备名称获取设备列表（如获取所有以"A"开头的设备）
	@Select("SELECT * FROM device WHERE device_name LIKE CONCAT(#{originName}, '%')")
	List<device> getDevicesByOriginName(@Param("originName") String originName);

        // 
}
