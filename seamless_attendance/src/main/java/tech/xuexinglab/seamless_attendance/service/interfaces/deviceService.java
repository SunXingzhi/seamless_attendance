package tech.xuexinglab.seamless_attendance.service.interfaces;

import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
import tech.xuexinglab.seamless_attendance.entity.device;
import java.util.List;

public interface deviceService {
    // 获取设备列表
    List<device> getAllDevices();
    
    // 获取设备详情
    device getDeviceById(Integer id);
    
    // 添加设备
    int addDevice(deviceDTO deviceDTO);
    
    // 更新设备
    int updateDevice(device device);
    
    // 删除设备
    int deleteDevice(Integer id);
    
    // 重新连接设备
    boolean reconnectDevice(Integer id);
    
    // 激活设备
    boolean activateDevice(Integer id);
    
    // 刷新设备状态
    device refreshDeviceStatus(Integer id);
    
    // 配对人员与设备
    boolean pairPersonWithDevice(String userNumber, Integer deviceId);
    
    // 解除人员与设备的配对
    boolean unpairPersonWithDevice(String userNumber);
    
    // 检查人员是否已与其他设备绑定
    boolean isPersonPairedWithOtherDevice(String userNumber, Integer deviceId);
    
    // 通过设备ID和用户编号解除配对
    boolean unpairPersonByDeviceAndUser(Integer deviceId, String userNumber);
}
