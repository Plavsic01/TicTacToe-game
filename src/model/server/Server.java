package model.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {

    private final int serverPort;
    private ServerSocket serverSocket;


    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public void startServer(){
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server is waiting for connection...");
            while (true){
                Socket player1 = serverSocket.accept();
                System.out.println("Player1 connected");
                Socket player2 = serverSocket.accept();
                System.out.println("Player2 connected");

                Session session = new Session(player1,player2);
                Thread thread = new Thread(session);
                thread.start();

            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error while trying to close Server socket - message error: " + e.getMessage());
            }
        }

    }

    public static class Session implements Runnable {
        private final Socket player1;
        private final Socket player2;
        private static int sessionID = 0;
        private final char[][] board = new char[3][3];
        private char currentPlayer = 'X'; // X or O
        private boolean isGameOver = false;

        private BufferedReader player1In;
        private PrintWriter player1Out;

        private BufferedReader player2In;
        private PrintWriter player2Out;

        public Session(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
            sessionID++;
            initializeInputsAndOutputs();
        }

        @Override
        public void run() {
            try {
                gameLogic();
            }finally {
                // CLOSE CONNECTIONS
                closeAllConnections();
                System.out.println("Session is finished");
            }
        }

        private void gameLogic(){
            // Game logic goes here...
            System.out.println("Session: " + sessionID + " started...");

            // create game board
            initializeBoard(); // both players should see the board

            // loop starts here
            while (!isGameOver){
                // player1 sets his move
                String player1Move = playerMove(true);

                currentPlayer = 'X';

                // check if valid or is it win / remi
                boolean isValidMove1 = updateBoardIfValid(player1Move);
                if(isValidMove1){
                    if(checkWin()){
                        // Send message to both players
                        sendBoardToPlayers();
                        sendMessage("Player 1 has won!");
                        isGameOver = true;
                        break;
                    }else if(isTie()){
                        sendBoardToPlayers();
                        sendMessage("It's a Tie!");
                        isGameOver = true;
                        break;
                    }
                }
                // send new board to both players
                sendBoardToPlayers();

                // player2 sets his move
                String player2Move = playerMove(false);

                currentPlayer = 'O';
                // check if valid or is it win / remi
                boolean isValidMove2 = updateBoardIfValid(player2Move);
                if(isValidMove2){
                    if(checkWin()){
                        // Send message to both players
                        sendBoardToPlayers();
                        sendMessage("Player 2 has won!");
                        isGameOver = true;
                        break;
                    }else if(isTie()){
                        sendBoardToPlayers();
                        sendMessage("It's a Tie!");
                        isGameOver = true;
                        break;
                    }
                }
                // send new board to both players
                sendBoardToPlayers();
            }
        }

        private void initializeInputsAndOutputs(){
            try {
                player1Out = new PrintWriter(player1.getOutputStream(),true);
                player2Out = new PrintWriter(player2.getOutputStream(),true);

                player1In = new BufferedReader(new InputStreamReader(player1.getInputStream()));
                player2In = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            }catch (IOException e){
                System.out.println(e.getMessage());
            }
        }


        private void initializeBoard(){
            for(int row = 0; row < board.length;row++){
                for(int column = 0; column < board[0].length;column++){
                    board[row][column] = '-';
                }
            }
        }

        private String playerMove(boolean isPlayer1){
            try {
                if(isPlayer1){
                    player1Out.println("its your turn");
                    return player1In.readLine();
                }else{
                    player2Out.println("its your turn");
                    return player2In.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean updateBoardIfValid(String move){
            if(move != null){
                String[] position = move.split(",");
                int playerRow = Integer.parseInt(position[0]);
                int playerCol = Integer.parseInt(position[1]);

                for(int row = 0; row < board.length;row++){
                    for(int column = 0; column < board[0].length;column++){
                        if(board[playerRow][playerCol] == '-'){
                            board[playerRow][playerCol] = currentPlayer;
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        private void sendBoardToPlayers(){
            player1Out.println(Arrays.deepToString(board));
            player2Out.println(Arrays.deepToString(board));
        }

        private void sendMessage(String message){
            player1Out.println(message);
            player2Out.println(message);
        }

        private boolean checkWin(){
            return checkRightDiagonal() || checkLeftDiagonal() || checkRows() || checkColumns();
        }

        private boolean checkRightDiagonal(){
            return board[0][0] == currentPlayer && board[0][0] == board[1][1] && board[0][0] == board[2][2];
        }

        private boolean checkLeftDiagonal(){
            return board[0][2] == currentPlayer && board[0][2] == board[1][1] && board[0][2] == board[2][0];
        }

        private boolean checkRows(){
            for(int row = 0; row < board.length; row++){
                 if(board[row][0] == board[row][1] &&
                        board[row][0] == board[row][2] &&
                        board[row][0] == currentPlayer){
                     return true;
                 }
            }
            return false;
        }

        private boolean checkColumns(){
            for(int column = 0; column < board[0].length;column++){
                if(board[0][column] == board[1][column] &&
                        board[0][column] == board[2][column] &&
                        board[0][column] == currentPlayer){
                    return true;
                }
            }
            return false;
        }

        private boolean isTie(){
            for(int row = 0; row < board.length;row++){
                for(int column = 0; column < board[0].length;column++){
                    if(board[row][column] == '-'){
                        return false;
                    }
                }
            }
            return true;
        }



        private void closeAllConnections() {
            try {
                if (player1Out != null) {
                    player1Out.close();
                }

                if (player2Out != null) {
                    player2Out.close();
                }

                if (player1In != null) {
                    try {
                        player1In.close();
                    } catch (IOException e) {
                        System.out.println("Error closing player1In: " + e.getMessage());
                    }
                }

                if (player2In != null) {
                    try {
                        player2In.close();
                    } catch (IOException e) {
                        System.out.println("Error closing player2In: " + e.getMessage());
                    }
                }

                if (player1 != null) {
                    try {
                        player1.close();
                    } catch (IOException e) {
                        System.out.println("Error closing player1 socket: " + e.getMessage());
                    }
                }

                if (player2 != null) {
                    try {
                        player2.close();
                    } catch (IOException e) {
                        System.out.println("Error closing player2 socket: " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Error during connection close", e);
            }
        }
    }





    public static void main(String[] args) {
        Server server = new Server(3000);
        server.startServer();
    }

}
