package com.funcionarios;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FuncionarioMenu extends AbstractContainerMenu {
    private static final int SLOTS_FUNC = 27;
    private static final int INICIO_JOGADOR = SLOTS_FUNC;
    /** Primeiro slot de equipamento (depois do baú e do inventário do jogador). */
    private static final int INICIO_EQUIP = SLOTS_FUNC + 36;

    /** Posição (relativa à tela) da coluna de equipamento, à esquerda do baú. */
    public static final int EQUIP_X = -22;
    public static final int[] EQUIP_Y = {18, 36, 54, 72, 96, 114};

    private final FuncionarioEntity funcionario;

    /** Construtor usado no cliente (lê o id da entidade enviado pelo servidor). */
    public FuncionarioMenu(int id, Inventory inventarioJogador, FriendlyByteBuf buf) {
        this(id, inventarioJogador, (FuncionarioEntity) inventarioJogador.player.level().getEntity(buf.readInt()));
    }

    public FuncionarioMenu(int id, Inventory inventarioJogador, FuncionarioEntity funcionario) {
        super(Registros.MENU.get(), id);
        this.funcionario = funcionario;
        Container container = funcionario.getInventario();
        checkContainerSize(container, SLOTS_FUNC);
        container.startOpen(inventarioJogador.player);

        // 0..26: inventário do funcionário (estilo baú)
        for (int linha = 0; linha < 3; linha++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(container, col + linha * 9, 8 + col * 18, 18 + linha * 18));
            }
        }
        // 27..62: inventário do jogador
        for (int linha = 0; linha < 3; linha++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventarioJogador, col + linha * 9 + 9, 8 + col * 18, 85 + linha * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventarioJogador, col, 8 + col * 18, 143));
        }
        // 63..68: armadura (4) + mão principal + mão secundária
        Container equipamento = new EquipamentoContainer(funcionario);
        for (int i = 0; i < EquipamentoContainer.SLOTS.length; i++) {
            this.addSlot(new SlotEquipamento(equipamento, i, EQUIP_X, EQUIP_Y[i],
                    EquipamentoContainer.SLOTS[i], funcionario));
        }
    }

    public FuncionarioEntity getFuncionario() {
        return funcionario;
    }

    @Override
    public boolean stillValid(Player jogador) {
        return funcionario.isAlive() && jogador.distanceToSqr(funcionario) < 64.0D;
    }

    @Override
    public void removed(Player jogador) {
        super.removed(jogador);
        funcionario.getInventario().stopOpen(jogador);
    }

    @Override
    public ItemStack quickMoveStack(Player jogador, int indice) {
        ItemStack copia = ItemStack.EMPTY;
        Slot slot = this.slots.get(indice);
        if (slot != null && slot.hasItem()) {
            ItemStack pilha = slot.getItem();
            copia = pilha.copy();

            if (indice >= INICIO_EQUIP) {
                // equipamento -> baú / inventário do jogador
                if (!this.moveItemStackTo(pilha, 0, INICIO_EQUIP, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // tenta vestir primeiro (só os 4 slots de armadura)
                boolean movido = false;
                for (int i = 0; i < 4 && !movido; i++) {
                    int destino = INICIO_EQUIP + i;
                    Slot s = this.slots.get(destino);
                    if (!s.hasItem() && s.mayPlace(pilha)) {
                        movido = this.moveItemStackTo(pilha, destino, destino + 1, false);
                    }
                }
                if (!movido) {
                    if (indice < INICIO_JOGADOR) {
                        if (!this.moveItemStackTo(pilha, INICIO_JOGADOR, INICIO_EQUIP, true)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(pilha, 0, SLOTS_FUNC, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (pilha.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copia;
    }

    /** Slot de equipamento: só aceita o que cabe naquele lugar (as mãos aceitam qualquer coisa). */
    private static class SlotEquipamento extends Slot {
        private final EquipmentSlot tipo;
        private final FuncionarioEntity entidade;

        SlotEquipamento(Container container, int indice, int x, int y, EquipmentSlot tipo, FuncionarioEntity entidade) {
            super(container, indice, x, y);
            this.tipo = tipo;
            this.entidade = entidade;
        }

        @Override
        public boolean mayPlace(ItemStack pilha) {
            if (tipo == EquipmentSlot.MAINHAND || tipo == EquipmentSlot.OFFHAND) return true;
            return pilha.canEquip(tipo, entidade);
        }

        @Override
        public int getMaxStackSize() {
            return tipo.getType() == EquipmentSlot.Type.ARMOR ? 1 : super.getMaxStackSize();
        }
    }
}
