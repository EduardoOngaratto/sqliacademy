package br.edu.uri.sqliacademy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "progresso_conteudo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "conteudo_id"}))
public class ProgressoConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conteudo_id", nullable = false)
    private Conteudo conteudo;

    @Column(name = "concluido_em", nullable = false, updatable = false)
    private LocalDateTime concluidoEm;

    public ProgressoConteudo() {
    }

    public ProgressoConteudo(Usuario usuario, Conteudo conteudo) {
        this.usuario = usuario;
        this.conteudo = conteudo;
    }

    @PrePersist
    private void prePersist() {
        this.concluidoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Conteudo getConteudo() {
        return conteudo;
    }

    public void setConteudo(Conteudo conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }

    public void setConcluidoEm(LocalDateTime concluidoEm) {
        this.concluidoEm = concluidoEm;
    }
}
