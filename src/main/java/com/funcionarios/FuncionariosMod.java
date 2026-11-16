package com.funcionarios;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FuncionariosMod.MOD_ID)
public class FuncionariosMod {
    public static final String MOD_ID = "funcionarios";

    public FuncionariosMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registros.ENTIDADES.register(bus);
        Registros.MENUS.register(bus);
        Registros.ITENS.register(bus);
        bus.addListener(this::aoIniciar);
        bus.addListener(this::aoMontarAbas);
    }

    private void aoIniciar(FMLCommonSetupEvent evento) {
        evento.enqueueWork(Rede::registrar);
    }

    private void aoMontarAbas(BuildCreativeModeTabContentsEvent evento) {
        if (evento.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            evento.accept(Registros.CONTRATO.get());
        }
    }
}
