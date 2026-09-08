package com.example.controle_gastos.utils;

import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import java.util.ArrayList;
import java.util.List;

public class DadosMock {

    // ==================== TRANSAÇÕES ====================
    public static List<Transacao> getTransacoesIniciais() {
        List<Transacao> lista = new ArrayList<>();
        lista.add(new Transacao("1", "Supermercado", 250.50, "05/09/2026", "DESPESA", "Alimentação"));
        lista.add(new Transacao("2", "Salário", 1200.00, "01/09/2026", "RECEITA", "Renda"));
        lista.add(new Transacao("3", "Gasolina", 80.00, "03/09/2026", "DESPESA", "Transporte"));
        lista.add(new Transacao("4", "Lanche", 35.00, "04/09/2026", "DESPESA", "Alimentação"));
        return lista;
    }

    // ==================== DÍVIDAS ====================
    // Lista estática compartilhada entre todas as Activities
    private static List<Divida> dividas = null;

    public static List<Divida> getDividasIniciais() {
        if (dividas == null) {
            dividas = new ArrayList<>();
            dividas.add(new Divida(1, "Fatura Nubank Roxinho", 2450.00, 0.00,
                    "Nubank Principal", "Cartão de Crédito", "1/1", "15/10/2026", false));
            dividas.add(new Divida(2, "Fatura Banco Inter Gold", 939.90, 0.00,
                    "Inter Gold", "Transporte e Peças", "2/3", "20/10/2026", false));
            dividas.add(new Divida(3, "Spotify Premium Família", 34.90, 0.00,
                    "Nubank Principal", "Assinatura", "Mensal", "10/10/2026", false));
            dividas.add(new Divida(4, "IPTU 2026", 1200.00, 500.00,
                    "Prefeitura", "Imposto", "1/1", "30/11/2026", false));
        }
        return dividas;
    }

    // Adiciona uma nova dívida à lista compartilhada
    public static void adicionarDivida(Divida divida) {
        if (dividas == null) {
            getDividasIniciais(); // Inicializa se necessário
        }
        dividas.add(divida);
    }

    // Remove uma dívida da lista compartilhada por posição
    public static void removerDivida(int position) {
        if (dividas != null && position >= 0 && position < dividas.size()) {
            dividas.remove(position);
        }
    }
}