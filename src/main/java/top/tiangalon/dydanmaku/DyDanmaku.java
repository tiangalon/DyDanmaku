package top.tiangalon.dydanmaku;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import DyDanmaku.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static DyDanmaku.WebSocketClientNetty.getSignFile;

public class DyDanmaku implements ModInitializer {
    public static final String MOD_ID = "DyDanmaku";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static WebSocketClientNetty websocket = new WebSocketClientNetty();

    public static String DyDanmakuPath = WebSocketClientNetty.getPath();
    public static String ConfigDirPath = DyDanmakuPath.substring(0, DyDanmakuPath.lastIndexOf("/")) + "/config/DyDanmaku";

    @Override
    public void onInitialize() {
        LOGGER.info("DyDanmaku initialized");
        FileInit();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("dydanmaku").executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]Called /dydanmaku with no arguments."), false);
                    return Command.SINGLE_SUCCESS;
                })
                    .then(CommandManager.literal("connect")
                            .then(CommandManager.argument("live_id", StringArgumentType.string())
                                .executes(context -> {
                                    if (websocket.isConnected) {
                                        context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]已经连接到房间，无法重复连接"), false);
                                        LOGGER.info("[DyDanmaku]已经连接到房间，无法重复连接");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    String live_id = StringArgumentType.getString(context, "live_id");
                                    Map<String, String> params = myRequest.getParams(live_id);
                                    LOGGER.info("[DyDanmaku]直播间参数： " + params);
                                    if (params == null) {
                                        context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]无法获取房间号：" + live_id + " 的参数,请检查网络环境或房间号是否正确"), false);
                                        LOGGER.info("[DyDanmaku]无法获取房间号：" + live_id + " 的参数,请检查网络环境或房间号是否正确");
                                    } else{
                                        if (websocket.isConnected) {
                                            context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]已经连接到房间号：" + live_id + "，无法重复连接"), false);
                                            LOGGER.info("[DyDanmaku]已经连接到房间号：" + live_id + "，无法重复连接");
                                        }
                                        else {
                                            try {
                                                websocket.init(params.get("roomId"), params.get("user_unique_id"), params.get("ttwid"), context.getSource());
                                                websocket.run();
                                            } catch (Exception e) {
                                                LOGGER.info("[DyDanmaku]无法连接房间：" + live_id, e);
                                                context.getSource().getPlayer().sendMessage(Text.literal("[DyDanmaku]无法连接房间：" + live_id));
                                                throw new RuntimeException(e);
                                            }
                                            context.getSource().getPlayer().sendMessage(Text.literal("[DyDanmaku]已经连接到房间号：" + live_id));
                                            LOGGER.info("[DyDanmaku]已经连接到房间号：" + live_id);
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                    )
                    .then(CommandManager.literal("disconnect")
                            .executes(context -> {
                                if (!websocket.isConnected) {
                                    context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]尚未连接到房间，无法断开连接"), false);
                                    LOGGER.info("[DyDanmaku]尚未连接到房间，无法断开连接");
                                    return Command.SINGLE_SUCCESS;
                                }else{
                                    websocket.close();
                                    context.getSource().getPlayer().sendMessage(Text.literal("[DyDanmaku]已经断开直播间连接"));
                                    LOGGER.info("[DyDanmaku]已经断开直播间连接");
                                    return Command.SINGLE_SUCCESS;
                                }
                            })
                    )
            );
        });
    }

    public static boolean isRunInJar() {
        String runType = String.valueOf(WebSocketClientNetty.class.getResource("WebSocketClientNetty.class"));
        return runType != null && runType.startsWith("jar:");
    }

    public static void FileInit() {

        if (isRunInJar()) {
            LOGGER.info("[DyDanmaku]DyDanmaku is Running in JAR");
            File ConfigDir = new File(ConfigDirPath);
            if  (!ConfigDir.exists()  && !ConfigDir.isDirectory()) {
                LOGGER.info("[DyDanmaku]/config/DyDanmaku不存在,创建目录");
                ConfigDir.mkdir();
            } else {
                LOGGER.info("[DyDanmaku]/config/DyDanmaku目录存在");
            }
            String SignFilePath = ConfigDirPath + "/Signature.exe";
            File SignFile = new File(SignFilePath);
            if(!SignFile.exists()) {
                LOGGER.info("[DyDanmaku]Signature.exe不存在,创建文件");
                try {
                    getSignFile(SignFilePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.info("[DyDanmaku]Signature.exe文件存在");
            }
        } else {
            LOGGER.info("[DyDanmaku]DyDanmaku is Running in IDE");
        }
    }
}
