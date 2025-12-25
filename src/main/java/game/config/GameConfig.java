package game.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс конфигурации игры "Морской бой с минами и подлодками".
 * Загружает настройки из файла config.properties или использует значения по умолчанию.
 * Предоставляет статические методы для доступа к параметрам конфигурации игры.
 */
public class GameConfig {
    /**
     * Объект Properties для хранения конфигурационных параметров.
     * Инициализируется статическим блоком при загрузке класса.
     */
    private static final Properties properties = new Properties();

    /**
     * Статический блок инициализации для загрузки конфигурации.
     * Пытается загрузить настройки из файла config.properties в classpath.
     * Если файл не найден или произошла ошибка загрузки, используются значения по умолчанию.
     */
    static {
        try (InputStream input = GameConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input != null) {
                properties.load(input);
            } else {
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.out.println("Error loading config: " + e.getMessage());
            setDefaultProperties();
        }
    }

    /**
     * Устанавливает значения конфигурации по умолчанию.
     * Вызывается при отсутствии файла config.properties или ошибке его загрузки.
     */
    private static void setDefaultProperties() {
        properties.setProperty("board.size", "10");
        properties.setProperty("max.turns", "100");
        properties.setProperty("game.timeout.minutes", "5");
        properties.setProperty("use.russian.alphabet", "false");

        properties.setProperty("battleship.count", "1");
        properties.setProperty("cruiser.count", "2");
        properties.setProperty("destroyer.count", "3");
        properties.setProperty("submarine.count", "1");
        properties.setProperty("minesweeper.count", "1");
        properties.setProperty("mine.count", "1");

        properties.setProperty("allow.touching", "false");
        properties.setProperty("mine.reveals.cell", "true");
        properties.setProperty("minesweeper.protected.radius", "1");
    }

    /**
     * Возвращает размер игрового поля.
     *
     * @return размер поля в клетках
     */
    public static int getBoardSize() {
        return Integer.parseInt(properties.getProperty("board.size"));
    }

    /**
     * Возвращает максимальное количество ходов в игре.
     * Игра завершается при достижении этого лимита.
     *
     * @return максимальное количество ходов
     */
    public static int getMaxTurns() {
        return Integer.parseInt(properties.getProperty("max.turns"));
    }

    /**
     * Возвращает таймаут игры в минутах.
     * Игра автоматически завершается по истечении этого времени.
     *
     * @return таймаут игры в минутах
     */
    public static int getGameTimeoutMinutes() {
        return Integer.parseInt(properties.getProperty("game.timeout.minutes"));
    }

    /**
     * Определяет, используется ли русский алфавит для обозначения столбцов.
     *
     * @return true если используется русский алфавит, false для английского
     */
    public static boolean useRussianAlphabet() {
        return Boolean.parseBoolean(properties.getProperty("use.russian.alphabet"));
    }

    /**
     * Возвращает количество линкоров (4-палубных кораблей) на поле.
     *
     * @return количество линкоров
     */
    public static int getBattleshipCount() {
        return Integer.parseInt(properties.getProperty("battleship.count"));
    }

    /**
     * Возвращает количество крейсеров (3-палубных кораблей) на поле.
     *
     * @return количество крейсеров
     */
    public static int getCruiserCount() {
        return Integer.parseInt(properties.getProperty("cruiser.count"));
    }

    /**
     * Возвращает количество эсминцев (2-палубных кораблей) на поле.
     *
     * @return количество эсминцев
     */
    public static int getDestroyerCount() {
        return Integer.parseInt(properties.getProperty("destroyer.count"));
    }

    /**
     * Возвращает количество подводных лодок (одноклеточных кораблей) на поле.
     * Подлодки могут быть скрыты и требуют специальных правил обнаружения.
     *
     * @return количество подводных лодок
     */
    public static int getSubmarineCount() {
        return Integer.parseInt(properties.getProperty("submarine.count"));
    }

    /**
     * Возвращает количество минных тральщиков на поле.
     * Тральщики могут раскрывать мины противника.
     *
     * @return количество минных тральщиков
     */
    public static int getMinesweeperCount() {
        return Integer.parseInt(properties.getProperty("minesweeper.count"));
    }

    /**
     * Возвращает количество мин на поле.
     * Мины наносят урон и раскрывают координаты кораблей при срабатывании.
     *
     * @return количество мин
     */
    public static int getMineCount() {
        return Integer.parseInt(properties.getProperty("mine.count"));
    }

    /**
     * Определяет, разрешено ли касание кораблей друг друга при расстановке.
     * Если false, между кораблями должна быть минимум одна клетка расстояния.
     *
     * @return true если касание разрешено, false если запрещено
     */
    public static boolean isAllowTouching() {
        return Boolean.parseBoolean(properties.getProperty("allow.touching"));
    }

    /**
     * Определяет, раскрывает ли мина клетку корабля при срабатывании.
     * Если true, игрок получает координату случайного корабля противника.
     *
     * @return true если мина раскрывает клетку, false в противном случае
     */
    public static boolean isMineRevealsCell() {
        return Boolean.parseBoolean(properties.getProperty("mine.reveals.cell"));
    }

    /**
     * Возвращает радиус защиты минного тральщика.
     * Тральщик защищает все клетки в указанном радиусе от мин.
     *
     * @return радиус защиты тральщика в клетках
     */
    public static int getMinesweeperProtectedRadius() {
        return Integer.parseInt(properties.getProperty("minesweeper.protected.radius"));
    }
}