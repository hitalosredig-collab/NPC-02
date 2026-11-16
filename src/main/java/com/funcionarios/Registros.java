package com.funcionarios;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registros {
    public static final DeferredRegister<EntityType<?>> ENTIDADES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FuncionariosMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FuncionariosMod.MOD_ID);
    public static final DeferredRegister<Item> ITENS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FuncionariosMod.MOD_ID);

    public static final RegistryObject<EntityType<FuncionarioEntity>> FUNCIONARIO =
            ENTIDADES.register("funcionario", () ->
                    EntityType.Builder.of(FuncionarioEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("funcionario"));

    public static final RegistryObject<MenuType<FuncionarioMenu>> MENU =
            MENUS.register("funcionario", () -> IForgeMenuType.create(FuncionarioMenu::new));

    public static final RegistryObject<Item> CONTRATO =
            ITENS.register("contrato", () -> new ContratoItem(new Item.Properties().stacksTo(16)));
}
