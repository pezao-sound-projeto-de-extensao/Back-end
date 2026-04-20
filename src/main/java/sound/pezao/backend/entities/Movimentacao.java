package sound.pezao.backend.entities;

import jakarta.persistence.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Movimentacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String tipo;
    private Integer quantidade;
    private Integer estoqueAntes;
    private Integer estoqueDepois;
    private LocalDate data;
    private String observacao;
    private LocalDateTime criadoEm;
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getEstoqueAntes() {
        return estoqueAntes;
    }

    public void setEstoqueAntes(Integer estoqueAntes) {
        this.estoqueAntes = estoqueAntes;
    }

    public Integer getEstoqueDepois() {
        return estoqueDepois;
    }

    public void setEstoqueDepois(Integer estoqueDepois) {
        this.estoqueDepois = estoqueDepois;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    @PrePersist
    public void setCriadoSalvo() {
        this.criadoEm = LocalDateTime.now();
    }

}
