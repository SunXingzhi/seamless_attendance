# Seamless Attendance System Framework

## 系统概述

无缝考勤系统是一个基于MQTT物联网协议和Spring Boot + Vue.js的全栈考勤管理系统。

### 技术栈

- **后端**: Spring Boot 2.6.x, MyBatis, MySQL
- **前端**: Vue 3, Pinia, Vue Router
- **通信协议**: MQTT (物联网设备通信), RESTful API
- **实时通信**: WebSocket

---

## 权限系统

### 三种权限级别

| 角色 | 英文标识 | 说明 |
| ---- | -------- | ---- |
| 超级管理员 | super_admin | 拥有系统全部权限，可管理所有工作室和设备 |
| 工作室管理员 | studio_admin | 管理特定工作室，查看本工作室人员和考勤 |
| 工作室成员 | studio_member | 查看本工作室人员和考勤，执行任务 |

### 权限控制详情

#### 页面访问权限

| 页面/功能 | 超级管理员 | 工作室管理员 | 工作室成员 |
| --------- | ---------- | ------------ | ---------- |
| 工作室管理-工作室列表 | ✓ | ✗ | ✗ |
| 工作室管理-创建工作室 | ✓ | ✗ | ✗ |
| 设备管理 | ✓ | ✗ | ✗ |
| 人员管理-人员列表 | ✓ | 仅本工作室 | 仅本工作室 |
| 人员管理-考勤记录 | ✓ | 仅本工作室 | 仅本工作室 |
| 统计分析-活跃度统计 | ✓ | 仅本工作室 | ✗ |
| 统计分析-出勤率分析 | ✓ | 仅本工作室 | ✗ |
| 统计分析-报告生成 | ✓ | 仅本工作室 | ✗ |
| 工作任务-任务看板 | ✓ | ✓ | ✓ |
| 工作任务-任务分配 | ✓ | ✓ | ✗ |

#### 功能按钮权限

| 功能 | 超级管理员 | 工作室管理员 | 工作室成员 | ---------- |
| ---- | ------------ | ---------- |
| 添加人员 | ✓ | ✗ | ✗ |
| 编辑人员 | ✓ | ✗ | ✗ |
| 删除人员 | ✓ | ✗ | ✗ |
| 设备配对/解配 | ✓ | ✗ | ✗ |
| 创建工作室 | ✓ | ✗ | ✗ |
| 编辑工作室 | ✓ | ✗ | ✗ |
| 删除工作室 | ✓ | ✗ | ✗ |

---

## 数据库设计

### 用户表 (user)

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | INT | 主键，自增 |
| user_number | VARCHAR(50) | 学号/工号，唯一 |
| name | VARCHAR(50) | 姓名 |
| role | VARCHAR(20) | 角色：super_admin, studio_admin, studio_member |
| password | VARCHAR(100) | 登录密码 |
| contact_type | VARCHAR(20) | 联系方式类型：wechat, qq, phone |
| contact_number | VARCHAR(50) | 联系方式 |
| studio_id | INT | 所属工作室ID |
| studio_admin_id | INT | 所属工作室ID（管理员用） |
| status | VARCHAR(20) | 考勤状态：active, late, leave, absent, holiday, excused |
| pairing_status | VARCHAR(20) | 配对状态：paired, unpaired |
| work_task | VARCHAR(100) | 当前工作任务 |
| device_id | INT | 配对的设备ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 工作室表 (studio)

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | INT | 主键，自增 |
| studio_name | VARCHAR(100) | 工作室名称 |
| studio_code | VARCHAR(50) | 工作室代码，唯一 |
| description | TEXT | 工作室描述 |
| personnels | TEXT | 人员ID列表，逗号分隔 |
| admin_id | INT | 管理员用户ID |
| admin_name | VARCHAR(50) | 管理员姓名 |
| admin_user_number | VARCHAR(50) | 管理员工号 |
| member_count | INT | 当前成员数量 |
| max_member_count | INT | 最大成员数量 |
| status | VARCHAR(20) | 状态：active, inactive |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 设备表 (device)

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | INT | 主键，自增 |
| device_id | VARCHAR(50) | 设备唯一标识 |
| device_name | VARCHAR(100) | 设备名称 |
| device_type | VARCHAR(50) | 设备类型 |
| status | VARCHAR(20) | 设备状态：active, absent |
| pairing_status | VARCHAR(20) | 配对状态：paired, unpaired |
| paired_user_id | INT | 配对的用户ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 考勤记录表 (attendance)

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | INT | 主键，自增 |
| user_id | INT | 用户ID |
| date | DATE | 考勤日期 |
| check_in_time | DATETIME | 签到时间 |
| check_out_time | DATETIME | 签退时间 |
| status | VARCHAR(20) | 状态：active, late, leave, absent, holiday, excused |
| create_time | DATETIME | 创建时间 |

