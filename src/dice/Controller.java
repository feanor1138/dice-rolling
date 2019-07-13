package dice;

import javafx.animation.RotateTransition;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.util.Duration;

import java.util.LinkedList;

public class Controller {
    @FXML
    private ComboBox<String> cboNumDice;

    private enum BorderType {
        NONE, SINGLE, DOUBLE
    }

    @FXML
    private GridPaneFixedCols gridSides;

    @FXML
    private ComboBox<String> cboSides1;

    @FXML
    private TextArea txtResults;

    @FXML
    private CheckBox chkSame;

    @FXML
    private CheckBox chkAnimate;

    @FXML
    private TextField txtModifier;

    @FXML
    private GridPaneFixedCols diceTray;

    @FXML
    private CheckBox chkConsole;

    private LinkedList<Die> diceToRoll = new LinkedList<>();

    private static final int maxDice = 10;
    private int maxSides = 100;
    private int defaultVal = 5;
    private int rollSum = 0;
    private int numDiceRolling = 0;
    private boolean doAnimations = true;

    public Controller() {
    }

    @FXML
    private void initialize() {
        //set up the grids
        gridSides.setCurrentCol(1);
        diceTray.setMaxCols(9);
        gridSides.setMaxCols(4);
        //set up the combo boxes
        addNumberOptions(cboNumDice, maxDice, 0);
        addNumberOptions(cboSides1, maxSides, 5);
        //add listener to autoscroll to bottom of text area
        txtResults.textProperty().addListener((ObservableValue<?> observable, Object oldValue, Object newValue) ->
                txtResults.setScrollTop(Double.MAX_VALUE));
    }

    private void addNumberOptions(ComboBox<String> cbo, int max, int select) {
        //filling up the combo boxes
        ObservableList<String> options = FXCollections.observableArrayList();
        for (int i = 1; i <= max; i++) {
            options.add(String.valueOf(i));
        }
        cbo.setItems(options);
        cbo.getSelectionModel().select(select);
    }

    @FXML
    private void updateCombos() {
        //add or remove combo boxes based on number of dice selected
        int numDice = Integer.parseInt(cboNumDice.getSelectionModel().getSelectedItem());
        ObservableList<Node> children = gridSides.getChildren();
        if (children.size() > numDice) {
            //remove hboxes
            children.remove(numDice, children.size());
        } else if (children.size() < numDice) {
            //add hboxes
            while (children.size() < numDice) {
                int num = children.size()+1;
                HBox hBox = new HBox();
                hBox.setSpacing(10);
                hBox.setAlignment(Pos.CENTER);
                Label lbl = new Label("Die #" + num + ":");
                hBox.getChildren().add(lbl);
                ComboBox<String> cbo = new ComboBox<>();
                addNumberOptions(cbo, maxSides, defaultVal);
                hBox.getChildren().add(cbo);
                gridSides.add(hBox);
            }
        }
        //show the "match all die sizes to #1" checkbox if there's more than one die
        if (children.size() == 1) {
            chkSame.setVisible(false);
        } else {
            chkSame.setVisible(true);
        }
    }

    @FXML
    private void rollDice() {
        //reset the dice tray
        diceTray.clear();
        rollSum = 0;

        txtResults.appendText("\n\nStarting a new roll! Let's go!");
        ObservableList<Node> boxes = gridSides.getChildren();
        numDiceRolling = boxes.size();
        for (Node box : boxes) {
            ObservableList<Node> nodes = ((HBox)box).getChildren();
            String name = "";
            for (Node node : nodes) {
                if (node instanceof Label) {
                    name = ((Label)node).getText().replaceAll(":", "");
                } else if (node instanceof ComboBox) {
                    ComboBox cbo = (ComboBox)node;
                    int sides = Integer.parseInt(cbo.getSelectionModel().getSelectedItem().toString());
                    diceToRoll.add(new Die(name, sides));
                }
            }
        }
        rollNextDie();
    }

