package com.app.gerencia.entities;

import jakarta.persistence.*;


@Entity
@Table(name = "tb_contract_clause")
public class ContractClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_clause_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id", nullable = false)
    private ContractTemplate template;

    @Column(name = "clause_order", nullable = false)
    private Integer clauseOrder;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractTemplate getTemplate() { return template; }
    public void setTemplate(ContractTemplate template) { this.template = template; }

    public Integer getClauseOrder() { return clauseOrder; }
    public void setClauseOrder(Integer clauseOrder) { this.clauseOrder = clauseOrder; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
