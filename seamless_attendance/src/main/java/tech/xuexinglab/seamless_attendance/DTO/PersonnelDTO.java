package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelDTO {

	private boolean is_active;

	private String user_number;
}
