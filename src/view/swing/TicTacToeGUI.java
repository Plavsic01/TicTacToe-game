package view.swing;

import controller.TicTacToeController;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicTacToeGUI extends JFrame {

    private final List<JButton> buttons = new ArrayList<>();

    public TicTacToeGUI() throws HeadlessException, IOException {
        super("TicTacToe Game");

        TicTacToeController controller = new TicTacToeController(this);

        createButtons(controller);
        setButtonsToLayout();


        this.setLayout(new GridLayout(3,3));
        this.setSize(400,400);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Do you want to exit?", "Close Application",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    controller.closeConnection();
                    System.exit(0);
                }
            }
        });
    }


    private void createButtons(TicTacToeController controller){
        for(int i = 0; i < 9; i++){
            JButton btn = new JButton("-");
            btn.addActionListener(controller);
            buttons.add(btn);
        }
    }

    private void setButtonsToLayout(){
        for (JButton button : buttons) {
            this.add(button);
        }
    }

    public void updateGrid(List<String> elements,boolean isGameOver){
        if(isGameOver){
            for(int i = 0; i < buttons.size(); i++){
                buttons.get(i).setEnabled(false);
            }
        }else{
            for(int i = 0; i < buttons.size(); i++){
                JButton button = buttons.get(i);
                button.setText(elements.get(i));
            }
        }
    }

    public void showNotification(String msg){
        JOptionPane.showMessageDialog(null,msg);
    }

    public static void main(String[] args) throws IOException {
        new TicTacToeGUI();
    }
}