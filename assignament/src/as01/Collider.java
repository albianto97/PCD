package as01;

import java.util.ArrayList;

/**
 * Worker che si occupa di gestire le collisioni.
 */
public class Collider extends Worker {
    // Tutte le paline contenute nel simulator.
    private ArrayList<ConcBody> myBodies, allBodies;

    public Collider(String name,
                    ConcSimulator sim,
                    ArrayList<ConcBody> myBodies,
                    ArrayList<ConcBody> allBodies,
                    long maxIter) {
        super(name, sim, maxIter);
        this.allBodies = allBodies;
        this.myBodies = myBodies;
    }

    @Override
    public void run() {
        long iter = 0;

        while(iter < this.maxIter) {
            // Ciclo su tutte le palline.
            for (ConcBody b1 : myBodies) {
                //log(String.format("Chiedo la pallina %d", b1.getIndex()));
                // Prendo la prima pallina che non è stata presa.
                //if (!b1.isTaken()) {
                    log(String.format("Ho preso la pallina %d", b1.getIndex()));

                    // Quando l'ho presa controllo che sia disponibile e la confronto con la
                    // prima palline disponibile.
                    for (int i = b1.getIndex(); i< allBodies.size()-1; i++) {
                        ConcBody b2 = allBodies.get(i+1);
                        //if ((!b2.getCheckCollider()) && (b1.getIndex() != b2.getIndex())) {
                            log(this.getName()+ "-> controllo le Palline: "+ b1.getIndex()+ " e: "+  b2.getIndex());
                            // Controllo la collisione con quest'altra pallina.
                            try {
                                if (b1.collideWith(b2, this)) {
                                    log(String.format("Collisione tra %d/%d", b1.getIndex(), b2.getIndex()));
                                    // Se collide risolvo la collisione.
                                    ConcBody.solveCollision(b1, b2, this);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        /*}else{
                            log(this.getName()+ "-> Pallina: "+ b1.getIndex()+ " Pallina: "+  b2.getIndex() + "-> controllate"+"\n");
                        }*/
                    }

                    // Risolvo le collisioni coi bordi di questo corpo.
                    b1.checkAndSolveBoundaryCollision(sim.getBounds());

                    // log(String.format("Dichiaro sfruttata la pallina %d", i));
                    // Dichiarare la pallina b1 come sfruttata.
                    log(String.format("Ho sfruttato la pallina %d", b1.getIndex()));
                    b1.signalCollisionCompleted();
                /*} else {
                    log(String.format("La pallina %d è stata già presa, passiamo alla prossima.", b1.getIndex()));
                }*/
            }

            log("-----------------------------------------");
            log("|            FINE ITERAZIONE " + iter + "           |");
            log("-----------------------------------------");
            iter++;
        }
    }
}
