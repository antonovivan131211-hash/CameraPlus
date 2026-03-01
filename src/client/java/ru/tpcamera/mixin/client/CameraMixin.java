package ru.tpcamera.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.tpcamera.accessor.CameraAccessor;
import ru.tpcamera.camera.DynamicMotion;
import ru.tpcamera.config.CameraConfig;
import ru.tpcamera.config.CameraConfigManager;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraAccessor {
	@Unique
	private static final double TPCAMERA_VANILLA_DISTANCE = 4.0D;

	@Unique
	private static final double TPCAMERA_MIN_DISTANCE = 0.1D;

	@Unique
	private static final double TPCAMERA_COLLISION_MARGIN = 0.08D;

	@Unique
	private static final double TPCAMERA_SPEED_FOR_MAX_INERTIA = 0.35D;

	@Unique
	private static final double TPCAMERA_SPRINT_SPEED = 0.28D;

	@Unique
	private static double tpcamera$lastEntitySpeed = 0.0D;

	@Unique
	private boolean tpcamera$hasSmoothingState;

	@Unique
	private double tpcamera$smoothedDistance;

	@Unique
	private double tpcamera$smoothedOffsetX;

	@Unique
	private double tpcamera$smoothedOffsetY;

	@Unique
	private double tpcamera$smoothedOffsetZ;

	@Unique
	private double tpcamera$motionTime;

	@Unique
	private double tpcamera$smoothedAcceleration;

	@Unique
	private double tpcamera$lastHorizontalSpeed;

	@Shadow
	private float yaw;

	@Shadow
	private float pitch;

	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	protected abstract void setPos(double x, double y, double z);

	@Inject(method = "update", at = @At("HEAD"))
	private void tpcamera$resetStateWhenInactive(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (!thirdPerson || !CameraConfigManager.getConfig().enabled) {
			this.tpcamera$hasSmoothingState = false;
			this.tpcamera$motionTime = 0.0D;
			this.tpcamera$smoothedAcceleration = 0.0D;
			this.tpcamera$lastHorizontalSpeed = 0.0D;

			if (focusedEntity == null) {
				tpcamera$lastEntitySpeed = 0.0D;
				return;
			}

			Vec3d velocity = focusedEntity.getVelocity();
			tpcamera$lastEntitySpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
		}
	}

	@Inject(method = "update", at = @At("RETURN"))
	private void tpcamera$applyCustomOffsets(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (!thirdPerson || focusedEntity == null) {
			return;
		}

		CameraConfig config = CameraConfigManager.getConfig();
		if (!config.enabled) {
			return;
		}

		Vec3d velocity = focusedEntity.getVelocity();
		double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
		tpcamera$lastEntitySpeed = horizontalSpeed;

		double smoothingAlpha = this.tpcamera$computeSmoothingAlpha(config, horizontalSpeed);
		DynamicMotion motion = this.tpcamera$computeDynamicMotion(config, horizontalSpeed);

		float targetYaw = this.yaw + (float) config.yawOffset + motion.yawOffset;
		float targetPitch = (float) MathHelper.clamp(
			this.pitch + config.pitchOffset + motion.pitchOffset,
			CameraConfig.MIN_PITCH_OFFSET,
			CameraConfig.MAX_PITCH_OFFSET
		);

		double targetDistance = Math.max(TPCAMERA_MIN_DISTANCE, TPCAMERA_VANILLA_DISTANCE + config.distanceOffset + motion.distanceOffset);
		double smoothedDistance = this.tpcamera$smoothDistance(targetDistance, config, smoothingAlpha);

		double smoothedOffsetX = this.tpcamera$smoothValue(this.tpcamera$smoothedOffsetX, config.offsetX, smoothingAlpha);
		double smoothedOffsetY = this.tpcamera$smoothValue(this.tpcamera$smoothedOffsetY, config.offsetY, smoothingAlpha);
		double smoothedOffsetZ = this.tpcamera$smoothValue(this.tpcamera$smoothedOffsetZ, config.offsetZ, smoothingAlpha);
		this.tpcamera$smoothedOffsetX = smoothedOffsetX;
		this.tpcamera$smoothedOffsetY = smoothedOffsetY;
		this.tpcamera$smoothedOffsetZ = smoothedOffsetZ;

		Vec3d basePos = focusedEntity.getCameraPosVec(tickDelta);
		Vec3d desiredPos = this.tpcamera$buildDesiredPosition(
			basePos,
			targetYaw,
			targetPitch,
			smoothedDistance,
			smoothedOffsetX + motion.offsetX,
			smoothedOffsetY + motion.offsetY,
			smoothedOffsetZ + motion.offsetZ,
			config.lockHorizontalDistance
		);

		Vec3d finalPos = config.enableCollision
			? this.tpcamera$clipToWorld(focusedEntity, basePos, desiredPos)
			: desiredPos;

		this.setPos(finalPos.x, finalPos.y, finalPos.z);
		this.setRotation(targetYaw, targetPitch);
	}

	@Unique
	private double tpcamera$computeSmoothingAlpha(CameraConfig config, double horizontalSpeed) {
		double alpha = 1.0D - config.smoothness;

		if (config.enableCameraInertia) {
			double speedFactor = MathHelper.clamp(horizontalSpeed / TPCAMERA_SPEED_FOR_MAX_INERTIA, 0.0D, 1.0D);
			double inertiaSlowdown = 1.0D - config.cameraInertia * speedFactor * 0.85D;
			alpha *= MathHelper.clamp(inertiaSlowdown, 0.08D, 1.0D);
		}

		return MathHelper.clamp(alpha, 0.02D, 1.0D);
	}

	@Unique
	private double tpcamera$smoothDistance(double targetDistance, CameraConfig config, double alpha) {
		if (!this.tpcamera$hasSmoothingState) {
			this.tpcamera$smoothedDistance = targetDistance;
			this.tpcamera$smoothedOffsetX = config.offsetX;
			this.tpcamera$smoothedOffsetY = config.offsetY;
			this.tpcamera$smoothedOffsetZ = config.offsetZ;
			this.tpcamera$hasSmoothingState = true;
			return this.tpcamera$smoothedDistance;
		}

		this.tpcamera$smoothedDistance = this.tpcamera$smoothValue(this.tpcamera$smoothedDistance, targetDistance, alpha);
		return this.tpcamera$smoothedDistance;
	}

	@Unique
	private double tpcamera$smoothValue(double current, double target, double alpha) {
		return current + (target - current) * alpha;
	}

	@Unique
	private Vec3d tpcamera$buildDesiredPosition(
		Vec3d basePos,
		float yaw,
		float pitch,
		double distance,
		double offsetX,
		double offsetY,
		double offsetZ,
		boolean lockHorizontalDistance
	) {
		Vec3d forward = Vec3d.fromPolar(pitch, yaw).normalize();
		Vec3d horizontalForward = Vec3d.fromPolar(0.0F, yaw).normalize();
		Vec3d distanceForward = lockHorizontalDistance ? horizontalForward : forward;

		Vec3d right = new Vec3d(0.0D, 1.0D, 0.0D).crossProduct(forward);
		if (right.lengthSquared() < 1.0E-6D) {
			right = Vec3d.fromPolar(0.0F, yaw + 90.0F).normalize();
		} else {
			right = right.normalize();
		}

		Vec3d up = forward.crossProduct(right).normalize();
		Vec3d distanceOffset = distanceForward.multiply(-distance);
		Vec3d localOffsets = right.multiply(offsetX)
			.add(up.multiply(offsetY))
			.add(forward.multiply(offsetZ));
		return basePos.add(distanceOffset).add(localOffsets);
	}

	@Unique
	private Vec3d tpcamera$clipToWorld(Entity focusedEntity, Vec3d start, Vec3d target) {
		Vec3d delta = target.subtract(start);
		double distance = delta.length();
		if (distance < 1.0E-6D) {
			return target;
		}

		Vec3d normalized = delta.multiply(1.0D / distance);
		BlockHitResult hit = focusedEntity.getWorld().raycast(new RaycastContext(
			start,
			target,
			RaycastContext.ShapeType.OUTLINE,
			RaycastContext.FluidHandling.NONE,
			focusedEntity
		));

		if (hit.getType() == HitResult.Type.MISS) {
			return target;
		}

		double hitDistance = start.distanceTo(hit.getPos());
		double safeDistance = Math.max(0.0D, hitDistance - TPCAMERA_COLLISION_MARGIN);
		return start.add(normalized.multiply(safeDistance));
	}

	@Unique
	private DynamicMotion tpcamera$computeDynamicMotion(CameraConfig config, double horizontalSpeed) {
		if (!config.enableDynamicMotion || config.dynamicMotionIntensity <= 0.0D) {
			this.tpcamera$smoothedAcceleration *= 0.85D;
			this.tpcamera$lastHorizontalSpeed = horizontalSpeed;
			return DynamicMotion.ZERO;
		}

		double speedDelta = horizontalSpeed - this.tpcamera$lastHorizontalSpeed;
		this.tpcamera$lastHorizontalSpeed = horizontalSpeed;
		this.tpcamera$smoothedAcceleration += (speedDelta - this.tpcamera$smoothedAcceleration) * 0.18D;

		double speedNorm = MathHelper.clamp(horizontalSpeed / TPCAMERA_SPRINT_SPEED, 0.0D, 1.4D);
		double intensity = config.dynamicMotionIntensity * speedNorm;
		if (intensity <= 1.0E-4D) {
			return DynamicMotion.ZERO;
		}

		this.tpcamera$motionTime += 0.05D + speedNorm * 0.35D;

		double bob = Math.sin(this.tpcamera$motionTime * 2.3D);
		double sway = Math.cos(this.tpcamera$motionTime * 1.5D);
		double lift = Math.abs(Math.sin(this.tpcamera$motionTime * 2.3D));
		double accelImpact = MathHelper.clamp(this.tpcamera$smoothedAcceleration * 8.0D, -1.0D, 1.0D);

		double dynamicX = sway * 0.09D * intensity + accelImpact * 0.03D * config.dynamicMotionIntensity;
		double dynamicY = lift * 0.07D * intensity;
		double dynamicZ = -lift * 0.05D * intensity;
		double dynamicDistance = -lift * 0.12D * intensity;
		float dynamicYaw = (float) (sway * 1.6D * intensity + accelImpact * 0.8D * config.dynamicMotionIntensity);
		float dynamicPitch = (float) (-bob * 0.9D * intensity - accelImpact * 0.6D * config.dynamicMotionIntensity);

		return new DynamicMotion(dynamicX, dynamicY, dynamicZ, dynamicDistance, dynamicYaw, dynamicPitch);
	}

	@Override
	public double tpcamera$getEntitySpeed() {
		return tpcamera$lastEntitySpeed;
	}

}
