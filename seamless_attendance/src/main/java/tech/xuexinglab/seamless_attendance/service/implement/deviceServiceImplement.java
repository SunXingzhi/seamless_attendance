package tech.xuexinglab.seamless_attendance.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
import tech.xuexinglab.seamless_attendance.entity.device;
import tech.xuexinglab.seamless_attendance.mapper.deviceMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import tech.xuexinglab.seamless_attendance.service.interfaces.deviceService;
import java.util.List;

@Service
public class deviceServiceImplement implements deviceService {

	@Autowired
	private deviceMapper deviceMapper;
	
	@Autowired
	private userMapper userMapper;

	@Override
	public List<device> getAllDevices() {
                // 从数据库获取所有设备
                List<device> devices = deviceMapper.getAllDevices();
		return devices;
	}

	@Override
	public device getDeviceById(Integer id) {
		return deviceMapper.getDeviceById(id);
	}

	@Override
	public int addDevice(deviceDTO deviceDTO) {
		device device = new device();
		device.setDevice_name(deviceDTO.getDevice_name());
		device.setDevice_id(deviceDTO.getDevice_id());
		device.setStudio_codes(deviceDTO.getStudio_codes());
		device.setPersonnels(deviceDTO.getPersonnels());
		// 默认新建设备为未激活状态
		device.setStatus("inactive");
		// 如果绑定了人员，自动激活设备
		if (deviceDTO.getPersonnels() != null && !deviceDTO.getPersonnels().isEmpty()) {
			device.setStatus("activated");
		}
		return deviceMapper.insertDevice(device);
	}

	@Override
	public int updateDevice(device device) {
		// 如果绑定了人员，自动激活设备
		if (device.getPersonnels() != null && !device.getPersonnels().isEmpty()) {
			device.setStatus("activated");
		} else {
			// 如果没有绑定人员，设置为未激活状态
			device.setStatus("inactive");
		}
		return deviceMapper.updateDevice(device);
	}

	@Override
	public int deleteDevice(Integer id) {
		return deviceMapper.deleteDevice(id);
	}

	@Override
	public boolean reconnectDevice(Integer id) {
		device device = deviceMapper.getDeviceById(id);
		if (device != null) {
			deviceMapper.updateDeviceStatus(id, "connected");
			return true;
		}
		return false;
	}

	@Override
	public boolean activateDevice(Integer id) {
		device device = deviceMapper.getDeviceById(id);
		if (device != null) {
			deviceMapper.updateDeviceStatus(id, "activated");
			return true;
		}
		return false;
	}

	@Override
	public device refreshDeviceStatus(Integer id) {
		device device = deviceMapper.getDeviceById(id);
		if (device != null) {
			deviceMapper.updateDeviceStatus(id, "active");
			return deviceMapper.getDeviceById(id);
		}
		return null;
	}
	
	// 配对人员与设备
	public boolean pairPersonWithDevice(String userNumber, Integer deviceId) {
		// 检查人员是否已与其他设备绑定
		int count = userMapper.checkUserPaired(userNumber, deviceId);
		if (count > 0) {
			return false;
		}
		
		// 更新人员配对状态为已配对
		int result = userMapper.updateUserPairingStatus(userNumber, "paired", deviceId);
		return result > 0;
	}
	
	// 解除人员与设备的配对
	public boolean unpairPersonWithDevice(String userNumber) {
		// 更新人员配对状态为未配对
		int result = userMapper.updateUserPairingStatus(userNumber, "unpaired", null);
		return result > 0;
	}
	
	// 检查人员是否已与其他设备绑定
	public boolean isPersonPairedWithOtherDevice(String userNumber, Integer deviceId) {
		int count = userMapper.checkUserPaired(userNumber, deviceId);
		return count > 0;
	}
	
	// 通过设备ID和用户编号解除配对
	public boolean unpairPersonByDeviceAndUser(Integer deviceId, String userNumber) {
		// 更新人员配对状态为未配对
		int result = userMapper.updateUserPairingStatus(userNumber, "unpaired", null);
		return result > 0;
	}
}