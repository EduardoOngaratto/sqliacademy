package br.edu.uri.sqliacademy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "resposta",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tentativa_id", "questao_id"}))
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tentativa_id", nullable = false)
    private TentativaQuestionario tentativa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alternativa_id", nullable = false)
    private Alternativa alternativa;

    public Resposta() {
    }

    public Resposta(TentativaQuestionario tentativa, Questao questao, Alternativa alternativa) {
        this.tentativa = tentativa;
        this.questao = questao;
        this.alternativa = alternativa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TentativaQuestionario getTentativa() {
        return tentativa;
    }

    public void setTentativa(TentativaQuestionario tentativa) {
        this.tentativa = tentativa;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public Alternativa getAlternativa() {
        return alternativa;
    }

    public void setAlternativa(Alternativa alternativa) {
        this.alternativa = alternativa;
    }
}
