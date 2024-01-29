package org.educa.game;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class Player extends Thread {

    /////////////// DATOS SERVIDOR ///////////////
    private String gameType;
    private final String SERVERHOST = "localhost";
    private final int SERVERPORT = 5555;
    /////////////////////////////////////////////

    /////////////// DATOS JUGADOR ///////////////
    private String host;
    private int port;
    private boolean amphitryon;

    private int id_hall;
    /////////////////////////////////////////////

    /////////////// DATOS RIVALES ///////////////
    private String host_guest;
    private int port_guest;
    private String name_guest;

    //private static ArrayList<String[]> datosRivales;
    /////////////////////////////////////////////


    /**
     * Constructor para crear los jugadores desde el Cliente
     * @param name Nickname del jugador
     * @param gameType Tipo de juego al que va a jugar
     */
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    /**
     * Metodo run para cada hilo player que se ejecuta
     */
    @Override
    public void run() {
        //Primero recibe todos los datos necesarios de la partida de los dos jugadores
        boolean ok = setParameters(searchGame());
        waiting(1000);
        //si los datos se han recibido con exito
        if(ok){
            if(amphitryon){//y si se trata del invitado crea la partida y posteriormente se encarga de finalizarla
                createGame();
                gameFinished();
            }else {// en caso de no ser anfitrion es invitado y simplemente se unira a la partida creada por el anfitrion
                waiting(1000);
                joinGame();
            }
        }
    }

    /**
     * Metodo para establecer un teimpo de espera
     * @param tiempo
     */
    private void waiting(int tiempo) {
        try {
            sleep(tiempo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo para encontrar una partida en el servidor
     * @return
     */
    private String searchGame() {
        String respuesta = null;
        try (Socket socket = new Socket(this.SERVERHOST, this.SERVERPORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envia una solicitud al servidor
            writer.println(this.gameType + "," + getName());

            // Recibe la respuesta del servidor
            respuesta = reader.readLine();
            if(gameType.equals("dados")){
                showPlayersDados(respuesta);//Muestra la informacion de la sala encontrada en el servidor
            }else{
                showPlayers(respuesta);
            }

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error al conectar con el servidor --"+getName());
        }
        return respuesta;
    }

    /**
     * Metodo para finalizar el la partida
     */
    private void gameFinished(){
        try (Socket socket = new Socket(this.SERVERHOST, this.SERVERPORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envia una solicitud al servidor con el id de la sala que tiene que finalizar
            writer.println(this.id_hall);

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error al conectar con el servidor --"+getName());
        }
    }

    /**
     * Metodo para establecer los parametros necesarios al anfitrio e invitado en una partida
     * @param parametros parametro a establecer extraidos a traves de la busqueda de una partida en el servidor
     * @return devuelve true o false dependiendo de si se establecen correctamente los parametros en los participantes
     */
    private boolean setParameters(String parametros) {
        if(parametros!=null) {
            if (!parametros.equalsIgnoreCase("error")) {
                String[] datos = parametros.split(",");

                host = datos[0];
                port = Integer.parseInt(datos[1]);
                amphitryon = datos[3].equalsIgnoreCase("anfitrion");
                id_hall = Integer.parseInt(datos[4]);
                host_guest = datos[5];
                port_guest = Integer.parseInt(datos[6]);
                name_guest = datos[7];
                return true;
            } else {
                System.out.println("Jugador " + getName() + " no ha encontrado partida");
                return false;
            }
        }else {
            return false;
        }
    }

    /*
     * Metodo para crear la partida
     */
    private void createGame() {

        //System.out.println("Creando socket servidor");
        try (ServerSocket serverSocket = new ServerSocket();) {
            //System.out.println("Realizando el bind ");
            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            //asigna el socket a una dirección y puerto
            serverSocket.bind(addr);
            try (Socket newSocket = serverSocket.accept();
                 InputStream is = newSocket.getInputStream();
                 OutputStream os = newSocket.getOutputStream();
                 // Flujos que manejan caracteres
                 InputStreamReader isr = new InputStreamReader(is);
                 OutputStreamWriter osw = new OutputStreamWriter(os);
                 // Flujos de líneas
                 BufferedReader bReader = new BufferedReader(isr);
                 PrintWriter pWriter = new PrintWriter(osw);) {

                //Empieza tirando el invitado
                String tiradaInvitado = bReader.readLine();
                String tiradaAnfitrion = roll();
                System.out.println("\tTirada del invitado "+name_guest+": "+tiradaInvitado);
                System.out.println("\tTirada del anfitrion "+getName() +": "+tiradaAnfitrion);
                while (checkWinner(tiradaAnfitrion, tiradaInvitado).equals("E")){
                    System.out.println("Empate en sala "+ id_hall);
                    pWriter.println("E");//en caso de empate el anfitrion le envia la letra E al invitado para que vuelva a tirar
                    pWriter.flush();

                    tiradaInvitado = bReader.readLine();
                    tiradaAnfitrion = roll();
                }
                if(checkWinner(tiradaAnfitrion, tiradaInvitado).equals("V")){
                    System.out.println("\t\tGana el anfitrion "+getName());
                }else if(checkWinner(tiradaAnfitrion, tiradaInvitado).equals("D")){
                    System.out.println("\t\tGana el invitado "+name_guest);
                }
                //en el momento que no es empate recoge la letra correspondiente al resultado de la partida
                String resultado = checkWinner(tiradaAnfitrion, tiradaInvitado);

                //lo imprime y envia al invitado para que este tambien termine
                //System.out.println(resultado);
                pWriter.println(resultado);
                pWriter.flush();
            }

        } catch
        (IOException e) {
            //e.printStackTrace();
            System.out.println("Error de conexion");
        }
    }

    /**
     * Metodo para que el invitadi se una la partida y juege la partida estableciendo los flujos necesarios para la comincacion
     * necesaria entre sockets
     */
    private void joinGame() {

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

                //El invitado hace una tirada de dados
                pWriter.println(roll());
                pWriter.flush();

                String resultado = bReader.readLine();
                //si la respuesta del anfitrion a la tirada del invitado es "E" significa empate y el invitado volvera a tirar hasta que
                //el anfitrion devuelva otra cosa , lo cual significaria la victoria o derrota del anfitrion

                while (resultado.equals("E")){
                    pWriter.println(roll());
                    pWriter.flush();

                    resultado = bReader.readLine();
                }
            }
        } catch
        (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para generar un numero aleatorio del 1 al 6 y simular la tirada de los dados
     * @return Devuelve el numero resultante de la tirada aleatoria
     */
    public String roll() {
        return String.valueOf((int) (Math.random()*6)+1);
    }

    /**
     * Metodo para determinar el ganador de la ronda entre el anfitrion y el invitado en el juego de dados
     * @param anfitrion Resultado de la tirada del anfitrion
     * @param invitado Resultado de la tirada del invitado
     * @return Devuelve la letra correspondiente al resultado de la partida entre los dos jugadores
     */
    public String checkWinner(String anfitrion, String invitado){
        if (Integer.parseInt(anfitrion)>Integer.parseInt(invitado)){
            return "V";//Si gana el anfitrion
        }else if (Integer.parseInt(anfitrion)<Integer.parseInt(invitado)){
            return "D";//Si pierde el anfitrion
        }else{
            return "E";//En caso de empate
        }
    }

    /**
     * Metodo para mostrar todos los jugadores de una sala
     * @param infoString Cadena csv de todos los datos de los jugadores de una sala 
     */
    public synchronized void showPlayers(String infoString){
        String[] info = infoString.split(",");
        int n_rivals = (info.length-5)/3;//Se identifica el numero de participantes que existe en la partida

        System.out.print("===== "+getName()+" ===S:"+ info[4]);
        if (info[3].equals("anfitrion")){//si el participante es anfitrion
            System.out.println("(Anfitrion)");
        }else{
            System.out.println();
        }
        System.out.println("-"+info[2]);
        for (int i = 0; i < n_rivals; i++) {
            System.out.println("-"+info[(i*3)+7]);//Muestra el nickname de cada rival que hay en la sala
        }
        System.out.println("====================\n");

    }

    /**
     * Metodo para mostrar todos los jugadores de una sala
     * @param infoString Cadena csv de todos los datos de los jugadores de una sala
     */
    public synchronized void showPlayersDados(String infoString){
        if ( infoString!= null) {
            String[] info = infoString.split(",");
            System.out.println("=== " + getName() + " === S:" + info[4] + "\n-" + info[2] + "\n-" + info[7] + "\n====================\n");
        }
    }

}
