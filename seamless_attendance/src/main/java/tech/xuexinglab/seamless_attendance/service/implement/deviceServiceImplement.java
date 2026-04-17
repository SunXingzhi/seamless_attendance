package tech.xuexinglab.seamless_attendance.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.entity.device;
import tech.xuexinglab.seamless_attendance.mapper.deviceMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import tech.xuexinglab.seamless_attendance.mapper.LOTMapper;
import tech.xuexinglab.seamless_attendance.service.MqttMessageSender;
import tech.xuexinglab.seamless_attendance.service.interfaces.deviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;

@Service
public class deviceServiceImplement implements deviceService {
        private static final Logger logger = LoggerFactory.getLogger(deviceServiceImplement.class);

	@Autowired
	private deviceMapper deviceMapper;
	
        @Autowired
        private MqttMessageSender mqttMessageSender;

	@Autowired
	private userMapper userMapper;

	@Autowired
	private LOTMapper lotMapper;

        @Autowired 
        private MqttProperties mqttProperties;

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
		device.setDeviceName(deviceDTO.getDevice_name());
		device.setDeviceId(deviceDTO.getDevice_name()); // 统一使用device_name作为设备标识符
		device.setStudioCodes(deviceDTO.getStudio_codes());
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

        // ID为设备名称
	@Override
	public int deleteDevice(Integer id) {
		try {
			// 1. 首先获取设备信息
			device device = deviceMapper.getDeviceById(id);
			if (device == null) {
				logger.warn("Device not found with id: {}", id);
				return 0;
			}
			
			String deviceName = device.getDeviceName();
			String personnels = device.getPersonnels();
			
			// 2. 向设备发送fullreset命令，初始化硬件
			try {
				String fullresetCommand = String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"fullreset\"}", deviceName);
				mqttMessageSender.sendMessage(mqttProperties.commandTopic, fullresetCommand);
				logger.info("Sent fullreset command to device before deletion: {} (id: {})", deviceName, id);
			} catch (Exception mqttError) {
				logger.warn("Failed to send fullreset command to device {} (id: {}), continuing with deletion: {}", 
					deviceName, id, mqttError.getMessage());
				// 继续执行删除，不因为MQTT失败而中止
			}
			
			// 3. 清理与该设备绑定的用户引用
			if (personnels != null && !personnels.trim().isEmpty()) {
				List<String> personnelList = Arrays.asList(personnels.split(","));
				for (String personnel : personnelList) {
					String userNumber = (userMapper.getUserInfoByName(personnel.trim())).getUserNumber();
					if (userNumber != null) {
						// 解除人员与设备的配对关系
						userMapper.updateUserPairingStatus(userNumber, "unpaired", null);
						// 清除人员的device_id字段
						userMapper.updateUserDeviceId(userNumber, null);
					}
				}
			}
			
			// 4. 删除与该设备关联的考勤记录
			try {
				if (lotMapper != null) {
					int deletedRecords = lotMapper.deleteByDeviceId(deviceName);
					logger.info("Deleted {} attendance records associated with device: {} (id: {})", 
						deletedRecords, deviceName, id);
				}
			} catch (Exception lotError) {
				logger.warn("Failed to delete attendance records for device {} (id: {}): {}", 
					deviceName, id, lotError.getMessage());
				// 继续执行删除，不因为考勤记录删除失败而中止
			}
			
			// 5. 删除设备
			int result = deviceMapper.deleteDevice(id);
			
			// 6. 重新编号设备ID并重置自增计数器
			if (result > 0) {
				try {
					deviceMapper.renumberDeviceIds();
					// 获取当前最大ID并设置自增计数器
					int maxId = deviceMapper.getMaxDeviceId();
					int newAutoIncrement = maxId + 1;
					deviceMapper.resetDeviceAutoIncrement(newAutoIncrement);
					logger.info("Device IDs renumbered after deleting device id: {}", id);
				} catch (Exception e) {
					logger.warn("Failed to renumber device IDs: {}", e.getMessage());
				}
			}
			
			return result;
		} catch (Exception e) {
			logger.error("Error deleting device id: {}", id, e);
			return 0;
		}
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
	
