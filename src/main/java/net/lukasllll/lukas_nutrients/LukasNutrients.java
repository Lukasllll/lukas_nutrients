package net.lukasllll.lukas_nutrients;

import com.mojang.logging.LogUtils;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LukasNutrients.MOD_ID)
public class LukasNutrients
{
    public static final String MOD_ID = "lukas_nutrients";
    public static final Logger LOGGER = LogUtils.getLogger();
    public LukasNutrients()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        //Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        ModMessages.register();
        FoodNutrientProvider.addNutrientProperties();
    }
}
