package com.example.controle_gastos.model;

import java.util.List;

public class BackupData {

    public int versao;
    public String dataBackup;
    public String nomeUsuario;
    public int totalItens;

    public List<Divida> dividas;
    public List<Cartao> cartoes;
    public List<ChavePix> chavesPix;
    public List<Categoria> categorias;

    // ⭐ NOVOS CAMPOS
    public List<Conta> contas;
    public List<Transacao> transacoes;

    public BackupData() { }

    public int contarItens() {
        int total = 0;
        if (dividas != null) total += dividas.size();
        if (cartoes != null) total += cartoes.size();
        if (chavesPix != null) total += chavesPix.size();
        if (categorias != null) total += categorias.size();
        if (contas != null) total += contas.size();
        if (transacoes != null) total += transacoes.size();
        return total;
    }
}