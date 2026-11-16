package com.funcionarios;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FuncionariosMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EventosCliente {
    @SubscribeEvent
    public static void renderizadores(EntityRenderersEvent.RegisterRenderers evento) {
        evento.registerEntityRenderer(Registros.FUNCIONARIO.get(), FuncionarioRenderer::new);
    }

    @SubscribeEvent
    public static void configurarCliente(FMLClientSetupEvent evento) {
        evento.enqueueWork(() -> MenuScreens.register(Registros.MENU.get(), FuncionarioScreen::new));
    }
}
