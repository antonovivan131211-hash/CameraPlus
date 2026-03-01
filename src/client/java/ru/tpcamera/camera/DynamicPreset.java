package ru.tpcamera.camera;

import ru.tpcamera.config.CameraConfig;

public enum DynamicPreset {
	CLASSIC("classic", 0.15D, 0.30D, true, 0.15D, true, 0.35D),
	COMBAT("combat", 0.06D, 0.10D, true, 0.08D, true, 0.12D),
	CINEMATIC("cinematic", 0.55D, 0.75D, true, 0.24D, true, 0.72D);

	public static final String CUSTOM_ID = "custom";

	private final String id;
	private final double smoothness;
	private final double cameraInertia;
	private final boolean enableDynamicFov;
	private final double dynamicFovIntensity;
	private final boolean enableDynamicMotion;
	private final double dynamicMotionIntensity;

	DynamicPreset(
		String id,
		double smoothness,
		double cameraInertia,
		boolean enableDynamicFov,
		double dynamicFovIntensity,
		boolean enableDynamicMotion,
		double dynamicMotionIntensity
	) {
		this.id = id;
		this.smoothness = smoothness;
		this.cameraInertia = cameraInertia;
		this.enableDynamicFov = enableDynamicFov;
		this.dynamicFovIntensity = dynamicFovIntensity;
		this.enableDynamicMotion = enableDynamicMotion;
		this.dynamicMotionIntensity = dynamicMotionIntensity;
	}

	public String id() {
		return this.id;
	}

	public void applyTo(CameraConfig config) {
		config.smoothness = this.smoothness;
		config.cameraInertia = this.cameraInertia;
		config.enableDynamicFov = this.enableDynamicFov;
		config.dynamicFovIntensity = this.dynamicFovIntensity;
		config.enableDynamicMotion = this.enableDynamicMotion;
		config.dynamicMotionIntensity = this.dynamicMotionIntensity;
		config.dynamicPreset = this.id;
	}

	public static DynamicPreset byId(String id) {
		if (id == null) {
			return CLASSIC;
		}

		for (DynamicPreset preset : values()) {
			if (preset.id.equalsIgnoreCase(id)) {
				return preset;
			}
		}

		return CLASSIC;
	}

	public static boolean isBuiltin(String id) {
		if (id == null) {
			return false;
		}

		for (DynamicPreset preset : values()) {
			if (preset.id.equalsIgnoreCase(id)) {
				return true;
			}
		}

		return false;
	}

	public static String nextId(String currentId) {
		DynamicPreset[] presets = values();
		if (currentId == null || currentId.isBlank()) {
			return presets[0].id;
		}

		for (int i = 0; i < presets.length; i++) {
			if (presets[i].id.equalsIgnoreCase(currentId)) {
				return presets[(i + 1) % presets.length].id;
			}
		}

		// If config is in custom state, cycle from classic.
		return presets[0].id;
	}
}
