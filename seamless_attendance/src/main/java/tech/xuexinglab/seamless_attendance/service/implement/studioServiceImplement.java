package tech.xuexinglab.seamless_attendance.service.implement;

import tech.xuexinglab.seamless_attendance.service.interfaces.studioService;
import tech.xuexinglab.seamless_attendance.DTO.studioDTO;
import tech.xuexinglab.seamless_attendance.entity.studio;
import tech.xuexinglab.seamless_attendance.entity.user;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.xuexinglab.seamless_attendance.mapper.studioMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;

@Service
public class studioServiceImplement implements studioService {
        @Autowired
        private studioMapper studioMapper;
        
        @Autowired
        private userMapper userMapper;

        @Override
        public List<studio> getAllStudios() {
                return studioMapper.getAllStudios();
        }

        @Override
        public studio getStudioById(Integer id) {
                return studioMapper.getStudioById(id);
        }

        @Override
        public List<studio> getStudiosByAdminId(Integer admin_id) {
                return studioMapper.getStudiosByAdminId(admin_id);
        }

        // 创建工作室
        @Override
        public studio createStudio(studioDTO studioDTO) {
                studio studio = new studio();
                studio.setStudioName(studioDTO.getStudio_name());
                studio.setStudioCode(studioDTO.getStudio_code());
                studio.setDescription(studioDTO.getDescription());
                
                // 设置管理员信息
                String adminName = studioDTO.getAdmin_name();
                String adminUserNumber = studioDTO.getAdmin_user_number();
                Integer adminId = 1; // 默认管理员ID
                
                if (adminUserNumber != null && !adminUserNumber.isEmpty()) {
                        // 通过工号查询用户ID作为管理员ID
                        user adminUser = userMapper.getUserInfoByUserNumber(adminUserNumber);
                        if (adminUser != null) {
                                adminId = adminUser.getId();
                                studio.setAdminName(adminUser.getName());
                                studio.setAdminUserNumber(adminUser.getUserNumber());
                        }
                } else if (adminName != null && !adminName.isEmpty()) {
                        // 如果没有工号，尝试通过姓名查询
                        user adminUser = userMapper.getUserInfoByName(adminName);
                        if (adminUser != null) {
                                adminId = adminUser.getId();
                                studio.setAdminName(adminUser.getName());
                                studio.setAdminUserNumber(adminUser.getUserNumber());
                        }
                }
                
                studio.setAdminId(adminId);
                
                // 计算member_count和max_member_count
                int totalMemberCount = 0;
                int maxMemberCount = 0;
                List<Integer> userIds = new ArrayList<>();
                
                // 解析人员列表（学号），排除管理员
                String personnelNumbers = studioDTO.getPersonnels();
                if (personnelNumbers != null && !personnelNumbers.isEmpty()) {
                        String[] numbers = personnelNumbers.split(",");
                        for (String number : numbers) {
                                if (!number.trim().isEmpty()) {
                                        // 通过学号查询用户
                                        user userInfo = userMapper.getUserInfoByUserNumber(number.trim());
                                        if (userInfo != null) {
                                                // 检查是否为管理员，避免重复添加
                                                if (!userInfo.getId().equals(adminId)) {
                                                        userIds.add(userInfo.getId());
                                                }
                                        }
                                }
                        }
                }
                
                // 确保管理员被包含在成员列表中（如果尚未存在）
                if (adminId != null && adminId != 1 && !userIds.contains(adminId)) {
                        userIds.add(adminId);
                }
                
                totalMemberCount = userIds.size();
                maxMemberCount = totalMemberCount; // 最大成员数等于实际选择的人数
                
                // 将用户ID列表转换为逗号分隔的字符串
                StringBuilder userIdStr = new StringBuilder();
                for (int i = 0; i < userIds.size(); i++) {
                        userIdStr.append(userIds.get(i));
                        if (i < userIds.size() - 1) {
                                userIdStr.append(",");
                        }
                }
                studio.setPersonnels(userIdStr.toString());
                
                studio.setMemberCount(totalMemberCount);
                studio.setMaxMemberCount(maxMemberCount);
                studio.setStatus("active");
                
                studioMapper.insertStudio(studio);
                return studio;
        }

        @Override
        public int deleteStudio(Integer id) {
                return studioMapper.deleteStudio(id);
        }

        @Override
        public int updateStudio(Integer id, studioDTO studioDTO) {
                studio studio = new studio();
                studio.setId(id);
                studio.setStudioName(studioDTO.getStudio_name());
                studio.setStudioCode(studioDTO.getStudio_code());
                studio.setDescription(studioDTO.getDescription());
                
                // 设置管理员信息
                String adminName = studioDTO.getAdmin_name();
                String adminUserNumber = studioDTO.getAdmin_user_number();
                Integer adminId = 1; // 默认管理员ID
                
                if (adminUserNumber != null && !adminUserNumber.isEmpty()) {
                        // 通过工号查询用户ID作为管理员ID
                        user adminUser = userMapper.getUserInfoByUserNumber(adminUserNumber);
                        if (adminUser != null) {
                                adminId = adminUser.getId();
                                studio.setAdminName(adminUser.getName());
                                studio.setAdminUserNumber(adminUser.getUserNumber());
                        }
                } else if (adminName != null && !adminName.isEmpty()) {
                        // 如果没有工号，尝试通过姓名查询
                        user adminUser = userMapper.getUserInfoByName(adminName);
                        if (adminUser != null) {
                                adminId = adminUser.getId();
                                studio.setAdminName(adminUser.getName());
                                studio.setAdminUserNumber(adminUser.getUserNumber());
                        }
                }
                
                studio.setAdminId(adminId);
                
                // 计算member_count和max_member_count
                int totalMemberCount = 0;
                int maxMemberCount = 0;
                List<Integer> userIds = new ArrayList<>();
                
                // 解析人员列表（学号），排除管理员
                String personnelNumbers = studioDTO.getPersonnels();
                if (personnelNumbers != null && !personnelNumbers.isEmpty()) {
                        String[] numbers = personnelNumbers.split(",");
                        for (String number : numbers) {
                                if (!number.trim().isEmpty()) {
                                        // 通过学号查询用户
                                        user userInfo = userMapper.getUserInfoByUserNumber(number.trim());
                                        if (userInfo != null) {
                                                // 检查是否为管理员，避免重复添加
                                                if (!userInfo.getId().equals(adminId)) {
                                                        userIds.add(userInfo.getId());
                                                }
                                        }
                                }
                        }
                }
                
                // 确保管理员被包含在成员列表中（如果尚未存在）
                if (adminId != null && adminId != 1 && !userIds.contains(adminId)) {
                        userIds.add(adminId);
                }
                
                totalMemberCount = userIds.size();
                maxMemberCount = totalMemberCount; // 最大成员数等于实际选择的人数
                
                // 将用户ID列表转换为逗号分隔的字符串
                StringBuilder userIdStr = new StringBuilder();
                for (int i = 0; i < userIds.size(); i++) {
                        userIdStr.append(userIds.get(i));
                        if (i < userIds.size() - 1) {
                                userIdStr.append(",");
                        }
                }
                studio.setPersonnels(userIdStr.toString());
                
                studio.setMemberCount(totalMemberCount);
                studio.setMaxMemberCount(maxMemberCount);
                studio.setStatus("active");
                
                return studioMapper.updateStudio(studio);
        }
}
