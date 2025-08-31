package com.chadate.spellelemental.integration.jei.data;

import com.chadate.spellelemental.SpellElemental;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 剑油数据管理器
 * 负责注册数据加载器到资源重载系统
 */
@EventBusSubscriber(modid = SpellElemental.MODID)
public class SwordOilDataManager {
    
    private static SwordOilConfigLoader configLoader;
    
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        configLoader = new SwordOilConfigLoader();
        event.addListener(configLoader);
    }
    
    /**
     * 获取配置加载器实例
     */
    public static SwordOilConfigLoader getConfigLoader() {
        return configLoader;
    }
}
