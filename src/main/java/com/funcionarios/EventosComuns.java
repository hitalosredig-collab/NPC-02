package com.funcionarios;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FuncionariosMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventosComuns {
    @SubscribeEvent
    public static void atributos(EntityAttributeCreationEvent evento) {
        evento.put(Registros.FUNCIONARIO.get(), FuncionarioEntity.criarAtributos().build());
    }
}
