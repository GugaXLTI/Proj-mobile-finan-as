package com.example.controle_gastos.utils;

import android.content.Context;
import com.example.controle_gastos.dao.CategoriaDao;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Categoria;

public class CategoriaSeeder {

    /**
     * Cria as categorias padrão para o usuário, caso ele ainda não tenha nenhuma.
     * Deve ser chamado quando o usuário abrir a tela de Categorias pela primeira vez.
     */
    public static void popularSeVazio(Context context, int usuarioId) {
        AppDatabase db = AppDatabase.getInstance(context);
        CategoriaDao categoriaDao = db.categoriaDao();

        // Só popula se o usuário ainda não tiver nenhuma categoria
        if (categoriaDao.contarPorUsuario(usuarioId) == 0) {
            // Categorias padrão (nome + cor em hexadecimal)
            categoriaDao.inserir(new Categoria(usuarioId, "Alimentação", "#EF4444"));
            categoriaDao.inserir(new Categoria(usuarioId, "Transporte", "#10B981"));
            categoriaDao.inserir(new Categoria(usuarioId, "Saúde", "#A855F7"));
            categoriaDao.inserir(new Categoria(usuarioId, "Educação", "#3B82F6"));
            categoriaDao.inserir(new Categoria(usuarioId, "Lazer", "#EC4899"));
            categoriaDao.inserir(new Categoria(usuarioId, "Moradia", "#F59E0B"));
            categoriaDao.inserir(new Categoria(usuarioId, "Assinaturas", "#06B6D4"));
            categoriaDao.inserir(new Categoria(usuarioId, "Outros", "#64748B"));
        }
    }
}