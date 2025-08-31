package com.chadate.spellelemental.data;

import com.chadate.spellelemental.register.ModFluid;
import com.chadate.spellelemental.register.ModItems;
import io.redspace.ironsspellbooks.datagen.IronRecipeProvider;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.concurrent.CompletableFuture;

public class SpellRecipeProvider extends IronRecipeProvider {
    public SpellRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }


    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(Fluids.WATER, 250))
                .withReagent(ModItems.STELLAR_FRAGMENT.get())
                .withResult(ModFluid.STELLAR_ESSENCE, 250)
                .save(recipeOutput);

        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(ModFluid.STELLAR_ESSENCE, 250))
                .withReagent(Items.BLAZE_ROD)
                .withResult(ModFluid.FIRE_SWORD_OIL, 250)
                .save(recipeOutput);

        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(ModFluid.STELLAR_ESSENCE, 250))
                .withReagent(ItemRegistry.FROZEN_BONE_SHARD.get())
                .withResult(ModFluid.ICE_SWORD_OIL, 250)
                .save(recipeOutput);

        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(ModFluid.STELLAR_ESSENCE, 250))
                .withReagent(ItemRegistry.LIGHTNING_BOTTLE.get())
                .withResult(ModFluid.LIGHTNING_SWORD_OIL, 250)
                .save(recipeOutput);

        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(ModFluid.STELLAR_ESSENCE, 250))
                .withReagent(ItemRegistry.BLOOD_VIAL.get())
                .withResult(ModFluid.BLOOD_SWORD_OIL, 250)
                .save(recipeOutput);

        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(ModFluid.STELLAR_ESSENCE, 250))
                .withReagent(Items.POISONOUS_POTATO)
                .withResult(ModFluid.NATURE_SWORD_OIL, 250)
                .save(recipeOutput);



        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(Items.GLASS_BOTTLE)
                .withReturnItem(ModItems.FIRE_SWORD_OIL.get())
                .withFluid(ModFluid.FIRE_SWORD_OIL, 250)
                .withSound(SoundEvents.BOTTLE_FILL)
                .save(recipeOutput);


        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(Items.GLASS_BOTTLE)
                .withReturnItem(ModItems.ICE_SWORD_OIL.get())
                .withFluid(ModFluid.ICE_SWORD_OIL, 250)
                .withSound(SoundEvents.BOTTLE_FILL)
                .save(recipeOutput);


        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(Items.GLASS_BOTTLE)
                .withReturnItem(ModItems.LIGHTNING_SWORD_OIL.get())
                .withFluid(ModFluid.LIGHTNING_SWORD_OIL, 250)
                .withSound(SoundEvents.BOTTLE_FILL)
                .save(recipeOutput);


        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(Items.GLASS_BOTTLE)
                .withReturnItem(ModItems.BLOOD_SWORD_OIL.get())
                .withFluid(ModFluid.BLOOD_SWORD_OIL, 250)
                .withSound(SoundEvents.BOTTLE_FILL)
                .save(recipeOutput);


        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(Items.GLASS_BOTTLE)
                .withReturnItem(ModItems.NATURE_SWORD_OIL.get())
                .withFluid(ModFluid.NATURE_SWORD_OIL, 250)
                .withSound(SoundEvents.BOTTLE_FILL)
                .save(recipeOutput);
    }
}
