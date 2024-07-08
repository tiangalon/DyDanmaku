package top.tiangalon.dydanmaku;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import DyDanmaku.*;
import java.util.Map;

public class DyDanmaku implements ModInitializer {
    public static final String MOD_ID = "DyDanmaku";
    public static final String UserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final WebSocketClient DyDanmakuListener = new WebSocketClient();

    @Override
    public void onInitialize() {
        LOGGER.info("DyDanmaku initialized");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("dydanmaku").executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal("Called /dydanmaku with no arguments."), false);
                return Command.SINGLE_SUCCESS;
            })
                    .then(CommandManager.literal("connect")
                            .then(CommandManager.argument("live_id", StringArgumentType.string())
                                .executes(context -> {
                                    //WebSocketClient DyDanmakuListener = new WebSocketClient(context.getSource());
                                    if (DyDanmakuListener.isConnected()) {
                                        context.getSource().sendFeedback(() -> Text.literal("已经连接到房间，无法重复连接"), false);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    DyDanmakuListener.init(context.getSource());
                                    String live_id = StringArgumentType.getString(context, "live_id");
                                    Map<String, String> params = myRequest.getParams(live_id);
                                    if (params == null) {
                                        context.getSource().sendFeedback(() -> Text.literal("无法获取房间号：" + live_id + " 的参数,请检查网络环境或房间号是否正确"), false);
                                    } else{
                                        if (DyDanmakuListener.isConnected()) {
                                            context.getSource().sendFeedback(() -> Text.literal("已经连接到房间号：" + live_id + "，无法重复连接"), false);

                                        }
                                        else {
                                            DyDanmakuListener.connect(params.get("roomId"), UserAgent, params.get("ttwid"));
                                            context.getSource().getPlayer().sendMessage(Text.literal("已经连接到房间号：" + live_id));
                                        }
                                    }
                                    //context.getSource().sendFeedback(() -> Text.literal("Called /dydanmaku connect with live_id " + live_id), false);
                        return Command.SINGLE_SUCCESS;
                    })))
                    .then(CommandManager.literal("disconnect")
                            .executes(context -> {
                                if (!DyDanmakuListener.isConnected()) {
                                    context.getSource().sendFeedback(() -> Text.literal("尚未连接到房间，无法断开连接"), false);
                                    return Command.SINGLE_SUCCESS;
                                }else{
                                    DyDanmakuListener.close();
                                    context.getSource().getPlayer().sendMessage(Text.literal("已经断开直播间连接"));
                                    return Command.SINGLE_SUCCESS;
                                }
                            })));
        });
    }
}
