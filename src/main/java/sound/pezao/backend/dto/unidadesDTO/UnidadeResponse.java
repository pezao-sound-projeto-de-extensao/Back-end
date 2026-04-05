package sound.pezao.backend.dto.unidadesDTO;

public class UnidadeResponse {
    private String nome;
    private String abreviacao;

    public UnidadeResponse() {
    }

    public UnidadeResponse(String nome, String abreviacao) {
        this.nome = nome;
        this.abreviacao = abreviacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAbreviacao() {
        return abreviacao;
    }

    public void setAbreviacao(String abreviacao) {
        this.abreviacao = abreviacao;
    }

    
}
