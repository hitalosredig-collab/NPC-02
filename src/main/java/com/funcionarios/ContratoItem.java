package com.funcionarios;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Clique direito num bloco com o contrato para contratar um funcionário naquele lugar. */
public class ContratoItem extends Item {
    public ContratoItem(Properties propriedades) {
        super(propriedades);
    }

    @Override
    public InteractionResult useOn(UseOnContext contexto) {
        Level nivel = contexto.getLevel();
        if (nivel.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player jogador = contexto.getPlayer();
        if (jogador == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = contexto.getClickedPos().relative(contexto.getClickedFace());
        FuncionarioEntity funcionario = Registros.FUNCIONARIO.get().create(nivel);
        if (funcionario == null) {
            return InteractionResult.FAIL;
        }
        funcionario.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, jogador.getYRot() + 180.0F, 0.0F);
        funcionario.contratar(jogador);
        nivel.addFreshEntity(funcionario);
        if (!jogador.getAbilities().instabuild) {
            contexto.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
