package org.educa.game;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Player extends Thread {
    private String gameType;
    private final String SERVERHOST = "localhost";
    private final int SERVERPORT = 5555;

    private static String host;
    private static int port;
    private static boolean anfitrion;


    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        System.out.println("Start player");
        System.out.println("Creando socket cliente");

        establecerParametros(busquedaPartida());



    }

    private String busquedaPartida(){
        String respuesta = null;
        try (Socket socket = new Socket(this.SERVERHOST, this.SERVERPORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envia una solicitud al servidor
            writer.println(this.gameType+","+getName());

            // Recibe la respuesta del servidor
            respuesta = reader.readLine();
            System.out.println(getName()+" --- "+respuesta);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return respuesta;
    }

    private static void establecerParametros(String parametros){
        String[] datos = new String[4];
        if(parametros != null){
             datos = parametros.split(",");
        }
        host = datos[0];
    }

    private static void crearPartida(){

    }
    private static void unirsePartida(){

    }

}
