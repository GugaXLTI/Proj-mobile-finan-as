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

    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE usuarios ADD COLUMN perfilFinanceiro TEXT");
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

    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Cria a tabela de contas (Saldo Inicial)
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `contas` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`usuarioId` INTEGER NOT NULL, " +
                            "`nomeBanco` TEXT, " +
                            "`saldoInicial` REAL NOT NULL)"
            );

            // Garante que a tabela de transações existe (caso não tenha sido criada em migration anterior)
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `transacoes` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`usuarioId` INTEGER NOT NULL, " +
                            "`descricao` TEXT, " +
                            "`valor` REAL NOT NULL, " +
                            "`data` TEXT, " +
                            "`tipo` TEXT, " +
                            "`categoria` TEXT)"
            );
        }
    };
}