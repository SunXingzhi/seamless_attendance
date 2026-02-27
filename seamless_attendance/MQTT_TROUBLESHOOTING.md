# MQTT 消息接收问题诊断指南

## 问题描述
使用 MQTT 客户端发送数据 `{"ID":"A1","P1":1,"P2":1,"P3":1}` 到 `ESP32C3/status1` 主题，系统没有反应，断点也没有到达 `personnel_status_analysis` 函数。

## 可能的原因及解决方案

### 1. 检查 MQTT 配置是否正确加载

**步骤：**
1. 启动应用
2. 访问：`http://localhost:8081/api/mqtt/config`
3. 查看返回的配置信息

**预期输出：**
```
MQTT Configuration:
Broker: tcp://47.108.57.12:1883
ClientId: seamless_attendance_dev
Subscribe Topic: ESP32C3/status1
Username: emqx_test
QoS: 1
```

**如果配置不正确：**
- 检查 `application-dev.properties` 文件
- 确认 `mqtt.subscribe.topic=ESP32C3/status1` 配置正确
- 重启应用

### 2. 检查 MQTT 连接状态

**查看应用启动日志：**

在应用启动时，应该看到以下日志：
```
Connecting to MQTT broker tcp://47.108.57.12:1883 with clientId=seamless_attendance_dev
Successfully connected and subscribed to topic ESP32C3/status1 with qos=1
```

**如果看到连接失败日志：**
```
Failed to connect/subscribe to MQTT broker
```

**解决方案：**
- 检查 MQTT Broker 地址是否正确：`47.108.57.12:1883`
- 检查网络连接是否正常
- 检查用户名和密码是否正确：`emqx_test` / `emqx_test_password`
- 检查防火墙设置

### 3. 检查 MQTT Broker 连通性

**使用 MQTT 客户端工具测试：**

推荐使用以下工具：
- MQTT Explorer
- MQTT.fx
- mosquitto_pub/mosquitto_sub

**测试步骤：**
1. 使用相同的连接参数连接到 MQTT Broker
2. 订阅主题：`ESP32C3/status1`
3. 发送测试消息：`{"ID":"A1","P1":1,"P2":1,"P3":1}`
4. 确认能否收到消息

### 4. 检查日志级别

**确认日志级别设置为 DEBUG：**

在 `application-dev.properties` 中：
```properties
logging.level.tech.xuexinglab.seamless_attendance=DEBUG
```

**查看详细日志：**
- MQTT 连接日志
- 消息接收日志
- 消息解析日志

### 5. 检查消息格式

**确认消息格式正确：**

```json
{
  "ID": "A1",
  "P1": 1,
  "P2": 1,
  "P3": 1
}
```

**注意事项：**
- JSON 格式必须正确
- 字段名必须区分大小写（`ID` 是大写）
- 值必须是数字（0 或 1），不能是字符串

### 6. 检查代码逻辑

**查看 MqttDatabaseService.java：**

确认以下代码正确执行：
```java
client.setCallback(new MqttCallback() {
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), "UTF-8");
        logger.info("Received message. topic={}, payload={}", topic, payload);
        try {
            messageHandler.personnel_status_analysis(topic, payload);
        } catch (Exception e) {
            logger.error("Error processing MQTT message", e);
        }
    }
});
```

**查看 mqttDataParse.java：**

确认 `personnel_status_analysis` 方法被调用：
```java
@Override
public void personnel_status_analysis(String topic, String payload){
    logger.info("MQTT message received. topic='{}' payload='{}'", topic, payload);
    // ... 其他代码
}
```

### 7. 常见问题排查

#### 问题 1：应用启动时没有看到 MQTT 连接日志

**原因：** `@PostConstruct` 方法没有执行

**解决方案：**
- 确认 `MqttDatabaseService` 类上有 `@Service` 注解
- 确认 Spring Boot 正确扫描了该包
- 检查是否有其他启动错误

#### 问题 2：看到连接失败日志

**原因：** MQTT Broker 连接失败

**解决方案：**
- 检查网络连接
- 检查 MQTT Broker 是否运行
- 检查端口是否正确（默认 1883）
- 检查用户名密码是否正确

#### 问题 3：连接成功但收不到消息

**原因：** 订阅主题不匹配

**解决方案：**
- 确认发送的主题是 `ESP32C3/status1`
- 确认订阅的主题是 `ESP32C3/status1`
- 注意主题区分大小写

#### 问题 4：收到消息但解析失败

**原因：** JSON 格式错误或字段不匹配

**解决方案：**
- 检查 JSON 格式是否正确
- 检查字段名是否匹配（`ID`、`P1`、`P2`、`P3`）
- 查看错误日志中的异常信息

### 8. 调试步骤

**步骤 1：确认应用正在运行**
```bash
# 检查应用是否在 8081 端口运行
curl http://localhost:8081/api/mqtt/config
```

**步骤 2：查看应用日志**
```bash
# 查看应用启动日志
# 查找 MQTT 相关日志
```

**步骤 3：使用 MQTT 客户端测试**
```bash
# 使用 mosquitto_pub 发送测试消息
mosquitto_pub -h 47.108.57.12 -p 1883 -u emqx_test -P emqx_test_password -t ESP32C3/status1 -m '{"ID":"A1","P1":1,"P2":1,"P3":1}'
```

**步骤 4：检查数据库**
```sql
-- 查看是否有数据插入
SELECT * FROM lot_record ORDER BY id DESC LIMIT 10;
```

### 9. 代码改进建议

**添加更多日志：**

在 `MqttDatabaseService.java` 中添加：
```java
@PostConstruct
public void start() {
    logger.info("Starting MQTT Database Service...");
    running = true;
    connectAndSubscribe();
}
```

在 `mqttDataParse.java` 中添加：
```java
@Override
public void personnel_status_analysis(String topic, String payload){
    logger.info("=== personnel_status_analysis called ===");
    logger.info("Topic: {}", topic);
    logger.info("Payload: {}", payload);
    // ... 其他代码
}
```

### 10. 测试清单

- [ ] 应用正常启动
- [ ] MQTT 配置正确加载（访问 /api/mqtt/config）
- [ ] MQTT 连接成功（查看日志）
- [ ] 订阅主题正确（ESP32C3/status1）
- [ ] MQTT Broker 可达（使用客户端工具测试）
- [ ] 消息格式正确（JSON 格式）
- [ ] 断点能够到达（personnel_status_analysis）
- [ ] 数据能够正确保存到数据库

## 联系支持

如果以上步骤都无法解决问题，请提供以下信息：
1. 应用启动日志（完整）
2. MQTT 配置信息（访问 /api/mqtt/config）
3. 发送的消息内容
4. 错误日志（如果有）
5. 网络环境信息
