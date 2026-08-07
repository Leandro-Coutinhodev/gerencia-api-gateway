package com.app.gerencia.services;

import com.app.gerencia.entities.*;
import com.app.gerencia.enums.ParticipantRole;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Gera o PDF final do contrato apos todas as assinaturas.
 *
 * Estrutura:
 *   Paginas 1..N → Clausulas + campos de aceite (corpo do contrato)
 *   Ultima pagina → Registro de Assinaturas Eletronicas
 */
@Service
public class ContractPdfService {

    // ── Layout ──────────────────────────────────────────────────────────────
    private static final float PAGE_W      = PDRectangle.A4.getWidth();
    private static final float PAGE_H      = PDRectangle.A4.getHeight();
    private static final float MARGIN      = 60f;
    private static final float CONTENT_W   = PAGE_W - MARGIN * 2;
    private static final float TOP_Y       = PAGE_H - 55f;
    private static final float BOTTOM_STOP = 70f;
    private static final float LINE_H      = 14.5f;
    private static final float PARA_GAP    = 7f;
    private static final float BLOCK_PAD   = 12f;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    // ── Cores ────────────────────────────────────────────────────────────────
    private static final float[] DARK   = {0.10f, 0.10f, 0.10f};
    private static final float[] MID    = {0.28f, 0.28f, 0.28f};
    private static final float[] LIGHT  = {0.42f, 0.42f, 0.42f};
    private static final float[] RULE   = {0.78f, 0.78f, 0.78f};
    private static final float[] BG_BOX = {0.965f, 0.965f, 0.975f};
    private static final float[] C_RESPONSAVEL = {0.22f, 0.38f, 0.92f};
    private static final float[] C_EMPRESA     = {0.12f, 0.54f, 0.12f};
    private static final float[] C_TESTEMUNHA  = {0.84f, 0.54f, 0.10f};

    // ════════════════════════════════════════════════════════════════════════
    // Entrada publica
    // ════════════════════════════════════════════════════════════════════════

    public byte[] generateFinalPdf(Contract contract) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            Cursor cur = newPage(doc);
            cur = writeContractContent(doc, cur, contract);
            cur = newPage(doc);
            writeSignaturePage(doc, cur, contract);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Corpo do contrato (clausulas + campos de aceite)
    // ════════════════════════════════════════════════════════════════════════

