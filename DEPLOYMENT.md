# Seamless Attendance 系统部署指南

## 1. 服务器环境准备

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装JDK 8
sudo apt install openjdk-8-jdk -y

# 安装MySQL 8.0
sudo apt install mysql-server -y

# 安装Nginx
sudo apt install nginx -y

# 安装Node.js 20+
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装MQTT Broker (EMQX)
wget https://www.emqx.io/downloads/broker/v5.0.14/emqx-ubuntu20.04-amd64-v5.0.14.deb
dpkg -i emqx-ubuntu20.04-amd64-v5.0.14.deb
```

## 2. 数据库配置

```bash
# 登录MySQL
sudo mysql -u root

# 创建数据库和用户
CREATE DATABASE seamless_attendance CHARACTER SET utf8mb4;
CREATE USER 'seamless_attendance_user'@'localhost' IDENTIFIED BY 'strong_password_here';
GRANT ALL PRIVILEGES ON seamless_attendance.* TO 'seamless_attendance_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

## 3. 后端部署

```bash
# 创建应用目录
sudo mkdir -p /opt/seamless_attendance
sudo chown $USER:$USER /opt/seamless_attendance

# 复制JAR文件（在打包后）
cp target/seamless_attendance-0.0.1-SNAPSHOT.jar /opt/seamless_attendance/

# 复制systemd服务文件
sudo cp seamless-attendance.service /etc/systemd/system/

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable seamless-attendance
sudo systemctl start seamless-attendance
```

## 4. 前端部署

```bash
# 构建前端
cd Fronted
npm run build

# 复制构建文件到Nginx目录
sudo cp -r dist/* /usr/share/nginx/html/

# 重启Nginx
sudo systemctl restart nginx
```

## 5. 验证部署

- 访问 `http://your-server-ip` 查看前端页面
- 检查后端日志: `journalctl -u seamless-attendance -f`
- 检查Nginx访问日志: `sudo tail -f /var/log/nginx/access.log`