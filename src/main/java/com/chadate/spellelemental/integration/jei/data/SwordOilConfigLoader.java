package com.chadate.spellelemental.integration.jei.data;

import com.chadate.spellelemental.SpellElemental;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 剑油配置数据加载器
 * 从 JSON 配置文件中加载剑油物品定义
 */
public class SwordOilConfigLoader extends SimpleJsonResourceReloadListener {
    
    private static final Gson GSON = new Gson();
    private static final List<SwordOilConfig> SWORD_OIL_CONFIGS = new ArrayList<>();
    private static boolean initialized = false;
    
    public SwordOilConfigLoader() {
        super(GSON, "sword_oils");
    }
    
    /**
     * 预初始化方法，标记为已初始化状态
     * 实际配置完全由数据包加载，不进行任何硬编码或推断
     */
    public static void preInitialize() {
        if (initialized) {
            return;
        }
        
        // 清空配置列表，等待数据包加载
        SWORD_OIL_CONFIGS.clear();
        initialized = true;
        
        SpellElemental.LOGGER.info("剑油配置加载器已预初始化，等待数据包加载");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        SWORD_OIL_CONFIGS.clear();
        initialized = false; // 重置初始化状态，允许重新加载
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            
            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    
                    if (jsonObject.has("sword_oils")) {
                        JsonArray swordOilsArray = jsonObject.getAsJsonArray("sword_oils");
                        
                        for (JsonElement oilElement : swordOilsArray) {
                            if (oilElement.isJsonObject()) {
                                JsonObject oilObject = oilElement.getAsJsonObject();
                                SwordOilConfig config = parseSwordOilConfig(oilObject);
                                if (config != null) {
                                    SWORD_OIL_CONFIGS.add(config);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        
        // 数据包加载完成
        SpellElemental.LOGGER.info("精油配置数据包加载完成，共加载 {} 个配置", SWORD_OIL_CONFIGS.size());
    }
    
    /**
     * 解析单个剑油配置
     */
    private SwordOilConfig parseSwordOilConfig(JsonObject jsonObject) {
        try {
            String itemId = jsonObject.get("item").getAsString();
            String element = jsonObject.get("element").getAsString();
            int amount = jsonObject.get("amount").getAsInt();
            String displayName = jsonObject.has("display_name") ? 
                jsonObject.get("display_name").getAsString() : itemId;

            return new SwordOilConfig(itemId, element, amount, displayName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取所有剑油配置
     * 如果配置为空且未初始化，则自动进行预初始化
     */
    public static List<SwordOilConfig> getSwordOilConfigs() {
        // 如果配置为空且未初始化，则进行预初始化
        if (SWORD_OIL_CONFIGS.isEmpty() && !initialized) {
            preInitialize();
        }
        return new ArrayList<>(SWORD_OIL_CONFIGS);
    }
    
    /**
     * 根据物品获取剑油配置
     */
    public static SwordOilConfig getSwordOilConfig(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        return SWORD_OIL_CONFIGS.stream()
            .filter(config -> config.getItemId().equals(itemId.toString()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 检查物品是否为剑油
     */
    public static boolean isSwordOil(Item item) {
        return getSwordOilConfig(item) != null;
    }
    
    /**
     * 清空配置列表（用于客户端同步）
     */
    public static void clearConfigs() {
        SWORD_OIL_CONFIGS.clear();
    }
    
    /**
     * 添加配置（用于客户端同步）
     */
    public static void addConfig(SwordOilConfig config) {
        SWORD_OIL_CONFIGS.add(config);
    }
    
    /**
     * 剑油配置数据类
     */
    public static class SwordOilConfig {
        private final String itemId;
        private final String element;
        private final int amount;
        private final String displayName;
        
        public SwordOilConfig(String itemId, String element, int amount, String displayName) {
            this.itemId = itemId;
            this.element = element;
            this.amount = amount;
            this.displayName = displayName;
        }
        
        public String getItemId() {
            return itemId;
        }
        
        public String getElement() {
            return element;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * 获取物品堆叠
         */
        public ItemStack getItemStack() {
            ResourceLocation resourceLocation = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(resourceLocation);
            return new ItemStack(item);
        }
    }
}
