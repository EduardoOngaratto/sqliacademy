package br.edu.uri.sqliacademy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "laboratorio")
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Um laboratorio por modulo
    @OneToOne(optional = false)
    @JoinColumn(name = "modulo_id", nullable = false, unique = true)
    private Modulo modulo;

    // Liga o registro a classe validadora (LAB1, LAB2, LAB3)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(columnDefinition = "TEXT")
    private String explicacao;

    @Column(nullable = false)
    private Integer pontos;

    public Laboratorio() {
    }

    public Laboratorio(Modulo modulo, String codigo, String titulo, String enunciado, String explicacao, Integer pontos) {
        this.modulo = modulo;
        this.codigo = codigo;
        this.titulo = titulo;
        this.enunciado = enunciado;
        this.explicacao = explicacao;
        this.pontos = pontos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String explicacao) {
        this.explicacao = explicacao;
    }

    public Integer getPontos() {
        return pontos;
    }

    public void setPontos(Integer pontos) {
        this.pontos = pontos;
    }
}
