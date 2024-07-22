

# DyDanmaku

一个用于在聊天框接收抖音直播弹幕的Minecraft模组


<!-- PROJECT LOGO -->
<br />

<p align="center">
  <a href="https://github.com/tiangalon/DyDanmaku/">
    <img src="src/main/resources/assets/dydanmaku/icon.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">抖音弹幕获取</h3>
  <p align="center">
    在Minecraft游戏内接收你的 <s>或者别人的</s> 直播间弹幕
    <br />
    <a href="https://github.com/tiangalon/DyDanmaku"><strong>探索本项目的文档 »</strong></a>
    <br />
    <br />
    <a href="https://github.com/tiangalon/DyDanmaku">查看Demo</a>
    ·
    <a href="https://github.com/tiangalon/DyDanmaku/issues">报告Bug</a>
    ·
    <a href="https://github.com/tiangalon/DyDanmaku/issues">提出新特性</a>

<h3>注意：该模组正处于早期开发阶段，支持版本较少，且仅为单人游戏设计，可能会出现bug或者是兼容性问题，请谨慎使用</h3>
<h3>注意：该模组仅供学习交流使用，请勿用于商业，甚至是违法犯罪用途，违者后果自负</h3>
 
## 目录

- [写在前面](#写在前面)
- [使用指南](#使用指南)
  - [游戏环境要求](#游戏环境要求)
  - [安装方法](#安装方法)
  - [使用方法](#使用方法)
- [效果图](#效果图)
- [后续改进计划](#后续改进计划)
- [鸣谢](#鸣谢)
  - [部分依赖来源](#部分依赖来源)
  - [灵感来源](#灵感来源)


### 写在前面
<p>本人偶尔在抖音直播mc，用直播伴侣的时候看弹幕比较麻烦，而且会发生严重的掉帧，于是想到有没有模组能直接把弹幕发送到游戏内，这样抓取一下推流码即可实现完全摆脱直播伴侣</p>
<p>去搜了下，发现<a href="https://github.com/TartaricAcid/BakaDanmaku">BakaDanmaku</a>可实现类似的功能，可惜仅支持b站弹幕，所以照着它的效果图写了个抖音版的</p>
<p>没有参考代码，但是参考了起名(</p>
<p>期间还有个试做品<a href="https://github.com/tiangalon/dy_danmaku_java">BakaDanmaku</a>，代码思路差不多，也许参考意义更大</p>

### 使用指南

###### 游戏环境要求

1. Minecraft1.20.1（更多版本待日后支持，提issue可能加急）
2. Java17以上
3. fabric0.15.11以上

###### **安装方法**

1. 从release中下载对应版本的jar文件
2. 准备好符合上述要求的游戏环境
3. 丢进.minecraft/mods里面

###### 使用方法
1. 进入游戏内世界
2. 输入以下命令连接直播间
```Java
/dydanmaku connect [live_id]
```
live_id为抖音直播间链接最后的数字部分，比如某人直播间链接为https://live.douyin.com/1234567890 ，那么live_id就是1234567890，所以应该输入/dydanmaku connect 1234567890

3. 想关闭时输入一下命令断开连接
```Java
/dydanmaku disconnect
```

<h3>注意：一次只能连接一个直播间，想连接新的直播间需要先断开原来的连接</h3>

### 效果图
![屏幕截图 2024-07-22 114905](https://github.com/user-attachments/assets/ea129dfe-e4d3-4a08-8669-9ae0913a6db4)


### 后续改进计划
1. 添加对forge以及更多游戏版本的支持
2. 添加json文件配置，通过修改文件自定义弹幕输出文本
3. 添加游戏内GUI



### 鸣谢

##### 部分依赖来源
- [protobuffers](https://github.com/protocolbuffers/protobuf)
- [netty](https://netty.io/)
- [dy_danmaku_java](https://github.com/tiangalon/dy_danmaku_java)

##### 灵感来源
- [bakadanmaku](https://github.com/TartaricAcid/BakaDanmaku)

