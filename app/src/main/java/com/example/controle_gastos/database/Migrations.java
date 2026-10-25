package com.example.controle_gastos.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE dividas ADD COLUMN grupoId INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

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

    // ⭐ NOVO (T125): Migration para adicionar o campo de salário na tabela usuarios
    public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN salarioMensal REAL NOT NULL DEFAULT 0.0"
            );
        }
    };
}