package top.tiangalon.dydanmaku;

import com.mojang.brigadier.Command;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import DyDanmaku.*;
import top.tiangalon.dydanmaku.gui.gui;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static DyDanmaku.WebSocketClientNetty.getSignFile;


public class DyDanmaku implements ModInitializer {

    public ServerCommandSource gameSource;

    @Override
    public void onInitialize() {

    }


}
