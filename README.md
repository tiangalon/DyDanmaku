
# DyDanmaku

一个用于在聊天框接收抖音直播弹幕的Minecraft模组

<!-- PROJECT LOGO -->
<br />

<p align="center">
  <a href="https://github.com/tiangalon/DyDanmaku/">
    <img src="./pic/icon.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">抖音弹幕获取</h3>
  <p align="center">
    在Minecraft游戏内接收你的 <s>或者别人的</s> 直播间弹幕
    <br />
    <a href="https://github.com/tiangalon/DyDanmaku"><strong>探索本项目的文档 &raquo;</strong></a>
    <br />
    <br />
    <a href="https://www.mcmod.cn/class/17853.html">MCMOD</a>
    &middot;
    <a href="https://github.com/tiangalon/DyDanmaku/issues">报告Bug</a>
    &middot;
    <a href="https://github.com/tiangalon/DyDanmaku/issues">提出新特性</a>
  </p>
</p>

> **注意：** 该模组正处于早期开发阶段，支持版本较少，且仅为单人游戏设计，可能会出现bug或者是兼容性问题，请谨慎使用。
>
> **注意：** 该模组仅供学习交流使用，请勿用于商业，甚至是违法犯罪用途，违者后果自负。

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

## 写在前面

本人偶尔在抖音直播mc，用直播伴侣的时候看弹幕比较麻烦，而且会发生严重的掉帧，于是想到有没有模组能直接把弹幕发送到游戏内，这样抓取一下推流码即可实现完全摆脱直播伴侣。

