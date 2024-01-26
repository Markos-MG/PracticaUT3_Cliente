package org.educa.game;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Player extends Thread {
    private String gameType;
    private final String HOST = "localhost";
    private final int PORT = 5555;
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        System.out.println("Start player");
        System.out.println("Creando socket cliente");
        try (Socket clientSocket = new Socket()) {
            System.out.println("Estableciendo la conexión");
            // Para indicar la dirección IP y el número de puerto del socket stream servidor
            // al que se desea conectar, el método connect() hace uso de un objeto
            // de la clase java.net.InetSocketAddress
            InetSocketAddress addr = new InetSocketAddress(this.HOST, this.PORT);
            clientSocket.connect(addr);
            try (
                    OutputStream os = clientSocket.getOutputStream();
                    // Flujos que manejan caracteres
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    // Flujos de líneas
                    PrintWriter pWriter = new PrintWriter(osw)) {
                System.out.println("Enviando mensaje");
                String mensaje = this.gameType+","+getName();
                pWriter.print(mensaje);
                pWriter.flush();
                System.out.println("Mensaje enviado");
            }
            System.out.println
                    ("Terminado");
        } catch
        (IOException e) {
            //e.printStackTrace();
            System.out.println("Error al conectar con el cliente desde el jugador: "+getName());
        }
    }
}
