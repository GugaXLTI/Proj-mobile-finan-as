package com.example.controle_gastos.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.controle_gastos.dao.CategoriaDao;
import com.example.controle_gastos.dao.ComprovanteProcessadoDao;
import com.example.controle_gastos.dao.DividaDao;
import com.example.controle_gastos.dao.RendaDao;
import com.example.controle_gastos.dao.TransacaoDao;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.dao.CartaoDao;
import com.example.controle_gastos.dao.ChavePixDao;

import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.ComprovanteProcessado;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Renda;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.ChavePix;

@Database(
        entities = {
                Usuario.class, Divida.class, Transacao.class, Categoria.class,
                Cartao.class, ChavePix.class, ComprovanteProcessado.class, Renda.class
        },
        version = 12,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract DividaDao dividaDao();
    public abstract TransacaoDao transacaoDao();
    public abstract CategoriaDao categoriaDao();
    public abstract CartaoDao cartaoDao();
    public abstract ChavePixDao chavePixDao();
    public abstract ComprovanteProcessadoDao comprovanteProcessadoDao();
    public abstract RendaDao rendaDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "controle_gastos_db"
                    )
                    .addMigrations(
                            Migrations.MIGRATION_8_9,
                            Migrations.MIGRATION_9_10,
                            Migrations.MIGRATION_10_11,
                            Migrations.MIGRATION_11_12
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}