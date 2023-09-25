package net.lukasllll.lukas_nutrients.nutrients.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerNutrientProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerNutrients> PLAYER_NUTRIENTS = CapabilityManager.get(new CapabilityToken<PlayerNutrients>() { });

    private PlayerNutrients nutrients = null;
    private final LazyOptional<PlayerNutrients> optional = LazyOptional.of(this::getPlayerNutrients);

    private PlayerNutrients getPlayerNutrients() {
        if(nutrients == null) {
            nutrients = new PlayerNutrients();
        }
        return nutrients;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_NUTRIENTS) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        getPlayerNutrients().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getPlayerNutrients().loadNBTData(nbt);
    }
}
