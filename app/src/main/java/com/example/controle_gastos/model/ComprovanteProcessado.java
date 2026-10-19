package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Cache local do texto OCR extraído de um comprovante.
 *
 * Serve para evitar reprocessar a mesma imagem/PDF duas vezes.
 * Não guarda o bitmap — só o texto bruto, que é o resultado caro do OCR.
 *
 * A chave de busca é o par (usuarioId, uri). Se o usuário compartilhar
 * o mesmo arquivo duas vezes, a segunda vez é instantânea.
 */
@Entity(
        tableName = "comprovantes_processados",
        indices = {@Index(value = {"usuarioId", "uri"}, unique = true)}
)
public class ComprovanteProcessado {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId;
    public String uri;
    public String textoOcr;
    public String mimeType;
    public long timestamp;

    /**
     * Construtor vazio marcado com @Ignore — o Room NÃO usa ele.
     * Usado apenas se algum dia precisarmos criar via reflexão ou Gson.
            */
    @Ignore
    public ComprovanteProcessado() { }

    /**
     * Construtor principal — este é o que o Room usa.
     */
    public ComprovanteProcessado(int usuarioId, String uri, String textoOcr, String mimeType) {
        this.usuarioId = usuarioId;
        this.uri = uri;
        this.textoOcr = textoOcr;
        this.mimeType = mimeType;
        this.timestamp = System.currentTimeMillis();
    }
}