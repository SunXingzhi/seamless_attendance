package tech.xuexinglab.seamless_attendance.service.interfaces;

import tech.xuexinglab.seamless_attendance.DTO.studioDTO;
import tech.xuexinglab.seamless_attendance.entity.studio;

import java.util.List;


public interface studioService {
        List<studio> getAllStudios();
        studio getStudioById(Integer id);
        List<studio> getStudiosByAdminId(Integer admin_id);

        studio createStudio(studioDTO studio);
        int updateStudio(Integer id, studioDTO studio);
        int deleteStudio(Integer id);
}
