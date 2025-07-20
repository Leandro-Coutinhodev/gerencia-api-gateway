package com.app.gerencia.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_role")
public class Role {

    @Id
    @Column(name = "role_id")
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Values{

        //Declarando os perfis do sistema através de enumeradores
        ADMIN(1L),
        SECRETARY(2L),
        PROFESSIONAL(3L),
        ASSISTANT(4L);

        Long roleid;
        Values(Long roleid){
            this.roleid = roleid;
        }
    }
}
