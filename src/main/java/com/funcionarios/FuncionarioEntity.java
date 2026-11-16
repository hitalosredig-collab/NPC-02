package com.funcionarios;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/** O funcionário: um NPC humanoide com inventário, equipamento, dono e tarefas. */
public class FuncionarioEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> FALA =
            SynchedEntityData.defineId(FuncionarioEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TAREFA =
            SynchedEntityData.defineId(FuncionarioEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKIN =
            SynchedEntityData.defineId(FuncionarioEntity.class, EntityDataSerializers.INT);

    private static final String[] NOMES = {"Zé", "Tião", "Beto", "Cida", "Nando", "Lúcia", "Dedé", "Marcão"};

    /** Raio (em blocos) ao redor do ponto onde o funcionário recebeu a ordem. */
    public static final int RAIO = 16;

    private final SimpleContainer inventario = new SimpleContainer(27);
    private final LazyOptional<IItemHandler> manipulador = LazyOptional.of(() -> new InvWrapper(inventario));
    private UUID dono;
    private BlockPos casa = BlockPos.ZERO;
    private int personalidade;
    private Tarefa tarefaAnterior = Tarefa.PARAR;

    public FuncionarioEntity(EntityType<? extends PathfinderMob> tipo, Level nivel) {
        super(tipo, nivel);
        this.setPersistenceRequired();
        // tudo que o jogador colocar nos slots de equipamento cai inteiro quando ele morre
        for (EquipmentSlot slot : EquipamentoContainer.SLOTS) {
            this.setDropChance(slot, 2.0F);
        }
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
            nav.setCanFloat(true);
        }
    }

    public static AttributeSupplier.Builder criarAtributos() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FALA, "");
        this.entityData.define(TAREFA, 0);
        this.entityData.define(SKIN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(2, new TarefaGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------------ capacidades (outros mods enxergam o inventário)

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capacidade, Direction lado) {
        if (capacidade == ForgeCapabilities.ITEM_HANDLER) {
            return manipulador.cast();
        }
        return super.getCapability(capacidade, lado);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        manipulador.invalidate();
    }

    // ------------------------------------------------------------------ dono / contratação

    public void contratar(Player jogador) {
        this.dono = jogador.getUUID();
        this.entityData.set(SKIN, this.getRandom().nextInt(3));
        this.personalidade = this.getRandom().nextInt(3);
        this.setCustomName(Component.literal(NOMES[this.getRandom().nextInt(NOMES.length)]));
        this.setCustomNameVisible(true);
        this.casa = this.blockPosition();
        falar(Falas.Situacao.CONTRATADO);
    }

    public boolean ehDono(Player jogador) {
        return dono != null && dono.equals(jogador.getUUID());
    }

    public Player getDonoJogador() {
        return dono == null ? null : this.level().getPlayerByUUID(dono);
    }

    @Override
    public InteractionResult mobInteract(Player jogador, InteractionHand mao) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (dono == null) {
            dono = jogador.getUUID();
        }
        if (!ehDono(jogador)) {
            falar(Falas.Situacao.NAO_E_PATRAO);
            return InteractionResult.CONSUME;
        }
        if (jogador instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp,
                    new SimpleMenuProvider((id, inv, p) -> new FuncionarioMenu(id, inv, this), this.getDisplayName()),
                    buf -> buf.writeInt(this.getId()));
        }
        return InteractionResult.CONSUME;
    }

    // ------------------------------------------------------------------ fala

    public void falar(Falas.Situacao situacao) {
        String texto = Falas.get(situacao, personalidade, this.getRandom());
        this.entityData.set(FALA, texto);
        if (!this.level().isClientSide) {
            Player d = getDonoJogador();
            if (d != null && this.distanceToSqr(d) < 1024.0D) {
                d.displayClientMessage(Component.literal(this.getName().getString() + ": " + texto), true);
            }
        }
    }

    public String getFala() {
        return this.entityData.get(FALA);
    }

    // ------------------------------------------------------------------ tarefas

    public Tarefa getTarefa() {
        return Tarefa.de(this.entityData.get(TAREFA));
    }

    public Tarefa getTarefaAnterior() {
        return tarefaAnterior;
    }

    public void definirTarefa(Tarefa tarefa) {
        if (tarefa.ehTrabalho()) {
            tarefaAnterior = tarefa;
        } else if (tarefa == Tarefa.PARAR || tarefa == Tarefa.SEGUIR || tarefa == Tarefa.DEFENDER) {
            tarefaAnterior = Tarefa.PARAR;
        }
        this.entityData.set(TAREFA, tarefa.ordinal());
        this.getNavigation().stop();
    }

    /** Chamado quando o jogador clica num botão da tela (já validado no servidor). */
    public void receberComando(Tarefa tarefa, Player jogador) {
        switch (tarefa) {
            case PARAR -> {
                definirTarefa(Tarefa.PARAR);
                falar(Falas.Situacao.PARADO);
            }
            case SEGUIR -> {
                definirTarefa(Tarefa.SEGUIR);
                falar(Falas.Situacao.SEGUINDO);
            }
            case DEFENDER -> {
                if (!temArmaParaDefender()) {
                    falar(Falas.Situacao.SEM_ARMA);
                    return;
                }
                definirTarefa(Tarefa.DEFENDER);
                falar(Falas.Situacao.DEFENDENDO);
            }
            case ENTREGAR -> {
                definirTarefa(Tarefa.ENTREGAR);
                falar(Falas.Situacao.ACEITO);
            }
            default -> {
                Class<? extends Item> ferramenta = ferramentaPara(tarefa);
                if (ferramenta != null && acharFerramenta(ferramenta).isEmpty()) {
                    falar(tarefa == Tarefa.MADEIRA ? Falas.Situacao.SEM_MACHADO : Falas.Situacao.SEM_PICARETA);
                    return;
                }
                if (inventarioCheio()) {
                    falar(Falas.Situacao.INV_CHEIO);
                    return;
                }
                this.casa = this.blockPosition();
                equipar(ferramenta != null ? ferramenta : HoeItem.class);
                definirTarefa(tarefa);
                falar(Falas.Situacao.ACEITO);
            }
        }
    }

    /** Depois de entregar os itens, volta ao trabalho anterior (se tiver) ou para. */
    public void retomarOuParar() {
        Tarefa anterior = tarefaAnterior;
        if (anterior.ehTrabalho()) {
            Class<? extends Item> tipo = ferramentaPara(anterior);
            if (tipo == null || !acharFerramenta(tipo).isEmpty()) {
                equipar(tipo != null ? tipo : HoeItem.class);
                definirTarefa(anterior);
                return;
            }
        }
        definirTarefa(Tarefa.PARAR);
    }

    public Class<? extends Item> ferramentaPara(Tarefa tarefa) {
        if (tarefa == Tarefa.MADEIRA) return AxeItem.class;
        if (tarefa == Tarefa.PEDRA) return PickaxeItem.class;
        return null;
    }

    // ------------------------------------------------------------------ inventário e equipamento

    public SimpleContainer getInventario() {
        return inventario;
    }

    public boolean inventarioCheio() {
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            if (inventario.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    /** Procura primeiro na mão principal e depois no inventário. Devolve o próprio item (não uma cópia). */
    public ItemStack acharItem(Predicate<ItemStack> serve) {
        ItemStack mao = this.getMainHandItem();
        if (!mao.isEmpty() && serve.test(mao)) return mao;
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            ItemStack s = inventario.getItem(i);
            if (!s.isEmpty() && serve.test(s)) return s;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack acharFerramenta(Class<? extends Item> tipo) {
        return acharItem(s -> tipo.isInstance(s.getItem()));
    }

    /**
     * Garante que a mão principal tenha um item que sirva. Se estiver no inventário, troca de lugar
     * com o que estava na mão (nada é copiado nem perdido).
     */
    public boolean equipar(Predicate<ItemStack> serve) {
        ItemStack mao = this.getMainHandItem();
        if (!mao.isEmpty() && serve.test(mao)) return true;
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            ItemStack s = inventario.getItem(i);
            if (!s.isEmpty() && serve.test(s)) {
                inventario.setItem(i, mao.isEmpty() ? ItemStack.EMPTY : mao);
                this.setItemSlot(EquipmentSlot.MAINHAND, s);
                return true;
            }
        }
        return false;
    }

    public boolean equipar(Class<? extends Item> tipo) {
        return equipar(s -> tipo.isInstance(s.getItem()));
    }

    public boolean temArmaDeFogo() {
        return CompatArmas.disponivel() && !acharItem(CompatArmas::ehArma).isEmpty();
    }

    public boolean temArmaParaDefender() {
        return temArmaDeFogo()
                || !acharItem(s -> s.getItem() instanceof SwordItem).isEmpty()
                || !acharItem(s -> s.getItem() instanceof AxeItem).isEmpty();
    }

    /** Entrega tudo (menos ferramentas e armas) para o jogador. O que estiver na mão e na armadura fica. */
    public void entregarItens(Player jogador) {
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            ItemStack s = inventario.getItem(i);
            if (s.isEmpty() || s.getItem() instanceof DiggerItem || s.getItem() instanceof SwordItem
                    || CompatArmas.ehArma(s)) continue;
            ItemStack copia = s.copy();
            inventario.setItem(i, ItemStack.EMPTY);
            jogador.getInventory().add(copia);
            if (!copia.isEmpty()) {
                jogador.drop(copia, false);
            }
        }
        inventario.setChanged();
    }

    // ------------------------------------------------------------------ trabalho

    public BlockPos getCasa() {
        return casa;
    }

    /**
     * Quebra/colhe o bloco, guarda os drops no inventário e gasta a ferramenta.
     * Retorna true se o inventário ficou sem espaço.
     */
    public boolean quebrar(BlockPos pos, Tarefa tarefa) {
        if (!(this.level() instanceof ServerLevel nivel)) return false;
        BlockState estado = nivel.getBlockState(pos);
        Class<? extends Item> tipo = ferramentaPara(tarefa);
        ItemStack ferramenta = acharFerramenta(tipo != null ? tipo : HoeItem.class);

        List<ItemStack> drops = Block.getDrops(estado, nivel, pos, nivel.getBlockEntity(pos), this, ferramenta);
        boolean cheio = false;
        for (ItemStack drop : drops) {
            ItemStack resto = inventario.addItem(drop);
            if (!resto.isEmpty()) {
                Block.popResource(nivel, pos, resto);
                cheio = true;
            }
        }

        if (estado.getBlock() instanceof CropBlock plantacao) {
            nivel.levelEvent(2001, pos, Block.getId(estado));
            nivel.setBlock(pos, plantacao.getStateForAge(0), 3); // replanta
        } else {
            nivel.destroyBlock(pos, false, this);
        }

        if (tarefa != Tarefa.COLHER && !ferramenta.isEmpty()) {
            ferramenta.hurtAndBreak(1, this, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            inventario.setChanged();
        }
        return cheio || inventarioCheio();
    }

    // ------------------------------------------------------------------ dados

    public int getSkin() {
        return this.entityData.get(SKIN);
    }

    @Override
    public boolean removeWhenFarAway(double distancia) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource fonte, int saque, boolean atingidoRecentemente) {
        super.dropCustomDeathLoot(fonte, saque, atingidoRecentemente);
        Containers.dropContents(this.level(), this.blockPosition(), inventario);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventario", inventario.createTag());
        if (dono != null) tag.putUUID("Dono", dono);
        tag.putInt("Tarefa", getTarefa().ordinal());
        tag.putInt("TarefaAnterior", tarefaAnterior.ordinal());
        tag.putInt("Skin", getSkin());
        tag.putInt("Personalidade", personalidade);
        tag.putInt("CasaX", casa.getX());
        tag.putInt("CasaY", casa.getY());
        tag.putInt("CasaZ", casa.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventario.fromTag(tag.getList("Inventario", Tag.TAG_COMPOUND));
        if (tag.hasUUID("Dono")) dono = tag.getUUID("Dono");
        this.entityData.set(TAREFA, tag.getInt("Tarefa"));
        tarefaAnterior = Tarefa.de(tag.getInt("TarefaAnterior"));
        this.entityData.set(SKIN, tag.getInt("Skin"));
        personalidade = tag.getInt("Personalidade");
        casa = new BlockPos(tag.getInt("CasaX"), tag.getInt("CasaY"), tag.getInt("CasaZ"));
    }
}
