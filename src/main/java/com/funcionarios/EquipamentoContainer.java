package com.funcionarios;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Container "de mentira" que expõe os slots de equipamento do funcionário (armadura e mãos)
 * para a tela. Os itens ficam de verdade nos slots de equipamento da entidade.
 */
public class EquipamentoContainer implements Container {
    /** Ordem na tela: capacete, peitoral, calça, bota, mão principal, mão secundária. */
    public static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    private final FuncionarioEntity entidade;

    public EquipamentoContainer(FuncionarioEntity entidade) {
        this.entidade = entidade;
    }

    @Override
    public int getContainerSize() {
        return SLOTS.length;
    }

    @Override
    public boolean isEmpty() {
        for (EquipmentSlot s : SLOTS) {
            if (!entidade.getItemBySlot(s).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int indice) {
        return entidade.getItemBySlot(SLOTS[indice]);
    }

    @Override
    public ItemStack removeItem(int indice, int quantidade) {
        ItemStack atual = getItem(indice);
        if (atual.isEmpty() || quantidade <= 0) return ItemStack.EMPTY;
        ItemStack retirado = atual.split(quantidade);
        if (atual.isEmpty()) {
            entidade.setItemSlot(SLOTS[indice], ItemStack.EMPTY);
        }
        return retirado;
    }

    @Override
    public ItemStack removeItemNoUpdate(int indice) {
        ItemStack atual = getItem(indice);
        entidade.setItemSlot(SLOTS[indice], ItemStack.EMPTY);
        return atual;
    }

    @Override
    public void setItem(int indice, ItemStack pilha) {
        entidade.setItemSlot(SLOTS[indice], pilha);
    }

    @Override
    public void setChanged() { }

    @Override
    public boolean stillValid(Player jogador) {
        return entidade.isAlive();
    }

    @Override
    public void clearContent() {
        for (EquipmentSlot s : SLOTS) {
            entidade.setItemSlot(s, ItemStack.EMPTY);
        }
    }
}
