package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class userDTO {

        // @NotBlank(message = "用户ID不能为空")
        private Integer userId;

        @NotBlank(message = "姓名不能为空")
        private String name;

        @NotBlank(message = "学号/工号不能为空")
        private String user_number;

        private String contact_type;

        @NotBlank(message = "联系方式不能为空")
        private String contact_value;

        private String role;

        private String work_task;

        private String pairing_status;

        private String studio_id;

        private Integer device_id;
        // private String pairingStatus;
}
