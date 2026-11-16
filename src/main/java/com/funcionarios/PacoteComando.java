package com.funcionarios;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

/** Pacote enviado do cliente ao servidor quando o jogador clica num botão de comando. */
public class PacoteComando {
    private final int entidade;
    private final int tarefa;

    public PacoteComando(int entidade, int tarefa) {
        this.entidade = entidade;
        this.tarefa = tarefa;
    }

    public void codificar(FriendlyByteBuf buf) {
        buf.writeInt(entidade);
        buf.writeInt(tarefa);
    }

    public static PacoteComando decodificar(FriendlyByteBuf buf) {
        return new PacoteComando(buf.readInt(), buf.readInt());
    }

    public static void tratar(PacoteComando pacote, Supplier<NetworkEvent.Context> contexto) {
        contexto.get().enqueueWork(() -> {
            ServerPlayer jogador = contexto.get().getSender();
            if (jogador == null) return;
            Entity e = jogador.level().getEntity(pacote.entidade);
            if (e instanceof FuncionarioEntity f && f.ehDono(jogador) && jogador.distanceToSqr(f) < 100.0D) {
                f.receberComando(Tarefa.de(pacote.tarefa), jogador);
            }
        });
        contexto.get().setPacketHandled(true);
    }
}
