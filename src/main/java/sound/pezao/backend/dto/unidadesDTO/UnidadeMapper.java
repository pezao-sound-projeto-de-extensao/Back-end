package sound.pezao.backend.dto.unidadesDTO;

import java.util.List;

import sound.pezao.backend.entities.Unidade;

public class UnidadeMapper {
    public static UnidadeResponse toResponse(Unidade unidade){
        return new UnidadeResponse(unidade.getId(), unidade.getNome(), unidade.getAbreviacao());
    }

    public static List<UnidadeResponse> toResponse(List<Unidade> unidades){
        return unidades.stream().map(UnidadeMapper::toResponse).toList();
    }

    public static Unidade toEntity(UnidadeRequest request){
        Unidade unidade = new Unidade();
        unidade.setNome(request.nome());
        unidade.setAbreviacao(request.abreviacao());
        return unidade;
    }
}
