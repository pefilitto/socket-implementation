package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Message {
    private String id;
    private String remetente;
    private List<String> destinatarios;
    private String conteudo;
    private LocalDateTime timestamp;
    private TipoMensagem tipo;
    private String grupo; // null se for mensagem privada
    private boolean entregue;
    private boolean lida;

    public enum TipoMensagem {
        PRIVADA, GRUPO, GRUPO_PRIVADA, SISTEMA
    }

    public Message(String remetente, List<String> destinatarios, String conteudo, TipoMensagem tipo) {
        this.id = generateId();
        this.remetente = remetente;
        this.destinatarios = destinatarios;
        this.conteudo = conteudo;
        this.tipo = tipo;
        this.timestamp = LocalDateTime.now();
        this.entregue = false;
        this.lida = false;
    }

    public Message(String remetente, List<String> destinatarios, String conteudo, TipoMensagem tipo, String grupo) {
        this(remetente, destinatarios, conteudo, tipo);
        this.grupo = grupo;
    }

    private String generateId() {
        return System.currentTimeMillis() + "_" + Math.random();
    }

    public String formatarMensagem() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeStr = timestamp.format(formatter);

        switch (tipo) {
            case PRIVADA:
                return String.format("[PRIVADA] %s (%s): %s", remetente, timeStr, conteudo);
            case GRUPO:
                return String.format("[%s] %s (%s): %s", grupo, remetente, timeStr, conteudo);
            case GRUPO_PRIVADA:
                return String.format("[%s - PRIVADA] %s (%s): %s", grupo, remetente, timeStr, conteudo);
            case SISTEMA:
                return String.format("[SISTEMA] (%s): %s", timeStr, conteudo);
            default:
                return String.format("%s (%s): %s", remetente, timeStr, conteudo);
        }
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public List<String> getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(List<String> destinatarios) {
        this.destinatarios = destinatarios;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TipoMensagem getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensagem tipo) {
        this.tipo = tipo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public boolean isEntregue() {
        return entregue;
    }

    public void setEntregue(boolean entregue) {
        this.entregue = entregue;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }
}