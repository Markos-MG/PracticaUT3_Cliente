package org.educa.game;

public class Player extends Thread {
    private String gameType;
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        System.out.println("Start player");
        //TODO
    }
}
