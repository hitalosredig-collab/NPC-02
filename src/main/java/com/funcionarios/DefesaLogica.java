package com.funcionarios;

import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/** Comportamento do comando "Me defender": ataca monstros perto do dono (arma de fogo, espada ou machado). */
public class DefesaLogica {
    private static final double DIST_MAX_TIRO = 24.0D;

    private final FuncionarioEntity f;
    private LivingEntity alvo;
    private int espera;
    private int ultimaRecarga = -1000;
    private int falhasMunicao;
    private boolean semMunicao;
    private boolean mirando;
    private ItemStack armaSacada = ItemStack.EMPTY;

    public DefesaLogica(FuncionarioEntity funcionario) {
        this.f = funcionario;
    }

    public void resetar() {
        alvo = null;
        espera = 0;
        ultimaRecarga = -1000;
        falhasMunicao = 0;
        semMunicao = false;
        mirando = false;
        armaSacada = ItemStack.EMPTY;
    }

    public void tick() {
        Player dono = f.getDonoJogador();
        if (dono == null) return;
        if (espera > 0) espera--;

        alvo = escolherAlvo(dono);
        if (alvo == null) {
            parouDeMirar();
            acompanhar(dono);
            return;
        }
        f.getLookControl().setLookAt(alvo, 30.0F, 30.0F);

        if (!semMunicao && f.temArmaDeFogo() && f.equipar(CompatArmas::ehArma)) {
            aDistancia();
            return;
        }
        parouDeMirar();
        if (f.equipar(s -> s.getItem() instanceof SwordItem) || f.equipar(s -> s.getItem() instanceof AxeItem)) {
            corpoACorpo();
            return;
        }
        if (semMunicao) {
            f.getNavigation().stop();
            return;
        }
        f.falar(Falas.Situacao.SEM_ARMA);
        f.definirTarefa(Tarefa.PARAR);
    }

    // ------------------------------------------------------------------ escolha do alvo

    private LivingEntity escolherAlvo(Player dono) {
        if (alvo != null && alvo.isAlive() && f.tickCount % 10 != 0) return alvo;

        LivingEntity agressor = dono.getLastHurtByMob();
        if (valido(agressor) && dono.tickCount - dono.getLastHurtByMobTimestamp() < 200) return agressor;

        LivingEntity atacado = dono.getLastHurtMob();
        if (valido(atacado) && atacado instanceof Enemy
                && dono.tickCount - dono.getLastHurtMobTimestamp() < 200) return atacado;

        List<Mob> lista = f.level().getEntitiesOfClass(Mob.class, dono.getBoundingBox().inflate(12.0D),
                m -> m instanceof Enemy && m.isAlive() && m != f);
        LivingEntity melhor = null;
        double melhorDist = Double.MAX_VALUE;
        for (Mob m : lista) {
            double d = m.distanceToSqr(dono);
            if (d < melhorDist) {
                melhorDist = d;
                melhor = m;
            }
        }
        return melhor;
    }

    private boolean valido(LivingEntity e) {
        return e != null && e.isAlive() && e != f && !(e instanceof FuncionarioEntity) && !(e instanceof Player);
    }

    // ------------------------------------------------------------------ movimento

    private void aproximar() {
        if (f.getNavigation().isDone() || f.tickCount % 10 == 0) {
            f.getNavigation().moveTo(alvo, 1.2D);
        }
    }

    private void acompanhar(Player dono) {
        double d2 = f.distanceToSqr(dono);
        if (d2 > 576.0D) {
            f.moveTo(dono.getX(), dono.getY(), dono.getZ(), f.getYRot(), f.getXRot());
            f.getNavigation().stop();
        } else if (d2 > 36.0D) {
            if (f.getNavigation().isDone() || f.tickCount % 10 == 0) {
                f.getNavigation().moveTo(dono, 1.15D);
            }
        } else {
            f.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ corpo a corpo

    private void corpoACorpo() {
        float largura = f.getBbWidth() * 2.0F;
        double alcance = largura * largura + alvo.getBbWidth() + 1.0D;
        if (f.distanceToSqr(alvo) <= alcance && f.hasLineOfSight(alvo)) {
            f.getNavigation().stop();
            if (espera <= 0) {
                espera = 20;
                f.swing(InteractionHand.MAIN_HAND);
                f.doHurtTarget(alvo);
                ItemStack arma = f.getMainHandItem();
                arma.hurtAndBreak(1, f, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            }
        } else {
            aproximar();
        }
    }

    // ------------------------------------------------------------------ arma de fogo

    private void parouDeMirar() {
        if (mirando) {
            CompatArmas.mirar(f, false);
            mirando = false;
        }
    }

    private void aDistancia() {
        ItemStack arma = f.getMainHandItem();
        if (arma != armaSacada) {
            CompatArmas.sacar(f);
            armaSacada = arma;
        }

        int municao = CompatArmas.municao(arma);
        if (municao == 0) {
            parouDeMirar();
            if (f.tickCount - ultimaRecarga >= 80) {
                if (falhasMunicao >= 3) {
                    semMunicao = true;
                    f.falar(Falas.Situacao.SEM_MUNICAO);
                    return;
                }
                CompatArmas.recarregar(f);
                ultimaRecarga = f.tickCount;
                falhasMunicao++;
            }
            return;
        }
        falhasMunicao = 0;

        double d2 = f.distanceToSqr(alvo);
        boolean visivel = f.hasLineOfSight(alvo);
        if (!visivel || d2 > DIST_MAX_TIRO * DIST_MAX_TIRO) {
            parouDeMirar();
            aproximar();
            return;
        }
        f.getNavigation().stop();
        if (!mirando) {
            CompatArmas.mirar(f, true);
            mirando = true;
        }
        if (f.tickCount % 2 == 0) {
            Vec3 olho = f.getEyePosition();
            Vec3 ponto = alvo.position().add(0.0D, alvo.getBbHeight() * 0.6D, 0.0D);
            double dx = ponto.x - olho.x;
            double dy = ponto.y - olho.y;
            double dz = ponto.z - olho.z;
            double h = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            float pitch = (float) -(Mth.atan2(dy, h) * (180.0D / Math.PI));
            f.setYRot(yaw);
            f.setYHeadRot(yaw);
            f.setYBodyRot(yaw);
            f.setXRot(pitch);
            CompatArmas.atirar(f, pitch, yaw);
        }
    }
}
