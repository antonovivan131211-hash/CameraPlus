package ru.tpcamera.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.tpcamera.accessor.CameraAccessor;
import ru.tpcamera.config.CameraConfig;
import ru.tpcamera.config.CameraConfigManager;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Unique
	private static final double TPCAMERA_SPRINT_SPEED = 0.28D;

	@Unique
	private static final double TPCAMERA_MAX_DYNAMIC_FOV = 12.0D;

	@Unique
	private double tpcamera$smoothedSpeedFactor;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void tpcamera$modifyFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		if (this.client.options.getPerspective().isFirstPerson()) {
			this.tpcamera$smoothedSpeedFactor = 0.0D;
			return;
		}

		CameraConfig config = CameraConfigManager.getConfig();
		if (!config.enabled) {
			this.tpcamera$smoothedSpeedFactor = 0.0D;
			return;
		}

		double originalFov = cir.getReturnValue();
		double modifiedFov = originalFov;

		// Применяем статический множитель FOV
		if (config.fovMultiplier != 1.0D) {
			modifiedFov *= config.fovMultiplier;
		}

		// Применяем динамический FOV на основе скорости движения
		if (config.enableDynamicFov && config.dynamicFovIntensity > 0.0D) {
			// Получаем скорость из CameraMixin через accessor
			double speed = 0.0D;
			if (camera instanceof CameraAccessor accessor) {
				speed = accessor.tpcamera$getEntitySpeed();
			}

			// Скорость хранится в блоках за тик: нормализуем и сглаживаем, чтобы убрать дергания.
			double normalizedSpeed = MathHelper.clamp(speed / TPCAMERA_SPRINT_SPEED, 0.0D, 1.4D);
			double smoothing = 0.14D + config.dynamicFovIntensity * 0.22D;
			this.tpcamera$smoothedSpeedFactor += (normalizedSpeed - this.tpcamera$smoothedSpeedFactor) * smoothing;
			double smoothed = MathHelper.clamp(this.tpcamera$smoothedSpeedFactor, 0.0D, 1.4D);
			double shaped = smoothed * smoothed * (3.0D - 2.0D * Math.min(smoothed, 1.0D));

			double dynamicFovChange = shaped * config.dynamicFovIntensity * TPCAMERA_MAX_DYNAMIC_FOV;
			modifiedFov += dynamicFovChange;
		} else {
			this.tpcamera$smoothedSpeedFactor *= 0.85D;
		}

		cir.setReturnValue(modifiedFov);
	}
}
