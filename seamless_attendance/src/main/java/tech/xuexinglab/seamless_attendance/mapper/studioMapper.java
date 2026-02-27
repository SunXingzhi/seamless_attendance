package tech.xuexinglab.seamless_attendance.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tech.xuexinglab.seamless_attendance.entity.studio;

import java.util.List;

@Mapper
public interface studioMapper {
        
        @Select("select * from studio")
        List<studio> getAllStudios();

        @Select("select * from studio where id = #{id}")
        studio getStudioById(Integer id);

        @Select("select * from studio where admin_id = #{admin_id}")
        List<studio> getStudiosByAdminId(Integer admin_id);

        @Insert("INSERT INTO studio (studio_name, studio_code, description, personnels, admin_id, member_count, max_member_count, status, create_time, update_time) " +
                "VALUES (#{studio_name}, #{studio_code}, #{description}, #{personnels}, #{admin_id}, #{member_count}, #{max_member_count}, #{status}, NOW(), NOW())")
        int insertStudio(studio studio);

        @Update("UPDATE studio SET studio_name = #{studio_name}, studio_code = #{studio_code}, description = #{description}, " +
                "personnels = #{personnels}, admin_id = #{admin_id}, " +
                "member_count = #{member_count}, max_member_count = #{max_member_count}, " +
                "status = #{status}, update_time = NOW() WHERE id = #{id}")
        int updateStudio(studio studio);

        // 删除工作室
        @Delete("DELETE FROM studio WHERE id = #{id}")
        int deleteStudio(Integer id);

}
