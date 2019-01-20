package trabajo.parte2.comportamiento;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import trabajo.parte2.dominio.Casilla;
import trabajo.parte2.dominio.Estado;

// Comportamiento de un Taxi cuando quiere pedir una casilla
// Envia una petición de casilla al GestorTablero
public class TaxiComportamiento extends Behaviour {
    // Variables
    private static double PENALIZACION_POR_PASO = 0.02;
    private static int VALOR_PERSONA = 1;
    private static int VALOR_MURO = -1;
    private static int VALOR_COCHE = -1;
    int fila, columna;
    AID receptor;

    // Constructor
    public TaxiComportamiento(int fila, int columna, AID receptor) {
        this.fila = fila;
        this.columna = columna;
        this.receptor = receptor;
    }

    // Se encarga de enviar los mensajes de petición de las casillas adyacentes
    @Override
    public void action() {
        Casilla c = pedirCasilla(0,0);
        System.out.println("El valor es " + c.getE().toString());
    }

    // Calcula la función de utilidad para una casilla
    // Tiene un coste exponencial, puede modificarse para lograr un coste
    // lineal en el número de casillas
    double funcionUtilidad(int i, int j) {
        Casilla c = pedirCasilla(i, j);
        if(c.getE() == Estado.MURO) {
            return VALOR_MURO;
        }
        else if(c.getE() == Estado.PERSONA) {
            return VALOR_PERSONA;
        }
        else if(c.getE() == Estado.COCHE) {
            return VALOR_COCHE;
        }
        else { // Persona
            // Hacermos la recursividad
            double[] resultados = new double[8];
            int cont = 0;
            for(int k=i-1; k<i+2; k++) {
                for(int m=j-1; m<j+2; m++) {
                    if(k!=i && m!=j) {
                        resultados[cont] = funcionUtilidad(k,m) - PENALIZACION_POR_PASO;
                        cont++;
                    }
                }
            }
            double max = resultados[0];
            for(int k=0; k<8; k++) {
                if(resultados[k]>max) {
                    max = resultados[k];
                }
            }
            return max - 1/(2*c.getNumCochesPasados());
        }
    }

    // Pide una casilla al GestorTablero
    private Casilla pedirCasilla(int i, int j) {
        // Enviamos el mensaje al GestorTablero
        ACLMessage m = new ACLMessage(ACLMessage.QUERY_REF);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        m.addReceiver(receptor);
        m.setContent(String.valueOf(i) + "\n" + String.valueOf(j));
        this.myAgent.send(m);
        m = this.myAgent.blockingReceive(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_QUERY));

        // Miramos la respuesta
        switch (m.getPerformative()) {
            case ACLMessage.INFORM_REF:
                try {
                    return (Casilla) m.getContentObject();
                }
                catch(UnreadableException e) {
                    throw new RuntimeException("Fallo al deserializar el objeto");
                }
            case ACLMessage.REFUSE: return new Casilla(Estado.MURO, 0);
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    @Override
    public boolean done() {
        return true;
    }
}
