package game.ui;

import game.models.*;
import game.config.GameConfig;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Панель для расстановки кораблей перед началом игры.
 * Позволяет игроку вручную размещать корабли на своем поле или
 * использовать автоматическую расстановку. Управляет процессом
 * последовательного размещения всех кораблей, включая специальные юниты.
 *
 * После завершения расстановки автоматически размещает мины и уведомляет
 * основное окно о готовности к началу игры.
 */
public class ShipPlacementPanel extends JPanel {
    /**
     * Игровое поле, на котором происходит расстановка кораблей.
     */
    private Board board;

    /**
     * Панель для отображения игрового поля во время расстановки.
     */
    private GameBoardPanel boardPanel;

    /**
     * Выпадающий список для выбора типа корабля для размещения.
     */
    private JComboBox<String> shipTypeCombo;

    /**
     * Радиокнопка для выбора горизонтальной ориентации корабля.
     */
    private JRadioButton horizontalRadio;

    /**
     * Радиокнопка для выбора вертикальной ориентации корабля.
     */
    private JRadioButton verticalRadio;

    /**
     * Метка для отображения текущего статуса расстановки.
     */
    private JLabel statusLabel;

    /**
     * Список кораблей, которые необходимо разместить на поле.
     */
    private List<Ship> shipsToPlace;

    /**
     * Индекс текущего корабля в списке shipsToPlace.
     */
    private int currentShipIndex;

    /**
     * Возвращает доску с размещенными кораблями.
     * Используется для передачи готовой доски в игровой контроллер.
     *
     * @return доска с размещенными кораблями
     */
    public Board getBoardWithShips() {
        return this.board;
    }

    /**
     * Создает панель расстановки кораблей для указанной доски.
     * Инициализирует список кораблей для размещения и пользовательский интерфейс.
     *
     * @param board доска для расстановки кораблей
     */
    public ShipPlacementPanel(Board board) {
        this.board = board;
        this.shipsToPlace = new ArrayList<>();
        this.currentShipIndex = 0;

        setLayout(new BorderLayout());
        initializeShipsList();
        initializeUI();
    }

    /**
     * Инициализирует список кораблей, которые необходимо разместить.
     * Создает корабли согласно конфигурации игры: броненосцы, крейсеры,
     * эсминцы, подлодки и минные тральщики.
     */
    private void initializeShipsList() {
        for (int i = 0; i < GameConfig.getBattleshipCount(); i++) {
            shipsToPlace.add(new Battleship());
        }
        for (int i = 0; i < GameConfig.getCruiserCount(); i++) {
            shipsToPlace.add(new Cruiser());
        }
        for (int i = 0; i < GameConfig.getDestroyerCount(); i++) {
            shipsToPlace.add(new Destroyer());
        }

        for (int i = 0; i < GameConfig.getSubmarineCount(); i++) {
            shipsToPlace.add(new Submarine());
        }

        for (int i = 0; i < GameConfig.getMinesweeperCount(); i++) {
            shipsToPlace.add(new Minesweeper());
        }
    }

    /**
     * Инициализирует пользовательский интерфейс панели расстановки.
     * Создает игровое поле, панель управления, элементы выбора кораблей
     * и ориентации, а также кнопки управления.
     */
    private void initializeUI() {
        boardPanel = new GameBoardPanel(board, true, true);
        boardPanel.setCellClickListener(this::handleCellClick);

        add(boardPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel(getNextShipToPlaceText());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel shipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        shipPanel.add(new JLabel("Корабль: "));

        String[] shipNames = new String[shipsToPlace.size()];
        for (int i = 0; i < shipsToPlace.size(); i++) {
            Ship ship = shipsToPlace.get(i);
            shipNames[i] = ship.getName() + " (" + ship.getSize() + " палуб)";
        }

        shipTypeCombo = new JComboBox<>(shipNames);
        shipTypeCombo.setSelectedIndex(0);
        shipTypeCombo.addActionListener(e -> updateCurrentShip());
        shipPanel.add(shipTypeCombo);
        controlPanel.add(shipPanel);

        JPanel orientationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        orientationPanel.add(new JLabel("Ориентация: "));

        horizontalRadio = new JRadioButton("Горизонтально", true);
        verticalRadio = new JRadioButton("Вертикально");

        ButtonGroup orientationGroup = new ButtonGroup();
        orientationGroup.add(horizontalRadio);
        orientationGroup.add(verticalRadio);

        orientationPanel.add(horizontalRadio);
        orientationPanel.add(verticalRadio);
        controlPanel.add(orientationPanel);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));

        JButton autoPlaceButton = new JButton("Авторасстановка");
        autoPlaceButton.addActionListener(e -> autoPlaceAllShips());

