package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "itens")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    String nome;
    Integer quantidadeAtual;
    Integer quantidadeMinima;
    Double precoCusto;
    Double precoVenda;
    Boolean ativo;
    LocalDateTime criadoEm;
    LocalDateTime atualizadoEm;

    @Column(name = "uri_imagem", length = 500)
    private String uriImagem;

    @Column(name = "nome_imagem", length = 255)
    private String nomeImagem;

    @Column(name = "mime_type_imagem", length = 100)
    private String mimeTypeImagem;

    @Column(name = "tamanho_imagem")
    private Integer tamanhoImagem;

    @PrePersist
    public void prePersist() {
        if (this.ativo == null) {
            this.ativo = true;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}