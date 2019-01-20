package trabajo.parte2.agente;

import jade.core.Agent;
import trabajo.parte2.comportamiento.RecibeConsultaComportamiento;
import trabajo.parte2.comportamiento.RecibeMovimientoComportamiento;
import trabajo.parte2.dominio.Tablero;

public class GestorTablero extends Agent {

    private Tablero tablero;
    @Override
    public void setup() {
        //TODO: crear tablero
        addBehaviour(new RecibeConsultaComportamiento(this));
        addBehaviour(new RecibeMovimientoComportamiento(this));
    }

    public Tablero getTablero(){return tablero;}
}
