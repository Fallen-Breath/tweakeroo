package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.ILightingProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightingProvider.class)
public class MixinLightingProvider implements ILightingProvider
{
	private World world$tweakeroo;

	public void setWorld$tweakeroo(World world$tweakeroo)
	{
		this.world$tweakeroo = world$tweakeroo;
	}

	@Inject(method = {"checkBlock", "addLightSource"}, at = @At("HEAD"), cancellable = true)
	private void noLightUpdate(CallbackInfo ci)
	{
		if (this.world$tweakeroo.isClient() && Configs.Disable.DISABLE_LIGHT_UPDATES_ALL.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "doLightUpdates", at = @At("HEAD"), cancellable = true)
	private void noLightUpdate(CallbackInfoReturnable<Integer> cir)
	{
		if (this.world$tweakeroo.isClient() && Configs.Disable.DISABLE_LIGHT_UPDATES_ALL.getBooleanValue())
		{
			cir.cancel();
		}
	}
}
