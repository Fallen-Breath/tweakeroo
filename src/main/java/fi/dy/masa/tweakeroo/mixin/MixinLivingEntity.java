package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    public MixinLivingEntity(EntityType<?> type, World worldIn)
    {
        super(type, worldIn);
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", ordinal = 1,
            target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean fixElytraLanding(World world)
    {
        return world.isClient && (Configs.Fixes.ELYTRA_FIX.getBooleanValue() == false || ((Object) this instanceof ClientPlayerEntity) == false);
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;"),
            cancellable = true)
    private void removeOwnPotionEffects(CallbackInfo ci)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (FeatureToggle.TWEAK_REMOVE_OWN_POTION_EFFECTS.getBooleanValue() &&
            ((Object) this) == mc.player && mc.options.perspective == 0)
        {
            ci.cancel();
        }
    }
}
