package trabajo.parte2.comportamiento;

import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import trabajo.parte2.agente.Taxi;
import trabajo.parte2.dominio.Casilla;
import trabajo.parte2.dominio.Tablero;
import java.util.HashMap;
import java.util.HashSet;

// Comportamiento de un Taxi cuando quiere pedir una casilla
// Envia una petición de casilla al GestorTablero
public class TaxiComportamiento extends Behaviour {
    // Variables
    private static double PENALIZACION_POR_PASO = 0.02;
    private static double VALOR_PERSONA = 1.0;
    private static double VALOR_MURO_COCHE = -1.0;

    // Se encarga de enviar los mensajes de petición de las casillas adyacentes
    @Override
    public void action() {
        Tablero c = pedirTablero(); System.out.println(c);
    }

    // Calcula la función de utilidad para una casilla
    // Algoritmo de programación dinámica
    private double funcionUtilidad(int fila, int columna) {
        // Variables necesarias
        Tablero t = new Tablero(10,10);
        HashMap<Casilla, Double> valoresUtilidad = new HashMap<>();
        HashSet<Casilla> yaCalculadas = new HashSet<>();
        HashSet<Casilla> ultimosConocidos = new HashSet<>();

        // Rellenamos los valores de utilidad ya conocidos
        for(int i=0; i<t.getNumFilas(); i++) {
            for(int j=0; j<t.getNumColumnas(); j++) {
                Casilla c = t.getCasilla(fila, columna);
                switch(c.getE()) {
                    case COCHE: case MURO:
                        valoresUtilidad.put(c, VALOR_MURO_COCHE);
                        yaCalculadas.add(c);
                        break;
                    case PERSONA:
                        valoresUtilidad.put(c, VALOR_PERSONA);
                        yaCalculadas.add(c);
                        ultimosConocidos.add(c);
                        break;
                    default: break;
                }
            }
        }

        // Bucle que calcula los valores de todas las casillas libres
        while(yaCalculadas.size()<(t.getNumColumnas()*t.getNumFilas())) {
            HashSet<Casilla> nuevos = new HashSet<>();
            for(Casilla c : ultimosConocidos) {
                Casilla aux;
                try { // Casilla norte
                    aux = t.getCasilla(c.getFila()-1,c.getColumna());
                    if(!yaCalculadas.contains(aux)) {
                        anyadirValores(aux, valoresUtilidad.get(c), valoresUtilidad, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla sur
                    aux = t.getCasilla(c.getFila()+1,c.getColumna());
                    if(!yaCalculadas.contains(aux)) {
                        anyadirValores(aux, valoresUtilidad.get(c), valoresUtilidad, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla este
                    aux = t.getCasilla(c.getFila(),c.getColumna()+1);
                    if(!yaCalculadas.contains(aux)) {
                        anyadirValores(aux, valoresUtilidad.get(c), valoresUtilidad, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla oeste
                    aux = t.getCasilla(c.getFila(),c.getColumna()-1);
                    if(!yaCalculadas.contains(aux)) {
                        anyadirValores(aux, valoresUtilidad.get(c), valoresUtilidad, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
            }
            ultimosConocidos = nuevos;
        }
        return valoresUtilidad.get(t.getCasilla(fila,columna));
    }

    // Función que añade los valores a los correspondientes almacenes
    private void anyadirValores(Casilla aux, double valorOriginal, HashMap<Casilla, Double> valoresUtilidad,
                                HashSet<Casilla> yaCalculadas, HashSet<Casilla> nuevos) {
        valoresUtilidad.put(aux, valoresUtilidad.get(valorOriginal) - PENALIZACION_POR_PASO);
        yaCalculadas.add(aux);
        nuevos.add(aux);
    }

    // Pide el tablero al GestorTablero
    private Tablero pedirTablero() {
        // Enviamos el mensaje al GestorTablero
        ACLMessage m = new ACLMessage(ACLMessage.QUERY_REF);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        m.addReceiver(((Taxi)this.myAgent).getGestorTablero());
        this.myAgent.send(m);
        m = this.myAgent.blockingReceive(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_QUERY));

        // Miramos la respuesta
        switch (m.getPerformative()) {
            case ACLMessage.INFORM_REF:
                try {
                    return (Tablero) m.getContentObject();
                }
                catch(UnreadableException e) {
                    throw new RuntimeException("Fallo al deserializar el objeto");
                }
            case ACLMessage.REFUSE: throw new RuntimeException("Tablero no recibido (REFUSE)");
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    @Override
    public boolean done() {
        return true;
    }
}