---

## API 接口设计

### 登录接口

**POST** `/seamless_attendance/api/login`

请求体:
```json
{
  "username": "admin",
  "password": "123456"
}
```

响应:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "管理员",
    "role": "super_admin",
    "studioId": null,
    "studioAdminId": null
  }
}
```

### 人员管理接口

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| GET | /api/personnel/all | 获取所有人员列表 |
| POST | /api/personnel | 添加人员 |
| PUT | /api/personnel/{id} | 更新人员信息 |
| DELETE | /api/personnel/{id} | 删除人员 |

### 工作室管理接口

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| GET | /api/studios/all | 获取所有工作室 |
| GET | /api/studios/{id} | 获取工作室详情 |
| POST | /api/studios | 创建工作室 |
| PUT | /api/studios/{id} | 更新工作室 |
| DELETE | /api/studios/studio/info/{id} | 删除工作室 |

### 设备管理接口

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| GET | /api/device/all | 获取所有设备 |
| POST | /api/device | 添加设备 |
| PUT | /api/device/{id} | 更新设备信息 |
| DELETE | /api/device/{id} | 删除设备 |
| POST | /api/device/pair | 配对设备 |
| POST | /api/device/unpair | 解除配对 |

---

## 前端页面结构

### 菜单栏设计

```
├── 工作室管理 (仅super_admin)
│   └── 工作室列表
├── 人员管理
│   ├── 人员列表
│   └── 考勤记录
├── 统计分析 (super_admin/studio_admin)
│   ├── 活跃度统计
│   ├── 出勤率分析
│   └── 报告生成
├── 工作任务
│   ├── 任务看板
│   └── 任务分配 (仅super_admin/studio_admin)
└── 设备管理 (仅super_admin)
```

---

## 核心业务逻辑

### 1. 设备配对

当人员与设备配对时:
1. 更新device表的paired_user_id为用户ID
2. 更新device表的pairing_status为"paired"
3. 更新user表的device_id为设备ID
4. 更新user表的pairing_status为"paired"

### 2. 解除配对

当解除设备配对时:
1. 清空device表的paired_user_id
2. 更新device表的pairing_status为"unpaired"
3. 清空user表的device_id
4. 更新user表的pairing_status为"unpaired"

### 3. 考勤状态更新

通过MQTT接收到设备消息时:
1. 解析消息获取用户ID和状态
2. 更新user表的status字段
3. 创建或更新attendance表记录

---

## 工作流程

### 超级管理员工作流程

1. 登录系统
2. 创建设备
3. 创建工作室，指定工作室管理员
4. 添加人员到工作室
5. 分配设备给人员
6. 查看统计分析

### 工作室管理员工作流程

1. 登录系统
2. 查看本工作室人员列表
3. 查看本工作室考勤记录
4. 查看统计分析
5. 分配任务给成员

### 工作室成员工作流程

1. 登录系统
2. 查看个人考勤状态
3. 查看任务看板
4. 执行工作任务
