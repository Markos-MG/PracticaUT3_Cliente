package org.educa;

import org.educa.game.Player;

public class Main {
    /**
     * Metodo main donde se llevan a cabo la ejecucion de todos los hilos de los jugadores
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 1; i <= 10; i++) {
            Player player = new Player("Jugador" + i, "dados");
            player.start();
        }
    }
}