    private void rollNextDie() {
        if (diceToRoll.isEmpty()) {
            //rolled them all; finish up
            if (numDiceRolling > 1) {
                txtResults.appendText("\nSum for this roll is: " + rollSum + ".");
            }
            int modifier = validateModifier();
            if (modifier != 0) {
                rollSum += modifier;
                txtResults.appendText("\nWith modifier, sum for this roll is: " + rollSum + ".");
                if (modifier < 0) {
                    createDieLabel("-", BorderType.NONE, null);
                } else {
                    createDieLabel("+", BorderType.NONE, null);
                }
                createDieLabel(String.valueOf(Math.abs(modifier)), BorderType.NONE, null);
            }
            if (numDiceRolling > 1) {
                createDieLabel("=", BorderType.NONE, null);
                createDieLabel(String.valueOf(rollSum), BorderType.DOUBLE, null);
            }
            return;
        }
        //get the next Die to roll
        Die d = diceToRoll.pop();
        String name = d.getName();
        int numDie = Integer.parseInt(name.substring(name.indexOf('#')+1));
        int value = d.roll();
        rollSum += value;
        if (numDie > 1) {
            //this isn't the first die we've rolled, so put a + in between
            createDieLabel("+", BorderType.NONE, null);
        }
        txtResults.appendText("\nRolling " + d.getName() + "...");
        //creating the die label will kick off the rolling animation.
        //when the animation is over, the next die will be rolled.
        createDieLabel(String.valueOf(value), BorderType.SINGLE, d);
        if (!doAnimations) {
            txtResults.appendText("\n...result is: " + value);
            rollNextDie();
        }
    }

    private void createDieLabel(String text, BorderType bt, Die d) {
        Label lbl = new Label(text);
        lbl.setAlignment(Pos.CENTER);
        lbl.setMinWidth(35);
        lbl.setMinHeight(35);
        if (bt == BorderType.SINGLE) {
            lbl.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null)));
        } else if (bt == BorderType.DOUBLE) {
            lbl.setMinWidth(35);
            lbl.setMinHeight(35);
            lbl.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, BorderStroke.THICK)));
        }

        if (d != null) {
            if (doAnimations) {
                //this is a die; let's draw a 3d shape and animate it.
                //when the animation is over, we'll roll the next die.
                Shape3D shape;
                if (d.getSides() == 4) {
                    shape = createMeshView();
                } else {
                    shape = new Box();
                    ((Box)shape).setWidth(30.0);
                    ((Box)shape).setHeight(30.0);
                    ((Box)shape).setDepth(30.0);
                }
                diceTray.add(shape);
                animateNode(shape, lbl, text);
            } else {
                diceTray.add(lbl);
            }
        } else {
            //this is just a label (+ or - or = or modifier), so just slap it on the grid
            diceTray.add(lbl);
        }
    }

    private MeshView createMeshView()
    {
        float[] points = {
            0, 0, 0,
            15, 30, 15,
            30, 0, 0,
            15, 0, 30
        };

        float[] texCoords = {
            0.5f, 0.5f,
            1.0f, 0.0f,
            1.5f, 0.5f,
            1.0f, 1.0f
        };

        int[] faces = {
            0, 0, 2, 2, 1, 1,
            0, 0, 3, 3, 2, 2,
            1, 1, 2, 2, 3, 3,
            0, 0, 1, 1, 3, 3
        };

        // Create a TriangleMesh
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);

        return new MeshView(mesh);
    }

    private void animateNode(Node n, Label lbl, String value) {
        //Creating a rotate transition
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setOnFinished((ActionEvent event) -> {
                //when the animation is over, display the results and roll the next die
                diceTray.remove(n);
                diceTray.add(lbl);
                txtResults.appendText("\n...result is: " + value);
                rollNextDie();
            }
        );

        //Setting the duration for the transition
        rotateTransition.setDuration(Duration.millis(1000));

        //Setting the node for the transition
        rotateTransition.setNode(n);

        rotateTransition.setAxis(new Point3D(10.0, 10.0, 10.0));

        //Spin it 360 degrees
        rotateTransition.setByAngle(360);

        //Spin it twice
        rotateTransition.setCycleCount(2);

        //Setting auto reverse value to false
        rotateTransition.setAutoReverse(false);

        //Playing the animation
        rotateTransition.play();
    }

    @FXML
    private void updateSame() {
        if (chkSame.isSelected()) {
            defaultVal = cboSides1.getSelectionModel().getSelectedIndex();
            //update all sides combos to be the same as defaultval
            ObservableList<Node> boxes = gridSides.getChildren();
            for (Node box : boxes) {
                ObservableList<Node> nodes = ((HBox) box).getChildren();
                for (Node node : nodes) {
                    if (node instanceof ComboBox) {
                        ComboBox cbo = (ComboBox) node;
                        cbo.getSelectionModel().select(defaultVal);
                    }
                }
            }
        } else {
            defaultVal = 5;
        }
    }

    @FXML
    private int validateModifier() {
        try {
            return Integer.parseInt(txtModifier.getText());
        } catch(Exception ex) {
            return 0;
        }
    }

    @FXML
    private void toggleConsole() {
        txtResults.setVisible(chkConsole.isSelected());
    }

    @FXML
    private void toggleAnimations() {
        doAnimations = chkAnimate.isSelected();
    }
}
