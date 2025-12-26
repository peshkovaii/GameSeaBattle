package game.ui;

import game.models.Board;
import game.models.Cell;
import game.models.CellState;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Панель для отображения игрового поля в графическом интерфейсе.
 * Представляет игровое поле в виде сетки кнопок, где каждая кнопка
 * соответствует клетке на поле. Поддерживает два режима отображения:
 * с показом кораблей (для своего поля) и без показа (для поля противника).
 */
public class GameBoardPanel extends JPanel {
    /**
     * Игровое поле, которое отображается на этой панели.
     */
    private Board board;

    /**
     * Двумерный массив кнопок, соответствующих клеткам игрового поля.
     */
    private JButton[][] buttons;

    /**
     * Флаг, указывающий показывать ли корабли на поле.
     * true - показывать корабли (используется для поля игрока)
     * false - скрывать корабли (используется для поля противника)
     */
    private boolean showShips;

    /**
     * Флаг, указывающий является ли поле интерактивным.
     * true - кнопки реагируют на клики (поле противника во время хода игрока)
     * false - кнопки не интерактивны (поле игрока или поле противника не во время хода игрока)
     */
    private boolean interactive;

    /**
     * Слушатель событий кликов по клеткам поля.
     */
    private ActionListener cellClickListener;

    /**
     * Создает панель игрового поля с указанными параметрами.
     * Инициализирует сетку кнопок и настраивает отображение.
     *
     * @param board игровое поле для отображения
     * @param showShips true для показа кораблей, false для скрытия
     * @param interactive true для интерактивного поля, false для неинтерактивного
     */
    public GameBoardPanel(Board board, boolean showShips, boolean interactive) {
        this.board = board;
        this.showShips = showShips;
        this.interactive = interactive;

        int size = board.getSize();
        setLayout(new GridLayout(size + 1, size + 1));

        createBoardButtons();
    }

    /**
     * Создает кнопки для всех клеток игрового поля.
     * Добавляет заголовки строк и столбцов, инициализирует кнопки клеток.
     */
    private void createBoardButtons() {
        int size = board.getSize();
        buttons = new JButton[size][size];

        add(new JLabel(""));

        for (int col = 0; col < size; col++) {
            JLabel label = new JLabel(String.valueOf((char)('A' + col)), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            add(label);
        }

        for (int row = 0; row < size; row++) {
            JLabel rowLabel = new JLabel(String.valueOf(row + 1), SwingConstants.CENTER);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 14));
            add(rowLabel);

            for (int col = 0; col < size; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(40, 40));
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setFont(new Font("Arial", Font.BOLD, 12));

                final int currentRow = row;
                final int currentCol = col;

                if (interactive) {
                    button.addActionListener(e -> {
                        if (cellClickListener != null) {
                            cellClickListener.actionPerformed(
                                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                            currentRow + "," + currentCol)
                            );
                        }
                    });
                } else {
                    button.setEnabled(false);
                }

