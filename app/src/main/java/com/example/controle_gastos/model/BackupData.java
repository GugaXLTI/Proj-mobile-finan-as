package com.example.controle_gastos.model;

import java.util.List;

/**
 * POJO que agrupa todos os dados do usuário para o backup em JSON.
 */
public class BackupData {

    public int versao;              // Versão do formato do backup
    public String dataBackup;       // Ex: "10/10/2026 22:30"
    public String nomeUsuario;
    public int totalItens;          // Quantidade total (para validação)

    public List<Divida> dividas;
    public List<Cartao> cartoes;
    public List<ChavePix> chavesPix;
    public List<Categoria> categorias;

    public BackupData() { }

    /**
     * Conta total de itens para exibir resumo no diálogo de confirmação.
     */
    public int contarItens() {
        int total = 0;
        if (dividas != null) total += dividas.size();
        if (cartoes != null) total += cartoes.size();
        if (chavesPix != null) total += chavesPix.size();
        if (categorias != null) total += categorias.size();
        return total;
    }
}