package com.example.controle_gastos.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.controle_gastos.dao.CategoriaDao;
import com.example.controle_gastos.dao.DividaDao;
import com.example.controle_gastos.dao.TransacaoDao;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.model.Usuario;

@Database(
        entities = {Usuario.class, Divida.class, Transacao.class, Categoria.class},
        version = 3, // ⭐ MUDOU DE 2 PARA 3
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract DividaDao dividaDao();
    public abstract TransacaoDao transacaoDao();
    public abstract CategoriaDao categoriaDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "controle_gastos_db"
                    )
                    .fallbackToDestructiveMigration() // Apaga e recria o banco na migração (OK para MVP)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}