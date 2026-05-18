package sound.pezao.backend.dto.imagemProdutoDTO;

import sound.pezao.backend.entities.ImagemProduto;

import java.util.List;

public class ImagemProdutoMapper {

    public static ImagemProdutoResponse toResponse(ImagemProduto imagem) {
        return new ImagemProdutoResponse(
                imagem.getId(),
                imagem.getNomeArquivo(),
                imagem.getMimeType(),
                imagem.getTamanhoBytes(),
                imagem.getCriadoEm(),
                "/api/itens/" + imagem.getItem().getId() + "/imagens/" + imagem.getId() + "/download"
        );
    }

    public static List<ImagemProdutoResponse> toResponseList(List<ImagemProduto> imagens) {
        return imagens.stream().map(ImagemProdutoMapper::toResponse).toList();
    }
}
