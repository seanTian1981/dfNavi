# Launcher Icons

由于Android应用需要多个不同尺寸的启动图标，请使用Android Studio的Image Asset工具生成所需的图标文件：

## 需要的图标文件

### mipmap-anydpi-v26/
- ic_launcher.xml (已创建)
- ic_launcher_round.xml (已创建)
- ic_launcher_foreground.xml (已创建)

### mipmap-mdpi/ (48x48)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-hdpi/ (72x72)
- ic_launcher.png (占位符已创建，需替换)
- ic_launcher_round.png

### mipmap-xhdpi/ (96x96)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-xxhdpi/ (144x144)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-xxxhdpi/ (192x192)
- ic_launcher.png
- ic_launcher_round.png

## 生成方法

### 使用Android Studio
1. 右键点击 `res` 文件夹
2. 选择 New -> Image Asset
3. 在 "Icon Type" 中选择 "Launcher Icons (Adaptive and Legacy)"
4. 设置前景层为白色导航图标
5. 设置背景层为黄色
6. 点击 "Next" 然后 "Finish"

### 或使用ImageMagick命令
```bash
# 示例：生成不同尺寸的图标
convert -resize 48x48 icon.png app/src/main/res/mipmap-mdpi/ic_launcher.png
convert -resize 72x72 icon.png app/src/main/res/mipmap-hdpi/ic_launcher.png
convert -resize 96x96 icon.png app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert -resize 144x144 icon.png app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert -resize 192x192 icon.png app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

## 应用说明

- 临时图标文件已创建，应用可以编译
- 为了更好的用户体验，建议生成正式的应用图标
- 图标应遵循高对比度设计原则，便于视障用户识别
