package com.example.controle_gastos.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Classe que agrupa TODAS as migrations do banco.
 *
 * ⭐ Como usar: ao subir a versão do AppDatabase (ex: v9 → v10),
 *    crie uma nova Migration aqui e registre no AppDatabase.
 *
 * Padrão de nomenclatura: MIGRATION_X_Y
 * onde X = versão de origem e Y = versão de destino.
 */
public class Migrations {

    /**
     * Migração v8 → v9
     * Adiciona o campo 'grupoId' (parcelamento por mês) na tabela 'dividas'.
     *
     * Contexto: antes, uma compra parcelada gerava 1 registro único.
     * Agora, gera N registros (um por parcela), agrupados por grupoId.
     * Registros antigos ficam com grupoId = 0 (sem grupo).
     */
    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE dividas ADD COLUMN grupoId INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

    // ============================================================
    // ⭐ PRÓXIMAS MIGRATIONS
    // Adicione aqui quando subir a versão do AppDatabase.
    //
    // Exemplo para v9 → v10 (quando criar a próxima feature):
    //
    // public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
    //     @Override
    //     public void migrate(SupportSQLiteDatabase database) {
    //         database.execSQL("ALTER TABLE dividas ADD COLUMN novoCampo TEXT");
    //     }
    // };
    // ============================================================
}