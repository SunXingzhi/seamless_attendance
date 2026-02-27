package tech.xuexinglab.seamless_attendance.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LOTMapperTest {

@Autowired
private LOTMapper lotMapper;

@Test
public void testSaveAttendanceRecord() {
String deviceId = "A1";
String personnel1Status = "1";
String personnel2Status = "1";
String personnel3Status = "0";

lotMapper.smallSaveAttendanceRecord(deviceId, personnel1Status, personnel2Status, personnel3Status);

System.out.println("测试成功：数据已插入到 users 表");
}

@Test
public void testSaveAttendanceRecordWithAllactive() {
String deviceId = "A2";
String personnel1Status = "1";
String personnel2Status = "1";
String personnel3Status = "1";

lotMapper.smallSaveAttendanceRecord(deviceId, personnel1Status, personnel2Status, personnel3Status);

System.out.println("测试成功：所有人员都在线的记录已插入");
}

@Test
public void testSaveAttendanceRecordWithAllabsent() {
String deviceId = "A3";
String personnel1Status = "0";
String personnel2Status = "0";
String personnel3Status = "0";

lotMapper.smallSaveAttendanceRecord(deviceId, personnel1Status, personnel2Status, personnel3Status);

System.out.println("测试成功：所有人员都不在线的记录已插入");
}

@Test
public void testSaveAttendanceRecordWithMixedStatus() {
String deviceId = "A4";
String personnel1Status = "1";
String personnel2Status = "0";
String personnel3Status = "1";

lotMapper.smallSaveAttendanceRecord(deviceId, personnel1Status, personnel2Status, personnel3Status);

System.out.println("测试成功：混合状态的记录已插入");
}

@Test
public void testSaveAttendanceRecordWithEmptyDeviceId() {
String deviceId = "";
String personnel1Status = "1";
String personnel2Status = "1";
String personnel3Status = "0";

lotMapper.smallSaveAttendanceRecord(deviceId, personnel1Status, personnel2Status, personnel3Status);

System.out.println("测试成功：空设备ID的记录已插入");
}
}