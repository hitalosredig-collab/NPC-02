package com.funcionarios;

import net.minecraft.util.RandomSource;

/**
 * Falas dos funcionários. Cada situação tem 3 conjuntos de frases:
 * 0 = normal, 1 = rabugento, 2 = animado.
 * Para adicionar frases, basta incluir mais textos nas listas abaixo.
 */
public final class Falas {
    public enum Situacao {
        CONTRATADO, ACEITO, PARADO, SEGUINDO, SEM_MACHADO, SEM_PICARETA, INV_CHEIO,
        NADA_ACHADO, FERRAMENTA_QUEBROU, ENTREGUEI, NAO_E_PATRAO, LONGE,
        DEFENDENDO, SEM_ARMA, SEM_MUNICAO
    }

    private static final String[][] T_CONTRATADO = {
            {"Prazer, patrão! É só me mandar trabalhar.", "Contratado! Onde eu começo?"},
            {"Mais um emprego... tá, tá. O que é pra fazer?", "Espero que o pagamento valha a pena."},
            {"Oba, emprego novo! Pode mandar, chefe!", "Tô muito feliz de trabalhar pra você!"}
    };
    private static final String[][] T_ACEITO = {
            {"Pode deixar, patrão!", "Tô indo nessa.", "Beleza, já começo."},
            {"Tá bom, tá bom... eu vou.", "Mais trabalho, né? Vamos lá.", "Hmpf. Já vou."},
            {"Oba, serviço novo! Bora!", "Deixa comigo, chefe!", "Já tô até suando de alegria!"}
    };
    private static final String[][] T_PARADO = {
            {"Parei. Fico aqui esperando.", "Certo, descansando um pouco."},
            {"Finalmente uma folga.", "Parei. Já não era sem tempo."},
            {"Parei! Mas é só dar o sinal que eu volto!", "Pausa pro café!"}
    };
    private static final String[][] T_SEGUINDO = {
            {"Tô logo atrás de você.", "Pode ir que eu te sigo."},
            {"Anda logo, eu te sigo...", "Já tô indo, não precisa correr."},
            {"Te sigo até o fim do mundo, chefe!", "Vamos nessa aventura!"}
    };
    private static final String[][] T_SEM_MACHADO = {
            {"Patrão, sem machado eu não consigo pegar madeira.", "Me dá um machado que eu resolvo isso."},
            {"Quer que eu derrube árvore na mão? Me dá um machado!", "Sem machado? Tá de brincadeira."},
            {"Eita, cadê meu machado? Coloca um no meu inventário!", "Sem machado não rola, chefe!"}
    };
    private static final String[][] T_SEM_PICARETA = {
            {"Patrão, sem picareta eu não consigo quebrar pedra.", "Me dá uma picareta e eu começo."},
            {"Quebrar pedra com a mão? Me dá uma picareta, ué.", "Sem picareta eu não faço milagre."},
            {"Ih, esqueci a picareta! Põe uma no meu inventário, chefe!", "Sem picareta fica difícil!"}
    };
    private static final String[][] T_INV_CHEIO = {
            {"Meu inventário tá cheio, patrão.", "Preciso entregar o que eu tenho antes."},
            {"Já tô carregando o mundo nas costas! Esvazia isso aí.", "Cheio. Não cabe mais nada."},
            {"Tô lotado de coisa, chefe!", "Inventário cheio! Vou te entregar tudo!"}
    };
    private static final String[][] T_NADA_ACHADO = {
            {"Não achei mais nada por aqui.", "Acabou o serviço nessa área."},
            {"Não tem mais nada. Pode parar de me mandar aqui.", "Nada. Zero. Procura outro lugar."},
            {"Nada por aqui! Me leva pra outro lugar, chefe!", "Acabou tudo! Que rápido que fui!"}
    };
    private static final String[][] T_FERRAMENTA_QUEBROU = {
            {"Minha ferramenta quebrou, patrão!", "Preciso de outra ferramenta."},
            {"Quebrou! Eu avisei que essa coisa era velha.", "Sem ferramenta, sem serviço."},
            {"Ops! Quebrei a ferramenta! Me dá outra, chefe!", "Foi-se a ferramenta... mas eu continuo animado!"}
    };
    private static final String[][] T_ENTREGUEI = {
            {"Entreguei tudo, patrão.", "Aqui está o que eu juntei."},
            {"Toma. Deu trabalho, viu?", "Entregue. Quero ver o pagamento."},
            {"Entrega feita! Tem mais serviço?", "Tudo na sua mão, chefe!"}
    };
    private static final String[][] T_NAO_E_PATRAO = {
            {"Você não é meu patrão.", "Eu trabalho pra outra pessoa."},
            {"Sai pra lá, eu não trabalho pra você.", "Meu patrão é outro."},
            {"Oi! Mas eu já tenho patrão, desculpa!", "Já tenho contrato com outra pessoa!"}
    };
    private static final String[][] T_LONGE = {
            {"Cadê você, patrão? Não te encontro.", "Não consigo te achar pra entregar."},
            {"Sumiu, é? Assim não dá pra entregar nada.", "Cadê o patrão?"},
            {"Chefe, cadê você? Quero te entregar tudo!", "Não te vejo por aqui!"}
    };

    private static final String[][] T_DEFENDENDO = {
            {"Pode deixar, fico de olho nos monstros!", "Eu protejo você, patrão."},
            {"Agora virei segurança também? Tá bom...", "Tô de olho. Mas quero um adicional."},
            {"Aaah, vou proteger o chefe! Que monstro vem?", "Ninguém encosta em você, chefe!"}
    };
    private static final String[][] T_SEM_ARMA = {
            {"Patrão, sem espada ou arma eu não consigo te defender.", "Me dá uma arma que eu te protejo."},
            {"Defender com as mãos nuas? Me dá uma arma!", "Sem arma não rola."},
            {"Cadê minha arma, chefe? Põe uma no meu inventário!", "Sem arma, só posso torcer por você!"}
    };
    private static final String[][] T_SEM_MUNICAO = {
            {"Acabou a munição, patrão!", "Sem munição pra essa arma."},
            {"Sem bala. Quer que eu jogue pedra?", "Munição acabou, de novo."},
            {"Eita, acabaram as balas, chefe!", "Sem munição! Mas continuo na luta!"}
    };

    private Falas() {}

    public static String get(Situacao situacao, int personalidade, RandomSource aleatorio) {
        String[][] tabela = switch (situacao) {
            case CONTRATADO -> T_CONTRATADO;
            case ACEITO -> T_ACEITO;
            case PARADO -> T_PARADO;
            case SEGUINDO -> T_SEGUINDO;
            case SEM_MACHADO -> T_SEM_MACHADO;
            case SEM_PICARETA -> T_SEM_PICARETA;
            case INV_CHEIO -> T_INV_CHEIO;
            case NADA_ACHADO -> T_NADA_ACHADO;
            case FERRAMENTA_QUEBROU -> T_FERRAMENTA_QUEBROU;
            case ENTREGUEI -> T_ENTREGUEI;
            case NAO_E_PATRAO -> T_NAO_E_PATRAO;
            case LONGE -> T_LONGE;
            case DEFENDENDO -> T_DEFENDENDO;
            case SEM_ARMA -> T_SEM_ARMA;
            case SEM_MUNICAO -> T_SEM_MUNICAO;
        };
        String[] frases = tabela[Math.floorMod(personalidade, 3)];
        return frases[aleatorio.nextInt(frases.length)];
    }
}
