package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import tech.xuexinglab.seamless_attendance.service.MqttDatabaseService;
import tech.xuexinglab.seamless_attendance.service.interfaces.studioService;
import tech.xuexinglab.seamless_attendance.entity.studio;

import tech.xuexinglab.seamless_attendance.entity.user;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.DTO.studioDTO;
import tech.xuexinglab.seamless_attendance.mapper.deviceMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;

import java.util.List;

@RestController
@RequestMapping("/seamless_attendance/api/studios")
public class studioController {
        
        @Autowired
        private MqttDatabaseService mqttDatabaseService;

        @Autowired
        private studioService studioService;

        @Autowired
        private deviceMapper deviceMapper;

        @Autowired
        private userMapper userMapper;

        @RequestMapping("all")
        public ResponseDTO<List<studio>> getAllStudios() {
                // 从数据库中获取所有工作室
                List<studio> studios = studioService.getAllStudios();
                return ResponseDTO.success(studios);
        }

        /**
         * 获取单个工作室详情
         * @param id 工作室ID
         * @return 工作室详情
         */
        @RequestMapping("/{id}")
        public ResponseDTO<studio> getStudioById(@PathVariable Integer id) {
                // 从数据库中获取工作室详情
                studio studio = studioService.getStudioById(id);
                return ResponseDTO.success(studio);
        }

        /**
         * 获取工作室考勤信息
         * @param id 工作室ID
         * @return 考勤信息
         */
        @RequestMapping("/{id}/attendance")
        public ResponseDTO<Object> getStudioAttendance(@PathVariable Integer id) {
                // 从数据库中获取工作室详情
                studio studio = studioService.getStudioById(id);
                
                // 构建考勤信息响应
                final int[] presentCount = {0}; // 使用数组包装，使其成为effectively final
                final int totalCount = studio.getMember_count() != null ? studio.getMember_count() : 0;
                final java.util.List<Object> presentPersons = new java.util.ArrayList<>();
                final java.util.List<Object> absentPersons = new java.util.ArrayList<>();
                
                // 解析人员列表（用户ID）
                String personnel = studio.getPersonnels();
                if (personnel != null && !personnel.isEmpty()) {
                        String[] userIds = personnel.split(",");
                        
                        // 查询每个人员的状态
                        for (String userId : userIds) {
                                if (!userId.trim().isEmpty()) {
                                        try {
                                                Integer userIdInt = Integer.parseInt(userId.trim());
                                                user userInfo = userMapper.getUserInfoById(userIdInt);
                                                if (userInfo != null) {
                                                        // 构建人员信息
                                                        Object personInfo = new Object() {
                                                                public String name = userInfo.getName();
                                                                public String userNumber = userInfo.getUser_number();
                                                                public String status = userInfo.getStatus();
                                                        };
                                                        
                                                        // 根据状态分类
                                                        if ("active".equals(userInfo.getStatus())) {
                                                                presentPersons.add(personInfo);
                                                                presentCount[0]++; // 修改数组中的值
                                                        } else {
                                                                absentPersons.add(personInfo);
                                                        }
                                                }
                                        } catch (NumberFormatException e) {
                                                // 忽略无效的用户ID
                                                System.err.println("Invalid user ID: " + userId);
                                        }
                                }
                        }
                }
                
                // 构建响应对象
                final java.util.List<Object> finalPresentPersons = presentPersons;
                final java.util.List<Object> finalAbsentPersons = absentPersons;
                
                Object attendanceInfo = new Object() {
                        public int present_count = presentCount[0]; // 使用数组中的值
                        public int total_count = totalCount;
                        public java.util.List<Object> presentPersons = finalPresentPersons;
                        public java.util.List<Object> absentPersons = finalAbsentPersons;
                };
                
                return ResponseDTO.success(attendanceInfo);
        }

        @PostMapping("")
        public ResponseDTO<studio> createStudio(@RequestBody studioDTO studio) {
                // 调用服务层创建工作室
                studio createdStudio = studioService.createStudio(studio);
                return ResponseDTO.success(createdStudio);
        }

        /**
         * 更新工作室
         * @param id 工作室ID
         * @param studio 工作室数据
         * @return 更新结果
         */
        @PutMapping("/{id}")
        public ResponseDTO<String> updateStudio(@PathVariable Integer id, @RequestBody studioDTO studio) {
                // 调用服务层更新工作室
                int result = studioService.updateStudio(id, studio);
                if (result > 0) {
                        return ResponseDTO.success("工作室更新成功");
                } else {
                        return ResponseDTO.error("工作室更新失败");
                }
        }

        /**
         * 删除工作室
         * @param id 工作室ID
         * @return 删除结果
         */
        @DeleteMapping("/{id}")
        public ResponseDTO<String> deleteStudio(@PathVariable Integer id) {
                // 调用服务层删除工作室
                int result = studioService.deleteStudio(id);
                if (result > 0) {
                        return ResponseDTO.success("工作室删除成功");
                } else {
                        return ResponseDTO.error("工作室删除失败");
                }
        }
}
