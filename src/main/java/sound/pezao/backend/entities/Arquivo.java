package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "arquivos")
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimentacao_id")
    private Movimentacao movimentacao;

    @Column(name = "tipo_arquivo", nullable = false, length = 20)
    private String tipoArquivo;

    @Column(name = "uri", length = 500)
    private String uri;

    @Column(name = "nome", length = 255)
    private String nome;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamanho")
    private Integer tamanho;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }
}