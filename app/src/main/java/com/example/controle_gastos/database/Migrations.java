package com.example.controle_gastos.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dividas ADD COLUMN grupoId INTEGER NOT NULL DEFAULT 0");
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

    public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE usuarios ADD COLUMN salarioMensal REAL NOT NULL DEFAULT 0.0");
        }
    };

    // ⭐ CORREÇÃO (T148): Removido o NOT NULL DEFAULT para bater com o Java
    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // O Room espera que a String no Java seja nullable, então não usamos NOT NULL aqui
            database.execSQL("ALTER TABLE usuarios ADD COLUMN perfilFinanceiro TEXT");

            // Cria a tabela de rendas
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `rendas` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`usuarioId` INTEGER NOT NULL, " +
                            "`descricao` TEXT, " +
                            "`valor` REAL NOT NULL, " +
                            "`diaRecebimento` INTEGER NOT NULL, " +
                            "`tipo` TEXT)"
            );
        }
    };
}