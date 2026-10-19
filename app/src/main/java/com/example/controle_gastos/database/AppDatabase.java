package com.example.controle_gastos.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.controle_gastos.dao.CategoriaDao;
import com.example.controle_gastos.dao.ComprovanteProcessadoDao;
import com.example.controle_gastos.dao.DividaDao;
import com.example.controle_gastos.dao.TransacaoDao;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.dao.CartaoDao;
import com.example.controle_gastos.dao.ChavePixDao;

import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.ComprovanteProcessado;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.ChavePix;

@Database(
        entities = {
                Usuario.class, Divida.class, Transacao.class, Categoria.class,
                Cartao.class, ChavePix.class, ComprovanteProcessado.class
        },
        version = 10, // ⭐ v10: cache de OCR (comprovantes_processados)
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

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "controle_gastos_db"
                    )
                    // ⭐ Migrations reais (não destrutivas)
                    .addMigrations(
                            Migrations.MIGRATION_8_9,
                            Migrations.MIGRATION_9_10
                    )
                    // ⚠️ Rede de segurança: se não houver migration para
                    // a versão do usuário, apaga e recria.
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}