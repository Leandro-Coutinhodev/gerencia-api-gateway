package com.app.gerencia.entities;

import jakarta.persistence.*;


@Entity
@Table(name = "tb_contract_accept_field")
public class ContractAcceptField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_accept_field_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id", nullable = false)
    private ContractTemplate template;

    @Column(name = "field_order", nullable = false)
    private Integer fieldOrder;

    @Column(nullable = false)
    private String label;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcceptFieldType fieldType;

    @Column(nullable = false)
    private Boolean required = true;

    public enum AcceptFieldType { CHECKBOX, SIM_NAO, TEXT, DATE, SIGNATURE }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractTemplate getTemplate() { return template; }
    public void setTemplate(ContractTemplate template) { this.template = template; }

    public Integer getFieldOrder() { return fieldOrder; }
    public void setFieldOrder(Integer fieldOrder) { this.fieldOrder = fieldOrder; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public AcceptFieldType getFieldType() { return fieldType; }
    public void setFieldType(AcceptFieldType fieldType) { this.fieldType = fieldType; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
}
