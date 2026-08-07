package com.app.gerencia.controllers.dto.contract;

import java.util.List;

public record CreateContractForSigningRequestDTO(
        Long templateId,
        Long patientId,
        Long guardianId,

        /**
         * Valores das variáveis manuais preenchidos no passo 4.
         * Chave = variableName (ex: "valor_plano"), Valor = valor preenchido.
         */
        java.util.Map<String, String> variableValues,

        /**
         * Se o modelo for OPCIONAL: define se usa testemunhas.
         * Se OBRIGATORIO: ignorado. Se NAO_UTILIZA: ignorado.
         */
        Boolean hasWitnesses,

        /**
         * IDs de secretárias/usuários que serão testemunhas.
         * Usado quando witnessConfig != NAO_UTILIZA.
         */
        List<Long> witnessUserIds
) {}
