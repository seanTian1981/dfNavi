# 黑龙江东方学院校园语音导航应用

专为盲人学生设计的校园语音导航Android应用，通过GPS定位、语音交互和步数指引实现离线校园导航。

## 功能特性

### 核心功能
- **离线导航**: 使用SQLite存储地图数据，无需网络连接
- **GPS定位**: 实时跟踪用户位置
- **语音播报**: 使用中文TTS提供导航指令
- **步数指引**: 基于用户步长计算需要走的步数
- **高对比度UI**: 专为视障用户设计的高对比度界面

### 主界面
1. **开始导航**: 选择起点和终点，开始语音导航
2. **管理位置**: 添加、编辑、删除校园位置
3. **设置**: 自定义步长、语音速度、音量等设置

## 技术架构

### 技术栈
- **语言**: Java
- **最低SDK**: 24 (Android 7.0)
- **目标SDK**: 34 (Android 14)
- **数据库**: SQLite
- **定位服务**: Google Play Services Location
- **语音服务**: Android TextToSpeech

### 项目结构
```
dfNavi/
├── app/src/main/
│   ├── java/com/heibeieast/campusnav/
│   │   ├── MainActivity.java              # 主界面
│   │   ├── NavigationActivity.java        # 导航界面
│   │   ├── LocationManagementActivity.java # 位置管理
│   │   ├── SettingsActivity.java          # 设置界面
│   │   ├── services/
│   │   │   ├── DatabaseService.java       # 数据库服务
│   │   │   ├── LocationService.java       # GPS定位服务
│   │   │   ├── VoiceService.java          # 语音服务
│   │   │   └── PathPlanningService.java   # 路径规划服务
│   │   ├── utils/
│   │   │   └── PermissionManager.java     # 权限管理
│   │   └── models/
│   │       ├── CampusLocation.java        # 位置模型
│   │       ├── Route.java                 # 路线模型
│   │       └── NavigationInstruction.java # 导航指令模型
│   ├── res/
│   │   ├── layout/                        # 布局文件
│   │   ├── values/
│   │   │   ├── strings.xml                # 中文字符串
│   │   │   └── colors.xml                 # 高对比度配色
│   │   └── drawable/                      # 可绘制资源
│   └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── README.md
```

## 数据库结构

### campus_locations (校园位置表)
- id: 主键
- name: 位置名称
- latitude: GPS纬度
- longitude: GPS经度
- category: 位置类别
- description: 位置描述
- created_at: 创建时间

### campus_routes (路线表)
- id: 主键
- from_location_id: 起点ID
- to_location_id: 终点ID
- distance_meters: 距离（米）
- route_description: 路线描述
- estimated_steps: 预估步数

### user_preferences (用户偏好表)
- id: 主键
- average_step_length: 平均步长（米）
- voice_speed: 语音速度
- voice_volume: 语音音量
- accessibility_mode: 无障碍模式

### user_navigation_history (导航历史表)
- id: 主键
- from_location_id: 起点ID
- to_location_id: 终点ID
- start_time: 开始时间
- end_time: 结束时间
- distance_traveled: 实际距离

## 使用说明

### 首次使用
1. 启动应用后，会自动请求位置和录音权限
2. 数据库会自动初始化，预置校园位置数据
3. 在设置中调整您的平均步长以获得准确的步数指引

### 开始导航
1. 点击"开始导航"
2. 选择起点和终点位置
3. 点击"开始导航"按钮
4. 根据语音提示行走
5. 到达目标后应用会自动提示

### 管理位置
1. 点击"管理位置"
2. 可以添加自定义位置（使用当前GPS坐标）
3. 编辑或删除已有位置
4. 所有操作都有语音反馈

### 设置
- **平均步长**: 调整以获得准确的步数计算（0.5-1.0米）
- **语音速度**: 调整语音播放速度（0.5-2.0倍）
- **语音音量**: 调整语音音量（0-100）
- **无障碍模式**: 开启/关闭无障碍功能

## 无障碍设计

### 视觉设计
- 黑色背景 + 白色文字 + 黄色按钮边框
- 最小按钮尺寸48x48dp
- 文字最小14sp
- 高对比度配色

### 辅助功能
- 所有UI元素都包含contentDescription
- 支持TalkBack屏幕阅读
- 所有操作都有语音反馈
- 导航指令清晰易懂

## 权限说明

应用需要以下权限：
- **ACCESS_FINE_LOCATION**: 获取精确GPS位置
- **ACCESS_COARSE_LOCATION**: 获取大致位置
- **RECORD_AUDIO**: 录制语音（预留功能）
- **INTERNET**: 网络访问（预留功能）

## 预置位置

黑龙江东方学院预置位置（估算坐标）：
- 图书馆
- 主楼（行政楼）
- 学生食堂
- 教学楼
- 体育馆
- 南门
- 北门
- 宿舍区

## 编译与运行

### 要求
- Android Studio Arctic Fox或更高版本
- JDK 8或更高版本
- Android SDK API 34
- Gradle 8.0

### 编译
```bash
./gradlew assembleDebug
```

### 安装
```bash
./gradlew installDebug
```

## 开发说明

### 代码规范
- 严格遵循Android命名规范
- 所有异步操作使用Thread或AsyncTask
- 完整的错误处理和用户提示
- 清晰的代码注释

### 服务说明
- **DatabaseService**: 单例模式，管理所有数据库操作
- **LocationService**: GPS定位、距离计算、方向计算
- **VoiceService**: TTS语音合成、语音播报
- **PathPlanningService**: 路径规划、导航指令生成

## 版本信息

- 版本: 1.0
- 开发日期: 2024年
- 目标用户: 黑龙江东方学院视障学生

## 许可证

本项目为黑龙江东方学院专用项目。

## 技术支持

如有问题或建议，请联系开发团队。

---

**黑龙江东方学院 © 2024**
