package Server;

import Database.Database;
import Model.Group;
import Model.Message;
import Model.User;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

public class ChatServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Database database;
    private boolean running;

    // Mapa de clientes conectados: login -> Server.ClientHandler
    private ConcurrentHashMap<String, ClientHandler> clientesConectados;

    public ChatServer() {
        this.database = Database.getInstance();
        this.clientesConectados = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.running = false;
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Servidor de Chat iniciado na porta " + PORT);
            System.out.println("Aguardando conexões...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nova conexão de: " + clientSocket.getInetAddress());

                    // Cria um novo handler para o cliente em uma thread separada
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler);

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }

    public void parar() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Desconecta todos os clientes
            for (ClientHandler client : clientesConectados.values()) {
                client.desconectar();
            }

            threadPool.shutdown();
            System.out.println("Servidor parado.");
        } catch (IOException e) {
            System.err.println("Erro ao parar servidor: " + e.getMessage());
        }
    }

    public void adicionarCliente(String login, ClientHandler clientHandler) {
        clientesConectados.put(login, clientHandler);

        // Marca usuário como online no banco
        User usuario = database.getUsuario(login);
        if (usuario != null) {
            usuario.setOnline(true);

            // Envia mensagens pendentes
            enviarMensagensPendentes(login);

            // Notifica outros usuários sobre o status
            notificarMudancaStatus(login, "entrou no chat");
        }
    }

    public void removerCliente(String login) {
        ClientHandler removido = clientesConectados.remove(login);
        if (removido != null) {
            // Marca usuário como offline no banco
            User usuario = database.getUsuario(login);
            if (usuario != null) {
                usuario.setOnline(false);

                // Notifica outros usuários sobre o status
                notificarMudancaStatus(login, "saiu do chat");
            }
        }
    }

    public ClientHandler getClientHandler(String login) {
        return clientesConectados.get(login);
    }

    public boolean isClienteConectado(String login) {
        return clientesConectados.containsKey(login);
    }

    private void enviarMensagensPendentes(String login) {
        List<Message> pendentes = database.getMensagensPendentes(login);
        ClientHandler client = clientesConectados.get(login);

        if (client != null && !pendentes.isEmpty()) {
            client.enviarMensagem("=== MENSAGENS PENDENTES ===");
            for (Message msg : pendentes) {
                client.enviarMensagem(msg.formatarMensagem());
            }
            client.enviarMensagem("=== FIM DAS MENSAGENS PENDENTES ===");

            // Limpa as mensagens pendentes
            database.limparMensagensPendentes(login);
        }
    }

    private void notificarMudancaStatus(String login, String acao) {
        User usuario = database.getUsuario(login);
        if (usuario == null) return;

        String notificacao = String.format("[SISTEMA] %s %s", usuario.getNomeCompleto(), acao);

        // Notifica todos os usuários online
        for (String clienteLogin : clientesConectados.keySet()) {
            if (!clienteLogin.equals(login)) {
                ClientHandler client = clientesConectados.get(clienteLogin);
                if (client != null) {
                    client.enviarMensagem(notificacao);
                }
            }
        }
    }

    public boolean enviarMensagemPrivada(String remetente, String destinatario, String conteudo) {
        ClientHandler clienteDestinatario = clientesConectados.get(destinatario);
        ClientHandler clienteRemetente = clientesConectados.get(remetente);

        if (clienteDestinatario != null) {
            // Destinatário está online
            Message mensagem = new Message(remetente, List.of(destinatario), conteudo, Message.TipoMensagem.PRIVADA);
            database.salvarMensagem(mensagem);

            clienteDestinatario.enviarMensagem(mensagem.formatarMensagem());

            if (clienteRemetente != null) {
                clienteRemetente.enviarMensagem("✓ Mensagem entregue para " + destinatario);
            }
        } else {
            // Destinatário está offline - salva mensagem pendente
            User destinatarioUser = database.getUsuario(destinatario);
            if (destinatarioUser != null) {
                Message mensagem = new Message(remetente, List.of(destinatario), conteudo, Message.TipoMensagem.PRIVADA);
                database.salvarMensagem(mensagem);
                database.adicionarMensagemPendente(destinatario, mensagem);

                if (clienteRemetente != null) {
                    clienteRemetente.enviarMensagem("⏳ Mensagem será entregue quando " + destinatario + " estiver online");
                }
            } else {
                if (clienteRemetente != null) {
                    clienteRemetente.enviarMensagem("✗ Usuário " + destinatario + " não encontrado");
                }
            }
        }

        return true;
    }

    public void enviarMensagemGrupo(String remetente, String nomeGrupo, String conteudo, List<String> destinatariosEspecificos) {
        Group grupo = database.getGrupo(nomeGrupo);
        ClientHandler clienteRemetente = clientesConectados.get(remetente);

        if (grupo == null) {
            if (clienteRemetente != null) {
                clienteRemetente.enviarMensagem("✗ Grupo " + nomeGrupo + " não encontrado");
            }
            return;
        }

        if (!grupo.isMembro(remetente)) {
            if (clienteRemetente != null) {
                clienteRemetente.enviarMensagem("✗ Você não é membro do grupo " + nomeGrupo);
            }
            return;
        }

        List<String> destinatarios;
        Message.TipoMensagem tipoMensagem;

        if (destinatariosEspecificos != null && !destinatariosEspecificos.isEmpty()) {
            // Mensagem para usuários específicos do grupo
            destinatarios = destinatariosEspecificos;
            tipoMensagem = Message.TipoMensagem.GRUPO_PRIVADA;
        } else {
            // Mensagem para todo o grupo
            destinatarios = new ArrayList<>(grupo.getMembros());
            destinatarios.remove(remetente); // Remove o remetente da lista
            tipoMensagem = Message.TipoMensagem.GRUPO;
        }

        Message mensagem = new Message(remetente, destinatarios, conteudo, tipoMensagem, nomeGrupo);
        database.salvarMensagem(mensagem);

        int entregues = 0;
        int pendentes = 0;

        for (String destinatario : destinatarios) {
            if (!grupo.isMembro(destinatario)) {
                continue; // Pula se não for membro do grupo
            }

            ClientHandler clienteDestinatario = clientesConectados.get(destinatario);
            if (clienteDestinatario != null) {
                // Usuário online
                clienteDestinatario.enviarMensagem(mensagem.formatarMensagem());
                entregues++;
            } else {
                // Usuário offline
                database.adicionarMensagemPendente(destinatario, mensagem);
                pendentes++;
            }
        }

        // Confirma para o remetente
        if (clienteRemetente != null) {
            String confirmacao = String.format("✓ Mensagem enviada: %d entregues, %d pendentes", entregues, pendentes);
            clienteRemetente.enviarMensagem(confirmacao);
        }
    }

    public Database getDatabase() {
        return database;
    }

    public static void main(String[] args) {
        ChatServer servidor = new ChatServer();

        // Hook para parar o servidor graciosamente
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::parar));

        servidor.iniciar();
    }
}