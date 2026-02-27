package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class deviceDTO {
    
    @NotBlank(message = "设备名称不能为空")
    private String device_name;
    
    @NotBlank(message = "设备ID不能为空")
    private String device_id;
    
    private String type;
    
    private String location;
    
    private String studio_codes;
    
    private String personnels;
}
