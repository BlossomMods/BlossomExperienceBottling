package dev.codedsakura.blossom.experience_bottling;

import dev.codedsakura.blossom.lib.config.ConfigManager;
import dev.codedsakura.blossom.lib.utils.CustomLogger;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.core.Logger;

public class BlossomExperienceBottling implements ModInitializer {
    static BlossomExperienceBottlingConfig CONFIG = ConfigManager.register(BlossomExperienceBottlingConfig.class, "BlossomExperienceBottling.json", newConfig -> CONFIG = newConfig);
    public static final Logger LOGGER = CustomLogger.createLogger("BlossomExperienceBottling");

    @Override
    public void onInitialize() {
    }
}
