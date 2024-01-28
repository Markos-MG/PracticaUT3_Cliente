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
    private String host;
    private int port;
    private boolean anfitrion;

    private int id_sala;
    /////////////////////////////////////////////

    /////////////// DATOS RIVALES ///////////////
    private String host_Invitado;
    private int port_Invitado;
    private String nombre_Invitado;
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
                partidaFinalizada();
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
            mostrarParticipantes(respuesta);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error al conectar con el servidor --"+getName());
        }
        return respuesta;
    }

    private void partidaFinalizada(){
        try (Socket socket = new Socket(this.SERVERHOST, this.SERVERPORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envia una solicitud al servidor
            writer.println(this.id_sala);

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error al conectar con el servidor --"+getName());
        }
    }
    private boolean establecerParametros(String parametros) {
        if(parametros!=null) {
            if (!parametros.equalsIgnoreCase("error")) {
                String[] datos = parametros.split(",");

                host = datos[0];
                port = Integer.parseInt(datos[1]);
                anfitrion = datos[3].equalsIgnoreCase("anfitrion");
                id_sala = Integer.parseInt(datos[4]);
                host_Invitado = datos[5];
                port_Invitado = Integer.parseInt(datos[6]);
                nombre_Invitado = datos[7];


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

        //System.out.println("Creando socket servidor");
        try (ServerSocket serverSocket = new ServerSocket();) {
            //System.out.println("Realizando el bind ");
            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            //asigna el socket a una dirección y puerto
            serverSocket.bind(addr);
            //System.out.println("Aceptando conexiones "+port+getName());
            try (Socket newSocket = serverSocket.accept();
                 InputStream is = newSocket.getInputStream();
                 OutputStream os = newSocket.getOutputStream();
                 // Flujos que manejan caracteres
                 InputStreamReader isr = new InputStreamReader(is);
                 OutputStreamWriter osw = new OutputStreamWriter(os);
                 // Flujos de líneas
                 BufferedReader bReader = new BufferedReader(isr);
                 PrintWriter pWriter = new PrintWriter(osw);) {


                String tiradaInvitado = bReader.readLine();///////mmmmmm
                //System.out.println("invitado:"+tiradaInvitado);
                String tiradaAnfitrion = tirarDados();

                //System.out.println("anditrion:"+tiradaAnfitrion);


                while (compGanador(tiradaAnfitrion, tiradaInvitado).equals("E")){
                    System.out.println("empate en sala "+id_sala);
                    pWriter.println("E");///////---------
                    pWriter.flush();

                    tiradaInvitado = bReader.readLine();///////mmmmmm
                    //System.out.println("invitado:"+tiradaInvitado);

                    tiradaAnfitrion = tirarDados();
                    //System.out.println("anditrion:"+tiradaAnfitrion);

                }

                String resultado = compGanador(tiradaAnfitrion, tiradaInvitado);


                System.out.println(resultado);
                pWriter.println(resultado);///////---------
                pWriter.flush();
                System.out.println("-------------------------");

            }

        } catch
        (IOException e) {
            //e.printStackTrace();
            System.out.println("hola buenas");
        }
    }

    private void unirsePartida() {

        //System.out.println("Creando socket cliente");
        try (Socket clientSocket = new Socket()) {
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


                pWriter.println(tirarDados());///////---------
                pWriter.flush();

                String resultado = bReader.readLine();///////mmmmmm


                while (resultado.equals("E")){
                    pWriter.println(tirarDados());///////---------
                    pWriter.flush();

                    resultado = bReader.readLine();///////mmmmmm
                }

                //System.out.println("soy "+getName()+"  :"+resultado);

                //System.out.println("Mensaje enviado");
            }
            //System.out.println("Terminado");
        } catch
        (IOException e) {
            e.printStackTrace();
        }
    }

    public String tirarDados() {
        esperar(1000*id_sala);
        return String.valueOf((int) (Math.random()*6)+1);
    }

    public String compGanador(String anfitrion, String invitado){
        if (Integer.parseInt(anfitrion)>Integer.parseInt(invitado)){
            return "V";
        }else if (Integer.parseInt(anfitrion)<Integer.parseInt(invitado)){
            return "D";
        }else{
            return "E";
        }
    }

    public void mostrarParticipantes(String infoString){
        String[] info = infoString.split(",");
        int n_rivals = (info.length-5)/3;

        System.out.print("===== "+getName()+" ===== ");
        if (info[3].equals("anfitrion")){
            System.out.println("(Anfitrion)");
        }else{
            System.out.println();
        }
        System.out.println("-"+info[2]);
        for (int i = 0; i < n_rivals; i++) {
            System.out.println("-"+info[(i*3)+7]);
        }
        System.out.println("====================\n");

    }
}
