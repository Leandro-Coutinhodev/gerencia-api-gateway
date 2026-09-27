package com.app.gerencia.entities;

import com.app.gerencia.enums.RecordFieldType;
import jakarta.persistence.*;

// Campo de registro de execução associado a um item de atividade dentro de um modelo de ficha
// (ex: séries, repetições, carga, observações, desempenho).
@Entity
@Table(name = "tb_record_template_activity_field")
public class RecordTemplateActivityField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_template_activity_field_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_activity_id", nullable = false)
    private RecordTemplateActivity templateActivity;

    @Column(nullable = false)
    private String label;

    @Column(name = "field_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordFieldType fieldType;

    // Unidade opcional, usada principalmente em campos NUMBER (ex: "kg", "min", "repetições")
    private String unit;

    @Column(nullable = false)
    private boolean required = false;

    private Integer position;

    // Opções separadas por "|", usadas em campos CHECKBOX
    @Column(columnDefinition = "TEXT")
    private String options;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecordTemplateActivity getTemplateActivity() { return templateActivity; }
    public void setTemplateActivity(RecordTemplateActivity templateActivity) { this.templateActivity = templateActivity; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public RecordFieldType getFieldType() { return fieldType; }
    public void setFieldType(RecordFieldType fieldType) { this.fieldType = fieldType; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
}
