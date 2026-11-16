package com.funcionarios;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

/** A "IA" do funcionário: executa a tarefa escolhida (madeira, pedra, colher, seguir, entregar). */
public class TarefaGoal extends Goal {
    private final FuncionarioEntity f;
    private Tarefa ultima = Tarefa.PARAR;
    private BlockPos alvo;
    private final Set<BlockPos> falhas = new HashSet<>();
    private int progresso;
    private int tentativas;
    private int esperaBusca;
    private int tempoSemChegar;
    private int recalculos;
    private final DefesaLogica defesa;

    public TarefaGoal(FuncionarioEntity funcionario) {
        this.f = funcionario;
        this.defesa = new DefesaLogica(funcionario);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return f.getTarefa() != Tarefa.PARAR;
    }

    @Override
    public boolean canContinueToUse() {
        return f.getTarefa() != Tarefa.PARAR;
    }

    @Override
    public void stop() {
        f.getNavigation().stop();
        alvo = null;
        ultima = Tarefa.PARAR;
    }

    @Override
    public void tick() {
        Tarefa t = f.getTarefa();
        if (t != ultima) {
            ultima = t;
            alvo = null;
            falhas.clear();
            progresso = 0;
            tentativas = 0;
            esperaBusca = 0;
            tempoSemChegar = 0;
            recalculos = 0;
            defesa.resetar();
        }
        switch (t) {
            case SEGUIR -> seguir();
            case DEFENDER -> defesa.tick();
            case ENTREGAR -> entregar();
            case MADEIRA, PEDRA, COLHER -> trabalhar(t);
            default -> { }
        }
    }

    // ------------------------------------------------------------------ seguir / entregar

    private void irAte(Player dono, double velocidade) {
        if (f.getNavigation().isDone() || f.tickCount % 10 == 0) {
            f.getNavigation().moveTo(dono, velocidade);
        }
    }

    private void seguir() {
        Player dono = f.getDonoJogador();
        if (dono == null) return;
        double d2 = f.distanceToSqr(dono);
        if (d2 > 576.0D) {
            f.moveTo(dono.getX(), dono.getY(), dono.getZ(), f.getYRot(), f.getXRot());
            f.getNavigation().stop();
            return;
        }
        f.getLookControl().setLookAt(dono, 30.0F, 30.0F);
        if (d2 > 9.0D) {
            irAte(dono, 1.15D);
        } else {
            f.getNavigation().stop();
        }
    }

    private void entregar() {
        Player dono = f.getDonoJogador();
        if (dono == null) {
            f.falar(Falas.Situacao.LONGE);
            f.definirTarefa(Tarefa.PARAR);
            return;
        }
        double d2 = f.distanceToSqr(dono);
        if (d2 > 576.0D) {
            f.moveTo(dono.getX(), dono.getY(), dono.getZ(), f.getYRot(), f.getXRot());
            f.getNavigation().stop();
            return;
        }
        f.getLookControl().setLookAt(dono, 30.0F, 30.0F);
        if (d2 > 6.25D) {
            irAte(dono, 1.2D);
            return;
        }
        f.entregarItens(dono);
        f.falar(Falas.Situacao.ENTREGUEI);
        f.retomarOuParar();
    }

    // ------------------------------------------------------------------ trabalho

    private void trabalhar(Tarefa t) {
        Class<? extends Item> tipo = f.ferramentaPara(t);
        if (tipo != null && f.acharFerramenta(tipo).isEmpty()) {
            f.falar(Falas.Situacao.FERRAMENTA_QUEBROU);
            f.definirTarefa(Tarefa.PARAR);
            return;
        }
        if (tipo != null) {
            f.equipar(tipo);
        }

        if (alvo == null) {
            if (--esperaBusca > 0) return;
            alvo = buscar(t);
            if (alvo == null) {
                tentativas++;
                esperaBusca = 20;
                if (tentativas >= 3) {
                    f.falar(Falas.Situacao.NADA_ACHADO);
                    f.definirTarefa(Tarefa.PARAR);
                }
                return;
            }
            tentativas = 0;
            progresso = 0;
            tempoSemChegar = 0;
            recalculos = 0;
        }

        Level nivel = f.level();
        BlockState estado = nivel.getBlockState(alvo);
        if (!valido(t, estado, alvo)) {
            alvo = null;
            return;
        }

        Vec3 centro = Vec3.atCenterOf(alvo);
        f.getLookControl().setLookAt(centro.x, centro.y, centro.z);

        if (f.getEyePosition().distanceToSqr(centro) <= 16.0D) {
            f.getNavigation().stop();
            progresso++;
            if (progresso % 6 == 0) {
                f.swing(InteractionHand.MAIN_HAND);
            }
            if (progresso >= tempoParaQuebrar(t)) {
                boolean cheio = f.quebrar(alvo, t);
                alvo = null;
                progresso = 0;
                if (cheio) {
                    f.falar(Falas.Situacao.INV_CHEIO);
                    f.definirTarefa(Tarefa.ENTREGAR);
                }
            }
        } else {
            progresso = 0;
            tempoSemChegar++;
            if (tempoSemChegar > 400) {
                falhas.add(alvo);
                alvo = null;
                return;
            }
            if (f.getNavigation().isDone()) {
                if (recalculos++ >= 3) {
                    falhas.add(alvo);
                    alvo = null;
                    return;
                }
                Path caminho = f.getNavigation().createPath(alvo, 1);
                if (caminho == null) {
                    falhas.add(alvo);
                    alvo = null;
                    return;
                }
                f.getNavigation().moveTo(caminho, 1.0D);
            }
        }
    }

    private int tempoParaQuebrar(Tarefa t) {
        return switch (t) {
            case MADEIRA -> 24;
            case PEDRA -> 32;
            default -> 10;
        };
    }

    private BlockPos buscar(Tarefa t) {
        Level nivel = f.level();
        BlockPos c = f.getCasa();
        int r = FuncionarioEntity.RAIO;
        BlockPos melhor = null;
        double melhorDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-r, -r, -r), c.offset(r, r, r))) {
            if (falhas.contains(p)) continue;
            if (!nivel.hasChunkAt(p)) continue;
            BlockState estado = nivel.getBlockState(p);
            if (!valido(t, estado, p)) continue;
            double d = f.distanceToSqr(Vec3.atCenterOf(p));
            if (d < melhorDist) {
                melhorDist = d;
                melhor = p.immutable();
            }
        }
        return melhor;
    }

    private boolean valido(Tarefa t, BlockState estado, BlockPos pos) {
        return switch (t) {
            case MADEIRA -> estado.is(BlockTags.LOGS);
            case PEDRA -> ehPedra(estado) && exposto(pos);
            case COLHER -> estado.getBlock() instanceof CropBlock c && c.isMaxAge(estado);
            default -> false;
        };
    }

    private boolean ehPedra(BlockState estado) {
        return estado.is(BlockTags.BASE_STONE_OVERWORLD)
                || estado.is(Tags.Blocks.ORES)
                || estado.is(Tags.Blocks.COBBLESTONE);
    }

    /** O bloco precisa ter pelo menos uma face livre e nenhuma lava encostada. */
    private boolean exposto(BlockPos pos) {
        Level nivel = f.level();
        boolean livre = false;
        for (Direction d : Direction.values()) {
            BlockPos vizinho = pos.relative(d);
            BlockState e = nivel.getBlockState(vizinho);
            if (e.getFluidState().is(FluidTags.LAVA)) return false;
            if (e.getCollisionShape(nivel, vizinho).isEmpty()) livre = true;
        }
        return livre;
    }
}
