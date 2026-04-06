package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LOTMapper {
        @Insert("insert into lot_record (device_id, personnel1_status, personnel2_status, personnel3_status) " +
                        "values (#{deviceId}, #{personnel1Status}, #{personnel2Status}, #{personnel3Status})")
        public void smallSaveAttendanceRecord(@Param("deviceId") String deviceId,
                        @Param("personnel1Status") String personnel1Status,
                        @Param("personnel2Status") String personnel2Status,
                        @Param("personnel3Status") String personnel3Status);

        @Delete("DELETE FROM lot_record WHERE device_id = #{deviceId}")
        public int deleteByDeviceId(@Param("deviceId") String deviceId);
}