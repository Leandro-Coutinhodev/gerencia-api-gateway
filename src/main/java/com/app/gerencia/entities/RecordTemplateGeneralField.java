package com.app.gerencia.entities;

import com.app.gerencia.enums.RecordFieldType;
import jakarta.persistence.*;

// Campo geral do modelo de ficha, independente de atividades (ex: observações gerais, humor do paciente).
@Entity
@Table(name = "tb_record_template_general_field")
public class RecordTemplateGeneralField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_template_general_field_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RecordTemplate template;

    @Column(nullable = false)
    private String label;

    @Column(name = "field_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordFieldType fieldType;

    private String unit;

    @Column(nullable = false)
    private boolean required = false;

    private Integer position;

    @Column(columnDefinition = "TEXT")
    private String options;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecordTemplate getTemplate() { return template; }
    public void setTemplate(RecordTemplate template) { this.template = template; }

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
