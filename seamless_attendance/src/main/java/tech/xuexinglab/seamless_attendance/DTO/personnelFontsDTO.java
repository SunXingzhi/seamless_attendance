package tech.xuexinglab.seamless_attendance.DTO;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class personnelFontsDTO {
        private String fonts_name;
        private String personnel_index;
        private String[] fonts_data;
        private String type;
        private String total_parts;

}
