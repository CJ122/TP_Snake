package snake;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Random;

public class SnakeApp extends Application {

    private static final int TILE_SIZE = 20;
    private static final int WIDTH = 20;
    private static final int HEIGHT = 15;

    private StackPane root;
    private Rectangle food;
    private int directionX = 1;
    private int directionY = 0;
    private boolean gameOver = false;
    private int score = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SnakeFX");
        root = new StackPane();
        Scene scene = new Scene(root, WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

        // Initialisation du serpent et de la nourriture
        root.getChildren().add(createSnake());
        food = createFood();

        // Ajout des contrôles et du label
        addControls();
        addLabel();

        // Gestion des événements de touche
        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));

        // Mise en place d'une boucle temporelle pour le déplacement continu du serpent
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> moveSnake()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Affichage de la scène
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Création du rectangle représentant un segment du serpent
    private Rectangle createSnake() {
        Rectangle snake = new Rectangle(TILE_SIZE, TILE_SIZE, Color.BLACK);
        return snake;
    }

    // Création du rectangle représentant la nourriture
    private Rectangle createFood() {
        Random rand = new Random();
        int x = rand.nextInt(WIDTH) * TILE_SIZE;
        int y = rand.nextInt(HEIGHT) * TILE_SIZE;

        Rectangle food = new Rectangle(TILE_SIZE, TILE_SIZE, Color.RED);
        food.relocate(x, y);

        // Ajout de la nourriture au StackPane
        root.getChildren().add(food);

        return food;
    }

    // Ajout des boutons de contrôle au StackPane
    private void addControls() {
        Button buttonUp = new Button("Haut");
        buttonUp.setOnAction(e -> changeDirection(0, -1));

        Button buttonDown = new Button("Bas");
        buttonDown.setOnAction(e -> changeDirection(0, 1));

        Button buttonLeft = new Button("Gauche");
        buttonLeft.setOnAction(e -> changeDirection(-1, 0));

        Button buttonRight = new Button("Droite");
        buttonRight.setOnAction(e -> changeDirection(1, 0));

        // Utilisation d'un VBox pour aligner verticalement les boutons
        VBox controlBox = new VBox(10, buttonUp, new HSpacer(), buttonDown, new HSpacer(), buttonLeft, new HSpacer(), buttonRight);
        
        // Ajout du VBox au StackPane
        root.getChildren().add(controlBox);

        // Alignement du VBox au bas du StackPane
        StackPane.setAlignment(controlBox, javafx.geometry.Pos.BOTTOM_CENTER);
    }

    // Ajout du label de score au StackPane
    private void addLabel() {
        Label scoreLabel = new Label("Score : " + score);
        // Ajout du label au StackPane
        root.getChildren().add(scoreLabel);
        // Alignement du label en haut du StackPane
        StackPane.setAlignment(scoreLabel, javafx.geometry.Pos.TOP_CENTER);
    }

    // Gestion des événements de touche
    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case UP:
                changeDirection(0, -1);
                break;
            case DOWN:
                changeDirection(0, 1);
                break;
            case LEFT:
                changeDirection(-1, 0);
                break;
            case RIGHT:
                changeDirection(1, 0);
                break;
        }
    }

    // Changement de la direction de déplacement du serpent
    private void changeDirection(int x, int y) {
        directionX = x;
        directionY = y;
    }

    // Déplacement du serpent
    private void moveSnake() {
        if (!gameOver) {
            double oldX = root.getChildren().get(0).getTranslateX();
            double oldY = root.getChildren().get(0).getTranslateY();

            double newX = oldX + directionX * TILE_SIZE;
            double newY = oldY + directionY * TILE_SIZE;

            // Vérification des collisions
            checkCollision(newX, newY);

            if (!gameOver) {
                // Mise à jour de la position du premier segment du serpent
                ((Rectangle) root.getChildren().get(0)).setTranslateX(newX);
                ((Rectangle) root.getChildren().get(0)).setTranslateY(newY);

                // Vérification de la collision avec la nourriture
                checkFoodCollision();
            }
        }
    }

    // Vérification des collisions avec les bords et le corps du serpent
    private void checkCollision(double newX, double newY) {
        if (newX < 0 || newY < 0 || newX >= WIDTH * TILE_SIZE || newY >= HEIGHT * TILE_SIZE) {
            // Le serpent a touché le mur, réapparition de l'autre côté
            ((Rectangle) root.getChildren().get(0)).setTranslateX((newX + WIDTH * TILE_SIZE) % (WIDTH * TILE_SIZE));
            ((Rectangle) root.getChildren().get(0)).setTranslateY((newY + HEIGHT * TILE_SIZE) % (HEIGHT * TILE_SIZE));
        }

        // Vérification de la collision avec le corps du serpent
        for (int i = 1; i < root.getChildren().size(); i++) {
            Rectangle segment = (Rectangle) root.getChildren().get(i);
            if (((Rectangle) root.getChildren().get(0)).getBoundsInParent().intersects(segment.getBoundsInParent())) {
                gameOver = true;
                System.out.println("Partie terminée ! Score : " + score);
            }
        }
    }

    // Vérification de la collision avec la nourriture
    private void checkFoodCollision() {
        if (((Rectangle) root.getChildren().get(0)).getBoundsInParent().intersects(food.getBoundsInParent())) {
            // Le serpent a mangé la nourriture
            root.getChildren().remove(food);
            food = createFood();
            root.getChildren().add(food);

            // Mise à jour du score
            score++;
            Label scoreLabel = (Label) root.getChildren().get(root.getChildren().size() - 1);
            scoreLabel.setText("Score : " + score);

            // Augmentation de la taille du serpent seulement s'il n'y a pas d'autres segments aux mêmes coordonnées
            if (!isSegmentAt(new Rectangle(((Rectangle) root.getChildren().get(0)).getTranslateX(), ((Rectangle) root.getChildren().get(0)).getTranslateY(), TILE_SIZE, TILE_SIZE))) {
                Rectangle newSegment = createSnake();
                root.getChildren().add(newSegment);
            }
        }
    }

    // Vérification de la présence d'un segment aux mêmes coordonnées
    private boolean isSegmentAt(Rectangle testSegment) {
        for (int i = 1; i < root.getChildren().size(); i++) {
            Rectangle segment = (Rectangle) root.getChildren().get(i);
            if (testSegment.getBoundsInParent().intersects(segment.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }

    // Classe d'aide pour ajouter de l'espacement dans un VBox
    private static class HSpacer extends javafx.scene.layout.Region {
        HSpacer() {
            setMinWidth(10);
            setMinHeight(1);
        }
    }

    // Méthode principale pour lancer l'application JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
