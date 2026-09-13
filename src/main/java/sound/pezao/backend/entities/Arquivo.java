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

    @Column(name = "tabela_origem", nullable = false, length = 50)
    private String tabelaOrigem; // "item" ou "movimentacao"

    @Column(name = "registro_id", nullable = false)
    private Integer registroId;

    @Column(name = "tipo_arquivo", nullable = false, length = 20)
    private String tipoArquivo; // "imagem" ou "nota_entrada"

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