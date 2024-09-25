package model.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class PlayerConsole {
    private Socket player;
    private BufferedReader in;
    private PrintWriter out;

    public PlayerConsole() {
        try {
            player = new Socket("localhost",3000);
            in = new BufferedReader(new InputStreamReader(player.getInputStream()));
            out = new PrintWriter(player.getOutputStream(),true);


        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void readMessageFromServer(){
        try {
            String message;
            while((message = in.readLine()) != null){
                System.out.println(message);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendMove(){
        Scanner scanner = new Scanner(System.in);
        String move;
        while ((move = scanner.nextLine()) != null){
            out.println(move);
        }

    }

    public static void main(String[] args) {
        PlayerConsole playerConsole = new PlayerConsole();
        new Thread(playerConsole::readMessageFromServer).start();
        playerConsole.sendMove();

    }

}
