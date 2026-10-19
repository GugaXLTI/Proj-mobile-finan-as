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
     */
    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE dividas ADD COLUMN grupoId INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

    /**
     * Migração v9 → v10
     * Cria a tabela 'comprovantes_processados' (cache de OCR).
     *
     * Índice único em (usuarioId, uri) para permitir REPLACE e
     * isolamento por usuário.
     */
    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `comprovantes_processados` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`usuarioId` INTEGER NOT NULL, " +
                            "`uri` TEXT, " +
                            "`textoOcr` TEXT, " +
                            "`mimeType` TEXT, " +
                            "`timestamp` INTEGER NOT NULL)"
            );
            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                            "`index_comprovantes_processados_usuarioId_uri` " +
                            "ON `comprovantes_processados` (`usuarioId`, `uri`)"
            );
        }
    };
}