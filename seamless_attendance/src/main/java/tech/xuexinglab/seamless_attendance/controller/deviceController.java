package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
import tech.xuexinglab.seamless_attendance.entity.device;
import tech.xuexinglab.seamless_attendance.service.interfaces.deviceService;
import java.util.List;

@RestController
@RequestMapping("/seamless_attendance/api")
public class deviceController {

	@Autowired
	private deviceService deviceService;

	// 获取设备列表
	@GetMapping("/devices/all")
	public ResponseDTO<List<device>> getAllDevices() {
		return ResponseDTO.success(deviceService.getAllDevices());
	}

	// 获取设备详情
	@GetMapping("/device/detail/{id}")
	public ResponseDTO<device> getDeviceDetail(@PathVariable Integer id) {
		device device = deviceService.getDeviceById(id);
		if (device != null) {
			return ResponseDTO.success(device);
		} else {
			return ResponseDTO.error("设备不存在");
		}
	}

	// 添加设备
	@PostMapping("/devices")
	public ResponseDTO<String> addDevice(@RequestBody deviceDTO deviceDTO) {
		int result = deviceService.addDevice(deviceDTO);
		if (result > 0) {
			return ResponseDTO.success("设备添加成功");
		} else {
			return ResponseDTO.error("设备添加失败");
		}
	}

	// 更新设备
	@PutMapping("/device/info/{id}")
	public ResponseDTO<String> updateDevice(@PathVariable Integer id, @RequestBody deviceDTO deviceDTO) {
		device device = new device();
		device.setId(id);
		device.setDevice_name(deviceDTO.getDevice_name());
		device.setDevice_id(deviceDTO.getDevice_id());
		device.setStudio_codes(deviceDTO.getStudio_codes());
		device.setStatus("absent");
		
		if (deviceDTO.getPersonnels() != null) {
				device.setPersonnels(deviceDTO.getPersonnels());
			}
		
		int result = deviceService.updateDevice(device);
		if (result > 0) {
			return ResponseDTO.success("设备更新成功");
		} else {
			return ResponseDTO.error("设备更新失败");
		}
	}

	// 删除设备
	@DeleteMapping("/devices/{id}")
	public ResponseDTO<String> deleteDevice(@PathVariable Integer id) {
		int result = deviceService.deleteDevice(id);
		if (result > 0) {
			return ResponseDTO.success("设备删除成功");
		} else {
			return ResponseDTO.error("设备删除失败");
		}
	}

	// 重新连接设备
	@PostMapping("/devices/{id}/reconnect")
	public ResponseDTO<String> reconnectDevice(@PathVariable Integer id) {
		boolean result = deviceService.reconnectDevice(id);
		if (result) {
			return ResponseDTO.success("设备重新连接成功");
		} else {
			return ResponseDTO.error("设备重新连接失败");
		}
	}

	// 激活设备
	@PostMapping("/device/{id}/activate")
	public ResponseDTO<String> activateDevice(@PathVariable Integer id) {
		boolean result = deviceService.activateDevice(id);
		if (result) {
			return ResponseDTO.success("设备激活成功");
		} else {
			return ResponseDTO.error("设备激活失败");
		}
	}

	// 刷新设备状态
	@GetMapping("/devices/{id}/status")
	public ResponseDTO<device> refreshDeviceStatus(@PathVariable Integer id) {
		device device = deviceService.refreshDeviceStatus(id);
		if (device != null) {
			return ResponseDTO.success(device);
		} else {
			return ResponseDTO.error("设备状态刷新失败");
		}
	}
}