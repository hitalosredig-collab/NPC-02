package com.funcionarios;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Rede {
    private static final String VERSAO = "1";

    public static final SimpleChannel CANAL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FuncionariosMod.MOD_ID, "principal"),
            () -> VERSAO, VERSAO::equals, VERSAO::equals);

    public static void registrar() {
        CANAL.registerMessage(0, PacoteComando.class,
                PacoteComando::codificar, PacoteComando::decodificar, PacoteComando::tratar);
    }
}
