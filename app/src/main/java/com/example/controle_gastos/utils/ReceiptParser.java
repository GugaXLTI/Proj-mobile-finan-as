package com.example.controle_gastos.utils;

import android.util.Log;

import com.example.controle_gastos.model.ParsedReceipt;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    private static final String TAG = "ReceiptParser";

    // Regex para capturar valores com R$
    private static final Pattern PATTERN_VALOR =
            Pattern.compile("R\\$\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");

    // Regex para datas com mês em texto (ex: 18 OUT 2026)
    private static final Pattern PATTERN_DATA_TEXTO =
            Pattern.compile("(\\d{2})\\s+([A-Za-z]{3})\\s+(\\d{4})");

    // ⭐ NOVO: Regex para datas numéricas (ex: 18/10/2026 ou 18/10/26)
    private static final Pattern PATTERN_DATA_NUMERICA =
            Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4}|\\d{2})");

    private static final Map<String, String> MESES = new HashMap<>();
    static {
        MESES.put("JAN", "01"); MESES.put("FEV", "02"); MESES.put("FEB", "02");
        MESES.put("MAR", "03"); MESES.put("ABR", "04"); MESES.put("APR", "04");
        MESES.put("MAI", "05"); MESES.put("MAY", "05"); MESES.put("JUN", "06");
        MESES.put("JUL", "07"); MESES.put("AGO", "08"); MESES.put("AUG", "08");
        MESES.put("SET", "09"); MESES.put("SEP", "09"); MESES.put("OUT", "10");
        MESES.put("OCT", "10"); MESES.put("NOV", "11"); MESES.put("DEZ", "12");
        MESES.put("DEC", "12");
    }

    // ===== BANCOS AMPLIADOS =====
    private static final String[][] BANCOS = {
            // Nubank
            {"NU PAGAMENTOS", "Nubank"},
            {"NUBANK", "Nubank"},
            // Inter
            {"BANCO INTER", "Inter"},
            {"BCO INTER", "Inter"},
            // Bradesco
            {"BANCO BRADESCO", "Bradesco"},
            {"BCO BRADESCO", "Bradesco"},
            {"BRADESCO", "Bradesco"},
            // Itaú
            {"ITAU UNIBANCO", "Itaú"},
            {"BANCO ITAU", "Itaú"},
            {"BCO ITAU", "Itaú"},
            {"ITAU", "Itaú"},
            // Santander
            {"BANCO SANTANDER", "Santander"},
            {"BCO SANTANDER", "Santander"},
            {"SANTANDER", "Santander"},
            // Caixa
            {"CAIXA ECONOMICA", "Caixa"},
            {"CAIXA FEDERAL", "Caixa"},
            {"CEF", "Caixa"},
            // Banco do Brasil
            {"BANCO DO BRASIL", "Banco do Brasil"},
            {"BCO DO BRASIL", "Banco do Brasil"},
            // Outros digitais
            {"BANCO DIGIO", "Digio"},
            {"DIGIO", "Digio"},
            {"BANCO C6", "C6 Bank"},
            {"C6 BANK", "C6 Bank"},
            {"BANCO PAN", "Pan"},
            {"BANCO ORIGINAL", "Original"},
            {"BANCO NEON", "Neon"},
            {"NEON PAGAMENTOS", "Neon"},
            {"BANCO NEXT", "Next"},
            {"NEXT PAGAMENTOS", "Next"},
            {"N26", "N26"},
            {"WISE", "Wise"},
            {"REVOLUT", "Revolut"},
            {"PICPAY", "PicPay"},
            {"PAGSEGURO", "PagSeguro"},
            {"MERCADO PAGO", "Mercado Pago"},
            {"BTG PACTUAL", "BTG Pactual"},
            {"BANCO SAFRA", "Safra"},
            {"BANCO SICREDI", "Sicredi"},
            {"SICOOB", "Sicoob"},
            {"BANCO HSBC", "HSBC"},
            {"CITIBANK", "Citibank"},
            {"BANCO BMG", "BMG"},
            {"BANCO VOTORANTIM", "Votorantim"},
            {"BANCO DAYCOVAL", "Daycoval"},
    };

    // ===== CATEGORIAS AMPLIADAS =====
    private static final String[][] CATEGORIAS = {
            {"uber", "Transporte"}, {"99", "Transporte"}, {"taxi", "Transporte"},
            {"cabify", "Transporte"}, {"blablacar", "Transporte"}, {"posto", "Transporte"},
            {"auto posto", "Transporte"}, {"combustivel", "Transporte"}, {"gasolina", "Transporte"},
            {"alcool", "Transporte"}, {"etanol", "Transporte"}, {"diesel", "Transporte"},
            {"estacionamento", "Transporte"}, {"pedagio", "Transporte"}, {"metro", "Transporte"},
            {"onibus", "Transporte"}, {"passagem", "Transporte"},
            {"ifood", "Alimentação"}, {"rappi", "Alimentação"}, {"restaurante", "Alimentação"},
            {"lanche", "Alimentação"}, {"lanchonete", "Alimentação"}, {"pizza", "Alimentação"},
            {"pizzaria", "Alimentação"}, {"hamburguer", "Alimentação"}, {"hamburgueria", "Alimentação"},
            {"burger", "Alimentação"}, {"mcdonalds", "Alimentação"}, {"mc donalds", "Alimentação"},
            {"habibs", "Alimentação"}, {"giraffas", "Alimentação"}, {"outback", "Alimentação"},
            {"churrascaria", "Alimentação"}, {"bar ", "Alimentação"}, {"mercado", "Alimentação"},
            {"supermercado", "Alimentação"}, {"mercadinho", "Alimentação"}, {"hortifruti", "Alimentação"},
            {"acougue", "Alimentação"}, {"padaria", "Alimentação"}, {"panificadora", "Alimentação"},
            {"cafe", "Alimentação"}, {"cafeteria", "Alimentação"}, {"acai", "Alimentação"},
            {"sorvete", "Alimentação"}, {"sorveteria", "Alimentação"}, {"doceria", "Alimentação"},
            {"confeitaria", "Alimentação"}, {"pao de acucar", "Alimentação"}, {"carrefour", "Alimentação"},
            {"assai", "Alimentação"}, {"atacadao", "Alimentação"}, {"extra ", "Alimentação"},
            {"farmacia", "Saúde"}, {"drogaria", "Saúde"}, {"hospital", "Saúde"},
            {"clinica", "Saúde"}, {"laboratorio", "Saúde"}, {"exame", "Saúde"},
            {"dentista", "Saúde"}, {"odontologia", "Saúde"}, {"psicologo", "Saúde"},
            {"terapia", "Saúde"}, {"academia", "Saúde"}, {"smartfit", "Saúde"}, {"studio", "Saúde"},
            {"cinema", "Lazer"}, {"netflix", "Lazer"}, {"spotify", "Lazer"},
            {"deezer", "Lazer"}, {"show", "Lazer"}, {"ingresso", "Lazer"},
            {"teatro", "Lazer"}, {"parque", "Lazer"}, {"steam", "Lazer"},
            {"playstation", "Lazer"}, {"xbox", "Lazer"}, {"nintendo", "Lazer"},
            {"hotel", "Lazer"}, {"pousada", "Lazer"},
            {"aluguel", "Moradia"}, {"condominio", "Moradia"}, {"iptu", "Moradia"},
            {"luz", "Moradia"}, {"energia", "Moradia"}, {"agua", "Moradia"},
            {"gas ", "Moradia"}, {"internet", "Moradia"}, {"faxina", "Moradia"}, {"empregada", "Moradia"},
            {"curso", "Educação"}, {"faculdade", "Educação"}, {"universidade", "Educação"},
            {"escola", "Educação"}, {"livro", "Educação"}, {"livraria", "Educação"},
            {"udemy", "Educação"}, {"alura", "Educação"}, {"coursera", "Educação"},
            {"papelaria", "Educação"}, {"bemol", "Educação"}, {"kalunga", "Educação"},
            {"vivo", "Serviços"}, {"claro", "Serviços"}, {"tim ", "Serviços"},
            {"oi ", "Serviços"}, {"sky", "Serviços"}, {"amazon", "Serviços"},
            {"mercado livre", "Serviços"}, {"shopee", "Serviços"}, {"aliexpress", "Serviços"},
            {"disney", "Serviços"}, {"hbo", "Serviços"}, {"globoplay", "Serviços"},
            {"prime video", "Serviços"}, {"salao", "Serviços"}, {"barbearia", "Serviços"}, {"cabeleireiro", "Serviços"},
            {"renner", "Vestuário"}, {"riachuelo", "Vestuário"}, {"c&a", "Vestuário"},
            {"zara", "Vestuário"}, {"shein", "Vestuário"}, {"centauro", "Vestuário"},
            {"netshoes", "Vestuário"}, {"nike", "Vestuário"}, {"adidas", "Vestuário"},
            {"magazine luiza", "Vestuário"}, {"casas bahia", "Vestuário"}, {"americanas", "Vestuário"},
            {"petshop", "Pet"}, {"pet shop", "Pet"}, {"petz", "Pet"},
            {"cobasi", "Pet"}, {"racao", "Pet"}, {"agropet", "Pet"},
    };

    // ⭐ AMPLIADO: Adicionadas palavras para bloquear o Título/Beneficiário errados
    private static final String[] PALAVRAS_PROIBIDAS_NOME = {
            "S.A.", "S/A", "LTDA", "EIRELI",
            "BANCO", "PAGAMENTOS", "INSTITUIÇÃO", "INSTITUICAO",
            "CNPJ", "CPF", "OUVIDORIA", "ATENDIMENTO",
            "PIX", "VALOR", "TIPO", "DESTINO", "ORIGEM",
            "NOME", "CHAVE", "IDENTIFICADOR", "COMPROVANTE",
            "TRANSFERÊNCIA", "TRANSFERENCIA", "PAGAMENTO",
            "INFORMAÇÕES", "INFORMACOES", "ADICIONAIS",
            "ID DA TRANSAÇÃO", "ID DA TRANSACAO",
            "TIPO DE TRANSFERÊNCIA", "TIPO DE TRANSFERENCIA",
            "ESTAMOS", "AJUDAR", "DÚVIDA", "DUVIDA",
            "NUBANK", "INTER", "DIGIO", "ITAU", "BRADESCO",
            "SANTANDER", "CAIXA", "BCO ", "CEF",
            "CONTA", "AGENCIA", "AGÊNCIA", "SALDO",
            // ⭐ NOVAS PALAVRAS PARA BLOQUEAR OS RÓTULOS DO BRADESCO
            "DADOS", "QUEM", "FEZ", "RECEBEU", "TRANSAÇÃO", "TRANSACAO",
            "NÚMERO", "NUMERO", "CONTROLE", "AUTENTICAÇÃO", "AUTENTICACAO",
            "TELEFONES", "SAC", "0800"
    };

    public static ParsedReceipt parse(String texto, String nomeUsuario) {
        ParsedReceipt result = new ParsedReceipt();
        if (texto == null || texto.trim().isEmpty()) return result;

        String upper = texto.toUpperCase();
        String normalizado = normalizarParaBusca(texto);

        result.setValor(extrairValor(upper));
        result.setData(extrairData(upper)); // Agora lê os dois formatos
        result.setBanco(extrairBancoOrigem(normalizado)); // Agora pega o que aparece primeiro
        result.setBeneficiario(extrairBeneficiario(texto, nomeUsuario));
        result.setTitulo(extrairTitulo(texto, result.getBeneficiario()));
        result.setCategoria(inferirCategoria(result.getTitulo(), result.getBeneficiario()));
        result.setParcelas(1);

        Log.d(TAG, "Parse concluído: " + result);
        return result;
    }

    private static String normalizarParaBusca(String texto) {
        String upper = texto.toUpperCase();
        upper = upper.replaceAll("[.,/\\-]", " ");
        upper = upper.replaceAll("\\s+", " ");
        return upper;
    }

    private static Double extrairValor(String texto) {
        Matcher m = PATTERN_VALOR.matcher(texto);
        if (!m.find()) return null;
        try {
            String bruto = m.group(1);
            bruto = bruto.replace(".", "").replace(",", ".");
            return Double.parseDouble(bruto);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro ao converter valor: " + m.group(1), e);
            return null;
        }
    }

    private static String extrairData(String texto) {
        // Tenta primeiro o formato com texto (ex: 18 OUT 2026)
        Matcher mTexto = PATTERN_DATA_TEXTO.matcher(texto);
        if (mTexto.find()) {
            String dia = mTexto.group(1);
            String mesAbrev = mTexto.group(2).toUpperCase();
            String ano = mTexto.group(3);
            String mes = MESES.get(mesAbrev);
            if (mes != null) return dia + "/" + mes + "/" + ano;
        }

        // ⭐ Se não achar, tenta o formato numérico (ex: 18/10/2026)
        Matcher mNumerica = PATTERN_DATA_NUMERICA.matcher(texto);
        if (mNumerica.find()) {
            String dia = mNumerica.group(1);
            String mes = mNumerica.group(2);
            String ano = mNumerica.group(3);
            if (ano.length() == 2) ano = "20" + ano; // Converte 26 para 2026
            return dia + "/" + mes + "/" + ano;
        }

        return null;
    }

    /**
     * ⭐ MUDANÇA INTELIGENTE: Procura todos os bancos no texto e retorna aquele
     * que aparece na MENOR posição (mais perto do topo). Isso resolve o caso
     * do Bradesco, onde o banco de origem é citado antes do Nubank (destino).
     */
    private static String extrairBancoOrigem(String textoNormalizado) {
        String bancoEncontrado = null;
        int menorPosicao = Integer.MAX_VALUE;

        for (String[] par : BANCOS) {
            String chave = normalizarParaBusca(par[0]);
            int posicao = textoNormalizado.indexOf(chave);

            if (posicao != -1 && posicao < menorPosicao) {
                // Se achou uma chave mais específica antes, atualiza
                menorPosicao = posicao;
                bancoEncontrado = par[1];
            }
        }
        return bancoEncontrado;
    }

    private static String extrairBeneficiario(String textoOriginal, String nomeUsuario) {
        String[] linhas = textoOriginal.split("\\r?\\n");
        String primeiroNomeUsuario = primeiroNome(nomeUsuario);

        for (String linha : linhas) {
            String limpa = linha.trim();
            if (!pareceNomeProprio(limpa)) continue;
            if (contemPalavraProibida(limpa)) continue;
            if (primeiroNomeUsuario != null
                    && limpa.toUpperCase().contains(primeiroNomeUsuario.toUpperCase())) {
                continue;
            }
            return limpa;
        }
        return null;
    }

    private static String extrairTitulo(String textoOriginal, String beneficiario) {
        if (beneficiario != null) {
            String[] linhas = textoOriginal.split("\\r?\\n");
            boolean achouBeneficiario = false;

            for (String linha : linhas) {
                String limpa = linha.trim();

                if (!achouBeneficiario) {
                    if (limpa.equalsIgnoreCase(beneficiario)) {
                        achouBeneficiario = true;
                    }
                    continue;
                }

                if (linhaUtilParaTitulo(limpa)) {
                    return limpa;
                }
            }
        }
        return beneficiario;
    }

    private static boolean linhaUtilParaTitulo(String linha) {
        if (linha.length() < 3 || linha.length() > 60) return false;
        if (!linha.matches("[A-Za-zÀ-ÿ\\s]+")) return false;
        if (linha.replaceAll("[^A-Za-zÀ-ÿ]", "").length() < 3) return false;
        if (contemPalavraProibida(linha)) return false;
        if (pareceNomeProprio(linha)) return false;
        return true;
    }

    private static String inferirCategoria(String titulo, String beneficiario) {
        String base = ((titulo == null ? "" : titulo) + " "
                + (beneficiario == null ? "" : beneficiario)).toLowerCase();

        for (String[] par : CATEGORIAS) {
            if (base.contains(par[0])) return par[1];
        }
        return "Outros";
    }

    private static boolean pareceNomeProprio(String linha) {
        if (linha.length() < 5 || linha.length() > 60) return false;
        if (linha.matches(".*\\d.*")) return false;
        if (!linha.matches("[A-Za-zÀ-ÿ\\s.]+")) return false;

        String[] partes = linha.split("\\s+");
        if (partes.length < 2) return false;

        int palavrasCapitalizadas = 0;
        int palavrasValidas = 0;
        for (String p : partes) {
            if (p.isEmpty()) continue;
            palavrasValidas++;
            if (Character.isUpperCase(p.charAt(0))) palavrasCapitalizadas++;
        }
        if (palavrasValidas == 0) return false;

        return palavrasCapitalizadas >= Math.ceil(palavrasValidas * 0.6);
    }

    private static boolean contemPalavraProibida(String linha) {
        String upper = linha.toUpperCase();
        for (String p : PALAVRAS_PROIBIDAS_NOME) {
            if (upper.contains(p)) return true;
        }
        return false;
    }

    private static String primeiroNome(String nomeCompleto) {
        if (nomeCompleto == null) return null;
        String t = nomeCompleto.trim();
        if (t.isEmpty()) return null;
        String[] partes = t.split("\\s+");
        return partes[0];
    }
}