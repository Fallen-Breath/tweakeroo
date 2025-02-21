package fi.dy.masa.tweakeroo.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity
{
    @Shadow public Input input;
    @Shadow protected int field_3935; // sprintToggleTimer

    @Shadow
    protected abstract boolean isCamera();

    public MixinClientPlayerEntity(ClientWorld worldIn, GameProfile playerProfile)
    {
        super(worldIn, playerProfile);
    }

    @Redirect(method = "updateNausea()V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screen/Screen;isPauseScreen()Z"))
    private boolean onDoesGuiPauseGame(Screen gui)
    {
        // Spoof the return value to prevent entering the if block
        if (Configs.Disable.DISABLE_PORTAL_GUI_CLOSING.getBooleanValue())
        {
            return true;
        }

        return gui.isPauseScreen();
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void fixElytraDeployment(CallbackInfo ci)
    {
        if (Configs.Fixes.ELYTRA_FIX.getBooleanValue() && this.isSubmergedInWater() == false)
        {
            this.setFlag(7, true);
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "FIELD",
                target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z", ordinal = 1))
    private void overrideSprint(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_PERMANENT_SPRINT.getBooleanValue() &&
            ! this.isSprinting() && ! this.isUsingItem() && this.input.movementForward >= 0.8F &&
            (this.getHungerManager().getFoodLevel() > 6.0F || this.abilities.allowFlying) &&
            ! this.hasStatusEffect(StatusEffects.BLINDNESS))
        {
            this.setSprinting(true);
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "FIELD",
                target = "Lnet/minecraft/client/network/ClientPlayerEntity;horizontalCollision:Z"))
    private boolean overrideCollidedHorizontally(ClientPlayerEntity player)
    {
        if (Configs.Disable.DISABLE_WALL_UNSPRINT.getBooleanValue())
        {
            return false;
        }

        return player.horizontalCollision;
    }

    @Inject(method = "tickMovement",
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/network/ClientPlayerEntity;getHungerManager()" +
                                               "Lnet/minecraft/entity/player/HungerManager;")),
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/network/ClientPlayerEntity;field_3935:I")) // spintToggleTimer
    private void disableDoubleTapSprint(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_DOUBLE_TAP_SPRINT.getBooleanValue())
        {
            this.field_3935 = 0;
        }
    }

    @Redirect(method = "tickMovement", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/network/ClientPlayerEntity;isCamera()Z"))
    private boolean preventVerticalMotion(ClientPlayerEntity player)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && FeatureToggle.TWEAK_FREE_CAMERA_MOTION.getBooleanValue())
        {
            return false;
        }

        return this.isCamera();
    }

    @Redirect(method = "tickMovement", require = 0, at = @At(
                value = "FIELD", ordinal = 1,
                target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z"))
    private boolean preventFlyStateToggle(PlayerAbilities abilities)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && FeatureToggle.TWEAK_FREE_CAMERA_MOTION.getBooleanValue())
        {
            return false;
        }

        return abilities.allowFlying;
    }

    @Inject(method = "tickNewAi", at = @At("RETURN"))
    private void preventJumpingInCameraMode(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && FeatureToggle.TWEAK_FREE_CAMERA_MOTION.getBooleanValue())
        {
            this.jumping = false;
        }
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void preventSneakingInCameraMode(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && FeatureToggle.TWEAK_FREE_CAMERA_MOTION.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean isSpectator()
    {
        return super.isSpectator() || MiscUtils.getFreeCameraSpectator();
    }
}
