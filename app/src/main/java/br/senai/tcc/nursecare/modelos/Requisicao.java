package br.senai.tcc.nursecare.modelos;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.List;

@IgnoreExtraProperties
public class Requisicao implements Serializable {
    private long datahora;
    private double latitude, longitude, valor;
    private String id, cooperativa, enfermeiro, paciente, endereco, cartao, pagamento;
    private List<String> servico;
    private int estado;

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public int getEstado() {
        return estado;
    }

    @Exclude
    public void setEstado(int estado) {
        this.estado = estado;
    }

    public long getDatahora() {
        return datahora;
    }

    public void setDatahora(long datahora) {
        this.datahora = datahora;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getCooperativa() {
        return cooperativa;
    }

    public void setCooperativa(String cooperativa) {
        this.cooperativa = cooperativa;
    }

    public String getEnfermeiro() {
        return enfermeiro;
    }

    public void setEnfermeiro(String enfermeiro) {
        this.enfermeiro = enfermeiro;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCartao() {
        return cartao;
    }

    public void setCartao(String cartao) {
        this.cartao = cartao;
    }

    public String getPagamento() {
        return pagamento;
    }

    public void setPagamento(String pagamento) {
        this.pagamento = pagamento;
    }

    public List<String> getServico() {
        return servico;
    }

    public void setServico(List<String> servico) {
        this.servico = servico;
    }
}
