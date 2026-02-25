package com.app.gerencia.services;

import com.app.gerencia.entities.*;
import com.app.gerencia.enums.ParticipantRole;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ContractPdfService {

    @Value("${app.contracts.template-path:classpath:templates/contrato_template.pdf}")
    private String templatePath;

    @Value("${app.contracts.output-dir:./contracts}")
    private String outputDir;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy – HH:mm");

    private static final float MARGIN_LEFT = 60;
    private static final float MARGIN_RIGHT = 60;
    private static final float LINE_HEIGHT = 16;
    private static final float SECTION_GAP = 10;

    /**
     * Gera o PDF final do contrato com o bloco de assinaturas eletrônicas.
     * Retorna o caminho do arquivo gerado e o hash do contrato.
     */
    public GeneratedPdf generate(Contract contract) throws IOException, NoSuchAlgorithmException {

        // Garante que o diretório de saída exista
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Gera o hash identificador do contrato
        String hash = generateHash(contract);

        // Carrega o PDF template
        File templateFile = resolveTemplate();
        PDDocument document = PDDocument.load(templateFile);

        try {
            // 1) Preenche os placeholders na primeira página do template
            fillTemplatePlaceholders(document, contract);

            // 2) Adiciona a página de assinaturas eletrônicas
            addSignaturePage(document, contract, hash);

            // Salva o PDF gerado
            String fileName = String.format("contrato_%d_assinado.pdf", contract.getId());
            String outputPath = outputDir + File.separator + fileName;
            document.save(outputPath);

            return new GeneratedPdf(outputPath, hash);
        } finally {
            document.close();
        }
    }

    // ──────────────────────────────────────────────
    //  Preencher placeholders do template
    // ──────────────────────────────────────────────

    private void fillTemplatePlaceholders(PDDocument document, Contract contract) {
        // O preenchimento de placeholders em PDF existente é complexo com PDFBox
        // pois o texto já está renderizado. A abordagem mais robusta é usar
        // o template como base visual e sobrepor os dados nos campos corretos.
        //
        // Para um sistema de produção, considere:
        // - Usar um template com campos de formulário (AcroForm) preenchíveis
        // - Ou gerar o contrato inteiramente via código
        //
        // Aqui manteremos o template original e adicionaremos apenas
        // a página de assinaturas ao final.
    }

    // ──────────────────────────────────────────────
    //  Página de assinaturas eletrônicas
    // ──────────────────────────────────────────────

    private void addSignaturePage(PDDocument document, Contract contract, String hash)
            throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        float usableWidth = pageWidth - MARGIN_LEFT - MARGIN_RIGHT;

        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
        PDType1Font fontRegular = PDType1Font.HELVETICA;
        PDType1Font fontItalic = PDType1Font.HELVETICA_OBLIQUE;

        PDPageContentStream cs = new PDPageContentStream(document, page);

        try {
            float y = pageHeight - 60;

            // ── Linha separadora superior ──
            cs.setStrokingColor(0.75f, 0.75f, 0.75f);
            cs.setLineWidth(1f);
            cs.moveTo(MARGIN_LEFT, y);
            cs.lineTo(pageWidth - MARGIN_RIGHT, y);
            cs.stroke();
            y -= 30;

            // ── Título ──
            cs.beginText();
            cs.setFont(fontBold, 14);
            cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
            String title = "Assinaturas Eletronicas";
            float titleWidth = fontBold.getStringWidth(title) / 1000 * 14;
            cs.newLineAtOffset((pageWidth - titleWidth) / 2, y);
            cs.showText(title);
            cs.endText();
            y -= 8;

            // ── Subtítulo ──
            y -= LINE_HEIGHT;
            cs.beginText();
            cs.setFont(fontItalic, 9);
            cs.setNonStrokingColor(0.45f, 0.45f, 0.45f);
            String subtitle = "Este contrato foi assinado eletronicamente.";
            float subWidth = fontItalic.getStringWidth(subtitle) / 1000 * 9;
            cs.newLineAtOffset((pageWidth - subWidth) / 2, y);
            cs.showText(subtitle);
            cs.endText();
            y -= 30;

            // ── Linha separadora ──
            cs.setStrokingColor(0.85f, 0.85f, 0.85f);
            cs.moveTo(MARGIN_LEFT, y);
            cs.lineTo(pageWidth - MARGIN_RIGHT, y);
            cs.stroke();
            y -= 25;

            // ── Participantes ──
            List<ContractParticipant> participants = contract.getParticipants();
            int witnessCount = 0;

            for (ContractParticipant p : participants) {

                String roleName;
                String personName;
                String cpf;
                String email;

                if (p.getRole() == ParticipantRole.CONTRACTOR) {
                    roleName = "Contratante";
                    Guardian g = p.getGuardian();
                    personName = g != null ? g.getName() : "N/A";
                    cpf = g != null ? g.getCpf() : "N/A";
                    email = g != null ? g.getEmail() : "N/A";
                } else if (p.getRole() == ParticipantRole.WITNESS) {
                    witnessCount++;
                    roleName = "Testemunha " + witnessCount;
                    User u = p.getUser();
                    personName = u != null ? u.getName() : "N/A";
                    cpf = u != null ? u.getCpf() : "N/A";
                    email = u != null ? u.getEmail() : "N/A";
                } else {
                    roleName = p.getRole().name();
                    personName = "N/A";
                    cpf = "N/A";
                    email = "N/A";
                }

                String signedDate = p.getSignedAt() != null
                        ? p.getSignedAt().format(DATE_FMT)
                        : "Pendente";
                String signedIp = p.getSignedIp() != null
                        ? p.getSignedIp()
                        : "N/A";

                // Caixa de fundo
                float boxHeight = 80;
                cs.setNonStrokingColor(0.97f, 0.97f, 0.98f);
                cs.addRect(MARGIN_LEFT, y - boxHeight + 10, usableWidth, boxHeight);
                cs.fill();

                // Borda esquerda colorida
                cs.setNonStrokingColor(0.23f, 0.39f, 0.93f); // azul
                cs.addRect(MARGIN_LEFT, y - boxHeight + 10, 4, boxHeight);
                cs.fill();

                // Role (título)
                float textX = MARGIN_LEFT + 16;
                float textY = y;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
                cs.newLineAtOffset(textX, textY);
                cs.showText(roleName);
                cs.endText();

                // Nome
                textY -= LINE_HEIGHT;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                cs.newLineAtOffset(textX, textY);
                cs.showText("Nome: " + sanitize(personName));
                cs.endText();

                // CPF + E-mail na mesma linha
                textY -= LINE_HEIGHT;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(textX, textY);
                cs.showText("CPF: " + sanitize(cpf) + "     E-mail: " + sanitize(email));
                cs.endText();

                // Data/Hora + IP
                textY -= LINE_HEIGHT;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(textX, textY);
                cs.showText("Data/Hora: " + signedDate + "     IP: " + signedIp);
                cs.endText();

                y -= boxHeight + SECTION_GAP + 8;

                // Se ultrapassar o limite da página, cria nova página
                if (y < 120) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    y = pageHeight - 60;
                }
            }

            // ── Contratada (LP Kids) ──
            y -= 5;
            float boxHeight = 65;
            cs.setNonStrokingColor(0.97f, 0.97f, 0.98f);
            cs.addRect(MARGIN_LEFT, y - boxHeight + 10, usableWidth, boxHeight);
            cs.fill();

            cs.setNonStrokingColor(0.13f, 0.55f, 0.13f); // verde
            cs.addRect(MARGIN_LEFT, y - boxHeight + 10, 4, boxHeight);
            cs.fill();

            float textX = MARGIN_LEFT + 16;
            cs.beginText();
            cs.setFont(fontBold, 11);
            cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
            cs.newLineAtOffset(textX, y);
            cs.showText("Contratada");
            cs.endText();

            y -= LINE_HEIGHT;
            cs.beginText();
            cs.setFont(fontRegular, 10);
            cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            cs.newLineAtOffset(textX, y);
            cs.showText("Razao Social: LUANA PEREIRA DOS SANTOS LIMA");
            cs.endText();

            y -= LINE_HEIGHT;
            cs.beginText();
            cs.setFont(fontRegular, 10);
            cs.newLineAtOffset(textX, y);
            cs.showText("CNPJ: 46.210.211/0001-60     Nome Fantasia: LP Kids");
            cs.endText();

            y -= (LINE_HEIGHT + 30);

            // ── Identificador do contrato ──
            cs.setStrokingColor(0.85f, 0.85f, 0.85f);
            cs.moveTo(MARGIN_LEFT, y + 10);
            cs.lineTo(pageWidth - MARGIN_RIGHT, y + 10);
            cs.stroke();

            y -= 10;
            cs.beginText();
            cs.setFont(fontBold, 9);
            cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
            cs.newLineAtOffset(MARGIN_LEFT, y);
            cs.showText("Identificador do contrato:");
            cs.endText();

            y -= LINE_HEIGHT;
            cs.beginText();
            cs.setFont(PDType1Font.COURIER, 9);
            cs.setNonStrokingColor(0.23f, 0.39f, 0.93f);
            cs.newLineAtOffset(MARGIN_LEFT, y);
            cs.showText(hash);
            cs.endText();

            y -= 24;

            // ── Nota de rodapé ──
            cs.beginText();
            cs.setFont(fontItalic, 8);
            cs.setNonStrokingColor(0.55f, 0.55f, 0.55f);
            String note = "Documento gerado eletronicamente pelo sistema GerencIA.";
            float noteWidth = fontItalic.getStringWidth(note) / 1000 * 8;
            cs.newLineAtOffset((pageWidth - noteWidth) / 2, y);
            cs.showText(note);
            cs.endText();

        } finally {
            cs.close();
        }
    }

    // ──────────────────────────────────────────────
    //  Utilitários
    // ──────────────────────────────────────────────

    private File resolveTemplate() {
        // Tenta carregar do filesystem (configurável via application.properties)
        String path = templatePath.replace("classpath:", "");

        // Primeiro tenta o caminho absoluto/relativo
        File file = new File(templatePath);
        if (file.exists()) return file;

        // Tenta dentro de resources
        file = new File("src/main/resources/" + path);
        if (file.exists()) return file;

        // Fallback: tenta dentro de templates/
        file = new File("src/main/resources/templates/contrato_template.pdf");
        if (file.exists()) return file;

        throw new RuntimeException(
                "Template do contrato nao encontrado. Configure 'app.contracts.template-path' " +
                        "no application.properties ou coloque o arquivo em src/main/resources/templates/contrato_template.pdf"
        );
    }

    private String generateHash(Contract contract) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        sb.append(contract.getId());
        sb.append(contract.getCreatedAt());
        sb.append(contract.getCreatedIp());

        if (contract.getGuardian() != null) {
            sb.append(contract.getGuardian().getCpf());
        }

        for (ContractParticipant p : contract.getParticipants()) {
            sb.append(p.getId());
            sb.append(p.getSignedAt());
            sb.append(p.getSignedIp());
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

        // Converte para hex
        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Remove caracteres especiais que PDFBox não consegue renderizar
     * com fontes Type1 padrão (acentos, cedilha, etc.).
     */
    private String sanitize(String text) {
        if (text == null) return "";
        return text
                .replace("ã", "a").replace("Ã", "A")
                .replace("á", "a").replace("Á", "A")
                .replace("à", "a").replace("À", "A")
                .replace("â", "a").replace("Â", "A")
                .replace("é", "e").replace("É", "E")
                .replace("ê", "e").replace("Ê", "E")
                .replace("í", "i").replace("Í", "I")
                .replace("ó", "o").replace("Ó", "O")
                .replace("ô", "o").replace("Ô", "O")
                .replace("õ", "o").replace("Õ", "O")
                .replace("ú", "u").replace("Ú", "U")
                .replace("ç", "c").replace("Ç", "C")
                .replace("ñ", "n").replace("Ñ", "N");
    }

    // ──────────────────────────────────────────────
    //  DTO de retorno
    // ──────────────────────────────────────────────

    public static class GeneratedPdf {
        private final String filePath;
        private final String hash;

        public GeneratedPdf(String filePath, String hash) {
            this.filePath = filePath;
            this.hash = hash;
        }

        public String getFilePath() { return filePath; }
        public String getHash() { return hash; }
    }
}