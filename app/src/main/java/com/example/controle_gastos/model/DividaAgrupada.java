package com.example.controle_gastos.model;

import java.util.Calendar;
import java.util.List;

public class DividaAgrupada {
    public List<Divida> parcelas;

    public DividaAgrupada(List<Divida> parcelas) {
        this.parcelas = parcelas;
    }

    public String getTitulo() {
        return parcelas.isEmpty() ? "" : parcelas.get(0).getTitulo();
    }

    public double getValorTotalGrupo() {
        Divida proxima = getProximaParcela();
        return proxima != null ? proxima.getValorTotal() : 0.0;
    }

    public double getValorRestanteGrupo() {
        Divida proxima = getProximaParcela();
        return proxima != null ? proxima.getValorRestante() : 0.0;
    }

    // A próxima parcela em aberto (usado para cálculos gerais)
    public Divida getProximaParcela() {
        for (Divida d : parcelas) {
            if (!d.isPago()) return d;
        }
        return parcelas.isEmpty() ? null : parcelas.get(parcelas.size() - 1);
    }

    // ⭐ NOVO MÉTODO: Retorna a parcela que deve ser exibida no card da lista
    public Divida getParcelaParaLista() {
        Calendar hoje = Calendar.getInstance();
        int mesAtual = hoje.get(Calendar.MONTH) + 1;
        int anoAtual = hoje.get(Calendar.YEAR);

        Divida parcelaDoMes = null;

        for (Divida d : parcelas) {
            // 1. Prioridade: Se estiver atrasada e não paga
            if (!d.isPago() && ehAtrasada(d.getVencimento(), mesAtual, anoAtual)) {
                return d; // Retorna a primeira atrasada que encontrar
            }
            // 2. Segunda Prioridade: Se for do mês atual (paga ou não)
            if (pertenceAoMes(d.getVencimento(), mesAtual, anoAtual)) {
                parcelaDoMes = d;
            }
        }

        // Se achou alguma parcela do mês atual, mostra ela
        if (parcelaDoMes != null) return parcelaDoMes;

        // Se não, mostra a próxima parcela em aberto (meses futuros)
        return getProximaParcela();
    }

    public boolean isTotalmentePago() {
        for (Divida d : parcelas) {
            if (!d.isPago()) return false;
        }
        return true;
    }

    public int getPrimeiroId() {
        return parcelas.isEmpty() ? -1 : parcelas.get(0).getId();
    }

    public int getParcelasPagas() {
        int pagas = 0;
        for (Divida d : parcelas) {
            if (d.isPago()) pagas++;
        }
        return pagas;
    }

    public int getTotalParcelas() {
        return parcelas.size();
    }

    private boolean ehAtrasada(String vencimento, int mesAtual, int anoAtual) {
        try {
            String[] partes = vencimento.split("/");
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);
            return (ano < anoAtual) || (ano == anoAtual && mes < mesAtual);
        } catch (Exception e) { return false; }
    }

    private boolean pertenceAoMes(String vencimento, int mes, int ano) {
        try {
            String[] partes = vencimento.split("/");
            return Integer.parseInt(partes[1]) == mes && Integer.parseInt(partes[2]) == ano;
        } catch (Exception e) { return false; }
    }
}