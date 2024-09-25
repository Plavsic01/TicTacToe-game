package controller;

import view.swing.TicTacToeGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class TicTacToeController implements ActionListener {
    private TicTacToeGUI view;
    private Socket player;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isMyTurn;


    public TicTacToeController(TicTacToeGUI view) throws IOException {
        this.view = view;
        player = new Socket("localhost",3000);
        in = new BufferedReader(new InputStreamReader(player.getInputStream()));
        out = new PrintWriter(player.getOutputStream(),true);

        new Thread(this::readServerMessage).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        Container parent = button.getParent();
        GridLayout layout = (GridLayout) parent.getLayout();


        int row = 0, col = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i) == button) {
                row = i / layout.getColumns(); // Row
                col = i % layout.getColumns(); // Column
                break;
            }
        }
        sendMove(row,col);
    }

    private void sendMove(int row,int col){
        if(isMyTurn){
            String move = (row + "," + col);
            out.println(move);
            isMyTurn = false;
        }else{
            view.showNotification("It's not your turn yet.");
        }
    }

    private void readServerMessage(){
        try {
            String message;
            while((message = in.readLine()) != null){
                if(message.contains("your turn")){
                 isMyTurn = true;
                }else if(message.contains("Player") || message.contains("Tie")){
                    view.updateGrid(null,true);
                    view.showNotification(message);
                }else{
                    List<String> elements = formatToCharArray(message);
                    view.updateGrid(elements,false);
                }
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private List<String> formatToCharArray(String message){
        String formattedMsg = message.replace("[","").replace("]","");
        formattedMsg = formattedMsg.trim();
        return Arrays.asList(formattedMsg.split(","));
    }





    public void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (player != null && !player.isClosed()) player.close();
            System.out.println("Connection successfully closed...");
        } catch (IOException e) {
            System.out.println("Error while trying to close connections on client side!");
        }
    }


}

