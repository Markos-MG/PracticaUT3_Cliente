package org.educa;

import org.educa.game.Player;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        for (int i = 1; i <= 10; i++) {
            Player player = new Player("Jugador" + i, "dados");
            player.start();
        }
    }
}