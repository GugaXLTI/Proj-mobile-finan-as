package com.example.controle_gastos.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.controle_gastos.dao.CategoriaDao;
import com.example.controle_gastos.dao.DividaDao;
import com.example.controle_gastos.dao.TransacaoDao;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.dao.CartaoDao;
import com.example.controle_gastos.dao.ChavePixDao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.ChavePix;

@Database(
        entities = {
                Usuario.class, Divida.class, Transacao.class, Categoria.class,
                Cartao.class, ChavePix.class
        },
        version = 9, // ⭐ MUDOU DE 8 PARA 9 (campo grupoId em Divida)
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract DividaDao dividaDao();
    public abstract TransacaoDao transacaoDao();
    public abstract CategoriaDao categoriaDao();
    public abstract CartaoDao cartaoDao();
    public abstract ChavePixDao chavePixDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "controle_gastos_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}