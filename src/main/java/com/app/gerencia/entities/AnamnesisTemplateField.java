package com.app.gerencia.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_anamnesis_template_field")
public class AnamnesisTemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anamnesis_template_field_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private AnamnesisTemplate template;

    @Column(nullable = false)
    private String label;

    @Column(name = "field_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @Column(name = "required")
    private boolean required = false;

    @Column(name = "position")
    private Integer position;

    @Column(name = "placeholder")
    private String placeholder;


    @Column(columnDefinition = "TEXT")
    private String options;

    public enum FieldType {
        TEXT, TEXTAREA, DATE, CHECKBOX, FILE
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnamnesisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(AnamnesisTemplate template) {
        this.template = template;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
}
