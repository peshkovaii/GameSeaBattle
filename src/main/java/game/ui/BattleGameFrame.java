package game.ui;

import game.controllers.GameController;
import game.models.Board;
import game.models.Cell;
import game.models.CellState;
import game.utils.TimerManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Главное окно графического интерфейса игры "Морской бой с минами и подлодками".
 * Предоставляет пользовательский интерфейс для игры, включая экран расстановки кораблей
 * и экран битвы. Управляет переключением между режимами, отображением игрового состояния
 * и обработкой пользовательских действий.
 *
 * Окно использует CardLayout для переключения между экранами расстановки и игры.
 */
public class BattleGameFrame extends JFrame {
    /**
     * Контроллер игры, управляющий игровой логикой и состоянием.
     */
    private GameController controller;

    /**
     * Панель для отображения поля игрока.
     */
    private GameBoardPanel playerBoardPanel;

    /**
     * Панель для отображения поля противника (бота).
     */
    private GameBoardPanel enemyBoardPanel;

    /**
     * Метка для отображения текущего статуса игры.
     */
    private JLabel statusLabel;

    /**
     * Метка для отображения номера текущего хода.
     */
    private JLabel turnLabel;

    /**
     * Метка для отображения времени игры.
     */
    private JLabel timerLabel;

    /**
     * Основная панель с CardLayout для переключения между экранами.
     */
    private JPanel gamePanel;

    /**
     * Менеджер компоновки для переключения между экранами расстановки и игры.
     */
    private CardLayout cardLayout;

    /**
     * Панель для расстановки кораблей игроком.
     */
    private ShipPlacementPanel placementPanel;

    /**
     * Менеджер времени для отслеживания продолжительности игры.
     */
    private TimerManager timerManager;

    /**
     * Таймер для задержки хода бота.
     */
    private Timer botMoveTimer;

    /**
     * Задержка между ходами бота в миллисекундах.
     */
    private int botMoveDelay = 1500;

