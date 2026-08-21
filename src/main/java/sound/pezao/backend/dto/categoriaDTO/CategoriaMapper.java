package sound.pezao.backend.dto.categoriaDTO;

import java.util.List;

import sound.pezao.backend.entities.Categoria;

public class CategoriaMapper {
    public static List<CategoriaResponse> toResponse(List<Categoria> categorias) {
        return categorias.stream()
            .map(categoria -> new CategoriaResponse(categoria.getId(), categoria.getNome()))
            .toList();
    }

    public static Categoria toEntity(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNome(request.nome());
        return categoria;
    }

    public static CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
