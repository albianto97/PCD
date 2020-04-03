package as01;

import java.util.ArrayList;

/**
 * Worker che si occupa dell'aggiornamento dei corpi.
 */
public class Updater extends Worker {
    // Lista delle proprie palline.
    protected ArrayList <ConcBody> myBodies;

    public Updater(String name, ConcSimulator sim, long maxIter) {
        super(name, sim, maxIter);

        this.myBodies = new ArrayList<>();
    }

    /**
     * Aggiunge una pallina alla lista interna di palline che avrà il compito di
     * aggiornare.
     * @param body Pallina.
     */
    public void addBody(ConcBody body) {
        this.myBodies.add(body);
    }
    public ArrayList<ConcBody> getBody(){
        return this.myBodies;
    }

    public void run() {
        long iteration = 0;

        // Inizio il ciclo per tutte le iterazioni.
        while(iteration < this.maxIter) {
            // Per tutte le palline che gli competono
            for (ConcBody b : myBodies) {
                try {
                    // Aggiorna la posizione
                    b.updatePos(sim.getDt(), this, b);
                    // log(String.format("Aggiornata p:%d", b.getIndex()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            iteration++;
        }
    }
}
