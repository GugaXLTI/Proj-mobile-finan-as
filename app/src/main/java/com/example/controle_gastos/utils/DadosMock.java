package com.example.controle_gastos.utils;

import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;

import java.util.ArrayList;
import java.util.List;

public class DadosMock {

    // ==================== TRANSAÇÕES ====================
    public static List<Transacao> getTransacoesIniciais() {
        List<Transacao> lista = new ArrayList<>();
        lista.add(new Transacao("Supermercado", 250.50, "05/09/2026", "DESPESA", "Alimentação"));
        lista.add(new Transacao("Salário", 1200.00, "01/09/2026", "RECEITA", "Renda"));
        lista.add(new Transacao("Gasolina", 80.00, "03/09/2026", "DESPESA", "Transporte"));
        lista.add(new Transacao("Lanche", 35.00, "04/09/2026", "DESPESA", "Alimentação"));
        return lista;
    }

    // ==================== DÍVIDAS ====================
    private static List<Divida> dividas = null;

    public static List<Divida> getDividasIniciais() {
        if (dividas == null) {
            dividas = new ArrayList<>();
            dividas.add(new Divida("Fatura Nubank Roxinho", 2450.00, 0.00,
                    "Nubank Principal", "Cartão de Crédito", "1/1", "15/10/2026", false));
            dividas.add(new Divida("Fatura Banco Inter Gold", 939.90, 0.00,
                    "Inter Gold", "Transporte e Peças", "2/3", "20/10/2026", false));
            dividas.add(new Divida("Spotify Premium Família", 34.90, 0.00,
                    "Nubank Principal", "Assinatura", "Mensal", "10/10/2026", false));
            dividas.add(new Divida("IPTU 2026", 1200.00, 500.00,
                    "Prefeitura", "Imposto", "1/1", "30/11/2026", false));
        }
        return dividas;
    }

    public static void adicionarDivida(Divida divida) {
        if (dividas == null) {
            getDividasIniciais();
        }
        dividas.add(divida);
    }

    public static void removerDivida(int position) {
        if (dividas != null && position >= 0 && position < dividas.size()) {
            dividas.remove(position);
        }
    }

    public static void adicionarTransacao(Transacao transacao) {
        // Agora as transações são salvas no Room; este método fica como fallback
    }
}