去搜了下，发现 [BakaDanmaku](https://github.com/TartaricAcid/BakaDanmaku) 可实现类似的功能，可惜仅支持b站弹幕，所以照着它的效果图写了个抖音版的。

没有参考代码，但是参考了起名(

期间还有个试做品 [dy_danmaku_java](https://github.com/tiangalon/dy_danmaku_java)，代码思路差不多，也许参考意义更大。

## 使用指南

### 游戏环境要求

1. Minecraft 1.20.1~26.1.2（更多版本待日后支持，提issue可能加急）
2. fabric 0.19.2及以上

### 安装方法

1. 从release中下载对应版本的jar文件（当前最新版本0.1.6）
2. 准备好符合上述要求的游戏环境
3. 丢进 .minecraft/mods 里面

### 使用方法

<details>
<summary><b>前置步骤：设置模组配置（可选）</b></summary>
<br>

模组的缓存与配置文件存放于游戏版本文件夹中 `config/dydanmaku` 目录下，其中 `DyDanmakuSettings.toml` 为本模组配置文件，启动一次游戏后自动生成。

由于抖音官方政策的限制，现在需要配置用户sessionid才可获取礼物等全部弹幕信息，因此需要自行从官网cookies中复制该值并加入配置文件。

> 不配置不影响mod正常使用，若无需配置可直接进入下一步。

**sessionid的获取与配置方法：**

1. 进入 [抖音直播官网](https://live.douyin.com/) 并登录自己账号
2. 打开浏览器开发者工具，一般快捷键为 `F12`（下面以谷歌浏览器为例，其他浏览器自行寻找方法）
3. 找到 Cookie 中 `sessionid` 的值并复制（具体方法参考下图）

<img src="./pic/sessionid获取方法.png" style="margin: 0 auto;">

4. 在游戏版本文件夹 `/config/dydanmaku/DyDanmakuSettings.toml` 中配置 `sessionid`

<img src="./pic/sessionid设置方法.png" style="margin: 0 auto;">

</details>

<details>
<summary><b>高级配置：弹幕过滤、类型开关与自定义模板</b></summary>
<br>

模组支持在 `DyDanmakuSettings.toml` 中通过 TOML 配置来控制弹幕的显示行为，所有修改无需重启游戏即可实时生效。

---

#### 一、弹幕过滤器

通过关键词对弹幕进行筛选，支持黑名单和白名单两种模式，匹配不区分大小写。

```toml
[Filter]
mode = "blacklist"                     # "disabled"(禁用) / "blacklist"(黑名单) / "whitelist"(白名单)
keywords = ["广告", "加群", "刷屏"]    # 关键词列表
```

- **`blacklist`**：屏蔽包含任一关键词的消息
- **`whitelist`**：仅显示包含任一关键词的消息
- **`disabled`**：禁用过滤（默认）

---

#### 二、消息类型显示开关

可选择性地关闭或开启特定类型的消息显示。

```toml
[MethodVisibility]
chat = true       # 聊天消息 (WebcastChatMessage)
member = false    # 进入直播间消息 (WebcastMemberMessage)
roomStats = false # 直播间统计消息 (WebcastRoomUserSeqMessage)
like = true       # 点赞消息 (WebcastLikeMessage)
gift = true       # 礼物消息 (WebcastGiftMessage)
fansclub = true   # 粉丝团消息 (WebcastFansclubMessage)
```

设置为 `false` 的消息类型将不在聊天框和弹幕列表中显示。所有类型默认为 `true`。

---

#### 三、自定义输出模板

可以为每种消息类型编写自定义的输出格式，使用 `${变量名}` 引用消息中的数据字段。

**通用用户变量（Chat/Member/Like/Gift 均可使用）：**

| 变量 | 说明 |
|------|------|
| `${nickname}` | 用户昵称 |
| `${payGradeLevel}` | 消费等级（无消费等级时为空） |
| `${fansClubLevel}` | 粉丝团等级（无粉丝团时为空） |

**配置示例：**

```toml
[Template]
Chat = "§b[聊天]§f ${nickname}：${content}"
Member = "§e[入场]§f ${nickname} 进入了直播间"
Like = "§d[点赞]§f ${nickname} 点了${count}个赞"
Gift = "§a[礼物]§f ${nickname} 送出了${giftName}${giftCombo}"
Fansclub = "§6[粉丝团]§f ${content}"
RoomStats = "§9[统计]§f 当前观看：${totalStr}，累计观看：${totalPvForAnchor}"
```

**各消息类型的专属变量：**

| 配置键 | 消息类型 | 专属变量 |
|--------|---------|----------|
| `Chat` | 聊天 | `${content}` |
| `Member` | 入场 | `${memberCount}`, `${actionDescription}`, `${userId}` |
| `RoomStats` | 统计 | `${totalStr}`, `${totalPvForAnchor}` |
| `Like` | 点赞 | `${count}` |
| `Gift` | 礼物 | `${giftName}`, `${giftCombo}`, `${comboCount}`, `${repeatCount}`, `${giftId}`, `${giftDescribe}`, `${giftDiamondCount}`, `${giftType}` |
| `Fansclub` | 粉丝团 | `${content}` |

> **说明：**
> - `${payGradeLevel}` 和 `${fansClubLevel}` 可用于 Chat、Member、Like、Gift 四种类型
> - `${giftCombo}` 是预计算的连击文本（如 `x5`），无连击时为空字符串
> - 也可单独使用 `${comboCount}` 自行控制连击显示格式
> - 未赋值的占位符会在渲染时自动清除，不会出现 `${unknown}` 残留
> - 使用 `§` 加字母/数字表示 Minecraft 格式化代码（如 `§b` 青色、`§a` 绿色、`§d` 粉色等）
> - 不设置模板的消息类型将使用默认格式

</details>

#### 第一步：进入游戏内世界

启动游戏，进入任意一个单人世界。

#### 第二步：连接直播间

提供以下两种连接方式，任选其一即可：

**方式一：通过 GUI 连接**

按下 `F7`（默认按键，可在游戏内更改按键绑定），打开 GUI，点击"连接"按钮。

![gui界面](./pic/gui界面.jpg)

**方式二：通过命令连接**

```java
/dydanmaku connect [live_id]
```

> `live_id` 即抖音直播间链接最后的数字部分。例如直播间链接为 `https://live.douyin.com/1234567890`，则输入 `/dydanmaku connect 1234567890`。

#### 第三步：断开直播间连接

输入以下命令断开连接：

```java
/dydanmaku disconnect
```

也可以点击 GUI 界面上的"已连接（断开）"按钮。

#### 第四步：查看连接状态

输入以下命令查看当前连接直播间的状态：

```java
/dydanmaku status
```

> **注意：** 一次只能连接一个直播间，想连接新的直播间需要先断开原来的连接。

## 效果图

（图中连接直播间在首页随便找的）

![连接效果图](./pic/连接效果.png)
![gui效果图1](./pic/gui界面1.jpg)
![gui效果图2](./pic/gui界面2.jpg)

## 后续改进计划

1. 添加对Forge以及更多游戏版本的支持
2. 添加json文件配置，通过修改文件自定义弹幕输出文本

## 鸣谢

### 部分依赖来源

- [protobuffers](https://github.com/protocolbuffers/protobuf)
- [netty](https://netty.io/)
- [toml4j](https://github.com/mwanji/toml4j)
- [nashorn](https://github.com/openjdk/nashorn)
- [dy_danmaku_java](https://github.com/tiangalon/dy_danmaku_java)

### 灵感来源

- [BakaDanmaku](https://github.com/TartaricAcid/BakaDanmaku)
