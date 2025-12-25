package game.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Панель легенды для игрового поля.
 * Отображает условные обозначения, используемые на игровом поле,
 * чтобы помочь игроку понять значения различных символов и цветов.
 *
 * Содержит две разные версии легенды:
 * - Для своего поля (показывает все типы объектов)
 * - Для поля противника (показывает только видимую информацию)
 */
public class LegendPanel extends JPanel {

    /**
     * Создает панель легенды для указанного типа поля.
     *
     * @param isPlayerBoard true для легенды своего поля,
     *                      false для легенды поля противника
     */
    public LegendPanel(boolean isPlayerBoard) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Обозначения"));

        if (isPlayerBoard) {
            createPlayerBoardLegend();
        } else {
            createEnemyBoardLegend();
        }
    }

    /**
     * Создает легенду для своего поля.
     * Показывает все типы объектов, которые могут находиться на поле игрока.
     */
    private void createPlayerBoardLegend() {
        // Легенда для своего поля
        add(createLegendItem(Color.GRAY, "S", "Корабль"));
        add(createLegendItem(Color.CYAN, "U", "Подлодка"));
        add(createLegendItem(Color.ORANGE, "*", "Мина"));
        add(createLegendItem(Color.YELLOW, "T", "Минный тральщик"));
        add(createLegendItem(Color.RED, "X", "Попадание"));
        add(createLegendItem(Color.BLUE, "O", "Промах"));
        add(createLegendItem(Color.PINK, "R", "Обнаружен"));
    }

    /**
     * Создает легенду для поля противника.
     * Показывает только ту информацию, которая видна игроку
     * при атаке поля противника.
     */
    private void createEnemyBoardLegend() {
        // Легенда для поля противника
        add(createLegendItem(Color.RED, "X", "Попадание в корабль"));
        add(createLegendItem(Color.BLUE, "O", "Промах"));
        add(createLegendItem(Color.WHITE, "", "Неизвестно"));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(new JLabel("Корабли противника скрыты!"));
    }

    /**
     * Создает элемент легенды с цветным индикатором, символом и описанием.
     *
     * @param color цвет индикатора
     * @param symbol символ, отображаемый на поле
     * @param description текстовое описание элемента
     * @return панель с элементом легенды
     */
    private JPanel createLegendItem(Color color, String symbol, String description) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel colorLabel = new JLabel("   ");
        colorLabel.setOpaque(true);
        colorLabel.setBackground(color);
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorLabel.setPreferredSize(new Dimension(20, 20));

        JLabel symbolLabel = new JLabel(symbol + " ");
        symbolLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel descLabel = new JLabel("- " + description);

        panel.add(colorLabel);
        panel.add(symbolLabel);
        panel.add(descLabel);

        return panel;
    }
}