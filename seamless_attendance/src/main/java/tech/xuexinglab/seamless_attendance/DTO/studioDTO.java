package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class studioDTO {
    
    @NotBlank(message = "工作室名称不能为空")
    private String studio_name;
    
    @NotBlank(message = "工作室代码不能为空")
    private String studio_code;
    
    private String description;
    
    private String personnels;
    
    private String admin_name;
    
    private String admin_user_number;
}