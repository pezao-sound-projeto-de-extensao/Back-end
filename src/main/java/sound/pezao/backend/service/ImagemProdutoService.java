package sound.pezao.backend.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoMapper;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoResponse;
import sound.pezao.backend.entities.ImagemProduto;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ImagemProdutoRepository;
import sound.pezao.backend.repository.ItemRepository;

import java.util.List;

@Service
public class ImagemProdutoService {

    private static final String SUBPASTA = "imagens-produto";
    private static final String MIME_PADRAO = "application/octet-stream";

    private final ImagemProdutoRepository repository;
    private final ItemRepository itemRepository;
    private final ArmazenamentoArquivoService armazenamento;

    public ImagemProdutoService(ImagemProdutoRepository repository,
                                ItemRepository itemRepository,
                                ArmazenamentoArquivoService armazenamento) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.armazenamento = armazenamento;
    }

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ImagemProdutoResponse upload(Integer itemId, MultipartFile arquivo) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item", itemId));

        String caminho = armazenamento.salvar(arquivo, SUBPASTA);
        try {
            ImagemProduto imagem = new ImagemProduto();
            imagem.setItem(item);
            imagem.setNomeArquivo(arquivo.getOriginalFilename());
            imagem.setCaminho(caminho);
            imagem.setMimeType(arquivo.getContentType() != null ? arquivo.getContentType() : MIME_PADRAO);
            imagem.setTamanhoBytes((int) arquivo.getSize());
            return ImagemProdutoMapper.toResponse(repository.save(imagem));
        } catch (RuntimeException e) {
            armazenamento.deletar(caminho);
            throw e;
        }
    }

    public List<ImagemProdutoResponse> listar(Integer itemId) {
        garantirItemExiste(itemId);
        return ImagemProdutoMapper.toResponseList(repository.findByItem_Id(itemId));
    }

    public ArquivoDownload baixar(Integer itemId, Integer imagemId) {
        ImagemProduto imagem = buscarDoItem(itemId, imagemId);
        return new ArquivoDownload(
                armazenamento.carregar(imagem.getCaminho()),
                imagem.getNomeArquivo(),
                imagem.getMimeType()
        );
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public void deletar(Integer itemId, Integer imagemId) {
        ImagemProduto imagem = buscarDoItem(itemId, imagemId);
        repository.delete(imagem);
        armazenamento.deletar(imagem.getCaminho());
    }

    private void garantirItemExiste(Integer itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException("Item", itemId);
        }
    }

    private ImagemProduto buscarDoItem(Integer itemId, Integer imagemId) {
        ImagemProduto imagem = repository.findById(imagemId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem", imagemId));
        if (!imagem.getItem().getId().equals(itemId)) {
            throw new EntityNotFoundException("Imagem", imagemId);
        }
        return imagem;
    }
}
