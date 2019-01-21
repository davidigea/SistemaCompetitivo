package trabajo.parte2.agente;

import jade.core.AID;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import trabajo.parte2.comportamiento.RecibeConsultaComportamiento;
import trabajo.parte2.comportamiento.RecibeMovimientoComportamiento;
import trabajo.parte2.dominio.Posicion;
import trabajo.parte2.dominio.Tablero;

public class GestorTablero extends Agent {
    private Tablero tablero;

    @Override
    public void setup() {
        //TODO: crear tablero
        tablero = new Tablero(10, 10);
        ContainerController cc = getContainerController();
        AgentController ac;
        Object[] argumentos = new Object[3];
        argumentos[0] = 0;
        argumentos[1] = 0;
        argumentos[2] = this.getAID();
        try {
            ac = cc.createNewAgent("Paco", "trabajo.parte2.agente.Taxi", argumentos);
            ac.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        addBehaviour(new RecibeConsultaComportamiento(this));
        addBehaviour(new RecibeMovimientoComportamiento(this));
    }

    public Tablero getTablero(){return tablero;}

    public void moverTaxi(AID taxi, Posicion posicion){
        tablero.moverTaxi(taxi,posicion);
    }

    // Crea un tablero aleatorio
    // También lanza todos los agentes Taxi
    /*public Tablero crearTableroAleatorio(int numFilas, int numColumnas,
                                         int numMuros, int numPersonas,
                                         int numTaxis) {
        if((numMuros+numPersonas+numTaxis)>(numFilas*numPersonas)) {
            throw new RuntimeException("No puede crearse un tablero con más elementos que casillas");
        }
    }*/
}
