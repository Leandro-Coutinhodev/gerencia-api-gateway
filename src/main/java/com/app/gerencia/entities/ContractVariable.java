package com.app.gerencia.entities;

import jakarta.persistence.*;


@Entity
@Table(name = "tb_contract_variable")
public class ContractVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_variable_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id", nullable = false)
    private ContractTemplate template;


    @Column(name = "variable_name", nullable = false)
    private String variableName;

    @Column(name = "description")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariableType type;

    @Column(nullable = false)
    private Boolean required = true;


    @Column(name = "auto_filled")
    private Boolean autoFilled = false;

    public enum VariableType { TEXT, NUMBER, DATE, CURRENCY }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractTemplate getTemplate() { return template; }
    public void setTemplate(ContractTemplate template) { this.template = template; }

    public String getVariableName() { return variableName; }
    public void setVariableName(String variableName) { this.variableName = variableName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public VariableType getType() { return type; }
    public void setType(VariableType type) { this.type = type; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public Boolean getAutoFilled() { return autoFilled; }
    public void setAutoFilled(Boolean autoFilled) { this.autoFilled = autoFilled; }
}
