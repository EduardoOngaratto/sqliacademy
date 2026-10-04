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

import java.time.LocalDateTime;

@Entity
@Table(name = "tentativa_questionario")
public class TentativaQuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(nullable = false)
    private Integer acertos;

    @Column(nullable = false)
    private Integer total;

    @Column(name = "realizado_em", nullable = false, updatable = false)
    private LocalDateTime realizadoEm;

    public TentativaQuestionario() {
    }

    public TentativaQuestionario(Usuario usuario, Modulo modulo, Integer acertos, Integer total) {
        this.usuario = usuario;
        this.modulo = modulo;
        this.acertos = acertos;
        this.total = total;
    }

    @PrePersist
    private void prePersist() {
        this.realizadoEm = LocalDateTime.now();
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

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Integer getAcertos() {
        return acertos;
    }

    public void setAcertos(Integer acertos) {
        this.acertos = acertos;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public LocalDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public void setRealizadoEm(LocalDateTime realizadoEm) {
        this.realizadoEm = realizadoEm;
    }
}
