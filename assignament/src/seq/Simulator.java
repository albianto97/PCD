package seq;

import java.util.*;

public class Simulator {
        
	private SimulationViewer viewer;

	/* bodies in the field */ 
	ArrayList<Body> bodies;
	
	/* boundary of the field */
	private Boundary bounds;
	
    public Simulator(SimulationViewer viewer){
    	this.viewer = viewer;

    	/* initializing boundary and bodies */
    	
        bounds = new Boundary(-1.0,-1.0,1.0,1.0);

        /* test with 3 big bodies */
        
        /*
        bodies = new ArrayList<ConcBody>();
        bodies.add(new ConcBody(new Position(-0.5,0), new Velocity(0.005,0), 0.05));
        bodies.add(new ConcBody(new Position(0,0.05), new Velocity(0,0), 0.05));
        bodies.add(new ConcBody(new Position(0.07,-0.1), new Velocity(0,0), 0.05));
        */
        
        /* test with 100 small bodies */
       
        Random rand = new Random(System.currentTimeMillis());
        bodies = new ArrayList<Body>();
        for (int i = 0; i < 100; i++) {
            double x = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
            double y = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
            double dx = -1 + rand.nextDouble()*2;
            double speed = rand.nextDouble()*0.05;
            Body b = new Body(new Position(x, y), new Velocity(dx*speed,Math.sqrt(1 - dx*dx)*speed), 0.01);
            bodies.add(b);
        }

    }

    public void execute() {
    	
        /* init virtual time */
        
        double vt = 0;         
        double dt = 0.1;
        
        long iter = 0; 
        long nIterations = 10000; /* Nsteps */
        
        /* simulation loop */
        
        while (iter < nIterations){
	        
        	/* compute bodies new pos */

        	for (Body b: bodies) {
        		b.updatePos(dt);
		    }
		            
        	/* check collisions */
        	
		    for (int i = 0; i < bodies.size() - 1; i++) { // Cicla solo le mie palline.
		    	Body b1 = bodies.get(i);
		        for (int j = i + 1; j < bodies.size(); j++) { // Cicla tutte le palline.
		        	Body b2 = bodies.get(j);
		            if (b1.collideWith(b2)) {
		            	Body.solveCollision(b1, b2);
		            }
		            // Termine ciclo tutte.
		        }
				// Segnalo di aver finito di operare su questa pallina.
	        }
		    
		    /* check boundaries */
		    
		    for (Body b: bodies) {
		    	b.checkAndSolveBoundaryCollision(bounds);
		    }
		    
		    /* update virtual time */
		    
		    vt = vt + dt;
		    iter++;

		    /* display current stage */
		    
        	viewer.display(bodies, vt, iter);
        
        }
    }

}
