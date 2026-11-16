package com.funcionarios;

public enum Tarefa {
    PARAR("Parar"),
    SEGUIR("Seguir você"),
    DEFENDER("Me defender"),
    MADEIRA("Pegar madeira"),
    PEDRA("Minerar pedra"),
    COLHER("Colher plantação"),
    ENTREGAR("Entregar itens");

    private final String rotulo;

    Tarefa(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean ehTrabalho() {
        return this == MADEIRA || this == PEDRA || this == COLHER;
    }

    public static Tarefa de(int indice) {
        Tarefa[] v = values();
        return (indice >= 0 && indice < v.length) ? v[indice] : PARAR;
    }
}
