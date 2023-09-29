class MySemaphore {
    private int permits;

    public MySemaphore(int permits) {
        this.permits = permits;
    }

    public synchronized void acquire() throws InterruptedException {
        while (permits <= 0) {
            wait();
        }
        permits--;
    }

    public synchronized void release() {
        permits++;
        notify();
    }
}

class HoneyPot {
    private final int maxCapacity;
    private int currentAmount = 0;
    private final MySemaphore semaphore;

    public HoneyPot(int maxCapacity, MySemaphore semaphore) {
        this.maxCapacity = maxCapacity;
        this.semaphore = semaphore;
    }

    public void addHoney() throws InterruptedException {
        semaphore.acquire();
        synchronized (this) {
            while (isFull()) {
                wait();
            }
            currentAmount++;
            System.out.println("Бджола принесла мед до горщика. Поточна кількість меду: " + currentAmount);
            notifyAll();
        }
        semaphore.release();
    }

    public synchronized void eatAllHoney() {
        System.out.println("Вінні Пух з'їв увесь мед.");
        currentAmount = 0;
        notifyAll();
    }

    public synchronized boolean isFull() {
        return currentAmount == maxCapacity;
    }
}

class Bear implements Runnable {
    private final HoneyPot honeyPot;

    public Bear(HoneyPot honeyPot) {
        this.honeyPot = honeyPot;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (honeyPot) {
                while (!honeyPot.isFull()) {
                    try {
                        honeyPot.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                honeyPot.eatAllHoney();
            }
        }
    }
}

class Bee implements Runnable {
    private final HoneyPot honeyPot;

    public Bee(HoneyPot honeyPot) {
        this.honeyPot = honeyPot;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(500);
                honeyPot.addHoney();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        int numberOfBees = 4;
        int maxCapacity = 10;
        MySemaphore semaphore = new MySemaphore(numberOfBees);
        HoneyPot honeyPot = new HoneyPot(maxCapacity, semaphore);

        Bear bear = new Bear(honeyPot);
        Thread bearThread = new Thread(bear);
        bearThread.start();

        for (int i = 0; i < numberOfBees; i++) {
            Runnable bee = new Bee(honeyPot);
            Thread beeThread = new Thread(bee);
            beeThread.start();
        }
    }
}
