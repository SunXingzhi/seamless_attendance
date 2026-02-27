package tech.xuexinglab.seamless_attendance.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

	private Integer code;
	private String message;
	private T data;

	// 成功响应
	public static <T> ResponseDTO<T> success(T data){
		return new ResponseDTO<>(200, "成功响应", data);
	}

	// 错误响应
	public static <T> ResponseDTO<T> error(Integer code, String message){
		return new ResponseDTO<>(code, message, null);
	}
	
	// 失败响应
	public static <T> ResponseDTO<T> error(String message){
		return new ResponseDTO<>(500, message, null);
	}
}