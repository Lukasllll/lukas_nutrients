package net.lukasllll.lukas_nutrients;

import com.mojang.logging.LogUtils;
import net.lukasllll.lukas_nutrients.config.Config;
import net.lukasllll.lukas_nutrients.gamerule.ModGameRules;
import net.lukasllll.lukas_nutrients.integration.IntegrationHelper;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LukasNutrients.MOD_ID)
public class LukasNutrients
{
    public static final String MOD_ID = "lukas_nutrients";
    public static final Logger LOGGER = LogUtils.getLogger();
    public LukasNutrients()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        //yeah... idk why I have to register an instance of this class, but if I don't, the gamerules won't be registered.
        MinecraftForge.EVENT_BUS.register(new ModGameRules());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        IntegrationHelper.init();
        ModMessages.register();
        Config.loadCommonConfigs();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        Config.loadClientConfigs();
    }
}
