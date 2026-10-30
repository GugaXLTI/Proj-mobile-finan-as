package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nome;
    public String email;
    public String senha;
    public double salarioMensal;
    public String perfilFinanceiro;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.salarioMensal = 0.0;
        this.perfilFinanceiro = "CLT"; // Padrão inicial
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }

    public double getSalarioMensal() { return salarioMensal; }
    public void setSalarioMensal(double salarioMensal) { this.salarioMensal = salarioMensal; }

    public String getPerfilFinanceiro() { return perfilFinanceiro; }
    public void setPerfilFinanceiro(String perfilFinanceiro) { this.perfilFinanceiro = perfilFinanceiro; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setSenha(String senha) { this.senha = senha; }
}