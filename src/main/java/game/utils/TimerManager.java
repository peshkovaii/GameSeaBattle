package game.utils;

/**
 * Менеджер для отслеживания времени игры. Позволяет запускать, останавливать и сбрасывать таймер,
 * а также контролировать превышение установленного лимита времени.
 */
public class TimerManager {
    /**
     * Время начала отсчета текущего интервала в миллисекундах.
     */
    private long startTime;

    /**
     * Накопленное время в миллисекундах, когда таймер был остановлен.
     */
    private long elapsedTime;

    /**
     * Флаг, указывающий, запущен ли таймер в данный момент.
     */
    private boolean running;

    /**
     * Максимально допустимая продолжительность игры до тайм-аута (10 минут) в миллисекундах.
     */
    private static final long TIMEOUT_DURATION = 10 * 60 * 1000;

    /**
     * Создает новый менеджер таймера в остановленном состоянии с нулевым накопленным временем.
     */
    public TimerManager() {
        this.elapsedTime = 0;
        this.running = false;
    }

    /**
     * Запускает таймер, если он не был запущен ранее.
     * Время отсчитывается с учетом ранее накопленного времени (при паузе).
     */
    public void start() {
        if (!running) {
            startTime = System.currentTimeMillis() - elapsedTime;
            running = true;
        }
    }

    /**
     * Останавливает таймер, фиксируя текущее накопленное время.
     */
    public void stop() {
        if (running) {
            elapsedTime = System.currentTimeMillis() - startTime;
            running = false;
        }
    }

    /**
     * Полностью сбрасывает таймер: обнуляет накопленное время и останавливает отсчет.
     */
    public void reset() {
        elapsedTime = 0;
        running = false;
    }

    /**
     * Возвращает общее время, прошедшее с момента запуска таймера (с учетом пауз),
     * в миллисекундах.
     *
     * @return прошедшее время в миллисекундах
     */
    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        } else {
            return elapsedTime;
        }
    }

    /**
     * Возвращает отформатированную строку, представляющую прошедшее время в формате "MM:SS".
     *
     * @return строка времени в формате "минуты:секунды"
     */
    public String getFormattedTime() {
        long totalSeconds = getElapsedTime() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Проверяет, превышено ли максимально допустимое время игры.
     *
     * @return true, если прошедшее время больше или равно лимиту тайм-аута
     */
    public boolean isTimeout() {
        return getElapsedTime() >= TIMEOUT_DURATION;
    }

    /**
     * Возвращает состояние таймера.
     *
     * @return true, если таймер запущен, false - если остановлен
     */
    public boolean isRunning() {
        return running;
    }
}