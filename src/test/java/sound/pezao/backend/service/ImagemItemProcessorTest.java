package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Testes para ImagemItemProcessor")
class ImagemItemProcessorTest {

    @Test
    @DisplayName("Deve gerar icon (120px) e full (900px), ambos WebP, e apagar a antiga")
    void deveGerarVersoesWebp() throws Exception {
        ArmazenamentoArquivoService armazenamento = mock(ArmazenamentoArquivoService.class);
        ImagemItemProcessor processor = new ImagemItemProcessor(mock(RabbitTemplate.class), armazenamento);

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB), "png", png);
        Message mensagem = MessageBuilder.withBody(png.toByteArray())
                .setHeader("id", "abc")
                .setHeader("chavesAnteriores", "imagens/velha.jpg")
                .build();

        processor.processar(mensagem);

        ArgumentCaptor<byte[]> icon = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> full = ArgumentCaptor.forClass(byte[].class);
        verify(armazenamento).salvar(icon.capture(), eq("images/icon/abc.webp"), eq("image/webp"));
        verify(armazenamento).salvar(full.capture(), eq("images/full/abc.webp"), eq("image/webp"));
        verify(armazenamento).deletar("imagens/velha.jpg");

        BufferedImage iconLido = ImageIO.read(new ByteArrayInputStream(icon.getValue()));
        BufferedImage fullLido = ImageIO.read(new ByteArrayInputStream(full.getValue()));
        assertEquals(120, iconLido.getWidth());
        assertEquals(60, iconLido.getHeight());
        assertEquals(900, fullLido.getWidth());
        assertEquals(450, fullLido.getHeight());
    }
}
