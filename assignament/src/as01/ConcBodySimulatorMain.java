package as01;

public class ConcBodySimulatorMain {
    public static void main(String[] args) {
        //SimulationViewer viewer = new SimulationViewer(620,620);

        ConcSimulator sim = new ConcSimulator(9);
        sim.execute();
    }
}