        JButton clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> clearBoard());

        JButton doneButton = new JButton("Готово");
        doneButton.addActionListener(e -> finishPlacement());

        buttonPanel.add(autoPlaceButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(doneButton);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(buttonPanel);

        add(controlPanel, BorderLayout.EAST);

        updateCurrentShip();
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Обозначения"));

        legendPanel.add(createSimpleLegendItem(Color.GRAY, "S", "Корабль"));
        legendPanel.add(createSimpleLegendItem(Color.CYAN, "U", "Подлодка"));
        legendPanel.add(createSimpleLegendItem(Color.YELLOW, "T", "Минный тральщик"));
        legendPanel.add(createSimpleLegendItem(Color.ORANGE, "*", "Мина"));

        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(legendPanel);
    }

    /**
     * Обрабатывает клик игрока по клетке поля.
     * Пытается разместить текущий выбранный корабль в указанной клетке
     * с выбранной ориентацией.
     *
     * @param e событие клика с координатами клетки
     */
    private void handleCellClick(ActionEvent e) {
        String[] coordinates = e.getActionCommand().split(",");
        int row = Integer.parseInt(coordinates[0]);
        int col = Integer.parseInt(coordinates[1]);

        if (currentShipIndex < shipsToPlace.size()) {
            Ship ship = shipsToPlace.get(currentShipIndex);
            boolean horizontal = horizontalRadio.isSelected();

            boolean placed = board.placeShip(ship, row, col, horizontal);

            if (placed) {
                boardPanel.updateButtons();
                currentShipIndex++;

                if (currentShipIndex < shipsToPlace.size()) {
                    updateCurrentShip();
                    statusLabel.setText(getNextShipToPlaceText());
                } else {
                    statusLabel.setText("Все корабли расставлены! Нажмите 'Готово'");
                    shipTypeCombo.setEnabled(false);
                    horizontalRadio.setEnabled(false);
                    verticalRadio.setEnabled(false);
                }
            } else {
                statusLabel.setText("Невозможно разместить здесь! Попробуйте другое место.");
            }
        }
    }

    /**
     * Возвращает текст с описанием следующего корабля для размещения.
     *
     * @return текст с названием и размером следующего корабля
     */
    private String getNextShipToPlaceText() {
        if (currentShipIndex < shipsToPlace.size()) {
            Ship ship = shipsToPlace.get(currentShipIndex);
            return "Разместите: " + ship.getName() + " (" + ship.getSize() + " палуб)";
        }
        return "Все корабли расставлены!";
    }

    /**
     * Обновляет текущий выбранный корабль на основе выбора в выпадающем списке.
     */
    private void updateCurrentShip() {
        currentShipIndex = shipTypeCombo.getSelectedIndex();
        statusLabel.setText(getNextShipToPlaceText());
    }

    /**
     * Автоматически расставляет все корабли на поле.
     * Использует случайный алгоритм размещения и обновляет интерфейс.
     */
    private void autoPlaceAllShips() {
        board.placeShipsRandomly();
        boardPanel.updateButtons();
        currentShipIndex = shipsToPlace.size();
        statusLabel.setText("Все корабли расставлены автоматически!");
        shipTypeCombo.setEnabled(false);
        horizontalRadio.setEnabled(false);
        verticalRadio.setEnabled(false);
    }

    /**
     * Очищает поле от всех размещенных кораблей.
     * Сбрасывает состояние поля и позволяет начать расстановку заново.
     */
    private void clearBoard() {
        int size = board.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board.getCell(i, j).setState(CellState.EMPTY);
            }
        }
        board.getShips().clear();
        board.getMines().clear();
        board.getMinesweepers().clear();

        boardPanel.updateButtons();
        currentShipIndex = 0;
        shipTypeCombo.setEnabled(true);
        horizontalRadio.setEnabled(true);
        verticalRadio.setEnabled(true);
        statusLabel.setText(getNextShipToPlaceText());
    }

    /**
     * Завершает процесс расстановки кораблей.
     * Автоматически размещает мины и уведомляет основное окно
     * о готовности к началу игры.
     */
    private void finishPlacement() {
        placeMines();

        firePropertyChange("placementFinished", false, true);
    }

    /**
     * Автоматически размещает мины на поле после расстановки кораблей.
     * Размещает количество мин, указанное в конфигурации игры.
     */
    private void placeMines() {
        for (int i = 0; i < GameConfig.getMineCount(); i++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int row = (int)(Math.random() * board.getSize());
                int col = (int)(Math.random() * board.getSize());

                placed = board.placeMine(row, col);
                attempts++;
            }
        }

        boardPanel.updateButtons();
    }

    /**
     * Создает простой элемент легенды для панели управления.
     *
     * @param color цвет индикатора
     * @param symbol символ, отображаемый на поле
     * @param text текстовое описание
     * @return панель с элементом легенды
     */
    private JPanel createSimpleLegendItem(Color color, String symbol, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JLabel colorLabel = new JLabel("   ");
        colorLabel.setOpaque(true);
        colorLabel.setBackground(color);
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorLabel.setPreferredSize(new Dimension(15, 15));

        JLabel textLabel = new JLabel(symbol + " - " + text);

        panel.add(colorLabel);
        panel.add(textLabel);

        return panel;
    }

    /**
     * Проверяет, завершена ли расстановка всех кораблей.
     *
     * @return true если все корабли размещены, false в противном случае
     */
    public boolean isPlacementComplete() {
        return currentShipIndex >= shipsToPlace.size();
    }
}