package top.tiangalon.dydanmaku.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    /** 方法名到模板配置键的映射 */
    public static final Map<String, String> METHOD_TO_TEMPLATE_KEY = new HashMap<>();
    static {
        METHOD_TO_TEMPLATE_KEY.put("WebcastChatMessage",       "Chat");
        METHOD_TO_TEMPLATE_KEY.put("WebcastMemberMessage",      "Member");
        METHOD_TO_TEMPLATE_KEY.put("WebcastRoomUserSeqMessage", "RoomStats");
        METHOD_TO_TEMPLATE_KEY.put("WebcastLikeMessage",        "Like");
        METHOD_TO_TEMPLATE_KEY.put("WebcastGiftMessage",        "Gift");
        METHOD_TO_TEMPLATE_KEY.put("WebcastFansclubMessage",    "Fansclub");
    }

    /**
     * 弹幕过滤器配置
     */
    public static class FilterConfig {
        /** 过滤模式: "disabled"(禁用), "blacklist"(黑名单), "whitelist"(白名单) */
        public String mode = "disabled";
        /** 过滤关键词列表 */
        public List<String> keywords = new ArrayList<>();

        public boolean isEnabled() {
            return ("blacklist".equals(mode) || "whitelist".equals(mode)) && keywords != null && !keywords.isEmpty();
        }
    }

    /**
     * 消息类型可见性配置
     */
    public static class MethodVisibilityConfig {
        public boolean chat = true;      // WebcastChatMessage
        public boolean member = true;    // WebcastMemberMessage
        public boolean roomStats = true; // WebcastRoomUserSeqMessage
        public boolean like = true;      // WebcastLikeMessage
        public boolean gift = true;      // WebcastGiftMessage
        public boolean fansclub = true;  // WebcastFansclubMessage

        /**
         * 根据 method 名称判断是否应该显示该类型的消息
         */
        public boolean isMethodEnabled(String method) {
            switch (method) {
                case "WebcastChatMessage":       return chat;
                case "WebcastMemberMessage":      return member;
                case "WebcastRoomUserSeqMessage": return roomStats;
                case "WebcastLikeMessage":        return like;
                case "WebcastGiftMessage":        return gift;
                case "WebcastFansclubMessage":    return fansclub;
                default:                          return true; // 未知类型默认显示
            }
        }
    }

    /**
     * 消息类型自定义输出模板配置
     */
    public static class TemplateConfig {
        /** method 名称 -> 模板字符串 */
        public Map<String, String> templates = new HashMap<>();

        /**
         * 获取指定 method 对应的输出模板
         * @param method 方法名，如 "WebcastChatMessage"
         * @return 模板字符串，若未设置则返回 null
         */
        public String getTemplate(String method) {
            String key = METHOD_TO_TEMPLATE_KEY.get(method);
            if (key != null) {
                return templates.get(key);
            }
            return null;
        }
    }

    /**
     * 用户属性过滤器配置（粉丝团、消费等级）
     */
    public static class UserFilterConfig {
        /** 是否启用用户过滤 */
        public boolean enabled = false;
        /** 是否要求必须有粉丝团 */
        public boolean requireFanClub = false;
        /** 最小粉丝团等级（0=不限制） */
        public int fanClubMinLevel = 0;
        /** 是否要求必须有消费等级 */
        public boolean requirePayGrade = false;
        /** 最小消费等级（0=不限制） */
        public int payGradeMinLevel = 0;
    }

    /**
     * 在 configDirPath 下创建 DyDanmakuSettings.toml（若已存在则跳过）
     */
    public static void createDefaultConfig(String configDirPath) {
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                String defaultContent = "# 在下方输入抖音直播官网的sessionId\n"
                        + "DySessionId = \"\"\n"
                        + "\n"
                        + "# 弹幕过滤器设置\n"
                        + "# mode: 过滤模式，可选值:\n"
                        + "#   \"disabled\"  - 禁用过滤 (默认)\n"
                        + "#   \"blacklist\" - 黑名单模式，屏蔽包含关键词的弹幕\n"
                        + "#   \"whitelist\" - 白名单模式，仅显示包含关键词的弹幕\n"
                        + "# keywords: 关键词列表，匹配弹幕内容（不区分消息类型）\n"
                        + "[Filter]\n"
                        + "mode = \"blacklist\"\n"
                        + "keywords = [\"关键词1\", \"关键词2\"]\n"
                        + "\n"
                        + "# 消息类型显示开关\n"
                        + "# 设置为 false 则不在聊天框和弹幕列表中显示该类型的消息\n"
                        + "[MethodVisibility]\n"
                        + "chat = true      # 聊天消息\n"
                        + "member = true    # 进入直播间消息\n"
                        + "roomStats = true # 直播间统计消息\n"
                        + "like = true      # 点赞消息\n"
                        + "gift = true      # 礼物消息\n"
                        + "fansclub = true  # 粉丝团消息\n"
                        + "\n"
                        + "# 自定义输出模板\n"
                        + "# 使用 ${变量名} 引用消息中的数据，不设置则使用默认格式\n"
                        + "# 通用用户变量（Chat/Member/Like/Gift 均可用）:\n"
                        + "#   ${nickname} 用户名, ${payGradeLevel} 消费等级(无则为空), ${fansClubLevel} 粉丝团等级(无则为空)\n"
                        + "# 各消息类型专属变量:\n"
                        + "#   Chat:      ${content}\n"
                        + "#   Member:    ${memberCount}, ${actionDescription}, ${userId}\n"
                        + "#   RoomStats: ${totalStr}, ${totalPvForAnchor}\n"
                        + "#   Like:      ${count}\n"
                        + "#   Gift:      ${giftName}, ${giftCombo}, ${comboCount}, ${repeatCount}, ${giftId}, ${giftDescribe}, ${giftDiamondCount}, ${giftType}\n"
                        + "#   Fansclub:  ${content}\n"
                        + "[Template]\n"
                        + "Chat = \"\\u00a7b[消息]\\u00a7f ${nickname}：${content}\"\n"
                        + "Member = \"\\u00a7e[入场]\\u00a7f ${nickname} 进入了直播间\"\n"
                        + "RoomStats = \"\\u00a79[统计]\\u00a7f 当前观看：${totalStr}，累计观看：${totalPvForAnchor}\"\n"
                        + "Like = \"\\u00a7d[点赞]\\u00a7f ${nickname} 点了${count}个赞\"\n"
                        + "Gift = \"\\u00a7a[礼物]\\u00a7f ${nickname} 送出了${giftName}${giftCombo}\"\n"
                        + "Fansclub = \"\\u00a76[粉丝团]\\u00a7f ${content}\"\n"
                        + "\n"
                        + "# 用户属性过滤器（基于粉丝团/消费等级过滤）\n"
                        + "# 启用后，聊天、点赞、礼物消息会根据发送者属性进行过滤\n"
                        + "# requireFanClub: 是否要求必须有粉丝团（true=只显示有粉丝团用户的消息）\n"
                        + "# fanClubMinLevel: 最低粉丝团等级要求（0=不限制，如设为5则只显示粉丝团≥5级的用户消息）\n"
                        + "# requirePayGrade: 是否要求必须有消费等级（true=只显示有消费等级用户的消息）\n"
                        + "# payGradeMinLevel: 最低消费等级要求（0=不限制，如设为5则只显示消费≥5级的用户消息）\n"
                        + "# 多个条件为\"且\"关系，需同时满足\n"
                        + "[UserFilter]\n"
                        + "enabled = false\n"
                        + "requireFanClub = false\n"
                        + "fanClubMinLevel = 0\n"
                        + "requirePayGrade = false\n"
                        + "payGradeMinLevel = 0\n";
                Files.write(configFile.toPath(), defaultContent.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从 DyDanmakuSettings.toml 中读取 DySessionId
     * @return 用户设置的 DySessionId 值，若未设置则返回 null
     */
    public static String getSessionId(String configDirPath) {
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            return null;
        }
        try {
            Toml toml = new Toml().read(configFile);
            String sessionId = toml.getString("DySessionId");
            if (sessionId != null && !sessionId.isEmpty()) {
                return sessionId;
            }
        } catch (Exception e) {
            // 读取失败，返回 null
        }
        return null;
    }

    /**
     * 从 DyDanmakuSettings.toml 中读取弹幕过滤器配置
     * @param configDirPath 配置目录路径
     * @return FilterConfig 过滤配置，若未设置则返回默认（disabled）配置
     */
    public static FilterConfig getFilterConfig(String configDirPath) {
        FilterConfig config = new FilterConfig();
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            return config;
        }
        try {
            Toml toml = new Toml().read(configFile);
            String mode = toml.getString("Filter.mode");
            if (mode != null && !mode.isEmpty()) {
                config.mode = mode;
            }
            List<String> keywords = toml.getList("Filter.keywords");
            if (keywords != null) {
                config.keywords = new ArrayList<>(keywords);
            }
        } catch (Exception e) {
            // 读取失败，返回默认配置
        }
        return config;
    }

    /**
     * 从 DyDanmakuSettings.toml 中读取消息类型可见性配置
     * @param configDirPath 配置目录路径
     * @return MethodVisibilityConfig，若未设置则返回全 true 的默认配置
     */
    public static MethodVisibilityConfig getMethodVisibilityConfig(String configDirPath) {
        MethodVisibilityConfig config = new MethodVisibilityConfig();
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            return config;
        }
        try {
            Toml toml = new Toml().read(configFile);
            Boolean chat = toml.getBoolean("MethodVisibility.chat");
            if (chat != null) config.chat = chat;
            Boolean member = toml.getBoolean("MethodVisibility.member");
            if (member != null) config.member = member;
            Boolean roomStats = toml.getBoolean("MethodVisibility.roomStats");
            if (roomStats != null) config.roomStats = roomStats;
            Boolean like = toml.getBoolean("MethodVisibility.like");
            if (like != null) config.like = like;
            Boolean gift = toml.getBoolean("MethodVisibility.gift");
            if (gift != null) config.gift = gift;
            Boolean fansclub = toml.getBoolean("MethodVisibility.fansclub");
            if (fansclub != null) config.fansclub = fansclub;
        } catch (Exception e) {
            // 读取失败，返回默认配置
        }
        return config;
    }

    /**
     * 从 DyDanmakuSettings.toml 中读取自定义输出模板配置
     * @param configDirPath 配置目录路径
     * @return TemplateConfig，包含各消息类型的模板
     */
    public static TemplateConfig getTemplateConfig(String configDirPath) {
        TemplateConfig config = new TemplateConfig();
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            return config;
        }
        try {
            Toml toml = new Toml().read(configFile);
            for (Map.Entry<String, String> entry : METHOD_TO_TEMPLATE_KEY.entrySet()) {
                String key = entry.getValue();
                String template = toml.getString("Template." + key);
                if (template != null && !template.isEmpty()) {
                    config.templates.put(key, template);
                }
            }
        } catch (Exception e) {
            // 读取失败，返回默认配置
        }
        return config;
    }

    /**
     * 从 DyDanmakuSettings.toml 中读取用户属性过滤器配置
     * @param configDirPath 配置目录路径
     * @return UserFilterConfig，若未设置则返回默认（disabled）配置
     */
    public static UserFilterConfig getUserFilterConfig(String configDirPath) {
        UserFilterConfig config = new UserFilterConfig();
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            return config;
        }
        try {
            Toml toml = new Toml().read(configFile);
            Boolean enabled = toml.getBoolean("UserFilter.enabled");
            if (enabled != null) config.enabled = enabled;
            Boolean requireFanClub = toml.getBoolean("UserFilter.requireFanClub");
            if (requireFanClub != null) config.requireFanClub = requireFanClub;
            Long fanClubMinLevel = toml.getLong("UserFilter.fanClubMinLevel");
            if (fanClubMinLevel != null) config.fanClubMinLevel = fanClubMinLevel.intValue();
            Boolean requirePayGrade = toml.getBoolean("UserFilter.requirePayGrade");
            if (requirePayGrade != null) config.requirePayGrade = requirePayGrade;
            Long payGradeMinLevel = toml.getLong("UserFilter.payGradeMinLevel");
            if (payGradeMinLevel != null) config.payGradeMinLevel = payGradeMinLevel.intValue();
        } catch (Exception e) {
            // 读取失败，返回默认配置
        }
        return config;
    }
}
