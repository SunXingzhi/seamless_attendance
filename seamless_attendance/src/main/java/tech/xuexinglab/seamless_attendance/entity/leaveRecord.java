package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class leaveRecord {
        Integer id;
        String userNumber; // 用户工号
        String startDate; // 开始日期，格式：yyyy-MM-dd
        String endDate; // 结束日期，格式：yyyy-MM-dd
        String leaveType; // 请假类型：sick(病假), personal(事假), annual(年假)
        String reason; // 请假原因
        String status; // 状态：approved(已批准), pending(待批准), rejected(已拒绝)
        String createTime;
        String updateTime;
}