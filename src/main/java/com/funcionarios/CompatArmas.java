package com.funcionarios;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * Compatibilidade OPCIONAL com o mod de armas TACZ (modid "tacz").
 *
 * Tudo é feito por reflexão, então o mod compila e roda sem o TACZ instalado.
 * Se o TACZ não estiver presente (ou a API for diferente do esperado), os funcionários
 * simplesmente não usam armas de fogo e usam espada/machado.
 *
 * ATENÇÃO: os nomes abaixo foram montados a partir da documentação pública do TACZ e
 * ainda NÃO foram testados com o jogo. Se algo falhar, o motivo aparece no log do jogo
 * (procure por "CompatArmas").
 */
public final class CompatArmas {
    private static final Logger LOG = LogUtils.getLogger();
    private static final int MAX_FALHAS = 5;

    private static boolean iniciado;
    private static boolean disponivel;
    private static int falhas;

    private static Class<?> classeIGun;
    private static Method mFromLiving;
    private static Method mShoot;
    private static Method mAim;
    private static Method mReload;
    private static Method mDraw;     // opcional
    private static Method mMunicao;  // opcional

    private CompatArmas() {}

    public static synchronized boolean disponivel() {
        if (!iniciado) {
            iniciado = true;
            iniciar();
        }
        return disponivel;
    }

    private static void iniciar() {
        if (!ModList.get().isLoaded("tacz")) {
            return;
        }
        try {
            classeIGun = Class.forName("com.tacz.guns.api.item.IGun");
            Class<?> operador = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            mFromLiving = operador.getMethod("fromLivingEntity", LivingEntity.class);
            mShoot = operador.getMethod("shoot", Supplier.class, Supplier.class);
            mAim = operador.getMethod("aim", boolean.class);
            mReload = operador.getMethod("reload");
            mDraw = metodoOpcional(operador, "draw", Supplier.class);
            mMunicao = metodoOpcional(classeIGun, "getCurrentAmmoCount", ItemStack.class);
            disponivel = true;
            LOG.info("[CompatArmas] TACZ encontrado: funcionários vão poder usar armas de fogo.");
        } catch (ReflectiveOperationException | LinkageError e) {
            LOG.warn("[CompatArmas] TACZ está instalado, mas a API não bateu com o esperado. "
                    + "Armas de fogo desativadas.", e);
        }
    }

    private static Method metodoOpcional(Class<?> classe, String nome, Class<?>... parametros) {
        try {
            return classe.getMethod(nome, parametros);
        } catch (NoSuchMethodException e) {
            LOG.info("[CompatArmas] método opcional '{}' não existe nesta versão do TACZ.", nome);
            return null;
        }
    }

    public static boolean ehArma(ItemStack pilha) {
        return disponivel() && !pilha.isEmpty() && classeIGun.isInstance(pilha.getItem());
    }

    /** Munição atual dentro da arma, ou -1 se não for possível saber. */
    public static int municao(ItemStack arma) {
        if (!ehArma(arma) || mMunicao == null) return -1;
        try {
            Object r = mMunicao.invoke(arma.getItem(), arma);
            return r instanceof Integer i ? i : -1;
        } catch (ReflectiveOperationException | RuntimeException e) {
            registrarFalha(e);
            return -1;
        }
    }

    /** Avisa o TACZ de qual item o funcionário está segurando. */
    public static void sacar(LivingEntity entidade) {
        if (!disponivel() || mDraw == null) return;
        try {
            Supplier<ItemStack> mao = entidade::getMainHandItem;
            mDraw.invoke(operador(entidade), mao);
        } catch (ReflectiveOperationException | RuntimeException e) {
            registrarFalha(e);
        }
    }

    public static void mirar(LivingEntity entidade, boolean mirando) {
        if (!disponivel()) return;
        try {
            mAim.invoke(operador(entidade), mirando);
        } catch (ReflectiveOperationException | RuntimeException e) {
            registrarFalha(e);
        }
    }

    public static void atirar(LivingEntity entidade, float pitch, float yaw) {
        if (!disponivel()) return;
        try {
            Supplier<Float> p = () -> pitch;
            Supplier<Float> y = () -> yaw;
            mShoot.invoke(operador(entidade), p, y);
        } catch (ReflectiveOperationException | RuntimeException e) {
            registrarFalha(e);
        }
    }

    public static void recarregar(LivingEntity entidade) {
        if (!disponivel()) return;
        try {
            mReload.invoke(operador(entidade));
        } catch (ReflectiveOperationException | RuntimeException e) {
            registrarFalha(e);
        }
    }

    private static Object operador(LivingEntity entidade) throws ReflectiveOperationException {
        return mFromLiving.invoke(null, entidade);
    }

    private static void registrarFalha(Exception e) {
        falhas++;
        LOG.warn("[CompatArmas] erro ao falar com o TACZ ({}/{}).", falhas, MAX_FALHAS, e);
        if (falhas >= MAX_FALHAS) {
            disponivel = false;
            LOG.warn("[CompatArmas] muitos erros seguidos: armas de fogo desativadas até reiniciar o jogo.");
        }
    }
}
