package ru.tpcamera.screen;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.function.Consumer;

public final class ConfigSliderWidget extends SliderWidget {
	private final String labelKey;
	private final double min;
	private final double max;
	private final double step;
	private final Consumer<Double> onChanged;

	public ConfigSliderWidget(int x, int y, int width, int height, String labelKey, double min, double max, double step, double currentValue, Consumer<Double> onChanged) {
		super(x, y, width, height, Text.empty(), 0.0D);
		this.labelKey = labelKey;
		this.min = min;
		this.max = max;
		this.step = step;
		this.onChanged = onChanged;
		this.value = toSliderValue(currentValue);
		this.updateMessage();
	}

	public void setActualValueSilently(double actualValue) {
		this.value = toSliderValue(actualValue);
		this.updateMessage();
	}

	private double getActualValue() {
		double denormalized = MathHelper.lerp(this.value, this.min, this.max);
		return snap(denormalized);
	}

	private double toSliderValue(double actualValue) {
		double clamped = MathHelper.clamp(snap(actualValue), this.min, this.max);
		if (this.max <= this.min) {
			return 0.0D;
		}

		return (clamped - this.min) / (this.max - this.min);
	}

	private double snap(double value) {
		if (this.step <= 0.0D) {
			return value;
		}

		return Math.round(value / this.step) * this.step;
	}

	private static String formatValue(double value) {
		return String.format(Locale.ROOT, "%.2f", value);
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Text.translatable(
			"screen.tpcamera.slider",
			Text.translatable(this.labelKey),
			formatValue(this.getActualValue())
		));
	}

	@Override
	protected void applyValue() {
		double actualValue = this.getActualValue();
		this.value = toSliderValue(actualValue);
		this.onChanged.accept(actualValue);
		this.updateMessage();
	}
}