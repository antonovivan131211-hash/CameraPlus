package ru.tpcamera.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.tpcamera.ThirdPersonCameraClient;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void tpcamera$hideVanillaCrosshair(DrawContext context, CallbackInfo ci) {
		if (ThirdPersonCameraClient.shouldUseThirdPersonCrosshair(this.client)) {
			ci.cancel();
		}
	}
}
