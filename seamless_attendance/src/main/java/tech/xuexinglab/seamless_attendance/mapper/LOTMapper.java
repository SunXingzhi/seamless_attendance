package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LOTMapper {
        @Insert("insert into lot_record (device_id, personnel1_status, personnel2_status, personnel3_status) " +
                        "values (#{device_id}, #{personnel1_status}, #{personnel2_status}, #{personnel3_status})")
        public void smallSaveAttendanceRecord(@Param("device_id") String device_id,
                        @Param("personnel1_status") String personnel1_status,
                        @Param("personnel2_status") String personnel2_status,
                        @Param("personnel3_status") String personnel3_status);

        @Delete("DELETE FROM lot_record WHERE device_id = #{deviceId}")
        public int deleteByDeviceId(@Param("deviceId") String deviceId);
}