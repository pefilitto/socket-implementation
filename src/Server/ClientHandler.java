package Server;

import Database.Database;
import Model.Group;
import Model.User;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;
    private ChatServer servidor;
    private Database database;
    private String loginUsuario;
    private boolean conectado;
    private CommandProcessor commandProcessor;

    public ClientHandler(Socket socket, ChatServer servidor) {
        this.socket = socket;
        this.servidor = servidor;
        this.database = servidor.getDatabase();
        this.conectado = true;
        this.commandProcessor = new CommandProcessor(this, servidor, database);

        try {
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.saida = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Erro ao criar streams: " + e.getMessage());
            desconectar();
        }
    }

    @Override
    public void run() {
        try {
            enviarMensagem("=== BEM-VINDO AO CHAT ===");
            enviarMensagem("Digite 'help' para ver os comandos disponíveis");
            enviarMensagem("Digite 'login <usuario> <senha>' para entrar ou 'register' para se cadastrar");

            String mensagem;
            while (conectado && (mensagem = entrada.readLine()) != null) {
                if (mensagem.trim().isEmpty()) {
                    continue;
                }

                System.out.println("Cliente " + (loginUsuario != null ? loginUsuario : socket.getInetAddress()) +
                        ": " + mensagem);

                // Processa o comando
                commandProcessor.processarComando(mensagem.trim());
            }

        } catch (IOException e) {
            if (conectado) {
                System.err.println("Erro na comunicação com cliente: " + e.getMessage());
            }
        } finally {
            desconectar();
        }
    }

    public void enviarMensagem(String mensagem) {
        if (saida != null && !socket.isClosed()) {
            saida.println(mensagem);
        }
    }

    public void desconectar() {
        conectado = false;

        // Remove cliente do servidor se estiver logado
        if (loginUsuario != null) {
            servidor.removerCliente(loginUsuario);

            // Remove usuário de todos os grupos ao desconectar
            User usuario = database.getUsuario(loginUsuario);
            if (usuario != null) {
                for (String nomeGrupo : usuario.getGruposParticipando()) {
                    Group grupo = database.getGrupo(nomeGrupo);
                    if (grupo != null) {
                        // Notifica outros membros do grupo
                        notificarSaidaGrupo(nomeGrupo, loginUsuario, "desconectou do chat");
                    }
                }
            }
        }

        try {
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }

        System.out.println("Cliente desconectado: " +
                (loginUsuario != null ? loginUsuario : socket.getInetAddress()));
    }

    public void setLoginUsuario(String login) {
        this.loginUsuario = login;
    }

    public String getLoginUsuario() {
        return loginUsuario;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void notificarSaidaGrupo(String nomeGrupo, String usuario, String motivo) {
        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) return;

        User user = database.getUsuario(usuario);
        String nomeCompleto = user != null ? user.getNomeCompleto() : usuario;

        String notificacao = String.format("[%s] %s %s", nomeGrupo, nomeCompleto, motivo);

        // Notifica todos os membros do grupo
        for (String membro : grupo.getMembros()) {
            if (!membro.equals(usuario)) {
                ClientHandler clienteMembro = servidor.getClientHandler(membro);
                if (clienteMembro != null) {
                    clienteMembro.enviarMensagem(notificacao);
                }
            }
        }
    }

    public void mostrarAjuda() {
        enviarMensagem("\n=== COMANDOS DISPONÍVEIS ===");
        enviarMensagem("AUTENTICAÇÃO:");
        enviarMensagem("  register - Cadastrar novo usuário");
        enviarMensagem("  login <usuario> <senha> - Fazer login");
        enviarMensagem("  logout - Fazer logout");
        enviarMensagem("  recover <email> - Recuperar senha");
        enviarMensagem("");
        enviarMensagem("STATUS:");
        enviarMensagem("  status <online|offline|ocupado|ausente> - Alterar status");
        enviarMensagem("  users - Listar usuários online");
        enviarMensagem("  groups - Listar grupos disponíveis");
        enviarMensagem("");
        enviarMensagem("MENSAGENS PRIVADAS:");
        enviarMensagem("  msg <usuario> <mensagem> - Enviar mensagem privada");
        enviarMensagem("  accept <usuario> - Aceitar solicitação de conversa");
        enviarMensagem("  reject <usuario> - Rejeitar solicitação de conversa");
        enviarMensagem("");
        enviarMensagem("GRUPOS:");
        enviarMensagem("  create <nome_grupo> - Criar grupo");
        enviarMensagem("  invite <grupo> <usuario> - Convidar usuário para grupo");
        enviarMensagem("  join <grupo> - Solicitar entrada em grupo");
        enviarMensagem("  leave <grupo> - Sair do grupo");
        enviarMensagem("  group <grupo> <mensagem> - Enviar mensagem para grupo");
        enviarMensagem("  group <grupo> <mensagem> -> <usuario1,usuario2> - Mensagem para usuários específicos");
        enviarMensagem("  <mensagem> -> <grupo>@<usuario> - Mensagem particular no grupo");
        enviarMensagem("");
        enviarMensagem("OUTROS:");
        enviarMensagem("  help - Mostrar esta ajuda");
        enviarMensagem("  quit - Sair do chat");
        enviarMensagem("==============================\n");
    }
}