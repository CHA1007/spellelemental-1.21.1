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
import net.minecraft.world.item.ItemStack;
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
     */
    private List<SwordOilApplicationRecipe> createSwordOilApplicationRecipes() {
        List<SwordOilApplicationRecipe> recipes = new ArrayList<>();
        
        // 获取所有剑类物品
        List<ItemStack> swords = getSwordItems();
        
        // 获取所有配置的剑油
        List<SwordOilConfigLoader.SwordOilConfig> swordOilConfigs = SwordOilConfigLoader.getSwordOilConfigs();
        
        // 为每种剑和每种剑油创建配方
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
        
        return recipes;
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
