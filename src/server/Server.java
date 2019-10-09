    /*//case login
    try {
        estado = Estados.AUTENTICADO;
        response =  "LOGINRESPONSE";
    }catch (Exception e) {

    }
    */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import util.ArquivoTexto;
import util.Estados;
import util.Mensagem;
import util.Status;

/**
 *
 * @author elder
 */
public class Server {

    private ServerSocket serverSocket;
    int porta;

    public Server() {
    }

    /**
     * @param args the command line arguments 1 - Criar o servidor de conexÃµes
     * 2 -Esperar o um pedido de conexÃ£o; // Outro processo 2.1 e criar uma
     * nova conexÃ£o; 3 - Criar streams de enechar socket de comunicaÃ§Ã£o entre
     * servidor/cliente 4.2 - Fechar streams de entrada e saÃ­da trada e saÃ­da;
     * 4 - Tratar a conversaÃ§Ã£o entre cliente e servidor (tratar protocolo);
     * 4.1 - Fechar socket de comunicaÃ§Ã£o entre servidor/cliente 4.2 - Fechar
     * streams de entrada e saÃ­da
     *
     *
     *
     *
     */
    public static void main(String[] args) {

        try {
            Server server = new Server();
            //1 - Criar o servidor de conexÃµes

            ServerSocket serverSocket = server.criarServerSocket(5555);
            //2 -Esperar o um pedido de conexÃ£o;
            try {
                do {

                    System.out.println("Esperando conexao...");
                    Socket socket = server.esperaConexao(); //bloqueante
                    //3 - Criar streams de enechar socket de comunicaÃ§Ã£o entre servidor/cliente
                    
                    trataConexão(socket);
                    System.out.println("Conexão com cliente estabelecida.");

                } while (true);
            } catch (Exception e) {
                System.out.println("Erro no event loop do main(): " + e.getMessage());
                serverSocket.close();
            }

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

    private static void trataConexão(Socket socket) {
        //tratamento da comunicação com um cliente (socket)
        ObjectOutputStream output = null;
        ObjectInputStream input = null;

        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            output.flush();
            System.out.println("Conexao recebida, inciando protocolo...");
            //iniciar a conversa --- SINCRONO
            String msgResposta = "";
            String operacao = "";
            //Armazena o estado da comunicação com o cliente
            Estados estado = Estados.CONECTADO;

            boolean primeira = true;
            //event loop
            do {
                //leitura
                String msgCliente = input.readUTF(); //bloqueante
                String response = "";
                System.out.println("Mensagem recebida do cliente: " + msgCliente);
                //escrita

                String[] protocolo = msgCliente.split(";");
                operacao = protocolo[0];
                Mensagem resposta = new Mensagem(operacao.toUpperCase()+"RESPONSE");
                switch (estado) {

                    case CONECTADO:
                        System.out.println("Operação atual: " + operacao);
                        switch (operacao) {
                            case "SAIR":
                                try {
                                    resposta.setStatus(Status.OK);
                                } catch (Exception e) {
                                    resposta.setStatus(Status.ERROR);
                                }
                                break;
                            case "LOGIN":
                                try {
                                    if(protocolo[1].equals("usuario:gui") && protocolo[2].equals("senha:123")){
                                         estado = Estados.AUTENTICADO;
                                        resposta.setStatus(Status.OK);
                                    } else {
                                        resposta.setStatus(Status.ERROR);
                                        resposta.setParam("erro", "Usuário ou senha inválidos");
                                    }
                                } catch (Exception e) {
                                    resposta.setStatus(Status.ERROR);
                                    resposta.setParam("erro", "Ocorreu um erro ao realizar o login");
                                }
                                break;
                            default:
                                //mensagem inválida
                                resposta.setStatus(Status.NOTFOUND);
                                resposta.setParam("erro", "Opção inválida");
                                System.out.println("Parando comunicacao com cliente " + socket.getInetAddress());
                                break;
                        }
                        break;
                    case AUTENTICADO:
                        switch (operacao) {
                            //tratamento somente das mensagens possíveis no estado AUTENTICADO
                            case "JOGAR":
                                //validando protocolo (parse)
                                try {
                                    String caminho = "/home/guilherme/NetBeansProjects/ClienteTrunfo/src/cartas.txt";
                                    ArrayList<Trunfo> cartas = ArquivoTexto.leitor(caminho);
                                    for (Trunfo carta : cartas) {
                                        System.out.println(carta.toString());
                                    }
                                    resposta.setStatus(Status.OK);
                                } catch (Exception e) {
                                    resposta.setStatus(Status.ERROR);
                                }
                                break;
                                //exemplo com troca de estados
                            case "LOGOUT":
                                estado = Estados.CONECTADO;
                                try {
                                    resposta.setStatus(Status.OK);
                                } catch (Exception e) {
                                    resposta.setStatus(Status.ERROR);
                                }
                                break;
                            default:
                                //mensagem inválida
                                resposta.setStatus(Status.NOTFOUND);
                                resposta.setParam("erro", "Opção inválida");
                                System.out.println("Parando comunicacao com cliente " + socket.getInetAddress());
                                break;
                        }
                        break;
                }
                //enviar a resposta ao cliente
//                output.writeUTF(response);
                output.writeUTF(resposta.toString());
                output.flush();
            } while (!operacao.equals("pare"));
        } catch (Exception e) {
            System.out.println("Erro no loop de tratamento do cliente: " + socket.getInetAddress().getHostAddress());
        } finally {
            try {
                //fechar as conexões
                output.close();
                input.close();
            } catch (IOException ex) {
                System.out.println("Erro normal ao fechar conexão do cliente..." + ex.getMessage());
            }

        }

    }

}