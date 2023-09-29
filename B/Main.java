import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

class Visitor implements Runnable {
    private final int id;

    private Queue<Visitor> visitors;

    public Visitor(int id, Queue<Visitor> visitors) {
        this.id = id;
        this.visitors = visitors;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(new Random().nextInt(2000));
            System.out.println("Клієнт " + id + " прибув до барбершопу.");
            synchronized (visitors) {
                visitors.add(this);
                visitors.notify();
            }
            System.out.println("Клієнт " + id + " встав у чергу.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
}

class Barber implements Runnable {

    private Queue<Visitor> visitors;

    public Barber(Queue<Visitor> visitors) {
        this.visitors = visitors;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Visitor currentVisitor = visitors.poll();
                if (currentVisitor != null) {
                    System.out.println("Клієнт " + currentVisitor.getId() + " сів у крісло.");
                    Thread.sleep(500);
                    System.out.println("Клієнт " + currentVisitor.getId() + " покинув барбершоп.");
                }else {
                    synchronized (visitors){
                        System.out.println("Барбер засинає.");
                        visitors.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        int numberOfVisitors = 5;

        final Queue<Visitor> visitors = new ConcurrentLinkedDeque<>();
        Thread[] threads = new Thread[numberOfVisitors];
        Barber barber = new Barber(visitors);
        Thread barberThread = new Thread(barber);
        barberThread.start();

        for (int i = 0; i < numberOfVisitors; i++) {
            threads[i] = new Thread(new Visitor(i + 1, visitors));
            threads[i].start();
        }

        for (int i = 0; i < numberOfVisitors; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            barberThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}