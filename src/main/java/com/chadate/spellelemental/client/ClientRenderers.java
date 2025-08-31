package com.chadate.spellelemental.client;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.client.render.SwordStandRenderer;
import com.chadate.spellelemental.register.ModBlockEntities;
import com.chadate.spellelemental.register.ModFluid;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 客户端渲染器注册类
 * 负责注册所有客户端渲染器
 */
@EventBusSubscriber(modid = SpellElemental.MODID, value = Dist.CLIENT)
public class ClientRenderers {
    
    /**
     * 在客户端设置时注册方块实体渲染器
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 注册剑架方块实体渲染器
            BlockEntityRenderers.register(ModBlockEntities.SWORD_STAND.get(), SwordStandRenderer::new);
            
            // 设置流体渲染层级为半透明
            ItemBlockRenderTypes.setRenderLayer(ModFluid.STELLAR_ESSENCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluid.FIRE_SWORD_OIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluid.ICE_SWORD_OIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluid.LIGHTNING_SWORD_OIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluid.BLOOD_SWORD_OIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluid.NATURE_SWORD_OIL.get(), RenderType.translucent());
        });
    }
    
    /**
     * 注册流体客户端扩展
     */
    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // 通用的水纹理资源位置
        final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
        final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
        
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFF6A5ACD;
            }
        }, ModFluid.STELLAR_ESSENCE_TYPE.get());

        // 火焰精油 - 橙红色
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFFFF4500;
            }
        }, ModFluid.FIRE_SWORD_OIL_TYPE.get());

        // 冰霜精油 - 浅蓝色
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFF87CEEB;
            }
        }, ModFluid.ICE_SWORD_OIL_TYPE.get());

        // 雷电精油 - 蓝色
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFF1E90FF;
            }
        }, ModFluid.LIGHTNING_SWORD_OIL_TYPE.get());

        // 鲜血精油 - 深红色
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFF8B0000;
            }
        }, ModFluid.BLOOD_SWORD_OIL_TYPE.get());

        // 自然精油 - 绿色
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return WATER_STILL;
            }
            
            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }
            
            @Override
            public int getTintColor() {
                return 0xFF32CD32;
            }
        }, ModFluid.NATURE_TYPE.get());
    }
}
