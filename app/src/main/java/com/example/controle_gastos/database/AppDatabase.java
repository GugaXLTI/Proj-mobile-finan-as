package com.example.controle_gastos.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.controle_gastos.dao.CategoriaDao;
import com.example.controle_gastos.dao.DividaDao;
import com.example.controle_gastos.dao.TransacaoDao;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.dao.CartaoDao;      // ⭐ NOVO
import com.example.controle_gastos.dao.ChavePixDao;   // ⭐ NOVO
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.model.Cartao;      // ⭐ NOVO
import com.example.controle_gastos.model.ChavePix;    // ⭐ NOVO

@Database(
        entities = {
                Usuario.class, Divida.class, Transacao.class, Categoria.class,
                Cartao.class, ChavePix.class
        },
        version = 7, // ⭐ MUDOU DE 6 PARA 7 (campo tipo em Cartao)
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract DividaDao dividaDao();
    public abstract TransacaoDao transacaoDao();
    public abstract CategoriaDao categoriaDao();
    public abstract CartaoDao cartaoDao();      // ⭐ NOVO
    public abstract ChavePixDao chavePixDao();  // ⭐ NOVO

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