    /**
     * Создает главное окно игры с указанным контроллером.
     * Инициализирует пользовательский интерфейс и настраивает окно.
     *
     * @param controller контроллер игры для управления логикой
     */
    public BattleGameFrame(GameController controller) {
        this.controller = controller;
        this.controller.setGameFrame(this);
        this.timerManager = controller.getTimerManager();

        setTitle("Морской бой с минами и подлодками");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeUI();

        pack();
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Инициализирует пользовательский интерфейс окна.
     * Создает основные компоненты и настраивает их расположение.
     */
    private void initializeUI() {
        cardLayout = new CardLayout();
        gamePanel = new JPanel(cardLayout);

        placementPanel = new ShipPlacementPanel(controller.getPlayerBoard());
        placementPanel.addPropertyChangeListener("placementFinished",
                evt -> switchToBattleScreen());

        JPanel battleScreen = createBattleScreen();

        gamePanel.add(placementPanel, "placement");
        gamePanel.add(battleScreen, "battle");

        add(gamePanel, BorderLayout.CENTER);
        cardLayout.show(gamePanel, "placement");
    }

    /**
     * Создает экран битвы с игровыми полями и элементами управления.
     * Включает панель статуса, игровые поля с легендами и панель кнопок.
     *
     * @return панель с интерфейсом игры
     */
    private JPanel createBattleScreen() {
        JPanel battlePanel = new JPanel(new BorderLayout());

        JPanel statusPanel = createStatusPanel();
        battlePanel.add(statusPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel boardsPanel = createBoardsPanelWithLegends();
        mainPanel.add(boardsPanel, BorderLayout.CENTER);

        JPanel instructionPanel = createInstructionPanel();
        mainPanel.add(instructionPanel, BorderLayout.SOUTH);

        battlePanel.add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        battlePanel.add(buttonPanel, BorderLayout.SOUTH);

        return battlePanel;
    }

    /**
     * Создает панель статуса с информацией о ходе игры.
     * Включает текущий статус, номер хода и время игры.
     *
     * @return панель статуса
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.setBackground(new Color(240, 240, 240));

        statusLabel = new JLabel("Игра началась! Ваш ход.");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setOpaque(false);

        turnLabel = new JLabel("Ход: 0");
        turnLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        timerLabel = new JLabel("Время: 00:00");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        infoPanel.add(turnLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(timerLabel);

        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(infoPanel, BorderLayout.EAST);

        return statusPanel;
    }

    /**
     * Создает панель с игровыми полями и легендами.
     * Размещает поле игрока и поле противника рядом.
     *
     * @return панель с двумя игровыми полями
     */
    private JPanel createBoardsPanelWithLegends() {
        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        playerBoardPanel = new GameBoardPanel(controller.getPlayerBoard(), true, false);
        JPanel playerPanel = createBoardPanelWithLegend(playerBoardPanel, "Ваше поле", true);

        enemyBoardPanel = new GameBoardPanel(controller.getBotBoard(), false, true);
        enemyBoardPanel.setCellClickListener(this::handleEnemyCellClick);
        JPanel enemyPanel = createBoardPanelWithLegend(enemyBoardPanel, "Поле противника", false);

        boardsPanel.add(playerPanel);
        boardsPanel.add(enemyPanel);

        return boardsPanel;
    }

    /**
     * Создает панель игрового поля с заголовком и легендой.
     *
     * @param boardPanel панель игрового поля
     * @param title заголовок поля
     * @param isPlayerBoard true если это поле игрока, false если поле противника
     * @return панель с игровым полем и легендой
     */
    private JPanel createBoardPanelWithLegend(GameBoardPanel boardPanel, String title, boolean isPlayerBoard) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.add(boardPanel, BorderLayout.CENTER);

        LegendPanel legendPanel = new LegendPanel(isPlayerBoard);
        legendPanel.setPreferredSize(new Dimension(200, 0));
        contentPanel.add(legendPanel, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Создает панель с инструкцией по игре.
     *
     * @return панель инструкции
     */
    private JPanel createInstructionPanel() {
        JPanel instructionPanel = new JPanel();
        instructionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Как играть"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        instructionPanel.setBackground(new Color(245, 245, 245));

        JTextArea instructions = new JTextArea();
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setBackground(new Color(245, 245, 245));
        instructions.setFont(new Font("Arial", Font.PLAIN, 12));
        instructions.setText(
                "1. Кликайте по полю противника чтобы атаковать клетку\n" +
                        "2. После вашего хода противник делает ход через " + (botMoveDelay/1000) + " секунды\n" +
                        "3. X - попадание в корабль, O - промах\n" +
                        "4. Корабли противника скрыты до попадания\n" +
                        "5. Ваши корабли видны всегда на вашем поле"
        );

        instructionPanel.add(instructions);
        return instructionPanel;
    }

    /**
     * Создает панель с кнопками управления игрой.
     *
     * @return панель с кнопками
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton autoAttackButton = new JButton("Случайная атака");
        autoAttackButton.setToolTipText("Случайно атаковать клетку на поле противника");
        autoAttackButton.addActionListener(e -> makeRandomAttack());

        JButton surrenderButton = new JButton("Сдаться");
        surrenderButton.setToolTipText("Завершить игру досрочно");
        surrenderButton.addActionListener(e -> surrender());

        JButton restartButton = new JButton("Новая игра");
        restartButton.setToolTipText("Начать новую игру");
        restartButton.addActionListener(e -> restartGame());

        buttonPanel.add(autoAttackButton);
        buttonPanel.add(surrenderButton);
        buttonPanel.add(restartButton);

        return buttonPanel;
    }

    /**
     * Переключает интерфейс с экрана расстановки на экран битвы.
     * Использует доску, созданную в панели расстановки, создает новый контроллер
     * и начинает игру. Отображает начальное сообщение.
     */
    private void switchToBattleScreen() {
        Board playerBoardWithShips = placementPanel.getBoardWithShips();

        controller = new GameController(playerBoardWithShips);
        controller.setGameFrame(this);
        timerManager = controller.getTimerManager();

        playerBoardPanel.updateBoard(controller.getPlayerBoard());
        enemyBoardPanel.updateBoard(controller.getBotBoard());

        enemyBoardPanel.setCellClickListener(this::handleEnemyCellClick);

        controller.startGame();

        cardLayout.show(gamePanel, "battle");

        startGameTimer();

        statusLabel.setText("Игра началась! Ваш ход. Атакуйте поле противника.");
        JOptionPane.showMessageDialog(this,
                "Игра началась!\n" +
                        "Ваша задача - потопить все корабли противника.\n" +
                        "Кликайте по полю справа для атаки.",
                "Начало игры",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Обрабатывает клик игрока по клетке поля противника.
     * Выполняет атаку по выбранной клетке и передает ход боту.
     * Проверяет возможность атаки и отображает результат.
     *
     * @param e событие ActionEvent с координатами клетки в формате "row,col"
     */
    private void handleEnemyCellClick(ActionEvent e) {
        if (!controller.isPlayerTurn() || controller.isGameOver()) {
            JOptionPane.showMessageDialog(this,
                    "Сейчас не ваш ход!",
                    "Невозможно атаковать",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] coordinates = e.getActionCommand().split(",");
        int row = Integer.parseInt(coordinates[0]);
        int col = Integer.parseInt(coordinates[1]);

        Cell cell = controller.getBotBoard().getCell(row, col);
        if (cell != null &&
                (cell.getState() == CellState.HIT ||
                        cell.getState() == CellState.MISS ||
                        cell.getState() == CellState.DESTROYED ||
                        cell.getState() == CellState.REVEALED)) {

            JOptionPane.showMessageDialog(this,
                    "Эта клетка уже атакована!",
                    "Невозможно атаковать",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CellState previousState = (cell != null) ? cell.getState() : CellState.EMPTY;

        boolean attacked = controller.playerAttack(row, col);

        if (attacked) {
            enemyBoardPanel.updateButtons();
            playerBoardPanel.updateButtons();

            turnLabel.setText("Ход: " + controller.getTurnCount());

            showAttackResult(row, col, previousState);

            if (controller.isGameOver()) {
                endGame();
                return;
            }

            if (!controller.isPlayerTurn()) {
                statusLabel.setText("Ваш ход завершен. Ход противника через " +
                        (botMoveDelay/1000) + " сек...");
                startBotMoveTimer();
            } else {
                statusLabel.setText("Ваш ход. Атакуйте поле противника!");
            }
        } else {
            statusLabel.setText("Нельзя атаковать эту клетку! Попробуйте другую.");
        }
    }

    /**
     * Безопасно завершает ход бота и передает ход игроку.
     * Включает интерактивность поля противника и обновляет статус.
     */
    private void safelyEndBotTurn() {
        if (!controller.isPlayerTurn()) {
            controller.setPlayerTurn(true);
        }

        playerBoardPanel.updateButtons();
        enemyBoardPanel.updateButtons();
        enemyBoardPanel.setInteractive(true);
        statusLabel.setText("Ваш ход. Атакуйте поле противника!");
    }

    /**
     * Показывает результат атаки игрока.
     * Определяет тип пораженной цели и отображает соответствующее сообщение.
     *
     * @param row строка атакованной клетки
     * @param col столбец атакованной клетки
     * @param previousState состояние клетки до атаки
     */
    private void showAttackResult(int row, int col, CellState previousState) {
        char colLetter = (char)('A' + col);
        int rowNumber = row + 1;

        String message = "Вы атаковали " + colLetter + rowNumber + ": ";

        Cell cell = controller.getBotBoard().getCell(row, col);
        if (cell == null) {
            return;
        }

        CellState currentState = cell.getState();

        if (currentState == CellState.HIT) {
            if (previousState == CellState.SHIP || previousState == CellState.SUBMARINE) {
                message += "ПОПАДАНИЕ! Корабль противника поврежден.";
            } else if (previousState == CellState.MINE) {
                message += "ПОПАДАНИЕ В МИНУ! Вы узнали координату корабля противника.";
            } else if (previousState == CellState.MINESWEEPER) {
                message += "ПОПАДАНИЕ В ТРАЛЬЩИК! Вы узнали координату мины противника.";
            }
        } else if (currentState == CellState.DESTROYED) {
            message += "УНИЧТОЖЕНО! Корабль противника потоплен!";
        } else if (currentState == CellState.MISS) {
            message += "промах.";
        } else if (currentState == CellState.REVEALED) {
            message += "цель уже известна.";
        }

        if (!message.isEmpty()) {
            statusLabel.setText(message);
        }
    }

    /**
     * Запускает таймер для хода бота с заданной задержкой.
     * Временно отключает поле противника и запускает таймер.
     */
    private void startBotMoveTimer() {
        if (botMoveTimer != null && botMoveTimer.isRunning()) {
            botMoveTimer.stop();
        }

        enemyBoardPanel.setInteractive(false);

        botMoveTimer = new Timer(botMoveDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    botMove();
                } catch (Exception ex) {
                    System.err.println("Ошибка в ходе бота: " + ex.getMessage());
                    safelyEndBotTurn();
                }
                ((Timer)e.getSource()).stop();
            }
        });
        botMoveTimer.setRepeats(false);
        botMoveTimer.start();
    }

    /**
     * Инициирует ход бота с дополнительной задержкой для реалистичности.
     * Показывает сообщение о том, что бот "думает".
     */
    private void botMove() {
        if (controller.isGameOver()) return;

        statusLabel.setText("Противник выбирает цель для атаки...");

        Timer thinkTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeBotMove();
                ((Timer)e.getSource()).stop();
            }
        });
        thinkTimer.setRepeats(false);
        thinkTimer.start();
    }

    /**
     * Выполняет ход бота - случайную атаку по полю игрока.
     * Ищет доступную для атаки клетку и выполняет атаку через контроллер.
     */
    private void executeBotMove() {
        if (controller.isGameOver()) {
            statusLabel.setText("Игра окончена!");
            return;
        }

        boolean botMoved = false;
        int attempts = 0;
        int row = -1, col = -1;

        while (!botMoved && attempts < 1000) {
            row = (int)(Math.random() * controller.getPlayerBoard().getSize());
            col = (int)(Math.random() * controller.getPlayerBoard().getSize());

            Cell cell = controller.getPlayerBoard().getCell(row, col);
            if (cell != null &&
                    cell.getState() != CellState.HIT &&
                    cell.getState() != CellState.MISS &&
                    cell.getState() != CellState.DESTROYED &&
                    cell.getState() != CellState.REVEALED) {

                botMoved = controller.botAttack(row, col);
            }
            attempts++;
        }

        if (!botMoved) {
            statusLabel.setText("Противник не нашел цель для атаки. Ваш ход!");
            controller.setPlayerTurn(true);
            return;
        }

        char colLetter = (char)('A' + col);
        int rowNumber = row + 1;
        String message = "Противник атаковал " + colLetter + rowNumber;
        statusLabel.setText(message);

        playerBoardPanel.updateButtons();
        enemyBoardPanel.updateButtons();

        turnLabel.setText("Ход: " + controller.getTurnCount());

        if (controller.isGameOver()) {
            endGame();
        } else if (controller.isPlayerTurn()) {
            statusLabel.setText("Ваш ход. Атакуйте поле противника!");
        } else {
            controller.setPlayerTurn(true);
            statusLabel.setText("Ваш ход. Атакуйте поле противника!");
        }
    }

    /**
     * Выполняет случайную атаку игрока по полю противника.
     * Находит случайную неатакованную клетку и эмулирует клик по ней.
     */
    private void makeRandomAttack() {
        if (!controller.isPlayerTurn() || controller.isGameOver()) {
            JOptionPane.showMessageDialog(this,
                    "Сейчас не ваш ход!",
                    "Невозможно атаковать",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Board botBoard = controller.getBotBoard();
        int size = botBoard.getSize();
        int row, col;

        int attempts = 0;
        do {
            row = (int)(Math.random() * size);
            col = (int)(Math.random() * size);
            attempts++;
            if (attempts > 1000) {
                statusLabel.setText("Нет доступных клеток для атаки!");
                return;
            }
        } while (botBoard.getCell(row, col).getState() == CellState.HIT ||
                botBoard.getCell(row, col).getState() == CellState.MISS ||
                botBoard.getCell(row, col).getState() == CellState.DESTROYED ||
                botBoard.getCell(row, col).getState() == CellState.REVEALED);

        ActionEvent fakeEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, row + "," + col);
        handleEnemyCellClick(fakeEvent);
    }

    /**
     * Запускает игровой таймер для отсчета времени.
     * Обновляет отображение времени каждую секунду и проверяет таймаут.
     */
    private void startGameTimer() {
        Timer gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.isGameOver()) {
                    ((Timer)e.getSource()).stop();
                    return;
                }

                timerLabel.setText("Время: " + timerManager.getFormattedTime());

                if (timerManager.isTimeout()) {
                    controller.setGameOver(true);
                    controller.setWinner("Timeout");
                    endGame();
                }
            }
        });
        gameTimer.start();
    }

    /**
     * Завершает игру, показывая результат и статистику.
     * Показывает все поле противника и отображает итоговую статистику.
     */
    private void endGame() {
        String message;
        String title;

        if ("Player".equals(controller.getWinner())) {
            message = "ПОБЕДА!\nВы уничтожили все корабли противника!";
            title = "Поздравляем!";
        } else if ("Bot".equals(controller.getWinner())) {
            message = "ПОРАЖЕНИЕ\nПротивник уничтожил все ваши корабли.";
            title = "Игра окончена";
        } else if ("Timeout".equals(controller.getWinner())) {
            message = "ВРЕМЯ ВЫШЛО\nИгра завершена по таймауту.";
            title = "Таймаут";
        } else {
            message = "Игра завершена.";
            title = "Конец игры";
        }

        enemyBoardPanel.setShowShips(true);
        enemyBoardPanel.updateButtons();
        statusLabel.setText(message.split("\n")[0]);
        showGameStatistics(message, title);
    }

    /**
     * Показывает статистику завершенной игры.
     * Отображает диалоговое окно с результатами игры и статистикой.
     *
     * @param message основное сообщение о результате
     * @param title заголовок диалога
     */
    private void showGameStatistics(String message, String title) {
        int totalTurns = controller.getTurnCount();
        String time = timerManager.getFormattedTime();

        String stats = message + "\n\n" +
                "Статистика игры:\n" +
                "• Всего ходов: " + totalTurns + "\n" +
                "• Затраченное время: " + time + "\n" +
                "• Ваши потопленные корабли: " + countSunkShips(controller.getPlayerBoard()) + "\n" +
                "• Потопленные корабли противника: " + countSunkShips(controller.getBotBoard());

        Object[] options = {"Новая игра", "Выход"};
        int choice = JOptionPane.showOptionDialog(this,
                stats,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    /**
     * Подсчитывает количество потопленных кораблей на поле.
     * Игнорирует минные тральщики при подсчете.
     *
     * @param board игровое поле
     * @return количество потопленных кораблей
     */
    private int countSunkShips(Board board) {
        int count = 0;
        for (game.models.Ship ship : board.getShips()) {
            if (ship.isSunk() && !(ship instanceof game.models.Minesweeper)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Перезапускает игру с новыми полями и расстановками.
     * Создает новый контроллер и открывает новое окно.
     */
    private void restartGame() {
        controller = new GameController();
        controller.setGameFrame(this);
        timerManager = controller.getTimerManager();

        dispose();

        SwingUtilities.invokeLater(() -> {
            new BattleGameFrame(controller);
        });
    }

    /**
     * Обрабатывает сдачу игрока.
     * Подтверждает намерение сдаться и завершает игру поражением.
     */
    private void surrender() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите сдаться?\nЭто засчитается как поражение.",
                "Сдаться",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            controller.setGameOver(true);
            controller.setWinner("Bot");
            endGame();
        }
    }

    /**
     * Обновляет игровое состояние на интерфейсе.
     * Обновляет отображение полей и статусной информации.
     */
    public void refreshGameState() {
        playerBoardPanel.updateButtons();
        enemyBoardPanel.updateButtons();
        statusLabel.setText(controller.isPlayerTurn() ?
                "Ваш ход. Атакуйте поле противника!" :
                "Ход противника...");
        turnLabel.setText("Ход: " + controller.getTurnCount());
    }
}