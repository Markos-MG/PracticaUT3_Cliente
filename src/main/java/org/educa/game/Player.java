package org.educa.game;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Player extends Thread {

    /////////////// DATOS SERVIDOR ///////////////
    private String gameType;
    private final String SERVERHOST = "localhost";
    private final int SERVERPORT = 5555;
    /////////////////////////////////////////////

    /////////////// DATOS JUGADOR ///////////////
    private static String host;
    private int port;
    private static boolean anfitrion;
    /////////////////////////////////////////////
    /////////////// DATOS RIVALES ///////////////
    private static String host_Invitado;
    private static int port_Invitado;
    private static String nombre_Invitado;
    //private static ArrayList<String[]> datosRivales;
    /////////////////////////////////////////////


    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        //System.out.println("Start player");
        //System.out.println("Creando socket cliente");

        boolean ok = establecerParametros(busquedaPartida());

        esperar(1000);
        if(ok){
            if(anfitrion){
                crearPartida();
            }else {
                esperar(1000);
                unirsePartida();
            }
        }

    }

    private void esperar(int tiempo) {
        try {
            sleep(tiempo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String busquedaPartida() {
        String respuesta = null;
        try (Socket socket = new Socket(this.SERVERHOST, this.SERVERPORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envia una solicitud al servidor
            writer.println(this.gameType + "," + getName());

            // Recibe la respuesta del servidor
            respuesta = reader.readLine();
            System.out.println(getName() + " --- " + respuesta);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error al conectar con el servidor --"+getName());
        }
        return respuesta;
    }

    private boolean establecerParametros(String parametros) {
        if(parametros!=null) {
            if (!parametros.equalsIgnoreCase("error")) {
                String[] datos = parametros.split(",");

                host = datos[0];
                //System.out.println("antes"+datos[1]);
                port = Integer.parseInt(datos[1]);
                //System.out.println("despues"+port);
                if (datos[3].equalsIgnoreCase("anfitrion")) {
                    anfitrion = true;
                } else {
                    anfitrion = false;
                }

                host_Invitado = datos[4];
                port_Invitado = Integer.parseInt(datos[5]);
                nombre_Invitado = datos[6];
                return true;
            } else {
                System.out.println("Jugador " + getName() + " no ha encontrado partida");
                return false;
            }
        }else {
            return false;
        }
    }

    private void crearPartida() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor esperando conexiones...");

            // Espera a que un cliente se conecte
            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado");

            // Configura flujos de entrada y salida
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                // Lee el mensaje del cliente
                String clientMessage = reader.readLine();
                System.out.println("Mensaje recibido del cliente: " + clientMessage);

                // Envia un mensaje de respuesta al cliente
                writer.println("Hola, soy el servidor. ¡Mensaje recibido!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


/*
        //System.out.println("Creando socket servidor");
        try (ServerSocket serverSocket = new ServerSocket();) {
            //System.out.println("Realizando el bind ");
            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            //asigna el socket a una dirección y puerto
            serverSocket.bind(addr);
            System.out.println("Aceptando conexiones "+port+getName());
            try (Socket newSocket = serverSocket.accept();
                 InputStream is = newSocket.getInputStream();
                 OutputStream os = newSocket.getOutputStream();
                 // Flujos que manejan caracteres
                 InputStreamReader isr = new InputStreamReader(is);
                 OutputStreamWriter osw = new OutputStreamWriter(os);
                 // Flujos de líneas
                 BufferedReader bReader = new BufferedReader(isr);
                 PrintWriter pWriter = new PrintWriter(osw);) {
                System.out.println("Conexión recibida");
                String mensaje = bReader.readLine();
                System.out.println("Mensaje recibido: " + mensaje);
            }
            //System.out.println("Cerrando el nuevo socket");
            //System.out.println("Cerrando el socket servidor");
            //System.out.println("Terminado");
        } catch
        (IOException e) {
            //e.printStackTrace();
            System.out.println("hola buenas");
        }*/
    }

    private void unirsePartida() {

        try (Socket socket = new Socket("localhost", port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envía un mensaje al servidor
            writer.println("Hola, soy el cliente. ¿Cómo estás?");

            // Lee la respuesta del servidor
            String serverResponse = reader.readLine();
            System.out.println("Respuesta del servidor: " + serverResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }


        /*
        //System.out.println("Creando socket cliente");
        try (Socket clientSocket = new Socket()) {
            //System.out.println("Estableciendo la conexión");
            // Para indicar la dirección IP y el número de puerto del socket stream servidor
            // al que se desea conectar, el método connect() hace uso de un objeto
            // de la clase java.net.InetSocketAddress
            System.out.println("ENviendo info"+port+getName());
            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            clientSocket.connect(addr);
            try (InputStream is = clientSocket.getInputStream();
                     OutputStream os = clientSocket.getOutputStream();
                     // Flujos que manejan caracteres
                     InputStreamReader isr = new InputStreamReader(is);
                     OutputStreamWriter osw = new OutputStreamWriter(os);
                     // Flujos de líneas
                     BufferedReader bReader = new BufferedReader(isr);
                     PrintWriter pWriter = new PrintWriter(osw)) {
                //System.out.println("Enviando mensaje");
                String mensaje = "-----------------------"+getName();
                pWriter.print(mensaje);
                pWriter.flush();
                //System.out.println("Mensaje enviado");
            }
            System.out.println("Terminado");
        } catch
        (IOException e) {
            e.printStackTrace();
        }*/
    }

}
