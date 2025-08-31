package com.chadate.spellelemental;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";
    public static final Logger LOGGER = LogManager.getLogger();

    public SpellElemental() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the setup method for modloading
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("SpellElemental mod setup starting...");
        
        event.enqueueWork(() -> {
            // Code that needs to run on the main thread
            LOGGER.info("SpellElemental mod setup completed!");
        });
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // Do something that can only be done on the client
        LOGGER.info("SpellElemental client setup completed!");
    }
}
