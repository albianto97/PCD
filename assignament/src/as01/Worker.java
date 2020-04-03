package as01;

/**
 * Classe base per il worker del nostro progetto.
 */
public class Worker extends Thread {
    protected ConcSimulator sim;
    protected long maxIter;

    public Worker(String name, ConcSimulator sim, long maxIter) {
        super(name);

        this.maxIter = maxIter;
        this.sim = sim;
    }

    protected  void logAndWait(String msg){
        this.log(msg);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + getName() + "]: " + msg);
        }
    }
}
