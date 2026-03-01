package ru.tpcamera.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.tpcamera.ThirdPersonCameraClient;
import ru.tpcamera.camera.DynamicPreset;
import ru.tpcamera.config.CameraConfig;
import ru.tpcamera.config.CameraConfigManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class CameraCommand {
	private CameraCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(literal("tpcam")
				.executes(CameraCommand::show)
				.then(literal("show").executes(CameraCommand::show))
				.then(literal("gui").executes(CameraCommand::openGui))
				.then(literal("toggle").executes(CameraCommand::toggle))
				.then(literal("save").executes(CameraCommand::save))
				.then(literal("reset").executes(CameraCommand::reset))
				.then(literal("preset")
					.executes(CameraCommand::nextPreset)
					.then(literal("next").executes(CameraCommand::nextPreset))
					.then(literal("classic").executes(ctx -> applyPreset(ctx, "classic")))
					.then(literal("combat").executes(ctx -> applyPreset(ctx, "combat")))
					.then(literal("cinematic").executes(ctx -> applyPreset(ctx, "cinematic")))
				)
				.then(literal("set")
					.then(literal("enabled")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "enabled"))))
					.then(literal("front_view")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "front_view"))))
					.then(literal("collision")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "collision"))))
					.then(literal("lock_horizontal")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "lock_horizontal"))))
					.then(literal("inertia")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "inertia"))))
					.then(literal("dynamic_fov")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "dynamic_fov"))))
					.then(literal("crosshair")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "crosshair"))))
					.then(literal("dynamic_motion")
						.then(argument("value", BoolArgumentType.bool())
							.executes(ctx -> setBoolean(ctx, "dynamic_motion"))))
					.then(literal("distance")
						.then(argument("value", DoubleArgumentType.doubleArg(CameraConfig.MIN_DISTANCE_OFFSET, CameraConfig.MAX_DISTANCE_OFFSET))
							.executes(ctx -> setDouble(ctx, "distance"))))
					.then(literal("smoothness")
						.then(argument("value", DoubleArgumentType.doubleArg(0.0D, 1.0D))
							.executes(ctx -> setDouble(ctx, "smoothness"))))
					.then(literal("camera_inertia")
						.then(argument("value", DoubleArgumentType.doubleArg(0.0D, 1.0D))
							.executes(ctx -> setDouble(ctx, "camera_inertia"))))
					.then(literal("fov")
						.then(argument("value", DoubleArgumentType.doubleArg(0.5D, 2.0D))
							.executes(ctx -> setDouble(ctx, "fov"))))
					.then(literal("dynamic_fov_intensity")
						.then(argument("value", DoubleArgumentType.doubleArg(0.0D, 1.0D))
							.executes(ctx -> setDouble(ctx, "dynamic_fov_intensity"))))
					.then(literal("dynamic_motion_intensity")
						.then(argument("value", DoubleArgumentType.doubleArg(0.0D, 1.0D))
							.executes(ctx -> setDouble(ctx, "dynamic_motion_intensity"))))
					.then(literal("offset_x")
						.then(argument("value", DoubleArgumentType.doubleArg(CameraConfig.MIN_POSITION_OFFSET, CameraConfig.MAX_POSITION_OFFSET))
							.executes(ctx -> setDouble(ctx, "offset_x"))))
					.then(literal("offset_y")
						.then(argument("value", DoubleArgumentType.doubleArg(CameraConfig.MIN_POSITION_OFFSET, CameraConfig.MAX_POSITION_OFFSET))
							.executes(ctx -> setDouble(ctx, "offset_y"))))
					.then(literal("offset_z")
						.then(argument("value", DoubleArgumentType.doubleArg(CameraConfig.MIN_POSITION_OFFSET, CameraConfig.MAX_POSITION_OFFSET))
							.executes(ctx -> setDouble(ctx, "offset_z"))))
					.then(literal("yaw")
						.then(argument("value", DoubleArgumentType.doubleArg(CameraConfig.MIN_ANGLE_OFFSET, CameraConfig.MAX_ANGLE_OFFSET))
							.executes(ctx -> setDouble(ctx, "yaw"))))
					.then(literal("pitch")
						.then(argument("value", DoubleArgumentType.doubleArg(CameraConfig.MIN_PITCH_OFFSET, CameraConfig.MAX_PITCH_OFFSET))
							.executes(ctx -> setDouble(ctx, "pitch"))))
				)
			)
		);
	}

	private static int openGui(CommandContext<FabricClientCommandSource> context) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.execute(() -> ThirdPersonCameraClient.openConfigScreen(client));
		feedback(context, Text.literal("Открыл окно настроек камеры."));
		return 1;
	}

	private static int show(CommandContext<FabricClientCommandSource> context) {
		CameraConfig config = CameraConfigManager.getConfig();
		feedback(context, Text.literal(String.format(
			java.util.Locale.ROOT,
			"включено=%s, пресет=%s, передний_вид=%s, столкновения=%s, фиксация_горизонтали=%s, инерция=%s, динамический_fov=%s, f5_прицел=%s, динамика_движения=%s, дистанция=%.2f, плавность=%.2f, инерция_камеры=%.2f, fov=%.2f, динамический_fov_интенсивность=%.2f, динамика_интенсивность=%.2f, x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f",
			config.enabled,
			config.dynamicPreset,
			config.applyInFrontView,
			config.enableCollision,
			config.lockHorizontalDistance,
			config.enableCameraInertia,
			config.enableDynamicFov,
			config.showThirdPersonCrosshair,
			config.enableDynamicMotion,
			config.distanceOffset,
			config.smoothness,
			config.cameraInertia,
			config.fovMultiplier,
			config.dynamicFovIntensity,
			config.dynamicMotionIntensity,
			config.offsetX,
			config.offsetY,
			config.offsetZ,
			config.yawOffset,
			config.pitchOffset
		)));
		return 1;
	}

	private static int toggle(CommandContext<FabricClientCommandSource> context) {
		CameraConfig config = CameraConfigManager.getConfig().copy();
		config.enabled = !config.enabled;
		CameraConfigManager.replaceAndSave(config);
		feedback(context, Text.literal("Мод камеры " + (config.enabled ? "включен." : "выключен.")));
		return 1;
	}

	private static int save(CommandContext<FabricClientCommandSource> context) {
		CameraConfigManager.save();
		feedback(context, Text.literal("Конфиг сохранен."));
		return 1;
	}

	private static int reset(CommandContext<FabricClientCommandSource> context) {
		CameraConfigManager.replaceAndSave(CameraConfig.defaults());
		feedback(context, Text.literal("Настройки сброшены."));
		return 1;
	}

	private static int nextPreset(CommandContext<FabricClientCommandSource> context) {
		CameraConfig config = CameraConfigManager.getConfig().copy();
		String nextId = DynamicPreset.nextId(config.dynamicPreset);
		return applyPreset(context, nextId);
	}

	private static int applyPreset(CommandContext<FabricClientCommandSource> context, String presetId) {
		DynamicPreset preset = DynamicPreset.byId(presetId);
		CameraConfig config = CameraConfigManager.getConfig().copy();
		preset.applyTo(config);
		CameraConfigManager.replaceAndSave(config);
		feedback(context, Text.literal("Применен пресет динамики: " + preset.id()));
		return 1;
	}

	private static int setBoolean(CommandContext<FabricClientCommandSource> context, String key) {
		boolean value = BoolArgumentType.getBool(context, "value");
		CameraConfig config = CameraConfigManager.getConfig().copy();

		if ("enabled".equals(key)) {
			config.enabled = value;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Параметр enabled установлен в " + value + "."));
			return 1;
		}

		if ("front_view".equals(key)) {
			config.applyInFrontView = true;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Передний вид всегда использует настройки камеры."));
			return 1;
		}

		if ("collision".equals(key)) {
			config.enableCollision = value;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Столкновения камеры " + (value ? "включены." : "выключены.")));
			return 1;
		}

		if ("lock_horizontal".equals(key)) {
			config.lockHorizontalDistance = value;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Фиксация горизонтали " + (value ? "включена." : "выключена.")));
			return 1;
		}

		if ("inertia".equals(key)) {
			config.enableCameraInertia = value;
			config.dynamicPreset = DynamicPreset.CUSTOM_ID;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Инерция камеры " + (value ? "включена." : "выключена.")));
			return 1;
		}

		if ("dynamic_fov".equals(key)) {
			config.enableDynamicFov = value;
			config.dynamicPreset = DynamicPreset.CUSTOM_ID;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Динамический FOV " + (value ? "включен." : "выключен.")));
			return 1;
		}

		if ("crosshair".equals(key)) {
			config.showThirdPersonCrosshair = value;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("F5-прицел " + (value ? "включен." : "выключен.")));
			return 1;
		}

		if ("dynamic_motion".equals(key)) {
			config.enableDynamicMotion = value;
			config.dynamicPreset = DynamicPreset.CUSTOM_ID;
			CameraConfigManager.replaceAndSave(config);
			feedback(context, Text.literal("Динамика движения камеры " + (value ? "включена." : "выключена.")));
			return 1;
		}

		return 0;
	}

	private static int setDouble(CommandContext<FabricClientCommandSource> context, String key) {
		double value = DoubleArgumentType.getDouble(context, "value");
		CameraConfig config = CameraConfigManager.getConfig().copy();

		switch (key) {
			case "distance" -> config.distanceOffset = value;
			case "smoothness" -> config.smoothness = value;
			case "camera_inertia" -> config.cameraInertia = value;
			case "fov" -> config.fovMultiplier = value;
			case "dynamic_fov_intensity" -> config.dynamicFovIntensity = value;
			case "dynamic_motion_intensity" -> config.dynamicMotionIntensity = value;
			case "offset_x" -> config.offsetX = value;
			case "offset_y" -> config.offsetY = value;
			case "offset_z" -> config.offsetZ = value;
			case "yaw" -> config.yawOffset = value;
			case "pitch" -> config.pitchOffset = value;
			default -> {
				return 0;
			}
		}

		if ("smoothness".equals(key) ||
			"camera_inertia".equals(key) ||
			"fov".equals(key) ||
			"dynamic_fov_intensity".equals(key) ||
			"dynamic_motion_intensity".equals(key)) {
			config.dynamicPreset = DynamicPreset.CUSTOM_ID;
		}

		CameraConfigManager.replaceAndSave(config);
		feedback(context, Text.literal("Параметр " + key + " установлен в " + String.format(java.util.Locale.ROOT, "%.2f", value) + "."));
		return 1;
	}

	private static void feedback(CommandContext<FabricClientCommandSource> context, Text message) {
		context.getSource().sendFeedback(ThirdPersonCameraClient.modPrefix(message));
	}
}
