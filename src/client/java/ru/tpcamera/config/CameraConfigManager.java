package ru.tpcamera.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import ru.tpcamera.ThirdPersonCameraClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CameraConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(ThirdPersonCameraClient.MOD_ID + ".json");
	private static final Logger LOGGER = ThirdPersonCameraClient.LOGGER;
	private static CameraConfig config = CameraConfig.defaults();

	private CameraConfigManager() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			config = CameraConfig.defaults();
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			CameraConfig loaded = GSON.fromJson(reader, CameraConfig.class);
			if (loaded == null) {
				loaded = CameraConfig.defaults();
			}

			loaded.clamp();
			config = loaded;
		} catch (IOException | JsonParseException e) {
			LOGGER.error("Could not read camera config {}, using defaults", CONFIG_PATH, e);
			config = CameraConfig.defaults();
			save();
		}
	}

	public static CameraConfig getConfig() {
		return config;
	}

	public static void replaceRuntime(CameraConfig newConfig) {
		newConfig.clamp();
		config = newConfig;
	}

	public static void replaceAndSave(CameraConfig newConfig) {
		replaceRuntime(newConfig);
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			LOGGER.error("Could not save camera config {}", CONFIG_PATH, e);
		}
	}
}
