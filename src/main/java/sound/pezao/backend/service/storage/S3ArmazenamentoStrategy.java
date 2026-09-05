package sound.pezao.backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import sound.pezao.backend.exception.ArquivoInvalidoException;

import java.io.IOException;
import java.util.UUID;

@Component
public class S3ArmazenamentoStrategy implements ArmazenamentoArquivoStrategy {

    private final S3Client s3Client;
    private final String bucket;

    public S3ArmazenamentoStrategy(
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.region}") String region
    ) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    @Override
    public String salvar(MultipartFile arquivo, String pasta) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("Arquivo vazio ou não enviado.");
        }

        String key = pasta + "/" + gerarNomeUnico(arquivo.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(
                            arquivo.getContentType() != null
                                    ? arquivo.getContentType()
                                    : "application/octet-stream"
                    )
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            arquivo.getInputStream(),
                            arquivo.getSize()
                    )
            );

            return key;
        } catch (IOException | RuntimeException e) {
            throw new ArquivoInvalidoException(
                    "Falha ao salvar o arquivo no S3."
            );
        }
    }

    @Override
    public Resource carregar(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return new InputStreamResource(s3Client.getObject(request));
        } catch (NoSuchKeyException e) {
            throw new ArquivoInvalidoException(
                    "Arquivo não encontrado no S3: " + key
            );
        } catch (RuntimeException e) {
            throw new ArquivoInvalidoException(
                    "Falha ao carregar o arquivo do S3."
            );
        }
    }

    @Override
    public void deletar(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (RuntimeException e) {
            throw new ArquivoInvalidoException(
                    "Falha ao remover o arquivo do S3."
            );
        }
    }

    private String gerarNomeUnico(String nomeOriginal) {
        String extensao = "";

        if (nomeOriginal != null) {
            int ponto = nomeOriginal.lastIndexOf('.');

            if (ponto >= 0 && ponto < nomeOriginal.length() - 1) {
                String candidata = nomeOriginal.substring(ponto + 1);

                if (candidata.matches("[A-Za-z0-9]{1,10}")) {
                    extensao = "." + candidata.toLowerCase();
                }
            }
        }

        return UUID.randomUUID() + extensao;
    }
}