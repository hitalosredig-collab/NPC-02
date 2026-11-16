package com.funcionarios;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class FuncionarioScreen extends AbstractContainerScreen<FuncionarioMenu> {
    private static final ResourceLocation FUNDO = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int LARGURA_PAINEL = 112;
    private static final int ALTURA_BOTAO = 18;
    private static final int ESPACO_BOTAO = 19;

    public FuncionarioScreen(FuncionarioMenu menu, Inventory inventario, Component titulo) {
        super(menu, inventario, titulo);
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos + this.imageWidth + 6;
        int y = this.topPos;
        int i = 0;
        for (Tarefa tarefa : Tarefa.values()) {
            this.addRenderableWidget(Button.builder(Component.literal(tarefa.getRotulo()),
                            botao -> Rede.CANAL.sendToServer(
                                    new PacoteComando(this.menu.getFuncionario().getId(), tarefa.ordinal())))
                    .bounds(x, y + i * ESPACO_BOTAO, LARGURA_PAINEL, ALTURA_BOTAO)
                    .build());
            i++;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, parcial);
        desenharFala(g);
        this.renderTooltip(g, mouseX, mouseY);
    }

    private void desenharFala(GuiGraphics g) {
        FuncionarioEntity f = this.menu.getFuncionario();
        int x = this.leftPos + this.imageWidth + 6;
        int y = this.topPos + Tarefa.values().length * ESPACO_BOTAO + 4;

        g.fill(x, y, x + LARGURA_PAINEL, y + 68, 0xCC000000);
        g.drawString(this.font, f.getName(), x + 4, y + 4, 0xFFD700, false);
        g.drawString(this.font, Component.literal("Tarefa: " + f.getTarefa().getRotulo()), x + 4, y + 15, 0xAAAAAA, false);

        String fala = f.getFala();
        if (fala != null && !fala.isEmpty()) {
            int linhaY = y + 29;
            for (FormattedCharSequence linha : this.font.split(Component.literal(fala), LARGURA_PAINEL - 8)) {
                g.drawString(this.font, linha, x + 4, linhaY, 0xFFFFFF, false);
                linhaY += 10;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float parcial, int mouseX, int mouseY) {
        int linhas = 3;
        g.blit(FUNDO, this.leftPos, this.topPos, 0, 0, this.imageWidth, linhas * 18 + 17);
        g.blit(FUNDO, this.leftPos, this.topPos + linhas * 18 + 17, 0, 126, this.imageWidth, 96);
        desenharPainelEquipamento(g);
    }

    /** Coluna à esquerda: capacete, peitoral, calça, bota, mão principal e mão secundária. */
    private void desenharPainelEquipamento(GuiGraphics g) {
        int px = this.leftPos - 27;
        int py = this.topPos + 14;
        g.fill(px - 1, py - 1, px + 27, py + 123, 0xFF000000);
        g.fill(px, py, px + 26, py + 122, 0xFFC6C6C6);
        for (int y : FuncionarioMenu.EQUIP_Y) {
            int sx = this.leftPos + FuncionarioMenu.EQUIP_X;
            int sy = this.topPos + y;
            g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
            g.fill(sx, sy, sx + 17, sy + 17, 0xFFFFFFFF);
            g.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
        }
    }
}
