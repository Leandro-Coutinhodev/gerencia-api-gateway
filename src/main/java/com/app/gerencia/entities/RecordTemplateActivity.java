package com.app.gerencia.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

// Item de atividade dentro de um modelo de ficha: referencia uma atividade do banco pessoal
// do profissional e define os campos de registro de execução daquela atividade no modelo.
@Entity
@Table(name = "tb_record_template_activity")
public class RecordTemplateActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_template_activity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RecordTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    // Rótulo opcional exibido no modelo/ficha; quando ausente, usa-se o nome da atividade
    private String label;

    private Integer position;

    @OneToMany(mappedBy = "templateActivity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<RecordTemplateActivityField> fields = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecordTemplate getTemplate() { return template; }
    public void setTemplate(RecordTemplate template) { this.template = template; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public List<RecordTemplateActivityField> getFields() { return fields; }
    public void setFields(List<RecordTemplateActivityField> fields) { this.fields = fields; }
}
