package ru.tpcamera.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import ru.tpcamera.camera.DynamicPreset;
import ru.tpcamera.config.CameraConfig;
import ru.tpcamera.config.CameraConfigManager;

import java.util.Locale;

public final class CameraConfigScreen extends Screen {
	private static final int PANEL_MARGIN = 18;
	private static final int ROW_HEIGHT = 24;
	private static final int HEADER_HEIGHT = 38;

	private static final int COLOR_SCREEN_TOP = 0x8A0A1220;
	private static final int COLOR_SCREEN_BOTTOM = 0xB3050A12;
	private static final int COLOR_PANEL_TOP = 0xB4192738;
	private static final int COLOR_PANEL_BOTTOM = 0xB0101A28;
	private static final int COLOR_PANEL_BORDER = 0xAA66A9E6;
	private static final int COLOR_PANEL_HEADER_TOP = 0xCC3B83CC;
	private static final int COLOR_PANEL_HEADER_BOTTOM = 0xAA235C9C;
	private static final int COLOR_PANEL_HEADER_LINE = 0xCC8DD4FF;
	private static final int COLOR_SECTION_BG = 0x3A0B1625;
	private static final int COLOR_SECTION_BORDER = 0x705E8DB8;
	private static final int COLOR_SECTION_TITLE = 0xBFE2FF;

	private final Screen parent;
	private final CameraConfig originalConfig;
	private final CameraConfig workingConfig;

	private boolean discardChanges;

	private int panelLeft;
	private int panelRight;
	private int panelTop;
	private int panelBottom;
	private int switchesSectionTop;
	private int switchesSectionBottom;
	private int transformSectionTop;
	private int transformSectionBottom;
	private int dynamicsSectionTop;
	private int dynamicsSectionBottom;

	private ButtonWidget enabledButton;
	private ButtonWidget frontViewButton;
	private ButtonWidget collisionButton;
	private ButtonWidget lockHorizontalButton;
	private ButtonWidget inertiaButton;
	private ButtonWidget dynamicFovButton;
	private ButtonWidget crosshairButton;
	private ButtonWidget dynamicMotionButton;
	private ButtonWidget presetButton;
	private ConfigSliderWidget distanceSlider;
	private ConfigSliderWidget offsetXSlider;
	private ConfigSliderWidget offsetYSlider;
	private ConfigSliderWidget offsetZSlider;
	private ConfigSliderWidget yawSlider;
	private ConfigSliderWidget pitchSlider;
	private ConfigSliderWidget smoothnessSlider;
	private ConfigSliderWidget fovSlider;
	private ConfigSliderWidget inertiaSlider;
	private ConfigSliderWidget dynamicFovSlider;
	private ConfigSliderWidget dynamicMotionSlider;

	public CameraConfigScreen(Screen parent) {
		super(Text.translatable("screen.tpcamera.title"));
		this.parent = parent;
		this.originalConfig = CameraConfigManager.getConfig().copy();
		this.workingConfig = this.originalConfig.copy();
		this.discardChanges = false;
	}

