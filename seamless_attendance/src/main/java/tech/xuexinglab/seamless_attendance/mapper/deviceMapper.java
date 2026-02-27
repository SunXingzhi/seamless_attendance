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
    @Insert("INSERT INTO device (device_name, device_id, studio_codes, personnels, status, create_time, update_time) " +
            "VALUES (#{device_name}, #{device_id}, #{studio_codes}, #{personnels}, #{status}, NOW(), NOW())")
    int insertDevice(device device);
    
    // 获取设备列表
    @Select("SELECT * FROM device")
    List<device> getAllDevices();
    
    // 根据ID获取设备详情
    @Select("SELECT * FROM device WHERE id = #{id}")
    device getDeviceById(@Param("id") Integer id);
    
    // 根据设备名称获取设备
    @Select("SELECT * FROM device WHERE device_name = #{deviceName}")
    List<device> getDevicesByName(@Param("deviceName") String deviceName);
    
    // 根据设备名称获取单个设备
    @Select("SELECT * FROM device WHERE device_name = #{deviceName} LIMIT 1")
    device getDeviceByDeviceName(@Param("deviceName") String deviceName);
    
    // 更新设备
    @Update("UPDATE device SET device_name = #{device_name}, device_id = #{device_id}, studio_codes = #{studio_codes}, " +
            "personnels = #{personnels}, status = #{status}, update_time = NOW() " +
            "WHERE id = #{id}")
    int updateDevice(device device);
    
    // 删除设备
    @Delete("DELETE FROM device WHERE id = #{id}")
    int deleteDevice(@Param("id") Integer id);
    
    // 重新编号设备ID，保持连续性
    @Update("UPDATE device SET id = (SELECT new_id FROM (SELECT @i:=@i+1 AS new_id, id FROM device ORDER BY id) AS t WHERE t.id = device.id)")
    int renumberDeviceIds();
    
    // 重置设备表自增计数器
    @Update("ALTER TABLE device AUTO_INCREMENT = (SELECT IFNULL(MAX(id), 0) + 1 FROM device);")
    int resetDeviceAutoIncrement();
    
    // 更新设备状态
    @Update("UPDATE device SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateDeviceStatus(@Param("id") Integer id, @Param("status") String status);
}