    private Cursor writeContractContent(PDDocument doc, Cursor cur,
                                        Contract contract) throws Exception {

        String templateName = contract.getTemplate() != null
                ? contract.getTemplate().getName() : "Contrato";

        cur = lineCentered(doc, cur, templateName,
                PDType1Font.HELVETICA_BOLD, 15f, DARK);
        cur.y -= 4;
        cur = hRule(doc, cur, RULE);
        cur.y -= PARA_GAP * 2;

        ContractTemplate tmpl = contract.getTemplate();

        // ── Clausulas ──────────────────────────────────────────────────────
        if (tmpl != null && tmpl.getClauses() != null && !tmpl.getClauses().isEmpty()) {

            Map<String, String> vars = parseVariablesData(contract.getVariablesData());

            for (ContractClause clause : tmpl.getClauses()) {

                // Titulo da clausula
                cur.y -= PARA_GAP;
                if (cur.y < BOTTOM_STOP) cur = newPage(doc);

                String clauseTitle = clause.getClauseOrder() + ". " + safe(clause.getTitle());
                for (String wl : wrapText(clauseTitle, PDType1Font.HELVETICA_BOLD, 10.5f, CONTENT_W)) {
                    if (cur.y < BOTTOM_STOP) cur = newPage(doc);
                    cur = drawLine(doc, cur, wl, PDType1Font.HELVETICA_BOLD, 10.5f, DARK, MARGIN);
                }
                cur.y -= PARA_GAP / 2f;

                // Conteudo com variaveis substituidas
                String body = safe(clause.getContent());
                for (Map.Entry<String, String> e : vars.entrySet()) {
                    body = body.replace("{{" + e.getKey() + "}}", e.getValue());
                }
                body = body.replaceAll("\\{\\{[^}]+}}", "___");

                for (String para : body.split("\n")) {
                    String p = para.trim();
                    if (p.isEmpty()) { cur.y -= PARA_GAP / 2f; continue; }
                    for (String wl : wrapText(p, PDType1Font.HELVETICA, 10f, CONTENT_W)) {
                        if (cur.y < BOTTOM_STOP) cur = newPage(doc);
                        cur = drawLine(doc, cur, wl, PDType1Font.HELVETICA, 10f, MID, MARGIN);
                    }
                }
                cur.y -= PARA_GAP / 2f;
            }

        } else {
            // Fallback: renderiza o HTML do renderedContent
            String rendered = contract.getRenderedContent();
            if (rendered != null && !rendered.isBlank()) {
                cur = renderHtmlFallback(doc, cur, rendered);
            }
        }

        // ── Campos de aceite no corpo ──────────────────────────────────────
        if (tmpl != null && tmpl.getAcceptFields() != null && !tmpl.getAcceptFields().isEmpty()) {
            Map<Long, String> respostas = buildRespostaMap(contract);
            cur.y -= PARA_GAP * 2;
            if (cur.y < BOTTOM_STOP) cur = newPage(doc);
            cur = writeAcceptFieldsInBody(doc, cur, tmpl.getAcceptFields(), respostas);
        }

        return cur;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Campos de aceite no corpo: todas opcoes, [X] na selecionada
    // ════════════════════════════════════════════════════════════════════════

    private Cursor writeAcceptFieldsInBody(PDDocument doc, Cursor cur,
                                           List<ContractAcceptField> fields,
                                           Map<Long, String> respostas) throws Exception {

        cur = drawLine(doc, cur, "Declaracoes do Contratante:",
                PDType1Font.HELVETICA_BOLD, 10.5f, DARK, MARGIN);
        cur.y -= PARA_GAP / 2f;

        for (ContractAcceptField field : fields) {
            if (cur.y < BOTTOM_STOP) cur = newPage(doc);

            String resposta = respostas.getOrDefault(field.getId(), "");
            boolean marcado = isTruthy(resposta);
            String label;

            if (field.getFieldType() == ContractAcceptField.AcceptFieldType.SIM_NAO) {
                String sim = marcado ? "[X] Sim" : "[ ] Sim";
                String nao = "false".equalsIgnoreCase(resposta.trim()) ? "[X] Nao" : "[ ] Nao";
                label = safe(field.getLabel()) + "    " + sim + "    " + nao;
            } else {
                label = (marcado ? "[X] " : "[ ] ") + safe(field.getLabel());
            }

            for (String wl : wrapText(label, PDType1Font.HELVETICA, 9.5f, CONTENT_W - 4)) {
                if (cur.y < BOTTOM_STOP) cur = newPage(doc);
                cur = drawLine(doc, cur, wl, PDType1Font.HELVETICA, 9.5f, MID, MARGIN);
            }
            cur.y -= 2;
        }
        return cur;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Pagina de assinaturas
    // ════════════════════════════════════════════════════════════════════════

    private void writeSignaturePage(PDDocument doc, Cursor cur,
                                    Contract contract) throws Exception {

        cur = hRule(doc, cur, RULE);
        cur.y -= 14;
        cur = lineCentered(doc, cur, "Registro de Assinaturas Eletronicas",
                PDType1Font.HELVETICA_BOLD, 13f, DARK);
        cur.y -= 2;
        cur = lineCentered(doc, cur,
                "Documento gerado pelo sistema GerenciA apos a conclusao das assinaturas.",
                PDType1Font.HELVETICA_OBLIQUE, 8.5f, LIGHT);
        cur.y -= 14;
        cur = hRule(doc, cur, RULE);
        cur.y -= 20;

        // Participantes manuais (Responsavel + Testemunhas)
        List<ContractParticipant> manuais = new ArrayList<>();
        for (ContractParticipant p : contract.getParticipants()) {
            if (p.getRole() != ParticipantRole.EMPRESA) manuais.add(p);
        }

        int witnessIdx = 1;
        for (ContractParticipant p : manuais) {
            String roleTitle = buildRoleTitle(p.getRole(), witnessIdx);
            if (p.getRole() == ParticipantRole.TESTEMUNHA) witnessIdx++;

            float boxH = blockHeight(p);
            if (cur.y - boxH < BOTTOM_STOP) cur = newPage(doc);
            cur = drawParticipantBlock(doc, cur, p, roleTitle, boxH);
            cur.y -= 10;
        }

        // Bloco fixo da contratada
        cur.y -= 4;
        float companyH = 62f;
        if (cur.y - companyH < BOTTOM_STOP) cur = newPage(doc);
        cur = drawCompanyBlock(doc, cur, companyH);
        cur.y -= 10;

        // Identificador
        cur.y -= 10;
        if (cur.y < BOTTOM_STOP) cur = newPage(doc);
        cur = hRule(doc, cur, RULE);
        cur.y -= 10;
        cur = drawLine(doc, cur, "Identificador: " + safe(contract.getHash()),
                PDType1Font.COURIER, 7.5f, LIGHT, MARGIN);
        cur.y -= 6;
        lineCentered(doc, cur,
                "Documento gerado eletronicamente pelo sistema GerenciA.",
                PDType1Font.HELVETICA_OBLIQUE, 8f, LIGHT);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Bloco de participante manual
    // ════════════════════════════════════════════════════════════════════════

    private Cursor drawParticipantBlock(PDDocument doc, Cursor cur,
                                        ContractParticipant p,
                                        String roleTitle,
                                        float boxH) throws Exception {
        float boxY  = cur.y - boxH;
        float textX = MARGIN + 14f;

        try (PDPageContentStream cs = new PDPageContentStream(
                doc, cur.page, PDPageContentStream.AppendMode.APPEND, true)) {

            cs.setNonStrokingColor(BG_BOX[0], BG_BOX[1], BG_BOX[2]);
            cs.addRect(MARGIN, boxY, CONTENT_W, boxH);
            cs.fill();

            float[] bar = barColor(p.getRole());
            cs.setNonStrokingColor(bar[0], bar[1], bar[2]);
            cs.addRect(MARGIN, boxY, 4f, boxH);
            cs.fill();

            float ty = cur.y - BLOCK_PAD;
            ty = textInStream(cs, roleTitle, PDType1Font.HELVETICA_BOLD, 10.5f, textX, ty, DARK);
            ty -= 3;
            ty = textInStream(cs, "Nome:      " + safe(p.getName()),
                    PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
            ty = textInStream(cs, "CPF:       " + safe(p.getCpf()),
                    PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);

            String email = resolveEmail(p);
            if (email != null) {
                ty = textInStream(cs, "E-mail:    " + email,
                        PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
            }

            ContractSignature sig = p.getSignature();
            if (sig != null && sig.getSignedAt() != null) {
                ty = textInStream(cs, "Data/Hora: " + sig.getSignedAt().format(DATE_FMT),
                        PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
                if (sig.getSignedIp() != null) {
                    ty = textInStream(cs, "IP:        " + sig.getSignedIp(),
                            PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
                }
            }
        }

        cur.y -= boxH + 2;
        return cur;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Bloco fixo da contratada (LP Kids)
    // ════════════════════════════════════════════════════════════════════════

    private Cursor drawCompanyBlock(PDDocument doc, Cursor cur, float boxH) throws Exception {
        float boxY  = cur.y - boxH;
        float textX = MARGIN + 14f;

        try (PDPageContentStream cs = new PDPageContentStream(
                doc, cur.page, PDPageContentStream.AppendMode.APPEND, true)) {

            cs.setNonStrokingColor(BG_BOX[0], BG_BOX[1], BG_BOX[2]);
            cs.addRect(MARGIN, boxY, CONTENT_W, boxH);
            cs.fill();
            cs.setNonStrokingColor(C_EMPRESA[0], C_EMPRESA[1], C_EMPRESA[2]);
            cs.addRect(MARGIN, boxY, 4f, boxH);
            cs.fill();

            float ty = cur.y - BLOCK_PAD;
            ty = textInStream(cs, "Contratada", PDType1Font.HELVETICA_BOLD, 10.5f, textX, ty, DARK);
            ty -= 3;
            ty = textInStream(cs, "Nome Fantasia: LP Kids",
                    PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
            ty = textInStream(cs, "Razao Social:  LUANA PEREIRA DOS SANTOS LIMA",
                    PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
            textInStream(cs, "CNPJ:          46.210.211/0001-60",
                    PDType1Font.HELVETICA, 9.5f, textX, ty - LINE_H, MID);
        }

        cur.y -= boxH + 2;
        return cur;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Fallback HTML
    // ════════════════════════════════════════════════════════════════════════

    private Cursor renderHtmlFallback(PDDocument doc, Cursor cur,
                                      String rendered) throws Exception {
        String plain = rendered
                .replaceAll("</section>\\s*<section[^>]*>", "\n")
                .replaceAll("<section[^>]*>|</section>", "\n")
                .replaceAll("<h3>", "\n@@")
                .replaceAll("</h3>", "\n")
                .replaceAll("</?p>", "\n")
                .replaceAll("<[^>]+>", "")
                .replaceAll("(?m)^[ \t]+", "")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        for (String raw : plain.split("\n")) {
            String tok = raw.trim();
            if (tok.isEmpty()) { cur.y -= PARA_GAP / 2f; continue; }

            boolean isTitle = tok.startsWith("@@");
            if (isTitle) { tok = tok.substring(2).trim(); cur.y -= PARA_GAP; }

            PDType1Font font = isTitle ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
            float       size = isTitle ? 10.5f : 10f;
            float[]     col  = isTitle ? DARK : MID;

            for (String wl : wrapText(tok, font, size, CONTENT_W)) {
                if (cur.y < BOTTOM_STOP) cur = newPage(doc);
                cur = drawLine(doc, cur, wl, font, size, col, MARGIN);
            }
            if (isTitle) cur.y -= PARA_GAP / 2f;
        }
        return cur;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Primitivos de layout
    // ════════════════════════════════════════════════════════════════════════

    /** Escreve texto num stream ja aberto. Retorna o y atual (nao avanca). */
    private float textInStream(PDPageContentStream cs, String txt,
                               PDType1Font font, float size,
                               float x, float y, float[] col) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(col[0], col[1], col[2]);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(txt));
        cs.endText();
        return y;
    }

    /** Abre stream, escreve uma linha, fecha e avanca cursor. */
    private Cursor drawLine(PDDocument doc, Cursor cur, String txt,
                            PDType1Font font, float size,
                            float[] col, float x) throws Exception {
        try (PDPageContentStream cs = new PDPageContentStream(
                doc, cur.page, PDPageContentStream.AppendMode.APPEND, true)) {
            textInStream(cs, txt, font, size, x, cur.y, col);
        }
        cur.y -= LINE_H;
        return cur;
    }

    /** Texto centralizado. */
    private Cursor lineCentered(PDDocument doc, Cursor cur, String txt,
                                PDType1Font font, float size,
                                float[] col) throws Exception {
        float w = font.getStringWidth(sanitize(txt)) / 1000f * size;
        float x = (PAGE_W - w) / 2f;
        return drawLine(doc, cur, txt, font, size, col, x);
    }

    /** Linha horizontal. */
    private Cursor hRule(PDDocument doc, Cursor cur, float[] col) throws Exception {
        try (PDPageContentStream cs = new PDPageContentStream(
                doc, cur.page, PDPageContentStream.AppendMode.APPEND, true)) {
            cs.setStrokingColor(col[0], col[1], col[2]);
            cs.setLineWidth(0.6f);
            cs.moveTo(MARGIN, cur.y);
            cs.lineTo(PAGE_W - MARGIN, cur.y);
            cs.stroke();
        }
        cur.y -= 3;
        return cur;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Gestao de paginas
    // ════════════════════════════════════════════════════════════════════════

    private static class Cursor {
        PDPage page;
        float  y;
    }

    private Cursor newPage(PDDocument doc) {
        PDPage page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
        doc.addPage(page);
        Cursor c = new Cursor();
        c.page = page;
        c.y    = TOP_Y;
        return c;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Auxiliares
    // ════════════════════════════════════════════════════════════════════════

    private float blockHeight(ContractParticipant p) {
        int rows = 3; // titulo, nome, cpf
        if (resolveEmail(p) != null) rows++;
        ContractSignature sig = p.getSignature();
        if (sig != null) {
            if (sig.getSignedAt() != null) rows++;
            if (sig.getSignedIp()  != null) rows++;
        }
        return BLOCK_PAD + (rows * LINE_H) + (rows * 2) + BLOCK_PAD;
    }

    private String resolveEmail(ContractParticipant p) {
        if (p.getGuardian() != null && p.getGuardian().getEmail() != null)
            return p.getGuardian().getEmail();
        if (p.getUser() != null && p.getUser().getEmail() != null)
            return p.getUser().getEmail();
        return null;
    }

    private Map<String, String> parseVariablesData(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;
        try {
            TypeToken<Map<String, String>> token = new TypeToken<>(){};
            Map<String, String> parsed = new Gson().fromJson(json, token);
            if (parsed != null) map.putAll(parsed);
        } catch (Exception ignored) {}
        return map;
    }

    private Map<Long, String> buildRespostaMap(Contract contract) {
        Map<Long, String> map = new LinkedHashMap<>();
        if (contract.getParticipants() == null) return map;
        for (ContractParticipant p : contract.getParticipants()) {
            if (p.getRole() == ParticipantRole.EMPRESA) continue;
            if (p.getAcceptResponses() == null) continue;
            for (ContractAcceptResponse r : p.getAcceptResponses()) {
                if (r.getAcceptField() != null) {
                    map.put(r.getAcceptField().getId(), r.getResponseValue());
                }
            }
            break; // apenas o primeiro signatario manual
        }
        return map;
    }

    private boolean isTruthy(String val) {
        if (val == null) return false;
        String v = val.trim().toLowerCase();
        return v.equals("true") || v.equals("sim") || v.equals("1") || v.equals("x");
    }

    private String buildRoleTitle(ParticipantRole role, int witnessIdx) {
        return switch (role) {
            case RESPONSAVEL -> "Contratante";
            case EMPRESA     -> "Contratada";
            case TESTEMUNHA  -> "Testemunha " + witnessIdx;
        };
    }

    private float[] barColor(ParticipantRole role) {
        return switch (role) {
            case RESPONSAVEL -> C_RESPONSAVEL;
            case EMPRESA     -> C_EMPRESA;
            case TESTEMUNHA  -> C_TESTEMUNHA;
        };
    }

    private List<String> wrapText(String text, PDType1Font font,
                                  float size, float maxW) throws Exception {
        List<String> result = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            float w = font.getStringWidth(sanitize(candidate)) / 1000f * size;
            if (w > maxW && !current.isEmpty()) {
                result.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) result.add(current.toString());
        return result;
    }

    private String safe(String s) { return s != null ? s : ""; }

    private String sanitize(String s) {
        if (s == null) return "";
        return s
                .replace("ã","a").replace("Ã","A")
                .replace("á","a").replace("Á","A")
                .replace("à","a").replace("À","A")
                .replace("â","a").replace("Â","A")
                .replace("ä","a").replace("Ä","A")
                .replace("é","e").replace("É","E")
                .replace("ê","e").replace("Ê","E")
                .replace("è","e").replace("È","E")
                .replace("í","i").replace("Í","I")
                .replace("ì","i").replace("Ì","I")
                .replace("ó","o").replace("Ó","O")
                .replace("ô","o").replace("Ô","O")
                .replace("õ","o").replace("Õ","O")
                .replace("ö","o").replace("Ö","O")
                .replace("ú","u").replace("Ú","U")
                .replace("ù","u").replace("Ù","U")
                .replace("ü","u").replace("Ü","U")
                .replace("ç","c").replace("Ç","C")
                .replace("ñ","n").replace("Ñ","N")
                .replace("\u2013","-").replace("\u2014","-")
                .replace("\u00A0"," ")
                .replace("\u2018","'").replace("\u2019","'")
                .replace("\u201C","\"").replace("\u201D","\"");
    }
}