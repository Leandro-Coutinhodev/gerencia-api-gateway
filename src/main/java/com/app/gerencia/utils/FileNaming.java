package com.app.gerencia.utils;

import java.time.LocalDate;
import java.util.UUID;

// Gera nomes de arquivo legíveis (prefixo + data + token aleatório) para laudos de anamnese e
// PDFs de contrato, sem expor o nome original do arquivo (pode conter o nome da pessoa) nem
// nenhum id interno (contractId, anamnesisId, referralId etc.) no nome baixado/exibido.
public final class FileNaming {

    private FileNaming() {}

    public static String generate(String prefix, String extension) {
        String date = LocalDate.now().toString().replace("-", "");
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + "_" + date + "_" + token + "." + extension;
    }
}
