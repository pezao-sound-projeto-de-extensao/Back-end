package sound.pezao.backend.dto.movimentacaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import sound.pezao.backend.dto.cargoDTO.CargoResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Dados retornados de uma movimentacao")
public record MovimentacaoResponse(
        @Schema(description = "ID da Movimentacao", example = "1")
        Integer id,

        @Schema(description = "Item da Movimentacao")
        ItemMovimentacao item,

        @Schema(description = "Usuario da Movimentacao")
        UsuarioMovimentacao usuario,

        @Schema(description = "Tipo da Movimentacao", example = "entrada")
        String tipo,

        @Schema(description = "Quantidade da Movimentacao", example = "5")
        Integer quantidade,

        @Schema(description = "Estoque Antes da Movimentacao", example = "0")
        Integer estoqueAntes,

        @Schema(description = "Estoque Depois da Movimentacao", example = "5")
        Integer estoqueDepois,

        @Schema(description = "Data da Movimentacao", example = "2026-03-24")
        LocalDate data,

        @Schema(description = "Observação da Movimentacao", example = "Compra fornecedor João, NF 1042")
        String observacao,

        @Schema(description = "ID do Usuario", example = "1")
        LocalDateTime criadoEm
) {
    public record CargoUsuarioMovimentacao(
            @Schema(description = "ID do cargo", example = "1")
            Integer id,

            @Schema(description = "Nome do cargo", example = "Supervisor")
            String nome
    ) {}

    public record UsuarioMovimentacao(
            @Schema(description = "ID do Usuario", example = "1")
            Integer id,

            @Schema(description = "Nome do Usuario", example = "Alexandre Gomes")
            String nome,

            @Schema(description = "Email do Usuario", example = "ale.gomes@gmail.com")
            String email,

            @Schema(description = "Cargo do Usuario")
            CargoUsuarioMovimentacao cargo
    ) {}

    public record ItemMovimentacao(
            @Schema(description = "ID do Item", example = "1")
            Integer id,

            @Schema(description = "Nome do Item", example = "Módulo amplificador 400W")
            String nome
    ) {}
}