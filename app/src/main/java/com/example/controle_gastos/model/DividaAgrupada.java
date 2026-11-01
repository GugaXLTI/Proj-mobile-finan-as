package com.example.controle_gastos.model;

import java.util.ArrayList;
import java.util.List;

public class DividaAgrupada {
    public List<Divida> parcelas;

    public DividaAgrupada(List<Divida> parcelas) {
        this.parcelas = parcelas;
    }

    // Retorna a próxima parcela não paga, ou a última se todas estiverem pagas
    public Divida getProximaParcela() {
        for (Divida d : parcelas) {
            if (!d.isPago()) return d;
        }
        return parcelas.get(parcelas.size() - 1);
    }

    public boolean isTotalmentePago() {
        for (Divida d : parcelas) {
            if (!d.isPago()) return false;
        }
        return true;
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

    public double getValorTotalGrupo() {
        double total = 0;
        for (Divida d : parcelas) {
            total += d.getValorTotal();
        }
        return total;
    }

    public double getValorRestanteGrupo() {
        double restante = 0;
        for (Divida d : parcelas) {
            restante += d.getValorRestante();
        }
        return restante;
    }

    public String getTitulo() {
        return parcelas.get(0).getTitulo();
    }

    public String getCategoria() {
        return parcelas.get(0).getCategoria();
    }

    public String getBanco() {
        return parcelas.get(0).getBanco();
    }

    public int getUsuarioId() {
        return parcelas.get(0).getUsuarioId();
    }

    public int getPrimeiroId() {
        return parcelas.get(0).getId();
    }
}