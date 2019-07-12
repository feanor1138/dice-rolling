package dice;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class Controller {
    @FXML
    private ComboBox cboNumDice;

    private enum BorderType {
        NONE, SINGLE, DOUBLE;
    }

    @FXML
    private GridPaneFixedCols gridSides;

    @FXML
    private ComboBox cboSides1;

    @FXML
    private TextArea txtResults;

    @FXML
    private CheckBox chkSame;

    @FXML
    private TextField txtModifier;

    @FXML
    private GridPaneFixedCols gridDice;

    @FXML
    private CheckBox chkConsole;

    private int maxDice = 8;
    private int maxSides = 100;
    private int defaultVal = 5;

    public Controller() {
    }

    @FXML
    private void initialize() {
        //set up the grids
        gridSides.setCurrentCol(1);
        gridDice.setMaxCols(9);
        gridSides.setMaxCols(4);
        //set up the combo boxes
        addNumberOptions(cboNumDice, maxDice, 0);
        addNumberOptions(cboSides1, maxSides, 5);
        //add listener to autoscroll to bottom of text area
        txtResults.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                            Object newValue) {
                txtResults.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    private void addNumberOptions(ComboBox cbo, int max, int select) {
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
        int numDice = Integer.parseInt(cboNumDice.getSelectionModel().getSelectedItem().toString());
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
                ComboBox cbo = new ComboBox();
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
        gridDice.clear();

        txtResults.appendText("\n\nStarting a new roll! Let's go!");
        ObservableList<Node> boxes = gridSides.getChildren();
        int sum = 0;
        int countDice = 0;
        for (Node box : boxes) {
            ObservableList<Node> nodes = ((HBox)box).getChildren();
            String name = "";
            for (Node node : nodes) {
                if (node instanceof Label) {
                    name = ((Label)node).getText();
                } else if (node instanceof ComboBox) {
                    ComboBox cbo = (ComboBox)node;
                    int sides = Integer.parseInt(cbo.getSelectionModel().getSelectedItem().toString());
                    countDice++;
                    Die die = new Die(sides);
                    int value = die.roll();
                    if (countDice > 1) {
                        createDieLabel("+", BorderType.NONE);
                    }
                    createDieLabel(String.valueOf(value), BorderType.SINGLE);
                    sum += value;

                    //display result
                    txtResults.appendText("\nRolling " + name + " " + value);
                }
            }
        }
        if (countDice > 1) {
            txtResults.appendText("\nSum for this roll is: " + sum + ".");
        }
        int modifier = validateModifier();
        if (modifier != 0) {
            sum += modifier;
            txtResults.appendText("\nWith modifier, sum for this roll is: " + sum + ".");
            if (modifier < 0) {
                createDieLabel("-", BorderType.NONE);
            } else {
                createDieLabel("+", BorderType.NONE);
            }
            createDieLabel(String.valueOf(Math.abs(modifier)), BorderType.NONE);
        }
        if (countDice > 1) {
            createDieLabel("=", BorderType.NONE);
            createDieLabel(String.valueOf(sum), BorderType.DOUBLE);
        }
    }

    private void createDieLabel(String text, BorderType bt) {
        Label lbl = new Label(text);
        lbl.setAlignment(Pos.CENTER);
        lbl.setMinWidth(30);
        lbl.setMinHeight(30);
        if (bt == BorderType.SINGLE) {
            lbl.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null)));
        } else if(bt == BorderType.DOUBLE) {
            lbl.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, BorderStroke.THICK)));
        }
        gridDice.add(lbl);
    }

    private boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception ex) {
            return false;
        }
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
            int modifier = Integer.parseInt(txtModifier.getText());
            return modifier;
        } catch(Exception ex) {
            return 0;
        }
    }

    @FXML
    private void toggleConsole() {
        txtResults.setVisible(chkConsole.isSelected());
    }
}
