package ru.tpcamera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tpcamera.camera.DynamicPreset;
import ru.tpcamera.command.CameraCommand;
import ru.tpcamera.config.CameraConfig;
import ru.tpcamera.config.CameraConfigManager;
import ru.tpcamera.mixin.client.GameRendererAccessor;
import ru.tpcamera.screen.CameraConfigScreen;

public final class ThirdPersonCameraClient implements ClientModInitializer {
	public static final String MOD_ID = "tpcamera";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding openConfigKey;
	private static KeyBinding cyclePresetKey;

	@Override
	public void onInitializeClient() {
		CameraConfigManager.load();
		CameraCommand.register();

		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.tpcamera.open_config",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_O,
			"category.tpcamera.general"
		));
		cyclePresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.tpcamera.cycle_preset",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_P,
			"category.tpcamera.general"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.wasPressed()) {
				openConfigScreen(client);
			}
			while (cyclePresetKey.wasPressed()) {
				cycleDynamicPreset(client);
			}
		});

		HudRenderCallback.EVENT.register(ThirdPersonCameraClient::renderThirdPersonCrosshair);

		LOGGER.info("TP Камера запущена. Открой настройки: O или /tpcam gui.");
	}

	private static void renderThirdPersonCrosshair(DrawContext context, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!shouldUseThirdPersonCrosshair(client) || client.currentScreen != null) {
			return;
		}

		Vec3d targetPosition = findTargetPosition(client, tickDelta);
		if (targetPosition == null) {
			return;
		}

		Vec2f screenPosition = projectToScreen(client, targetPosition, tickDelta);
		if (screenPosition == null) {
			return;
		}

		drawCrosshair(context, Math.round(screenPosition.x), Math.round(screenPosition.y));
	}

	public static boolean shouldUseThirdPersonCrosshair(MinecraftClient client) {
		if (client.player == null || client.world == null || client.options.hudHidden || client.currentScreen != null) {
			return false;
		}

		Perspective perspective = client.options.getPerspective();
		if (perspective.isFirstPerson()) {
			return false;
		}

		CameraConfig config = CameraConfigManager.getConfig();
		return config.enabled && config.showThirdPersonCrosshair;
	}

	private static Vec3d findTargetPosition(MinecraftClient client, float tickDelta) {
		Entity player = client.player;
		if (player == null || client.world == null) {
			return null;
		}

		double reachDistance = client.interactionManager != null ? client.interactionManager.getReachDistance() : 4.5D;
		reachDistance = Math.max(4.5D, reachDistance);

		Vec3d start = player.getCameraPosVec(tickDelta);
		Vec3d direction = player.getRotationVec(tickDelta);
		Vec3d end = start.add(direction.multiply(reachDistance));

		BlockHitResult blockHit = client.world.raycast(new RaycastContext(
			start,
			end,
			RaycastContext.ShapeType.OUTLINE,
			RaycastContext.FluidHandling.NONE,
			player
		));

		double bestDistanceSq = reachDistance * reachDistance;
		Vec3d bestPosition = end;
		if (blockHit.getType() != HitResult.Type.MISS) {
			bestDistanceSq = start.squaredDistanceTo(blockHit.getPos());
			bestPosition = blockHit.getPos();
		}

		Box searchBox = player.getBoundingBox().stretch(direction.multiply(reachDistance)).expand(1.0D);
		EntityHitResult entityHit = ProjectileUtil.raycast(
			player,
			start,
			end,
			searchBox,
			entity -> !entity.isSpectator() && entity.canHit() && entity != player,
			bestDistanceSq
		);

		if (entityHit != null) {
			return entityHit.getPos();
		}

		return bestPosition;
	}

	private static Vec2f projectToScreen(MinecraftClient client, Vec3d targetPosition, float tickDelta) {
		Camera camera = client.gameRenderer.getCamera();
		Vec3d cameraPos = camera.getPos();
		Vec3d cameraToTarget = targetPosition.subtract(cameraPos);

		Vec3d forward = Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).normalize();
		Vec3d right = forward.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
		if (right.lengthSquared() < 1.0E-6D) {
			right = Vec3d.fromPolar(0.0F, camera.getYaw() + 90.0F).normalize();
		} else {
			right = right.normalize();
		}

		Vec3d up = right.crossProduct(forward).normalize();

		double x = cameraToTarget.dotProduct(right);
		double y = cameraToTarget.dotProduct(up);
		double z = cameraToTarget.dotProduct(forward);
		if (z <= 0.01D) {
			return null;
		}

		double fov = getCurrentFov(client, tickDelta);
		double tanHalfFov = Math.tan(Math.toRadians(fov * 0.5D));
		if (tanHalfFov <= 0.0D) {
			return null;
		}

		int width = client.getWindow().getScaledWidth();
		int height = client.getWindow().getScaledHeight();
		if (width <= 0 || height <= 0) {
			return null;
		}

		double aspect = (double) width / (double) height;
		double ndcX = x / (z * tanHalfFov * aspect);
		double ndcY = y / (z * tanHalfFov);
		if (Double.isNaN(ndcX) || Double.isNaN(ndcY)) {
			return null;
		}

		// Если цель за пределами экрана, не подменяем позицию прицела искусственно.
		if (Math.abs(ndcX) > 1.0D || Math.abs(ndcY) > 1.0D) {
			return null;
		}

		float screenX = (float) ((ndcX * 0.5D + 0.5D) * width);
		float screenY = (float) ((0.5D - ndcY * 0.5D) * height);
		return new Vec2f(screenX, screenY);
	}

	private static void cycleDynamicPreset(MinecraftClient client) {
		if (client == null || client.player == null || client.currentScreen != null) {
			return;
		}

		CameraConfig config = CameraConfigManager.getConfig().copy();
		String nextId = DynamicPreset.nextId(config.dynamicPreset);
		DynamicPreset preset = DynamicPreset.byId(nextId);
		preset.applyTo(config);
		CameraConfigManager.replaceAndSave(config);
		client.player.sendMessage(modPrefix(Text.literal("Пресет динамики: " + preset.id())), true);
	}

	private static double getCurrentFov(MinecraftClient client, float tickDelta) {
		if (client.gameRenderer instanceof GameRendererAccessor accessor) {
			return accessor.tpcamera$invokeGetFov(client.gameRenderer.getCamera(), tickDelta, true);
		}

		return client.options.getFov().getValue();
	}

	private static void drawCrosshair(DrawContext context, int x, int y) {
		int outline = 0xE0000000;
		int core = 0xFFFFFFFF;
		context.fill(x - 5, y, x + 6, y + 1, outline);
		context.fill(x, y - 5, x + 1, y + 6, outline);
		context.fill(x - 4, y, x + 5, y + 1, core);
		context.fill(x, y - 4, x + 1, y + 5, core);
	}

	public static void openConfigScreen(MinecraftClient client) {
		if (client == null) {
			return;
		}

		client.setScreen(new CameraConfigScreen(client.currentScreen));
	}

	public static Text modPrefix(Text message) {
		return Text.literal("[TP Камера] ").append(message);
	}
}