                buttons[row][col] = button;
                add(button);
            }
        }

        updateButtons();
    }

    /**
     * Обновляет внешний вид всех кнопок в соответствии с текущим состоянием поля.
     * Устанавливает цвета, текст и состояние кнопок на основе состояния клеток.
     */
    public void updateButtons() {
        int size = board.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                JButton button = buttons[row][col];
                Cell cell = board.getCell(row, col);

                button.setBackground(Color.WHITE);
                button.setText("");
                button.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                if (cell == null) continue;

                if (showShips) {
                    displayForPlayerView(button, cell);
                } else {
                    displayForEnemyView(button, cell);
                }

                if (cell.getState() == CellState.HIT ||
                        cell.getState() == CellState.MISS ||
                        cell.getState() == CellState.DESTROYED) {
                    button.setEnabled(false);
                }
            }
        }
    }

    /**
     * Настраивает отображение кнопки для режима показа кораблей (поле игрока).
     * Показывает все объекты на поле: корабли, мины, тральщики и результаты атак.
     *
     * @param button кнопка для настройки
     * @param cell клетка, соответствующая этой кнопке
     */
    private void displayForPlayerView(JButton button, Cell cell) {
        switch (cell.getState()) {
            case EMPTY:
                button.setBackground(Color.WHITE);
                break;
            case SHIP:
                button.setBackground(Color.GRAY);
                button.setText("S");
                button.setForeground(Color.WHITE);
                break;
            case SUBMARINE:
                button.setBackground(Color.CYAN);
                button.setText("U");
                button.setForeground(Color.BLACK);
                break;
            case MINE:
                button.setBackground(Color.ORANGE);
                button.setText("*");
                button.setForeground(Color.BLACK);
                break;
            case MINESWEEPER:
                button.setBackground(Color.YELLOW);
                button.setText("T");
                button.setForeground(Color.BLACK);
                break;
            case HIT:
                button.setBackground(Color.RED);
                button.setText("X");
                button.setForeground(Color.WHITE);
                break;
            case MISS:
                button.setBackground(Color.BLUE);
                button.setText("O");
                button.setForeground(Color.WHITE);
                break;
            case DESTROYED:
                button.setBackground(Color.BLACK);
                button.setText("X");
                button.setForeground(Color.WHITE);
                break;
            case REVEALED:
                if (cell.isRevealedByMine()) {
                    button.setBackground(new Color(180, 220, 255));
                    button.setText("R");
                    button.setToolTipText("Раскрытая цель");
                } else {
                    button.setBackground(Color.PINK);
                    button.setText("R");
                }
                button.setForeground(Color.BLACK);
                break;
        }
    }

    /**
     * Настраивает отображение кнопки для режима скрытия кораблей (поле противника).
     * Показывает только результаты атак, скрывая расположение кораблей и мин.
     *
     * @param button кнопка для настройки
     * @param cell клетка, соответствующая этой кнопке
     */
    private void displayForEnemyView(JButton button, Cell cell) {
        switch (cell.getState()) {
            case HIT:
                button.setBackground(Color.RED);
                button.setText("X");
                button.setForeground(Color.WHITE);
                break;
            case MISS:
                button.setBackground(Color.BLUE);
                button.setText("O");
                button.setForeground(Color.WHITE);
                break;
            case DESTROYED:
                button.setBackground(Color.BLACK);
                button.setText("X");
                button.setForeground(Color.WHITE);
                break;
            case REVEALED:
                if (cell.isRevealedByMine()) {
                    button.setBackground(new Color(180, 220, 255));
                    button.setText("R");
                    button.setToolTipText("Раскрытая цель");
                } else {
                    button.setBackground(Color.PINK);
                    button.setText("R");
                }
                button.setForeground(Color.BLACK);
                break;
            default:
                button.setBackground(Color.WHITE);
                button.setText("");
                break;
        }
    }

    /**
     * Обновляет доску, заменяя текущую доску на новую.
     * Полностью пересоздает интерфейс с новой доской.
     *
     * @param newBoard новая доска для отображения
     */
    public void updateBoard(Board newBoard) {
        this.board = newBoard;
        removeAll();
        createBoardButtons();
        revalidate();
        repaint();
    }

    /**
     * Обновляет только состояние кнопок без замены доски.
     * Более легковесный метод, чем updateBoard.
     */
    public void refreshButtons() {
        updateButtons();
        repaint();
    }

    /**
     * Устанавливает новую доску без полной перерисовки компонентов.
     *
     * @param newBoard новая доска
     */
    public void setBoard(Board newBoard) {
        this.board = newBoard;
        updateButtons();
    }

    /**
     * Возвращает текущую доску, отображаемую на панели.
     *
     * @return текущая доска
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Очищает выделение всех кнопок на панели.
     * Сбрасывает границы всех кнопок к стандартным серым границам.
     */
    public void clearSelection() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                JButton button = buttons[row][col];
                button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        }
    }

    /**
     * Подсвечивает определенную клетку заданным цветом.
     * Устанавливает цветную границу вокруг указанной кнопки.
     *
     * @param row строка клетки
     * @param col столбец клетки
     * @param color цвет подсветки
     */
    public void highlightCell(int row, int col, Color color) {
        if (row >= 0 && row < buttons.length && col >= 0 && col < buttons[0].length) {
            buttons[row][col].setBorder(BorderFactory.createLineBorder(color, 3));
        }
    }

    /**
     * Включает или выключает интерактивность всех кнопок на панели.
     *
     * @param enabled true для включения интерактивности, false для выключения
     */
    public void setButtonsEnabled(boolean enabled) {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (interactive) {
                    buttons[row][col].setEnabled(enabled);
                }
            }
        }
    }

    /**
     * Включает или выключает конкретную кнопку на панели.
     *
     * @param row строка кнопки
     * @param col столбец кнопки
     * @param enabled true для включения, false для выключения
     */
    public void setButtonEnabled(int row, int col, boolean enabled) {
        if (row >= 0 && row < buttons.length && col >= 0 && col < buttons[0].length) {
            buttons[row][col].setEnabled(enabled);
        }
    }

    /**
     * Устанавливает слушатель событий кликов по клеткам поля.
     *
     * @param listener слушатель событий
     */
    public void setCellClickListener(ActionListener listener) {
        this.cellClickListener = listener;
    }

    /**
     * Устанавливает режим отображения кораблей на поле.
     *
     * @param showShips true для показа кораблей, false для скрытия
     */
    public void setShowShips(boolean showShips) {
        this.showShips = showShips;
        updateButtons();
    }

    /**
     * Устанавливает интерактивность поля.
     *
     * @param interactive true для интерактивного поля, false для неинтерактивного
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        updateButtons();
    }
}