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
        int filaActual = ((Taxi)this.myAgent).getFila();
        int columnaActual = ((Taxi)this.myAgent).getColumna();
        // Filas y columnas a comprobar. Sentido horario.
        int[] filas = { Math.abs(filaActual-1), filaActual, filaActual+1, filaActual };
        int[] columnas = { columnaActual, columnaActual+1, columnaActual, Math.abs(columnaActual-1) };

        // Resultado funciones de utilidad, sentido horario.
        double[] funcionesUtilidad = {0.0,0.0,0.0,0.0};
        int sentidoMax = 0; // 0^ 1> 2v 3<
        for(int i=0;i<4;i++){
            funcionesUtilidad[i] = funcionUtilidad(filas[i],columnas[i]);
            if(funcionesUtilidad[i]>funcionesUtilidad[sentidoMax]){
                sentidoMax = i;
            }
        }

        solicitarMoverse(filas[sentidoMax],columnas[sentidoMax]);
        System.out.println(funcionUtilidad(((Taxi)this.myAgent).getFila(), ((Taxi)this.myAgent).getColumna()));
    }

    // Calcula la función de utilidad para una casilla
    // Algoritmo de programación dinámica
    private double funcionUtilidad(int fila, int columna) {
        // Variables necesarias
        Tablero t = pedirTablero();
        double[][] valoresUtilidad = new double[t.getNumFilas()][t.getNumColumnas()];
        HashSet<Casilla> yaCalculadas = new HashSet<>();
        HashSet<Casilla> ultimosConocidos = new HashSet<>();

        // Rellenamos los valores de utilidad ya conocidos
        for(int i=0; i<t.getNumFilas(); i++) {
            for(int j=0; j<t.getNumColumnas(); j++) {
                Casilla c = t.getCasilla(i, j);
                switch(c.getE()) {
                    case COCHE: case MURO:
                        valoresUtilidad[i][j] = VALOR_MURO_COCHE;
                        yaCalculadas.add(c);
                        break;
                    case PERSONA:
                        valoresUtilidad[i][j] = VALOR_PERSONA;
                        yaCalculadas.add(c);
                        ultimosConocidos.add(c);
                        break;
                    default: valoresUtilidad[i][j] = 0.0; break;
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
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()] - PENALIZACION_POR_PASO;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla sur
                    aux = t.getCasilla(c.getFila()+1,c.getColumna());
                    if(!yaCalculadas.contains(aux)) {
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()] - PENALIZACION_POR_PASO;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla este
                    aux = t.getCasilla(c.getFila(),c.getColumna()+1);
                    if(!yaCalculadas.contains(aux)) {
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()] - PENALIZACION_POR_PASO;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla oeste
                    aux = t.getCasilla(c.getFila(),c.getColumna()-1);
                    if(!yaCalculadas.contains(aux)) {
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()] - PENALIZACION_POR_PASO;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
            }
            ultimosConocidos = nuevos;
        }
        return valoresUtilidad[fila][columna];
    }

    // Función que añade los valores a los correspondientes almacenes
    private void anyadirValores(Casilla aux, HashSet<Casilla> yaCalculadas, HashSet<Casilla> nuevos) {
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

    // Solicita realizar un movimiento al GestorTablero
    private void solicitarMoverse(int fila, int columna){
        // Enviamos el mensaje al GestorTablero
        ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        m.addReceiver(((Taxi)this.myAgent).getGestorTablero());
        m.setContent(String.valueOf(fila) + "\n" + String.valueOf(columna));
        this.myAgent.send(m);
        m = this.myAgent.blockingReceive(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_PROPOSE));

        // Miramos la respuesta
        switch (m.getPerformative()) {
            case ACLMessage.ACCEPT_PROPOSAL:
                ((Taxi)this.myAgent).setFila(fila);
                ((Taxi)this.myAgent).setColumna(columna);
                break;
            case ACLMessage.REJECT_PROPOSAL: break;
            case ACLMessage.REFUSE: throw new RuntimeException("Solicitud rechazada (REFUSE)");
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
