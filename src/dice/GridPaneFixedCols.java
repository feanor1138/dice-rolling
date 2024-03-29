package dice;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class GridPaneFixedCols extends GridPane {
    private int currentCol = 0;
    private int currentRow = 0;
    private int maxCols = 0;

    public GridPaneFixedCols() {
        super();
    }

    void setMaxCols(int maxCols) {
        this.maxCols = maxCols;
    }

    void setCurrentCol(int currentCol) {
        this.currentCol = currentCol;
    }

    void clear() {
        getChildren().clear();
        currentCol = 0;
        currentRow = 0;
    }

    void add(Node node) {
        GridPane.setConstraints(node, currentCol, currentRow);
        currentCol++;
        if (currentCol > maxCols) {
            currentCol = 0;
            currentRow++;
        }
        this.getChildren().add(node);
    }

    void remove(Node node) {
        currentCol--;
        if (currentCol < 0) {
            currentCol = maxCols;
            currentRow--;
        }
        this.getChildren().remove(node);
    }
}
