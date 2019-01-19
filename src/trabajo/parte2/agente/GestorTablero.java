package trabajo.parte2.agente;

import jade.core.Agent;
import trabajo.parte2.comportamiento.RecibeConsultaComportamiento;
import trabajo.parte2.dominio.Tablero;

public class GestorTablero extends Agent {
    @Override
    public void setup() {
        addBehaviour(new RecibeConsultaComportamiento(this, new Tablero(10, 10)));
    }
}
