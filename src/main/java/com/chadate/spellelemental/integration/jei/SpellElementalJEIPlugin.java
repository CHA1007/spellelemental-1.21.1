package com.chadate.spellelemental.integration.jei;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.integration.jei.category.SwordOilApplicationCategory;
import com.chadate.spellelemental.integration.jei.recipe.SwordOilApplicationRecipe;
import com.chadate.spellelemental.integration.jei.data.SwordOilConfigLoader;
import com.chadate.spellelemental.register.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * SpellElemental 模组的 JEI 集成插件
 */
@JeiPlugin
public class SpellElementalJEIPlugin implements IModPlugin {
            public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID, "jei_plugin");
    
    // 剑油应用配方类型
    public static final RecipeType<SwordOilApplicationRecipe> SWORD_OIL_APPLICATION = 
        RecipeType.create(SpellElemental.MODID, "sword_oil_application", SwordOilApplicationRecipe.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SwordOilApplicationCategory(registration.getJeiHelpers().getGuiHelper()));
    }


    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SWORD_STAND.get()), SpellElementalJEIPlugin.SWORD_OIL_APPLICATION);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SwordOilApplicationRecipe> recipes = createSwordOilApplicationRecipes();
        registration.addRecipes(SWORD_OIL_APPLICATION, recipes);
    }
    
    /**
     * 创建剑油应用配方列表
     * 支持动态配置加载，如果配置为空则创建基础配方用于展示
     */
    private List<SwordOilApplicationRecipe> createSwordOilApplicationRecipes() {
        List<SwordOilApplicationRecipe> recipes = new ArrayList<>();
        
        // 获取所有剑类物品
        List<ItemStack> swords = getSwordItems();
        
        // 获取所有配置的剑油
        List<SwordOilConfigLoader.SwordOilConfig> swordOilConfigs = SwordOilConfigLoader.getSwordOilConfigs();
        
        // 如果配置为空（数据包还未加载），创建基础配方用于展示
        if (swordOilConfigs.isEmpty()) {
            SpellElemental.LOGGER.info("精油配置为空，创建基础展示配方");
            recipes.addAll(createFallbackRecipes(swords));
        } else {
            // 使用实际配置创建配方
            for (ItemStack sword : swords) {
                for (SwordOilConfigLoader.SwordOilConfig oilConfig : swordOilConfigs) {
                    ItemStack swordOilItem = oilConfig.getItemStack();
                    if (!swordOilItem.isEmpty()) {
                        recipes.add(new SwordOilApplicationRecipe(
                            sword.copy(),                                           // 输入剑
                            swordOilItem,                                          // 剑油物品（从配置）
                            new ItemStack(ModBlocks.SWORD_STAND.get()),            // 剑座
                            createEnchantedSword(sword.getItem(), oilConfig.getElement(), oilConfig.getAmount()), // 输出的附着元素的剑
                            oilConfig.getElement(),                                // 元素类型（从配置）
                            oilConfig.getAmount()                                  // 元素量（从配置）
                        ));
                    }
                }
            }
        }
        
        return recipes;
    }
    
    /**
     * 创建回退配方（当配置数据未加载时使用）
     * 基于注册的精油物品创建基础展示配方
     */
    private List<SwordOilApplicationRecipe> createFallbackRecipes(List<ItemStack> swords) {
        List<SwordOilApplicationRecipe> fallbackRecipes = new ArrayList<>();
        
        // 获取所有注册的精油物品
        List<ItemStack> oilItems = getAllSwordOilItems();
        
        // 为每种剑和每种精油创建基础配方
        for (ItemStack sword : swords) {
            for (ItemStack oilItem : oilItems) {
                if (oilItem.getItem() instanceof com.chadate.spellelemental.item.SwordOilItem swordOilItem) {
                    // 从物品实例动态获取配置（如果可用）
                    String element = swordOilItem.getElementType();
                    int amount = swordOilItem.getElementAmount();
                    
                    // 如果动态获取失败，使用默认值
                    if (element.isEmpty()) {
                        element = "unknown";
                    }
                    if (amount <= 0) {
                        amount = 1000; // 默认展示数量
                    }
                    
                    fallbackRecipes.add(new SwordOilApplicationRecipe(
                        sword.copy(),                                           // 输入剑
                        oilItem.copy(),                                        // 剑油物品
                        new ItemStack(ModBlocks.SWORD_STAND.get()),            // 剑座
                        createEnchantedSword(sword.getItem(), element, amount), // 输出的附着元素的剑
                        element,                                               // 元素类型
                        amount                                                 // 元素量
                    ));
                }
            }
        }
        
        return fallbackRecipes;
    }
    
    /**
     * 获取所有注册的精油物品
     */
    private List<ItemStack> getAllSwordOilItems() {
        List<ItemStack> oilItems = new ArrayList<>();
        
        // 遍历物品注册表，查找所有 SwordOilItem
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
            Item item = entry.getValue();
            if (item instanceof com.chadate.spellelemental.item.SwordOilItem) {
                oilItems.add(new ItemStack(item));
            }
        }
        
        return oilItems;
    }
    
    /**
     * 获取所有剑类物品
     * 使用 tag 系统动态获取，支持所有模组的剑类物品
     */
    private List<ItemStack> getSwordItems() {
        List<ItemStack> swords = new ArrayList<>();
        
        // 使用 Minecraft 的剑类物品 tag
        var swordTag = BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.SWORDS);
        for (var holder : swordTag) {
            swords.add(new ItemStack(holder.value()));
        }
        
        return swords;
    }
    
    /**
     * 创建附着元素的剑
     */
    private ItemStack createEnchantedSword(net.minecraft.world.item.Item swordItem, String elementType, int elementAmount) {
        ItemStack sword = new ItemStack(swordItem);
        
        // 添加元素附着的 NBT 数据
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag elementData = new net.minecraft.nbt.CompoundTag();
        elementData.putString("element", elementType);
        elementData.putInt("amount", elementAmount);  // 使用传入的元素量
        elementData.putLong("appliedTime", System.currentTimeMillis());
        tag.put("ElementAttachment", elementData);
        
        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.of(tag));
        
        return sword;
    }
}