	@Override
	protected void init() {
		int panelWidth = Math.min(420, this.width - PANEL_MARGIN * 2);
		this.panelLeft = (this.width - panelWidth) / 2;
		this.panelRight = this.panelLeft + panelWidth;
		this.panelTop = Math.max(16, (this.height - 520) / 2);
		int y = this.panelTop + HEADER_HEIGHT + 8;

		int toggleGap = 6;
		int toggleWidth = (panelWidth - toggleGap) / 2;
		int switchesStart = y;

		this.enabledButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.enabled", this.workingConfig.enabled), button -> {
			this.workingConfig.enabled = !this.workingConfig.enabled;
			button.setMessage(toggleText("screen.tpcamera.toggle.enabled", this.workingConfig.enabled));
			applyRealtime();
		}).dimensions(this.panelLeft, y, toggleWidth, 20).build());

		this.workingConfig.applyInFrontView = true;
		this.frontViewButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.front_view", true), button -> {
		}).dimensions(this.panelLeft + toggleWidth + toggleGap, y, toggleWidth, 20).build());
		this.frontViewButton.active = false;
		y += ROW_HEIGHT;

		this.collisionButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.collision", this.workingConfig.enableCollision), button -> {
			this.workingConfig.enableCollision = !this.workingConfig.enableCollision;
			button.setMessage(toggleText("screen.tpcamera.toggle.collision", this.workingConfig.enableCollision));
			applyRealtime();
		}).dimensions(this.panelLeft, y, toggleWidth, 20).build());

		this.lockHorizontalButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.lock_horizontal", this.workingConfig.lockHorizontalDistance), button -> {
			this.workingConfig.lockHorizontalDistance = !this.workingConfig.lockHorizontalDistance;
			button.setMessage(toggleText("screen.tpcamera.toggle.lock_horizontal", this.workingConfig.lockHorizontalDistance));
			applyRealtime();
		}).dimensions(this.panelLeft + toggleWidth + toggleGap, y, toggleWidth, 20).build());
		y += ROW_HEIGHT;

		this.inertiaButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.inertia", this.workingConfig.enableCameraInertia), button -> {
			this.workingConfig.enableCameraInertia = !this.workingConfig.enableCameraInertia;
			markPresetCustom();
			button.setMessage(toggleText("screen.tpcamera.toggle.inertia", this.workingConfig.enableCameraInertia));
			applyRealtime();
		}).dimensions(this.panelLeft, y, toggleWidth, 20).build());

		this.dynamicFovButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.dynamic_fov", this.workingConfig.enableDynamicFov), button -> {
			this.workingConfig.enableDynamicFov = !this.workingConfig.enableDynamicFov;
			markPresetCustom();
			button.setMessage(toggleText("screen.tpcamera.toggle.dynamic_fov", this.workingConfig.enableDynamicFov));
			applyRealtime();
		}).dimensions(this.panelLeft + toggleWidth + toggleGap, y, toggleWidth, 20).build());
		y += ROW_HEIGHT;

		this.crosshairButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.crosshair", this.workingConfig.showThirdPersonCrosshair), button -> {
			this.workingConfig.showThirdPersonCrosshair = !this.workingConfig.showThirdPersonCrosshair;
			button.setMessage(toggleText("screen.tpcamera.toggle.crosshair", this.workingConfig.showThirdPersonCrosshair));
			applyRealtime();
		}).dimensions(this.panelLeft, y, panelWidth, 20).build());
		y += ROW_HEIGHT;

		this.dynamicMotionButton = this.addDrawableChild(ButtonWidget.builder(toggleText("screen.tpcamera.toggle.dynamic_motion", this.workingConfig.enableDynamicMotion), button -> {
			this.workingConfig.enableDynamicMotion = !this.workingConfig.enableDynamicMotion;
			markPresetCustom();
			button.setMessage(toggleText("screen.tpcamera.toggle.dynamic_motion", this.workingConfig.enableDynamicMotion));
			applyRealtime();
		}).dimensions(this.panelLeft, y, panelWidth, 20).build());
		y += ROW_HEIGHT;

		this.presetButton = this.addDrawableChild(ButtonWidget.builder(presetText(this.workingConfig.dynamicPreset), button -> {
			cyclePreset();
		}).dimensions(this.panelLeft, y, panelWidth, 20).build());
		y += ROW_HEIGHT;

		this.switchesSectionTop = switchesStart - 4;
		this.switchesSectionBottom = y - 6;

		int transformStart = y;
		this.distanceSlider = addSlider(y, "screen.tpcamera.slider.distance", CameraConfig.MIN_DISTANCE_OFFSET, CameraConfig.MAX_DISTANCE_OFFSET, 0.05D, this.workingConfig.distanceOffset, value -> this.workingConfig.distanceOffset = value);
		y += ROW_HEIGHT;
		this.offsetXSlider = addSlider(y, "screen.tpcamera.slider.offset_x", CameraConfig.MIN_POSITION_OFFSET, CameraConfig.MAX_POSITION_OFFSET, 0.05D, this.workingConfig.offsetX, value -> this.workingConfig.offsetX = value);
		y += ROW_HEIGHT;
		this.offsetYSlider = addSlider(y, "screen.tpcamera.slider.offset_y", CameraConfig.MIN_POSITION_OFFSET, CameraConfig.MAX_POSITION_OFFSET, 0.05D, this.workingConfig.offsetY, value -> this.workingConfig.offsetY = value);
		y += ROW_HEIGHT;
		this.offsetZSlider = addSlider(y, "screen.tpcamera.slider.offset_z", CameraConfig.MIN_POSITION_OFFSET, CameraConfig.MAX_POSITION_OFFSET, 0.05D, this.workingConfig.offsetZ, value -> this.workingConfig.offsetZ = value);
		y += ROW_HEIGHT;
		this.yawSlider = addSlider(y, "screen.tpcamera.slider.yaw", CameraConfig.MIN_ANGLE_OFFSET, CameraConfig.MAX_ANGLE_OFFSET, 0.50D, this.workingConfig.yawOffset, value -> this.workingConfig.yawOffset = value);
		y += ROW_HEIGHT;
		this.pitchSlider = addSlider(y, "screen.tpcamera.slider.pitch", CameraConfig.MIN_PITCH_OFFSET, CameraConfig.MAX_PITCH_OFFSET, 0.50D, this.workingConfig.pitchOffset, value -> this.workingConfig.pitchOffset = value);
		y += ROW_HEIGHT;

		this.transformSectionTop = transformStart - 4;
		this.transformSectionBottom = y - 6;

		int dynamicsStart = y;
		this.smoothnessSlider = addSlider(y, "screen.tpcamera.slider.smoothness", 0.0D, 1.0D, 0.01D, this.workingConfig.smoothness, value -> {
			markPresetCustom();
			this.workingConfig.smoothness = value;
		});
		y += ROW_HEIGHT;
		this.inertiaSlider = addSlider(y, "screen.tpcamera.slider.inertia", 0.0D, 1.0D, 0.01D, this.workingConfig.cameraInertia, value -> {
			markPresetCustom();
			this.workingConfig.cameraInertia = value;
		});
		y += ROW_HEIGHT;
		this.fovSlider = addSlider(y, "screen.tpcamera.slider.fov", 0.5D, 2.0D, 0.05D, this.workingConfig.fovMultiplier, value -> {
			markPresetCustom();
			this.workingConfig.fovMultiplier = value;
		});
		y += ROW_HEIGHT;
		this.dynamicFovSlider = addSlider(y, "screen.tpcamera.slider.dynamic_fov", 0.0D, 1.0D, 0.01D, this.workingConfig.dynamicFovIntensity, value -> {
			markPresetCustom();
			this.workingConfig.dynamicFovIntensity = value;
		});
		y += ROW_HEIGHT;
		this.dynamicMotionSlider = addSlider(y, "screen.tpcamera.slider.dynamic_motion", 0.0D, 1.0D, 0.01D, this.workingConfig.dynamicMotionIntensity, value -> {
			markPresetCustom();
			this.workingConfig.dynamicMotionIntensity = value;
		});
		y += ROW_HEIGHT;

		this.dynamicsSectionTop = dynamicsStart - 4;
		this.dynamicsSectionBottom = y - 6;
		y += 10;

		int actionWidth = (panelWidth - 12) / 3;
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.tpcamera.reset"), button -> resetToDefaults()).dimensions(this.panelLeft, y, actionWidth, 20).build());
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.tpcamera.cancel"), button -> {
			this.discardChanges = true;
			this.close();
		}).dimensions(this.panelLeft + actionWidth + 6, y, actionWidth, 20).build());
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.tpcamera.done"), button -> {
			this.discardChanges = false;
			this.close();
		}).dimensions(this.panelLeft + (actionWidth + 6) * 2, y, actionWidth, 20).build());

		this.panelBottom = y + 28;
		applyRealtime();
	}

	private ConfigSliderWidget addSlider(int y, String labelKey, double min, double max, double step, double value, java.util.function.Consumer<Double> onChanged) {
		return this.addDrawableChild(new ConfigSliderWidget(
			this.panelLeft,
			y,
			this.panelRight - this.panelLeft,
			20,
			labelKey,
			min,
			max,
			step,
			value,
			newValue -> {
				onChanged.accept(newValue);
				applyRealtime();
			}
		));
	}

	private void resetToDefaults() {
		this.workingConfig.copyFrom(CameraConfig.defaults());
		this.workingConfig.dynamicPreset = "classic";
		syncWidgetsFromConfig();
		applyRealtime();
	}

	private void cyclePreset() {
		String nextId = DynamicPreset.nextId(this.workingConfig.dynamicPreset);
		applyPreset(nextId);
	}

	private void applyPreset(String presetId) {
		DynamicPreset preset = DynamicPreset.byId(presetId);
		preset.applyTo(this.workingConfig);
		syncWidgetsFromConfig();
		applyRealtime();
	}

	private void markPresetCustom() {
		this.workingConfig.dynamicPreset = DynamicPreset.CUSTOM_ID;
		if (this.presetButton != null) {
			this.presetButton.setMessage(presetText(this.workingConfig.dynamicPreset));
		}
	}

	private void syncWidgetsFromConfig() {
		this.enabledButton.setMessage(toggleText("screen.tpcamera.toggle.enabled", this.workingConfig.enabled));
		this.workingConfig.applyInFrontView = true;
		this.frontViewButton.setMessage(toggleText("screen.tpcamera.toggle.front_view", true));
		this.collisionButton.setMessage(toggleText("screen.tpcamera.toggle.collision", this.workingConfig.enableCollision));
		this.lockHorizontalButton.setMessage(toggleText("screen.tpcamera.toggle.lock_horizontal", this.workingConfig.lockHorizontalDistance));
		this.inertiaButton.setMessage(toggleText("screen.tpcamera.toggle.inertia", this.workingConfig.enableCameraInertia));
		this.dynamicFovButton.setMessage(toggleText("screen.tpcamera.toggle.dynamic_fov", this.workingConfig.enableDynamicFov));
		this.crosshairButton.setMessage(toggleText("screen.tpcamera.toggle.crosshair", this.workingConfig.showThirdPersonCrosshair));
		this.dynamicMotionButton.setMessage(toggleText("screen.tpcamera.toggle.dynamic_motion", this.workingConfig.enableDynamicMotion));
		this.presetButton.setMessage(presetText(this.workingConfig.dynamicPreset));
		this.distanceSlider.setActualValueSilently(this.workingConfig.distanceOffset);
		this.smoothnessSlider.setActualValueSilently(this.workingConfig.smoothness);
		this.inertiaSlider.setActualValueSilently(this.workingConfig.cameraInertia);
		this.fovSlider.setActualValueSilently(this.workingConfig.fovMultiplier);
		this.dynamicFovSlider.setActualValueSilently(this.workingConfig.dynamicFovIntensity);
		this.dynamicMotionSlider.setActualValueSilently(this.workingConfig.dynamicMotionIntensity);
		this.offsetXSlider.setActualValueSilently(this.workingConfig.offsetX);
		this.offsetYSlider.setActualValueSilently(this.workingConfig.offsetY);
		this.offsetZSlider.setActualValueSilently(this.workingConfig.offsetZ);
		this.yawSlider.setActualValueSilently(this.workingConfig.yawOffset);
		this.pitchSlider.setActualValueSilently(this.workingConfig.pitchOffset);
	}

	private void applyRealtime() {
		CameraConfigManager.replaceRuntime(this.workingConfig.copy());
	}

	private static Text toggleText(String labelKey, boolean enabled) {
		return Text.translatable(
			"screen.tpcamera.toggle",
			Text.translatable(labelKey),
			Text.translatable(enabled ? "screen.tpcamera.on" : "screen.tpcamera.off")
		);
	}

	private static Text presetText(String presetId) {
		return Text.translatable(
			"screen.tpcamera.preset.label",
			Text.translatable("screen.tpcamera.preset"),
			Text.translatable("screen.tpcamera.preset." + normalizePresetId(presetId))
		);
	}

	private static String normalizePresetId(String presetId) {
		if (presetId == null || presetId.isBlank()) {
			return "classic";
		}

		String lowered = presetId.toLowerCase(Locale.ROOT);
		if ("classic".equals(lowered) || "combat".equals(lowered) || "cinematic".equals(lowered) || "custom".equals(lowered)) {
			return lowered;
		}

		return "classic";
	}

	@Override
	public void close() {
		if (this.client == null) {
			return;
		}

		if (this.discardChanges) {
			CameraConfigManager.replaceRuntime(this.originalConfig.copy());
		} else {
			CameraConfigManager.replaceAndSave(this.workingConfig.copy());
		}

		this.client.setScreen(this.parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackgroundDecor(context);
		drawMainPanel(context, this.panelLeft - 10, this.panelTop - 12, this.panelRight + 10, this.panelBottom + 8);
		drawSection(context, this.switchesSectionTop, this.switchesSectionBottom);
		drawSection(context, this.transformSectionTop, this.transformSectionBottom);
		drawSection(context, this.dynamicsSectionTop, this.dynamicsSectionBottom);

		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.panelTop - 4, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.tpcamera.subtitle"), this.width / 2, this.panelTop + 11, 0xCBE9FF);
		drawSectionTitle(context, "screen.tpcamera.section.switches", this.switchesSectionTop - 10);
		drawSectionTitle(context, "screen.tpcamera.section.transform", this.transformSectionTop - 10);
		drawSectionTitle(context, "screen.tpcamera.section.dynamics", this.dynamicsSectionTop - 10);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.tpcamera.hint"), this.width / 2, this.panelBottom + 14, 0xB6C7D8);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.tpcamera.hotkeys"), this.width / 2, this.panelBottom + 26, 0x90A7C0);
	}

	private void renderBackgroundDecor(DrawContext context) {
		context.fillGradient(0, 0, this.width, this.height, COLOR_SCREEN_TOP, COLOR_SCREEN_BOTTOM);

		int shimmerCenter = (int) (System.currentTimeMillis() / 8L % Math.max(1, this.width + 280)) - 140;
		int shimmerLeft = Math.max(0, shimmerCenter - 160);
		int shimmerRight = Math.min(this.width, shimmerCenter + 160);
		int shimmerTop = Math.max(0, this.panelTop - 64);
		int shimmerBottom = Math.min(this.height, this.panelTop + 92);
		context.fillGradient(shimmerLeft, shimmerTop, shimmerRight, shimmerBottom, 0x205FC6FF, 0x00000000);
	}

	private void drawMainPanel(DrawContext context, int left, int top, int right, int bottom) {
		context.fillGradient(left, top, right, bottom, COLOR_PANEL_TOP, COLOR_PANEL_BOTTOM);
		drawOutline(context, left, top, right, bottom, COLOR_PANEL_BORDER);
		context.fillGradient(left, top, right, top + 18, COLOR_PANEL_HEADER_TOP, COLOR_PANEL_HEADER_BOTTOM);
		context.fill(left, top + 18, right, top + 19, COLOR_PANEL_HEADER_LINE);
	}

	private void drawSection(DrawContext context, int top, int bottom) {
		if (bottom <= top) {
			return;
		}

		int left = this.panelLeft - 4;
		int right = this.panelRight + 4;
		context.fill(left, top, right, bottom, COLOR_SECTION_BG);
		drawOutline(context, left, top, right, bottom, COLOR_SECTION_BORDER);
	}

	private void drawSectionTitle(DrawContext context, String key, int y) {
		context.drawTextWithShadow(this.textRenderer, Text.translatable(key), this.panelLeft + 4, y, COLOR_SECTION_TITLE);
	}

	private static void drawOutline(DrawContext context, int left, int top, int right, int bottom, int color) {
		context.fill(left, top, right, top + 1, color);
		context.fill(left, bottom - 1, right, bottom, color);
		context.fill(left, top, left + 1, bottom, color);
		context.fill(right - 1, top, right, bottom, color);
	}
}
