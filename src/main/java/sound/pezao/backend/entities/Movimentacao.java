package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "movimentacoes")
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

    @Column(name = "uri_nota_entrada", length = 500)
    private String uriNotaEntrada;

    @Column(name = "nome_nota_entrada", length = 255)
    private String nomeNotaEntrada;

    @Column(name = "mime_type_nota_entrada", length = 100)
    private String mimeTypeNotaEntrada;

    @Column(name = "tamanho_nota_entrada")
    private Integer tamanhoNotaEntrada;

    @PrePersist
    public void setCriadoSalvo() {
        this.criadoEm = LocalDateTime.now();
    }
}