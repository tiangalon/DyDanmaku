package top.tiangalon.dydanmaku;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DyDanmaku implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DyDanmaku");

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
}
