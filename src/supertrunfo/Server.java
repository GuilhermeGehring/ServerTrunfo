/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package supertrunfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author 20171pf.cc0178
 */
public class Server {
    
    private ServerSocket serverSocket;
    int porta;
    private ArrayList<Trunfo> cartas;

    public Server() {
        this.cartas = new ArrayList<>();
    }
    
    public static void main(String[] args) {

        try {
            Server server = new Server();
            //1 - Criar o servidor de conexões

            ServerSocket serverSocket = server.criarServerSocket(5555);
            //2 -Esperar o um pedido de conexÃ£o;
            System.out.println("Esperando conexao...");
            Socket socket = server.esperaConexao();
            //3 - Criar streams de enechar socket de comunicaÃ§Ã£o entre servidor/cliente
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            output.flush();

            System.out.println("Conexao recebida, iniciando protocolo...");
            //iniciar a conversa --- SINCRONO
            String msgResposta;
            boolean primeira = true;
            do {
                //leitura
                String msgCliente = input.readUTF();
                System.out.println("Mensagem recebida do cliente: " + msgCliente);
                //escrita

                if (primeira && !msgCliente.equals("oi")) {
                    break;
                }

                primeira = false;

                if (!msgCliente.equals("tchau")) {
                    msgResposta = msgCliente;
                    output.writeUTF(msgResposta);
                    output.flush();
                    System.out.println("Resposta enviada ao cliente: " + msgResposta);
                } else {
                    msgResposta = "pare";
                    output.writeUTF(msgResposta);
                    output.flush();
                    System.out.println("Resposta enviada ao cliente: " + msgResposta);
                }
            } while (!msgResposta.equals("pare"));

            //fechar as conexões
            output.close();
            input.close();
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Erro na main do ServerSocket " + e.);
            System.exit(0);
        }
    }

    private ServerSocket criarServerSocket(int porta) {
        try {
            this.serverSocket = new ServerSocket(porta);
        } catch (Exception e) {
            System.out.println("Erro na Criação do server Socket " + e.getMessage());
        }

        return serverSocket;
    }

    private Socket esperaConexao() {
        try {
            return this.serverSocket.accept();
        } catch (IOException ex) {
            System.out.println("Erro ao criar socket do cliente " + ex.getMessage());
            return null;
        }
    }
}
