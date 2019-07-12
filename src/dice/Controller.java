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

    @FXML
    private GridPane gridSides;

    @FXML
    private ComboBox cboSides1;

    @FXML
    private TextArea txtResults;

    @FXML
    private CheckBox chkSame;

    @FXML
    private TextField txtModifier;

    @FXML
    private GridPane gridDice;

    private int maxDice = 8;
    private int maxSides = 100;
    private int defaultVal = 5;
    private int gridCol = 0;
    private int gridRow = 0;
    private int gridColMax = 10;

    public Controller() {
    }

    @FXML
    private void initialize() {
        addNumberOptions(cboNumDice, maxDice, 0);
        addNumberOptions(cboSides1, maxSides, 5);
        txtResults.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                            Object newValue) {
                txtResults.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    private void addNumberOptions(ComboBox cbo, int max, int select) {
        ObservableList<String> options = FXCollections.observableArrayList();
        for (int i = 1; i <= max; i++) {
            options.add(String.valueOf(i));
        }
        cbo.setItems(options);
        cbo.getSelectionModel().select(select);
    }

    @FXML
    private void updateCombos() {
        int numDice = Integer.parseInt(cboNumDice.getSelectionModel().getSelectedItem().toString());
        ObservableList<Node> children = gridSides.getChildren();
        if (children.size() > numDice) {
            //remove hboxes
            children.remove(numDice, children.size());
        } else if (children.size() < numDice) {
            //add hboxes
            while (children.size() < numDice) {
                int num = children.size()+1;
                int col = 0;
                int row = 0;
                switch (num) {
                    case 2:
                    case 3:
                    case 4:
                        row = 0;
                        col = (num-1);
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        row = 1;
                        col = (num-5);
                        break;
                    default:
                }
                HBox hBox = new HBox();
                hBox.setSpacing(10);
                hBox.setAlignment(Pos.CENTER);
                GridPane.setConstraints(hBox, col, row);
                Label lbl = new Label("Die #" + num + ":");
                hBox.getChildren().add(lbl);
                ComboBox cbo = new ComboBox();
                addNumberOptions(cbo, maxSides, defaultVal);
                hBox.getChildren().add(cbo);
                children.add(hBox);
            }
        }
        if (children.size() == 1) {
            chkSame.setVisible(false);
        } else {
            chkSame.setVisible(true);
        }
    }

    @FXML
    private void rollDice() {
        gridDice.getChildren().clear();
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
                        createDieLabel("+");
                    }
                    createDieLabel(String.valueOf(value));
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
                createDieLabel("-");
            } else {
                createDieLabel("+");
            }
            createDieLabel(String.valueOf(Math.abs(modifier)));
        }
        if (countDice > 1) {
            createDieLabel("=");
            createDieLabel(String.valueOf(sum));
        }
    }

    private void createDieLabel(String text) {
        Label lbl = new Label(text);
        GridPane.setConstraints(lbl, gridCol, gridRow);
        gridCol++;
        if (gridCol > gridColMax) {
            gridCol = 0;
            gridRow++;
        }
        lbl.setAlignment(Pos.CENTER);
        lbl.setMinWidth(30);
        lbl.setMinHeight(30);
        if (isNumeric(text)) {
            lbl.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null)));
        }
        gridDice.getChildren().add(lbl);
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
}
