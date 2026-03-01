package ru.tpcamera.config;

import net.minecraft.util.math.MathHelper;

public final class CameraConfig {
	public static final double MIN_DISTANCE_OFFSET = -3.9D;
	public static final double MAX_DISTANCE_OFFSET = 32.0D;
	public static final double MIN_POSITION_OFFSET = -16.0D;
	public static final double MAX_POSITION_OFFSET = 16.0D;
	public static final double MIN_ANGLE_OFFSET = -180.0D;
	public static final double MAX_ANGLE_OFFSET = 180.0D;
	public static final double MIN_PITCH_OFFSET = -89.9D;
	public static final double MAX_PITCH_OFFSET = 89.9D;

	public boolean enabled = true;
	public boolean applyInFrontView = true;
	public boolean enableCollision = true;
	public boolean lockHorizontalDistance = true;
	public boolean enableCameraInertia = true;
	public boolean enableDynamicFov = true;
	public boolean showThirdPersonCrosshair = true;
	public boolean enableDynamicMotion = true;
	public String dynamicPreset = "classic";
	public double distanceOffset = 0.0D;
	public double offsetX = 0.0D;
	public double offsetY = 0.0D;
	public double offsetZ = 0.0D;
	public double yawOffset = 0.0D;
	public double pitchOffset = 0.0D;
	public double smoothness = 0.15D;
	public double fovMultiplier = 1.0D;
	public double cameraInertia = 0.3D;
	public double dynamicFovIntensity = 0.15D;
	public double dynamicMotionIntensity = 0.35D;

	public static CameraConfig defaults() {
		return new CameraConfig();
	}

	public CameraConfig copy() {
		CameraConfig copy = new CameraConfig();
		copy.copyFrom(this);
		return copy;
	}

	public void copyFrom(CameraConfig other) {
		this.enabled = other.enabled;
		this.applyInFrontView = other.applyInFrontView;
		this.enableCollision = other.enableCollision;
		this.lockHorizontalDistance = other.lockHorizontalDistance;
		this.enableCameraInertia = other.enableCameraInertia;
		this.enableDynamicFov = other.enableDynamicFov;
		this.showThirdPersonCrosshair = other.showThirdPersonCrosshair;
		this.enableDynamicMotion = other.enableDynamicMotion;
		this.dynamicPreset = other.dynamicPreset;
		this.distanceOffset = other.distanceOffset;
		this.offsetX = other.offsetX;
		this.offsetY = other.offsetY;
		this.offsetZ = other.offsetZ;
		this.yawOffset = other.yawOffset;
		this.pitchOffset = other.pitchOffset;
		this.smoothness = other.smoothness;
		this.fovMultiplier = other.fovMultiplier;
		this.cameraInertia = other.cameraInertia;
		this.dynamicFovIntensity = other.dynamicFovIntensity;
		this.dynamicMotionIntensity = other.dynamicMotionIntensity;
	}

	public void clamp() {
		// Front third-person view is always supported and uses the same camera settings.
		this.applyInFrontView = true;
		this.distanceOffset = MathHelper.clamp(this.distanceOffset, MIN_DISTANCE_OFFSET, MAX_DISTANCE_OFFSET);
		this.offsetX = MathHelper.clamp(this.offsetX, MIN_POSITION_OFFSET, MAX_POSITION_OFFSET);
		this.offsetY = MathHelper.clamp(this.offsetY, MIN_POSITION_OFFSET, MAX_POSITION_OFFSET);
		this.offsetZ = MathHelper.clamp(this.offsetZ, MIN_POSITION_OFFSET, MAX_POSITION_OFFSET);
		this.yawOffset = MathHelper.clamp(this.yawOffset, MIN_ANGLE_OFFSET, MAX_ANGLE_OFFSET);
		this.pitchOffset = MathHelper.clamp(this.pitchOffset, MIN_PITCH_OFFSET, MAX_PITCH_OFFSET);
		this.smoothness = MathHelper.clamp(this.smoothness, 0.0D, 1.0D);
		this.fovMultiplier = MathHelper.clamp(this.fovMultiplier, 0.5D, 2.0D);
		this.cameraInertia = MathHelper.clamp(this.cameraInertia, 0.0D, 1.0D);
		this.dynamicFovIntensity = MathHelper.clamp(this.dynamicFovIntensity, 0.0D, 1.0D);
		this.dynamicMotionIntensity = MathHelper.clamp(this.dynamicMotionIntensity, 0.0D, 1.0D);

		if (this.dynamicPreset == null || this.dynamicPreset.isBlank()) {
			this.dynamicPreset = "classic";
		} else {
			String normalized = this.dynamicPreset.toLowerCase(java.util.Locale.ROOT);
			if (!"classic".equals(normalized) &&
				!"combat".equals(normalized) &&
				!"cinematic".equals(normalized) &&
				!"custom".equals(normalized)) {
				normalized = "classic";
			}
			this.dynamicPreset = normalized;
		}
	}
}
