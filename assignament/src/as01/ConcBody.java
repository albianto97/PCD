package as01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcBody {
    private int index;
    private Position pos;
    private Velocity vel;
    private double radius;
    private Lock mutex;
    private boolean available, isTaken, velTaken, checkCollider;
    Condition isUpdated, isNotUpdated, velWait;

    public ConcBody(int index, Position pos, Velocity vel, double radius){
        this.index = index;
        this.pos = pos;
        this.checkCollider = false;
        this.vel = vel;
        this.radius = radius;
        this.available = false;
        this.velTaken = false;
        //this.isTaken = false;
        this.mutex= new ReentrantLock();
        this.isUpdated = mutex.newCondition();
        this.isNotUpdated = mutex.newCondition();
        this.velWait = mutex.newCondition();
    }

    public int getIndex() {
        return this.index;
    }

    public double getRadius() {
        return radius;
    }

    public Position getPos() {
        return pos;
    }

    /**
     * Controlla che l'iterazione corrente sia la stessa alla quale è aggiornata il corpo.
     * @return True è sincronizzata, false altrimenti.
     */
    /*public boolean isTaken() {
        try{
            mutex.lock();

            // Se era già stata presa, dico che è già stata presa.
            if(this.isTaken)
                return true;

            // Se non era già stata presa, la segno come presa e dico che non
            // era già stata presa.
            this.isTaken = true;
            return false;
        } finally {
            mutex.unlock();
        }
    }*/

    /**
     * Segnalo di aver finito le operazioni sulla pallina corrente.
     * Quindi devo svegliare il thread che sta aspettando di aggiornare la posizione.
     * Aggiorno quindi l'indice dell'interazione a cui devo essere aggiornato.
     * Inoltre imposto la disponibilità della posizione a false.
     */
    public void signalCollisionCompleted() {
        try{
            mutex.lock();
            this.checkCollider = true;
            /*while() {
                this.available = false; ///CONTROLLARE!!
            }*/
            this.isNotUpdated.signal();
        } finally {
            mutex.unlock();
        }
    }

    public Velocity getVel(Worker wk) throws InterruptedException {
        try{
            mutex.lock();
            System.out.println("Thread Funzione getVel()-> "+ wk.getName() + " "+ this.velTaken );
            while(this.velTaken)
                this.velWait.await();

            return vel;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Aggiorna la posizione del corpo. Se venisse richiamato quando è ancora valida la sua
     * posizione, allora aspetta fino a quando il collider non la dichiara come da aggiornar
     * aggiornare.
     * @param dt Delta time.
     * @throws InterruptedException Lanciata dalla await.
     */
    public void updatePos(double dt, Worker wk, ConcBody b) throws InterruptedException {
        try {
            // Prendo il lock per la sezione critica.
            mutex.lock();

            // Devo rimanere in attesa finché sono aggiornato.
            System.out.println("Thread UpdatePos() "+ wk.getName() +" "+ "Pronta: "+ available + " Pallina: " + b.index+"\n");
            while (available)
                //System.out.println();
                this.isNotUpdated.await();

            // Quando non sono più aggiornato, mi devo sbloccare.
            // Prendo le coordinate aggiornate.
            double newPosX = pos.getX() + vel.getX() * dt;
            double newPosY = pos.getY() + vel.getY() * dt;
            pos.change(newPosX, newPosY);
            b.setCheckCollider(false);

            // Prepariamo la pallina ad essere processata dai collider.
            // La devono vedere disponibile.
            //this.isTaken = false;
            this.available = true;
            this.isUpdated.signal();
        } finally {
            mutex.unlock(); // Rilascio il lock per la sezione critica.
        }
    }

    private void log(String msg) {
        synchronized(System.out) {
            System.out.println("[ update pos ]" +msg);
        }
    }

    /**
     * Change the velocity
     *
     * @param vx Nuova componente x
     * @param vy Nuova componente y
     */
    public void changeVel(double vx, double vy) {
        try{
            mutex.lock();

            vel.change(vx, vy);
            this.velTaken = false;
            this.velWait.signal();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Computes the distance from the specified body
     *
     * @param b Body to check collision
     * @return Sqrt of distance between objects.
     */
    public double getDistance(ConcBody b, Worker wk) throws InterruptedException {
        try {
            // Prendo il lock per la sezione critica.
            mutex.lock();

            // Devo rimanere in attesa fintanto che non sono aggiornato.
            System.out.println("Thread getDistance"+ wk.getName() + " "+ (!this.available || !b.available));
            while (!this.available || !b.available)
                this.isUpdated.await();

            // Devo proseguire solo quando sono sincronizzato.
            double dx = pos.getX() - b.getPos().getX();
            double dy = pos.getY() - b.getPos().getY();
            return Math.sqrt(dx * dx + dy * dy);
        } finally {
            mutex.unlock();
        }
    }


    /**
     * Check if there is collision with the specified body
     * @param b Corpo con cui far collidere questo.
     * @return True se è stata trovata una collisione, false altrimenti.
     */
    public boolean collideWith(ConcBody b, Worker wk) throws InterruptedException {
        double distance = getDistance(b, wk);
        return distance < radius + b.getRadius();
    }


    public void setCheckCollider(Boolean check){
        try {
            mutex.lock();
            this.checkCollider = check;
        }finally{
            mutex.unlock();
        }
    }
    /**
     * Check if there collisions with the boundary and update the
     * position and velocity accordingly
     *
     * @param bounds Bordi del campo da gioco.
     */
    public void checkAndSolveBoundaryCollision(Boundary bounds){
        double x = pos.getX();
        double y = pos.getY();
        if (x > bounds.getX1()){
            pos.change(bounds.getX1(), pos.getY());
            this.changeVel(-vel.getX(), vel.getY());
        } else if (x < bounds.getX0()){
            pos.change(bounds.getX0(), pos.getY());
            this.changeVel(-vel.getX(), vel.getY());
        } else if (y > bounds.getY1()){
            pos.change(pos.getX(), bounds.getY1());
            this.changeVel(vel.getX(), -vel.getY());
        } else if (y < bounds.getY0()){
            pos.change(pos.getX(), bounds.getY0());
            this.changeVel(vel.getX(), -vel.getY());
        }

    }

    public static void solveCollision(ConcBody b1, ConcBody b2, Worker wk) throws InterruptedException {

        Position x1 = b1.getPos();
        Position x2 = b2.getPos();
        Velocity v1 = b1.getVel(wk);
        Velocity v2 = b2.getVel(wk);

        double x12dx = x1.getX() - x2.getX();
        double x12dy = x1.getY() - x2.getY();
        double v12dx = v1.getX() - v2.getX();
        double v12dy = v1.getY() - v2.getY();
        double fact12 = (x12dx*v12dx + x12dy*v12dy) / (x12dx*x12dx + x12dy*x12dy);
        double v1x = v1.getX() - x12dx*fact12;
        double v1y = v1.getY() - x12dy*fact12;

        double x21dx = x2.getX() - x1.getX();
        double x21dy = x2.getY() - x1.getY();
        double v21dx = v2.getX() - v1.getX();
        double v21dy = v2.getY() - v1.getY();
        double fact21 = (x21dx*v21dx + x21dy*v21dy) / (x21dx*x21dx + x21dy*x21dy);
        double v2x = v2.getX() - x21dx*fact21;
        double v2y = v2.getY() - x21dy*fact21;

        b1.changeVel(v1x, v1y);
        b2.changeVel(v2x, v2y);
    }
}
