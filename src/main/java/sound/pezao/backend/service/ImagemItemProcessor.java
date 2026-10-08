package sound.pezao.backend.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class ImagemItemProcessor {

    static final String FILA = "imagens.processar";
    static final String FILA_DLQ = FILA + ".dlq";
    static final List<String> TIPOS = List.of("icon", "full");
    static final int LADO_MAXIMO_ICON = 120;
    static final int LADO_MAXIMO_FULL = 900;

    private final RabbitTemplate rabbitTemplate;
    private final ArmazenamentoArquivoService armazenamento;

    public ImagemItemProcessor(
            RabbitTemplate rabbitTemplate,
            ArmazenamentoArquivoService armazenamento
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.armazenamento = armazenamento;
    }

    @Bean
    static Queue filaImagens() {
        return QueueBuilder.durable(FILA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_DLQ)
                .build();
    }

    @Bean
    static Queue filaImagensDlq() {
        return new Queue(FILA_DLQ);
    }

    public static String chave(String id, String tipo) {
        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de imagem inválido: " + tipo + ". Use icon ou full.");
        }
        return "images/" + tipo + "/" + id + ".webp";
    }

    public void enfileirar(String id, byte[] conteudo, List<String> chavesAnteriores) {
        Message mensagem = MessageBuilder.withBody(conteudo)
                .setHeader("id", id)
                .setHeader("chavesAnteriores", String.join(",", chavesAnteriores))
                .build();
        rabbitTemplate.send(FILA, mensagem);
    }

    @RabbitListener(queues = FILA)
    void processar(Message mensagem) {
        String id = mensagem.getMessageProperties().getHeader("id");
        String chavesAnteriores = mensagem.getMessageProperties().getHeader("chavesAnteriores");

        BufferedImage original = ler(mensagem.getBody());
        armazenamento.salvar(webp(redimensionar(original, LADO_MAXIMO_ICON)), chave(id, "icon"), "image/webp");
        armazenamento.salvar(webp(redimensionar(original, LADO_MAXIMO_FULL)), chave(id, "full"), "image/webp");

        assert chavesAnteriores != null;
        for (String chave : chavesAnteriores.split(",")) {
            armazenamento.deletar(chave);
        }
    }

    public static boolean isImagem(byte[] conteudo) {
        try (var entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(conteudo))) {
            return entrada != null && ImageIO.getImageReaders(entrada).hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    static BufferedImage ler(byte[] conteudo) {
        try {
            BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(conteudo));
            if (imagem == null) {
                throw new IllegalArgumentException("Formato de imagem não suportado.");
            }
            return imagem;
        } catch (IOException e) {
            throw new IllegalArgumentException("Imagem inválida.", e);
        }
    }

    static BufferedImage redimensionar(BufferedImage original, int ladoMaximo) {
        double escala = (double) ladoMaximo
                / Math.max(original.getWidth(), original.getHeight());
        if (escala >= 1) {
            return original;
        }

        int largura = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int altura = Math.max(1, (int) Math.round(original.getHeight() * escala));

        BufferedImage reduzida = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = reduzida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, largura, altura, null);
        g.dispose();
        return reduzida;
    }

    static byte[] webp(BufferedImage imagem) {
        try {
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            if (!ImageIO.write(imagem, "webp", saida)) {
                throw new IllegalStateException("Nenhum escritor WebP disponível.");
            }
            return saida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
