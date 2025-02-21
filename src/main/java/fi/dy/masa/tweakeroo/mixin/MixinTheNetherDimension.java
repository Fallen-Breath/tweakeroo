package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.TheNetherDimension;
import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(TheNetherDimension.class)
public abstract class MixinTheNetherDimension extends Dimension
{
    public MixinTheNetherDimension(World world, DimensionType dimensionType, float light)
    {
        super(world, dimensionType, light);
    }

    @Override
    public boolean isFogThick(int x, int z)
    {
        return ! Configs.Disable.DISABLE_NETHER_FOG.getBooleanValue();
    }
}
