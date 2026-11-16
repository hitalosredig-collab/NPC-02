# Mod Funcionários (Minecraft Java 1.20.1 / Forge 47.x)

Contrate funcionários humanoides que trabalham e defendem você.

## Como colocar pra rodar

1. Baixe o **Forge MDK 1.20.1** (versão 47.x) em https://files.minecraftforge.net e extraia numa pasta.
2. Dentro do MDK, apague `src/main/java/com/example` e `src/main/resources/META-INF/mods.toml`.
3. Copie a pasta `src` deste projeto para dentro do MDK (mesclando/substituindo).
4. No `gradle.properties` do MDK, troque: `mod_id=funcionarios`, `mod_name=Funcionarios`, `mod_group_id=com.funcionarios`.
5. Abra a pasta do MDK no IntelliJ IDEA (Java 17), deixe o Gradle sincronizar e rode a configuração `runClient`.
   Para gerar o `.jar`: `gradlew build` (sai em `build/libs`).

O mod NÃO precisa do TACZ para compilar nem para rodar. A compatibilidade é opcional e feita por reflexão.

## Gerar o .jar pelo GitHub (sem instalar nada)

1. Crie um repositório no GitHub e envie o CONTEÚDO desta pasta (`src`, `.github`, `README.md`).
   A pasta `.github` é escondida em alguns sistemas: ela precisa ir junto.
2. Abra a aba **Actions** do repositório. A compilação roda sozinha (ou use **Run workflow**).
3. Quando terminar (alguns minutos), clique na execução e baixe **funcionarios-jar** em *Artifacts*.
   Dentro do zip baixado está o `.jar` para a pasta `mods`.
4. Se a compilação falhar, clique na execução, copie a mensagem de erro do passo "Compilar" e mande para ajuste.

## Como jogar

- Receita: **papel + esmeralda** (sem forma) = Contrato de Funcionário (também na aba Ferramentas do criativo).
- Clique direito com o contrato num bloco para contratar.
- Clique direito no funcionário para abrir a tela.

### Tela do funcionário

- Centro: inventário dele (27 slots, estilo baú) e o seu inventário embaixo.
- Coluna à esquerda, de cima para baixo: capacete, peitoral, calça, bota, **mão principal**, mão secundária.
  A armadura aparece no corpo dele. O que ficar na mão principal é o que ele usa.
- Direita: botões de comando e a caixinha onde ele fala.

### Comandos

| Botão | O que precisa | O que faz |
|---|---|---|
| Parar | nada | para tudo |
| Seguir você | nada | anda atrás de você |
| Me defender | espada, machado ou arma do TACZ | ataca monstros perto de você e quem te machucar |
| Pegar madeira | machado (mão ou inventário) | corta árvores num raio de 16 blocos |
| Minerar pedra | picareta | quebra pedra e minérios expostos (evita lava) |
| Colher plantação | nada (enxada é opcional) | colhe plantações maduras e replanta |
| Entregar itens | nada | vem até você e entrega tudo, menos ferramentas e armas |

Sem a ferramenta ou arma certa, ele recusa com uma fala. Se o inventário enche no meio do trabalho,
ele entrega sozinho e volta a trabalhar. Ao morrer, solta o inventário e o equipamento.

## Armas do TACZ (opcional)

Com o TACZ instalado, ponha uma arma dele no inventário (ou na mão) e munição no inventário do funcionário,
e use "Me defender". Se algo falhar, procure por `CompatArmas` no log do jogo (`logs/latest.log`) e mande para ajuste.

Essa parte foi escrita a partir da documentação pública do TACZ e ainda não foi testada com o jogo.

## Próximas etapas (ainda não feitas)

- Salário/pagamento recorrente
- Baú vinculado para entrega
- Troca com villagers
- Construção de casas
- Skins escolhidas pelo jogador
