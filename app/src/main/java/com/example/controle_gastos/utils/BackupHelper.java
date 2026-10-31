package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.BackupData;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.ChavePix;
import com.example.controle_gastos.model.Conta;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class BackupHelper {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final int VERSAO_BACKUP = 2; // ⭐ Subimos a versão por causa das novas tabelas

    // ==========================================
    // EXPORTAR
    // ==========================================

    public static File exportar(Context context, int userId) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            SessionManager session = new SessionManager(context);

            BackupData data = new BackupData();
            data.versao = VERSAO_BACKUP;
            data.dataBackup = new SimpleDateFormat("dd/MM/yyyy HH:mm", LOCALE_BR)
                    .format(Calendar.getInstance().getTime());
            data.nomeUsuario = session.getNome();
            data.dividas = db.dividaDao().listarTodasParaHistorico(userId);
            data.cartoes = db.cartaoDao().listarPorUsuario(userId);
            data.chavesPix = db.chavePixDao().listarPorUsuario(userId);
            data.categorias = db.categoriaDao().listarPorUsuario(userId);

            // ⭐ Exportando as novas tabelas
            data.contas = new ArrayList<>();
            Conta conta = db.contaDao().buscarPorUsuario(userId);
            if (conta != null) data.contas.add(conta);

            data.transacoes = db.transacaoDao().listarPorUsuario(userId);

            data.totalItens = data.contarItens();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);

            File pasta = new File(context.getCacheDir(), "exports");
            if (!pasta.exists()) pasta.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm", LOCALE_BR)
                    .format(Calendar.getInstance().getTime());
            File arquivo = new File(pasta, "backup_org_" + timestamp + ".json");

            FileOutputStream fos = new FileOutputStream(arquivo);
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            fos.close();

            return arquivo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void compartilharBackup(Context context, File arquivo) {
        if (arquivo == null || !arquivo.exists()) return;

        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                arquivo
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Backup - ORG Gestão Financeira");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Compartilhar backup via..."));
    }

    // ==========================================
    // IMPORTAR
    // ==========================================

    public static BackupData lerArquivo(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                sb.append(linha);
            }
            reader.close();
            is.close();

            Gson gson = new Gson();
            BackupData data = gson.fromJson(sb.toString(), BackupData.class);

            if (data == null) return null;
            // Agora aceita versões antigas (1) e a nova (2)
            if (data.versao <= 0 || data.versao > VERSAO_BACKUP) return null;

            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean aplicarBackup(Context context, int userId, BackupData data) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);

            // 1. Apaga dados atuais do usuário (incluindo as novas tabelas)
            db.dividaDao().deletarTodosDoUsuario(userId);
            db.cartaoDao().deletarTodosDoUsuario(userId);
            db.chavePixDao().deletarTodosDoUsuario(userId);
            db.categoriaDao().deletarTodosDoUsuario(userId);

            // ⭐ Limpando Contas e Transações
            db.contaDao().deletarPorUsuario(userId);
            db.transacaoDao().deletarTodosDoUsuario(userId);

            // 2. Reinsere categorias
            if (data.categorias != null) {
                for (Categoria c : data.categorias) {
                    c.id = 0;
                    c.usuarioId = userId;
                    db.categoriaDao().inserir(c);
                }
            }

            // 3. Reinsere cartões
            if (data.cartoes != null) {
                for (Cartao c : data.cartoes) {
                    c.id = 0;
                    c.usuarioId = userId;
                    db.cartaoDao().inserir(c);
                }
            }

            // 4. Reinsere chaves Pix
            if (data.chavesPix != null) {
                for (ChavePix p : data.chavesPix) {
                    p.id = 0;
                    p.usuarioId = userId;
                    db.chavePixDao().inserir(p);
                }
            }

            // 5. Reinsere dívidas
            if (data.dividas != null) {
                for (Divida d : data.dividas) {
                    d.id = 0;
                    d.usuarioId = userId;
                    long novoId = db.dividaDao().inserir(d);

                    if (novoId > 0 && !d.isPago() && !d.isExcluida()) {
                        d.id = (int) novoId;
                        AlarmeHelper.agendar(context, d);
                    }
                }
            }

            // ⭐ 6. Reinsere Contas (Saldo Inicial)
            if (data.contas != null && !data.contas.isEmpty()) {
                for (Conta c : data.contas) {
                    c.id = 0;
                    c.usuarioId = userId;
                    db.contaDao().inserir(c);
                }
                // Atualiza também o SharedPreferences para a tela de Configurações refletir na hora
                OnboardingStore.salvarSaldoInicial(context, userId, data.contas.get(0).saldoInicial);
            }

            // ⭐ 7. Reinsere Transações (Espião de Notificações)
            if (data.transacoes != null) {
                for (Transacao t : data.transacoes) {
                    t.id = 0;
                    t.usuarioId = userId;
                    db.transacaoDao().inserir(t);
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}