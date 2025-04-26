package top.tiangalon.dydanmaku.client;

import DyDanmaku.DyDanmakuRequest;
import DyDanmaku.WebSocketClientNetty;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.tiangalon.dydanmaku.gui.gui;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static DyDanmaku.WebSocketClientNetty.getSignFile;

public class DyDanmakuClient implements ClientModInitializer {
    public static final String MOD_ID = "DyDanmaku";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final WebSocketClientNetty websocket = new WebSocketClientNetty();
    public static gui gui = new gui(Text.empty(), websocket);
    public static Map<String, String> params = null;
    String live_id = null;
    public static KeyBinding DyDanmakuKey;

    public static String DyDanmakuPath = WebSocketClientNetty.getPath();
    public static String ConfigDirPath = DyDanmakuPath.substring(0, DyDanmakuPath.lastIndexOf("/")) + "/config/DyDanmaku";


    @Override
    public void onInitializeClient() {
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
                                                if (websocket.isConnected()) {
                                                    context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]已经连接到房间，无法重复连接"), false);
                                                    LOGGER.info("[DyDanmaku]已经连接到房间，无法重复连接");
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                                live_id = StringArgumentType.getString(context, "live_id");
                                                params = DyDanmakuRequest.getParams(live_id);
                                                //LOGGER.info("[DyDanmaku]直播间参数： " + params);
                                                if (params == null) {
                                                    context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]无法获取房间号：" + live_id + " 的参数,请检查网络环境或房间号是否正确"), false);
                                                    LOGGER.info("[DyDanmaku]无法获取房间号：" + live_id + " 的参数,请检查网络环境或房间号是否正确");
                                                } else{
                                                    if (websocket.isConnected()) {
                                                        context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]已经连接到房间号：" + live_id + "，无法重复连接"), false);
                                                        LOGGER.info("[DyDanmaku]已经连接到房间号：" + live_id + "，无法重复连接");
                                                    }
                                                    else {
                                                        try {
                                                            websocket.init(params, context.getSource());
                                                            websocket.run();
                                                        } catch (Exception e) {
                                                            LOGGER.info("[DyDanmaku]无法连接房间：" + live_id, e);
                                                            context.getSource().getPlayer().sendMessage(Text.literal("[DyDanmaku]无法连接房间：" + live_id));
                                                            throw new RuntimeException(e);
                                                        }
                                                        websocket.LiveStatusOutput();
                                                        LOGGER.info("[DyDanmaku]已经连接到房间号：" + live_id);
                                                    }
                                                }
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(CommandManager.literal("disconnect")
                                    .executes(context -> {
                                        if (!websocket.isConnected()) {
                                            context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]尚未连接到房间，无法断开连接"), false);
                                            LOGGER.info("[DyDanmaku]尚未连接到房间，无法断开连接");
                                            return Command.SINGLE_SUCCESS;
                                        }else{
                                            try {
                                                websocket.close();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                                context.getSource().getPlayer().sendMessage(Text.literal("[DyDanmaku]断开直播间连接失败"));
                                                LOGGER.info("[DyDanmaku]断开直播间连接失败");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            context.getSource().getPlayer().sendMessage(Text.literal("[DyDanmaku]已经断开直播间连接"));
                                            LOGGER.info("[DyDanmaku]已经断开直播间连接");
                                            return Command.SINGLE_SUCCESS;
                                        }
                                    })
                            )
                            .then(CommandManager.literal("status")
                                    .executes(context -> {
                                        if (!websocket.isConnected()) {
                                            context.getSource().sendFeedback(() -> Text.literal("[DyDanmaku]尚未连接到房间，无法获取状态"), false);
                                            LOGGER.info("[DyDanmaku]尚未连接到房间，无法获取状态");
                                            return Command.SINGLE_SUCCESS;
                                        }
                                        websocket.LiveStatusOutput();
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
            );
        });

        DyDanmakuKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "直播间状态显示",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_F7,
                    "抖音弹幕"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (DyDanmakuKey.wasPressed()) {
                if(client.currentScreen == null)
                    client.setScreen(
                        gui
                    );
                else {
                    client.setScreen(null);
                }
            }
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
                ConfigDir.mkdirs();
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
