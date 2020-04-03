package as01;

import java.util.ArrayList;
import java.util.Random;

public class ConcSimulator {
    //private SimulationViewer viewer;

    /* bodies in the field */
    ArrayList<ConcBody> bodies;
    /* boundary of the field */
    private Boundary bounds;
    int nThread = 3;
    long nIter = 10;
    Updater[] updaters = new Updater[nThread];
    Collider[] colliders = new Collider[nThread];
    public double dt;

    public ConcSimulator(int nBody){
        this.dt = 0.1;
        //this.viewer = viewer;
        /* initializing boundary and bodies */
        for(int i=0; i< nThread;i++){
            updaters[i] = new Updater("Updater " + i,this, nIter);
        }
        bounds = new Boundary(-1.0,-1.0,1.0,1.0);

        /* test with 100 small bodies */

        Random rand = new Random(System.currentTimeMillis());
        bodies = new ArrayList<>();
        for (int i = 0; i < nBody; i++) {
            double x = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
            double y = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
            double dx = -1 + rand.nextDouble() * 2;
            double speed = rand.nextDouble() * 0.05;
            ConcBody b = new ConcBody(i, new Position(x, y), new Velocity(dx * speed,Math.sqrt(1 - dx * dx)*speed), 0.01);
            bodies.add(b);
            updaters[i % nThread].addBody(b);
        }

        for(int i = 0; i < nThread; i++){
            colliders[i] = new Collider("Collider " + i,this, updaters[i].getBody(), bodies , nIter);
        }
    }

    public double getDt(){
        return this.dt;
    }

    public Boundary getBounds() { return this.bounds; }

    public void execute() {
        /* init virtual time */

        // double vt = 0;

        // Partono tutti i thread.
        log("Start workers.");
        for (Worker worker : updaters) {
            worker.start();
        }
        log("Start colliders");
        for (Worker collider : colliders) {
            collider.start();
        }

        /* compute bodies new pos */
        try {
            for (Worker worker : updaters) {
                worker.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            for (Worker collider : colliders) {
                collider.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // vt = vt + this.dt;
    }

    protected void log(String msg) {
        synchronized (System.out) {
            System.out.println("[Simulator]: " + msg);
        }
    }
}
