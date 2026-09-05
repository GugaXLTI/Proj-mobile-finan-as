package com.example.controle_gastos.utils;

import com.example.controle_gastos.model.Transacao;
import java.util.ArrayList;
import java.util.List;

public class DadosMock {

    public static List<Transacao> getTransacoesIniciais() {
        List<Transacao> lista = new ArrayList<>();

        lista.add(new Transacao("1", "Supermercado", 250.50, "05/09/2026", "DESPESA", "Alimentação"));
        lista.add(new Transacao("2", "Salário", 1200.00, "01/09/2026", "RECEITA", "Renda"));
        lista.add(new Transacao("3", "Gasolina", 80.00, "03/09/2026", "DESPESA", "Transporte"));
        lista.add(new Transacao("4", "Lanche", 35.00, "04/09/2026", "DESPESA", "Alimentação"));

        return lista;
    }
}