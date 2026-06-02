package top.tiangalon.dydanmaku.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {

    /**
     * 在 configDirPath 下创建 DyDanmakuSettings.toml（若已存在则跳过）
     */
    public static void createDefaultConfig(String configDirPath) {
        File configFile = new File(configDirPath, "DyDanmakuSettings.toml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                String defaultContent = "# 取消下行注释并修改字符串内容来设置DySessionId\n# DySessionId = \"\"\n";
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
}
