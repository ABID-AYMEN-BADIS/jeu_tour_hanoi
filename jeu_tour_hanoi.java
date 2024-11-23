import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class jeu_tour_hanoi extends JFrame {
    private int nombreDisques;
    private Tour[] tours = new Tour[3];
    private JPanel panelTours;
    private JLabel instructions;
    private JButton btnSource, btnCible, btnReset, btnSolution;
    private Tour sourceTour, cibleTour;
    private List<Mouvement> cheminOptimal;
    private int indexChemin = 0;

    public jeu_tour_hanoi() {
        setTitle("Tour d'Hanoi - Defiez votre logique !");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Fenetre centree
        setLayout(new BorderLayout());

        // Initialisation du jeu
        nombreDisques = demanderNombreDisques();
        initialiserToursEtDisques(nombreDisques);
        genererCheminOptimal();

        // Creer l'interface
        creerInterface();

        setVisible(true);
    }

    private int demanderNombreDisques() {
        int nbDisques = 0;
        while (nbDisques < 3 || nbDisques > 8) {
            String input = JOptionPane.showInputDialog(this, 
                "Combien de disques voulez-vous (entre 3 et 8) ?");
            try {
                nbDisques = Integer.parseInt(input);
                if (nbDisques < 3 || nbDisques > 8) {
                    JOptionPane.showMessageDialog(this, 
                        "Choisissez un nombre entre 3 et 8.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "C'est pas un nombre valide.");
            }
        }
        return nbDisques;
    }

    private void initialiserToursEtDisques(int nbDisques) {
        for (int i = 0; i < 3; i++) {
            tours[i] = new Tour();
        }
        for (int i = nbDisques; i >= 1; i--) {
            tours[0].empiler(i);
        }
    }

    private void creerInterface() {
        // Panel pour les tours
        panelTours = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dessinerToursEtDisques(g);
            }
        };
        panelTours.setBackground(Color.WHITE);
        panelTours.setPreferredSize(new Dimension(900, 500));
        add(panelTours, BorderLayout.CENTER);

        // Zone de controle avec boutons
        JPanel panelControle = new JPanel();
        panelControle.setLayout(new FlowLayout());

        btnSource = new JButton("Source");
        btnCible = new JButton("Cible");
        btnReset = new JButton("Recommencer");
        btnSolution = new JButton("Afficher Solution");

        btnSource.addActionListener(new TourSelectionListener());
        btnCible.addActionListener(new TourSelectionListener());
        btnReset.addActionListener(e -> resetJeu());
        btnSolution.addActionListener(new SolutionListener());

        panelControle.add(btnSource);
        panelControle.add(btnCible);
        panelControle.add(btnReset);
        panelControle.add(btnSolution);

        add(panelControle, BorderLayout.SOUTH);

        // Zone d'instructions au joueur
        instructions = new JLabel("Deplacez les disques pour resoudre le puzzle.");
        instructions.setFont(new Font("SansSerif", Font.BOLD, 16));
        instructions.setHorizontalAlignment(SwingConstants.CENTER);
        add(instructions, BorderLayout.NORTH);
    }

    private void dessinerToursEtDisques(Graphics g) {
        int largeurBase = panelTours.getWidth() / 3;
        int hauteurTour = 300;

        // Dessiner les poteaux des tours
        g.setColor(Color.GRAY);
        for (int i = 0; i < 3; i++) {
            int x = largeurBase / 2 + i * largeurBase;
            g.fillRect(x - 5, 150, 10, hauteurTour);
        }

        // Dessiner les disques
        for (int i = 0; i < 3; i++) {
            Stack<Integer> pile = tours[i].getDisques();
            int xBase = largeurBase / 2 + i * largeurBase;
            int y = 450;
            for (int disque : pile) {
                int largeur = disque * 30;
                g.setColor(new Color(100 + 20 * disque, 50 * (disque % 2), 200 - 30 * disque));
                g.fillRect(xBase - largeur / 2, y, largeur, 20);
                y -= 25;
            }
        }
    }

    private void resetJeu() {
        initialiserToursEtDisques(nombreDisques);
        genererCheminOptimal();
        sourceTour = null;
        cibleTour = null;
        indexChemin = 0;
        instructions.setText("Jeu reinitialise. Faites vos mouvements.");
        repaint();
    }

    private void genererCheminOptimal() {
        cheminOptimal = new ArrayList<>();
        resoudreHanoi(nombreDisques, tours[0], tours[1], tours[2]);
    }

    private void resoudreHanoi(int n, Tour source, Tour auxiliaire, Tour cible) {
        if (n == 0) return;
        resoudreHanoi(n - 1, source, cible, auxiliaire);
        cheminOptimal.add(new Mouvement(source, cible));
        resoudreHanoi(n - 1, auxiliaire, source, cible);
    }

    class TourSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton btn = (JButton) e.getSource();
            String message = (btn == btnSource) 
                ? "Selectionnez la tour source (1, 2 ou 3) :"
                : "Selectionnez la tour cible (1, 2 ou 3) :";
            try {
                int choix = Integer.parseInt(JOptionPane.showInputDialog(jeu_tour_hanoi.this, message)) - 1;
                if (choix < 0 || choix > 2) throw new Exception();
                if (btn == btnSource) {
                    sourceTour = tours[choix];
                } else {
                    cibleTour = tours[choix];
                    effectuerMouvement();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(jeu_tour_hanoi.this, "Mauvais choix. Essayez encore.");
            }
        }

        private void effectuerMouvement() {
            if (sourceTour == null || cibleTour == null || sourceTour == cibleTour) {
                JOptionPane.showMessageDialog(jeu_tour_hanoi.this, "Mouvement invalide.");
                return;
            }
            if (sourceTour.estVide()) {
                JOptionPane.showMessageDialog(jeu_tour_hanoi.this, "La tour source est vide !");
                return;
            }
            if (!cibleTour.estVide() && sourceTour.getSommet() > cibleTour.getSommet()) {
                JOptionPane.showMessageDialog(jeu_tour_hanoi.this, 
                    "Erreur : Vous ne pouvez pas mettre un disque plus grand sur un plus petit.");
                return;
            }
            cibleTour.empiler(sourceTour.depiler());
            repaint();

            if (indexChemin < cheminOptimal.size()) {
                Mouvement m = cheminOptimal.get(indexChemin);
                if (m.getSource() == sourceTour && m.getCible() == cibleTour) {
                    indexChemin++;
                    instructions.setText("Bon mouvement. Continuez !");
                } else {
                    JOptionPane.showMessageDialog(jeu_tour_hanoi.this, 
                        "Ce n'est pas le bon mouvement selon la solution optimale.");
                }
            }

            if (tours[2].getDisques().size() == nombreDisques) {
                instructions.setText("Bravo ! Vous avez reussi.");
                JOptionPane.showMessageDialog(jeu_tour_hanoi.this, 
                    "Felicitations, vous avez resolu le puzzle !");
            }
        }
    }

    class SolutionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Timer(1000, new ActionListener() {
                int index = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (index < cheminOptimal.size()) {
                        Mouvement m = cheminOptimal.get(index);
                        m.getCible().empiler(m.getSource().depiler());
                        repaint();
                        index++;
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                }
            }).start();
        }
    }

    class Mouvement {
        private Tour source, cible;

        public Mouvement(Tour source, Tour cible) {
            this.source = source;
            this.cible = cible;
        }

        public Tour getSource() {
            return source;
        }

        public Tour getCible() {
            return cible;
        }
    }

    class Tour {
        private Stack<Integer> disques = new Stack<>();

        public boolean estVide() {
            return disques.isEmpty();
        }

        public int getSommet() {
            return disques.isEmpty() ? Integer.MAX_VALUE : disques.peek();
        }

        public void empiler(int disque) {
            disques.push(disque);
        }

        public int depiler() {
            return disques.pop();
        }

        public Stack<Integer> getDisques() {
            return disques;
        }
    }

    public static void main(String[] args) {
        new jeu_tour_hanoi();
    }
}