	// 通过设备名称和用户编号解除配对
	public boolean unpairPersonByDeviceAndUser(String deviceName, String userNumber) {
		try {
			// 根据设备名称获取设备
			device device = deviceMapper.getDeviceByDeviceName(deviceName);
			if (device == null) {
				logger.error("Device not found: {}", deviceName);
				return false;
			}
			
			// 更新人员配对状态为未配对，并清除device_id引用
			int result = userMapper.updateUserPairingStatus(userNumber, "unpaired", null);
			return result > 0;
		} catch (Exception e) {
			logger.error("Error unpairing person {} from device {}", userNumber, deviceName, e);
			return false;
		}
	}

        // 清除设备的人员信息数据
        public boolean clearDevicePersonnels(String deviceName) {
                String          personnels;
                List<String>    personnelList;

                personnels      = null;
                try {
                        // 1. 清除设备与人员的绑定关系(对应device表中的personnels字段, 该字段存储的是人员姓名列表, 以逗号分隔)
                        device device = deviceMapper.getDeviceByDeviceName(deviceName);
                        if (device == null) {
                                logger.error("Device not found: {}", deviceName);
                                return false;
                        }

                        // 获取当前设备绑定的人员列表
                        personnels = device.getPersonnels();
                        if(personnels == null || personnels.trim().isEmpty()) {
                                return true; // 设备没有绑定人员，无需清除
                        }

                        // 根据人名信息获取人员编号(学号)
                        personnelList   =  Arrays.asList(personnels.split(","));
                        for(String personnel : personnelList) {
                                String userNumber = (userMapper.getUserInfoByName(personnel.trim())).getUserNumber();
                                if(userNumber != null) {
                                        // 解除人员与设备的配对关系
                                        userMapper.updateUserPairingStatus(userNumber, "unpaired", null);
                                        // 清除人员的device_id字段
                                        userMapper.updateUserDeviceId(userNumber, null);
                                } else{
                                        return false;
                                }
                        }
                        
                        // 2.清除设备的人员信息
                        device.setPersonnels("");
                        deviceMapper.updateDevice(device);
                        
                        return true;
                } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                }
        }


        // 设备复位:蓝牙初始化
        public boolean resetDevice(Integer id) {
                try {
                        device device = deviceMapper.getDeviceById(id);
                        if (device != null) {
                                // 1. 清理与该设备绑定的用户引用
                                String personnels = device.getPersonnels();
                                if (personnels != null && !personnels.trim().isEmpty()) {
                                        List<String> personnelList = Arrays.asList(personnels.split(","));
                                        for (String personnel : personnelList) {
                                                String userNumber = (userMapper.getUserInfoByName(personnel.trim())).getUserNumber();
                                                if (userNumber != null) {
                                                        // 解除人员与设备的配对关系
                                                        userMapper.updateUserPairingStatus(userNumber, "unpaired", null);
                                                        // 清除人员的device_id字段
                                                        userMapper.updateUserDeviceId(userNumber, null);
                                                }
                                        }
                                }
                                
                                // 2. 清除设备的人员信息
                                device.setPersonnels("");
                                deviceMapper.updateDevice(device);
                                
                                // 3. 发送reset命令到设备
                                String resetCommand = String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"reset\"}", device.getDeviceName());
                                mqttMessageSender.sendMessage(mqttProperties.commandTopic, resetCommand);
                                logger.info("Sent reset command to device: {} (id: {})", device.getDeviceName(), id);
                                
                                return true;
                        }
                        return false;
                } catch (Exception e) {
                        logger.error("Error resetting device id: {}", id, e);
                        return false;
                }
        }

        // 设备完全复位(通过MQTT发送"fullreset"指令清除闪存数据)
        public boolean fullResetDevice(String deviceName) {
                try {
                        // 1. 发送MQTT指令给设备, 让设备执行完全复位操作
                        String fullresetCommand = String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"fullreset\"}", deviceName);
                        mqttMessageSender.sendMessage(mqttProperties.commandTopic, fullresetCommand);
                        // 2. 获取原设备名称(用于显示的主设备名称, 例如device_name:A1, origin_device_name:A)
                        String originDeviceName = deviceName.replaceAll("_[0-9]+$", ""); // 移除末尾的数字和下划线
                        fullresetCommand = String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"fullreset\"}", originDeviceName);
                        mqttMessageSender.sendMessage(mqttProperties.commandTopic, fullresetCommand);
                        logger.info("Sent fullreset command to device: {}", deviceName);
                        
                        // 2. 同时清除设备的人员信息数据
                        return clearDevicePersonnels(deviceName);
                } catch (Exception e) {
                        logger.error("Failed to send full reset command to device {}", deviceName, e);
                        return false;
                }
        }

}