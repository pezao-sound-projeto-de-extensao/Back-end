package sound.pezao.backend.relatorio.dto;

public record ItemCriticoDTO (
        String nome,
        Integer quantidadeAtual,
        Integer quantidadeMinima,
        String status
){

}
