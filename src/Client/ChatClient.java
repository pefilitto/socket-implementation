package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;
    private Scanner scanner;
    private boolean conectado;

    public ChatClient() {
        scanner = new Scanner(System.in);
        conectado = false;
    }

    public void conectar() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintWriter(socket.getOutputStream(), true);
            conectado = true;

            System.out.println("Conectado ao servidor de chat!");

            // Thread para receber mensagens do servidor
            Thread receberMensagens = new Thread(this::receberMensagens);
            receberMensagens.setDaemon(true);
            receberMensagens.start();

            // Thread principal para enviar mensagens
            enviarMensagens();

        } catch (IOException e) {
            System.err.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    private void receberMensagens() {
        try {
            String mensagem;
            while (conectado && (mensagem = entrada.readLine()) != null) {
                System.out.println(mensagem);
            }
        } catch (IOException e) {
            if (conectado) {
                System.err.println("Conexão com o servidor perdida!");
            }
        }
    }

    private void enviarMensagens() {
        try {
            String mensagem;
            while (conectado && (mensagem = scanner.nextLine()) != null) {
                if (mensagem.trim().isEmpty()) {
                    continue;
                }

                saida.println(mensagem);

                if (mensagem.equalsIgnoreCase("quit")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    private void desconectar() {
        conectado = false;
        try {
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
        System.out.println("Desconectado do servidor.");
    }

    public static void main(String[] args) {
        ChatClient cliente = new ChatClient();

        // Hook para desconectar graciosamente
        Runtime.getRuntime().addShutdownHook(new Thread(cliente::desconectar));

        System.out.println("=== CLIENTE DE CHAT ===");
        System.out.println("Conectando ao servidor...");

        cliente.conectar();
    }
}