import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;

public class Blackjack {
    private static class Card {
        String rank;
        String suit;

        Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String toString() {
            return rank + "-" + suit;
        }

        public int getRank() {
            if ("AJQK".contains(rank)) {
                if (rank.equals("A")) {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(rank);
        }

        public boolean isAce() {
            return rank.equals("A");
        }

        public String getImagePath() {
            return "./cards/" + this + ".png";
        }
    }

    ArrayList<Card> deck;
    Random rand = new Random();

    //    dealer and cards
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAces;

    //    player and cards
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAces;

    //    app window
    int boardHeight = 500;
    int boardWidth = 667;

    //    card size
    int cardWidth = 100;
    int cardHeight = 140;

    JFrame frame = new JFrame("Blackjack");
    JPanel panel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
//                drawing of the hidden card
                Image hiddenCardImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("./cards/BACK.png"))).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(hiddenCard.getImagePath()))).getImage();
                }
                g.drawImage(hiddenCardImage, 20, 20, cardWidth, cardHeight, null);

//                drawing of the dealer's hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(card.getImagePath()))).getImage();
                    g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth, cardHeight, null);
                }

//                drawing of the player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("./cards/" + card.toString() + ".png"))).getImage();
                    g.drawImage(cardImage, 20 + (cardWidth + 5) * i, 260, cardWidth, cardHeight, null);
                }

                if (!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAceRank();
                    playerSum = reducePlayerAceRank();
                    System.out.println("Stay:");
                    System.out.println(dealerSum);
                    System.out.println(playerSum);

                    String message;
                    if (playerSum > 21 || dealerSum > playerSum) {
                        message = "You lost.";
                    } else if (playerSum > dealerSum) {
                        message = "You won.";
                    } else {
                        message = "Tied.";
                    }

                    g.setFont(new Font("Arial", Font.BOLD, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 250, 225);
                }

                newGameButton.setEnabled(!stayButton.isEnabled());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("MORE!");
    JButton stayButton = new JButton("No more.");
    JButton newGameButton = new JButton("New Game");

    Blackjack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(0, 66, 37));
        frame.add(panel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        newGameButton.setFocusable(false);
        buttonPanel.add(newGameButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.removeLast();
                playerSum += card.getRank();
                playerAces += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAceRank() > 21) {
                    hitButton.setEnabled(false);
                }

                panel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17) {
                    Card card = deck.removeLast();
                    dealerSum += card.getRank();
                    dealerAces += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                panel.repaint();
            }
        });

        newGameButton.addActionListener(new ActionListener() {
            //            close and open again
            public void actionPerformed(ActionEvent e) {
                newGame();
                SwingUtilities.updateComponentTreeUI(panel);
            }
        });
    }

    public void startGame() {
//        deck building and shuffling
        buildDeck();
        shuffleDeck();

//        dealer, cards and ace count
        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAces = 0;

        hiddenCard = deck.removeLast();
        dealerSum += hiddenCard.getRank();
        dealerAces += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.removeLast();
        dealerSum += card.getRank();
        dealerAces += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        System.out.println("Dealer hand:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAces);

//        player, cards and ace count
        playerHand = new ArrayList<>();
        playerSum = 0;
        playerAces = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.removeLast();
            playerSum += card.getRank();
            playerAces += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("Player hand:");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAces);
    }

    public void buildDeck() {
        deck = new ArrayList<>();
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"C", "D", "H", "S"};

        for (String suit : suits) {
            for (String rank : ranks) {
                Card card = new Card(rank, suit);
                deck.add(card);
            }
        }

        System.out.println("Build deck:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = rand.nextInt(deck.size());

            Card currentCard = deck.get(i);
            Card randomCard = deck.get(j);

            deck.set(i, randomCard);
            deck.set(j, currentCard);
        }

        System.out.println("After shuffle:");
        System.out.println(deck);
    }

    public int reducePlayerAceRank() {
        while (playerSum > 21 && playerAces > 0) {
            playerSum -= 10;
            playerAces -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAceRank() {
        while (dealerSum > 21 && dealerAces > 0) {
            dealerSum -= 10;
            dealerAces -= 1;
        }
        return dealerSum;
    }

    public void newGame() {
        startGame();

        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        newGameButton.setEnabled(false);
    }
}
