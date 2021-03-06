package trabajo.parte2.comportamiento;

import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import trabajo.parte2.agente.Taxi;
import trabajo.parte2.dominio.Casilla;
import trabajo.parte2.dominio.Estado;
import trabajo.parte2.dominio.Tablero;

// Comportamiento de un Taxi cuando quiere pedir una casilla
// Envia una petición de casilla al GestorTablero
public class TaxiComportamiento extends Behaviour {
    // Variables
    private static final double PENALIZACION_POR_PASO = -0.02;
    private static final double VALOR_PERSONA = 100.0;
    private static final double VALOR_MURO_COCHE = -1.0;
    private static final double GAMMA = 0.9;
    private static final int NUM_ITERACIONES = 1000;
    private boolean seguirBuscandoPasajero;
    private boolean primeraIteracion;
    private int pasosMinimos;

    public TaxiComportamiento() {
        super();
        seguirBuscandoPasajero = true;
        primeraIteracion = true;
    }

    // Se encarga de enviar los mensajes de petición de las casillas adyacentes
    @Override
    public void action() {
        int filaActual = ((Taxi)this.myAgent).getFila();
        int columnaActual = ((Taxi)this.myAgent).getColumna();

        // Filas y columnas a comprobar. Sentido horario.
        int[] filas = { Math.abs(filaActual-1), filaActual, filaActual+1, filaActual };
        int[] columnas = { columnaActual, columnaActual+1, columnaActual, Math.abs(columnaActual-1) };

        // Valores de utilidad, sentido horario.
        Tablero t = pedirTablero();
        double[][] U = funcionUtilidad(t, filaActual, columnaActual);
        int sentidoMax = movimiento(U, filaActual, columnaActual);

        // Calculamos camino óptimo la primera vez
        if(primeraIteracion) {
            primeraIteracion = false;
            pasosMinimos = calcularCaminoMinimo(U, filaActual, columnaActual);
        }

        // Solo nos movemos si es mejor que quedarse quieto
        if(sentidoMax >=0) {
            Estado e = t.getCasilla(filas[sentidoMax], columnas[sentidoMax]).getE();
            if(solicitarMoverse(filas[sentidoMax],columnas[sentidoMax],pasosMinimos>0)) {
                pasosMinimos--;
                if(e == Estado.PERSONA) {
                    // Paramos si hemos llegado a un pasajero
                    seguirBuscandoPasajero = false;
                }
            }
        }
    }

    // Devuelve el valor de U
    private double[][] funcionUtilidad(Tablero t, int fila, int columna) {
        // Variables necesarias
        double[][] U = new double[t.getNumFilas()][t.getNumColumnas()];
        double[][] R = new double[t.getNumFilas()][t.getNumColumnas()];
        double probabilidadColision = ((double) t.getTaxis().size()-1)/(t.getNumFilas()*t.getNumFilas());

        // Rellenamos los valores iniciales de U y R
        for(int i=0; i<t.getNumFilas(); i++) {
            for (int j=0; j < t.getNumColumnas(); j++) {
                Casilla c = t.getCasilla(i, j);
                switch(c.getE()) {
                    case COCHE: case MURO:
                        R[i][j] = VALOR_MURO_COCHE;
                        U[i][j] = VALOR_MURO_COCHE;
                        break;
                    case PERSONA:
                        R[i][j] = VALOR_PERSONA;
                        U[i][j] = VALOR_PERSONA;
                        break;
                    default: // Casilla libre
                        double penalizacionCoches = 0.0;
                        for(int k=1; k<=c.getNumCochesPasados(); k++) {
                            penalizacionCoches -= 1.0/(2*k);
                        }
                        R[i][j] = PENALIZACION_POR_PASO + penalizacionCoches;
                        U[i][j] = PENALIZACION_POR_PASO + penalizacionCoches;
                        break;
                }
            }
        }
        // Rellenamos la casilla donde nos encontramos
        R[fila][columna] = 0.0;
        U[fila][columna] = 0.0;

        // Iteramos para calcular la utilidad óptima
        for(int num_iter=0; num_iter<NUM_ITERACIONES; num_iter++) {
            double[][] U_n = new double[t.getNumFilas()][t.getNumColumnas()];
            for(int i=0; i<t.getNumFilas(); i++) {
                for (int j = 0; j < t.getNumColumnas(); j++) {
                    Casilla c = t.getCasilla(i,j);
                    U_n[i][j] = U[i][j];
                    if(c.getE() == Estado.LIBRE || (i==fila && j==columna)) {
                        double[] valores = new double[4];
                        valores[0] = valorCasilla(t, c, -1, 0, U, probabilidadColision);
                        valores[1] = valorCasilla(t, c, +1, 0, U, probabilidadColision);
                        valores[2] = valorCasilla(t, c, 0, +1, U, probabilidadColision);
                        valores[3] = valorCasilla(t, c, 0, -1, U, probabilidadColision);
                        double max = valores[0];
                        for(int k=1; k<4; k++) {
                            if(valores[k]>max) {
                                max = valores[k];
                            }
                        }
                        U_n[i][j] = R[i][j] + GAMMA*max;
                    }
                }
            }
            U = U_n;
        }
        return U;
    }

    // Función auxiliar
    private double valorCasilla(Tablero t, Casilla c, int d_f, int d_c,
                                double[][] U, double probabilidadColision){
        try{
            Casilla aux = t.getCasilla(c.getFila()+d_f,c.getColumna()+d_c);
            return (1-probabilidadColision)*U[aux.getFila()][aux.getColumna()]
                    + probabilidadColision*U[c.getFila()][c.getColumna()];
        }
        catch(IndexOutOfBoundsException e) {
            return Double.NEGATIVE_INFINITY;
        }

    }

    // Devuelve -1 si quedarse en sitio
    // Devuelve 0 si ir arriba
    // Devuelve 1 si ir al este
    // Devuelve 2 si ir al sur
    // Devuelve 3 si ir al oeste
    private int movimiento(double[][] U, int f, int c) {
        double[] valores = new double[4];
        valores[0] = devolverValor(U, f-1, c);
        valores[1] = devolverValor(U, f, c+1);
        valores[2] = devolverValor(U, f+1, c);
        valores[3] = devolverValor(U, f, c-1);
        int sentido=0;
        for(int i=1; i<4; i++) {
            if(valores[i]>valores[sentido]) {
                sentido = i;
            }
        }
        return valores[sentido]>0.0 ? sentido : -1;
    }

    // Devuelve el valor de U[f][c]
    // Si fuera de rango, devuelve -infinito
    private double devolverValor(double[][] U, int f, int c) {
        try{
            return U[f][c];
        }
        catch(IndexOutOfBoundsException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    // Devuelve los pasos hasta el pasajero más cercano
    private int calcularCaminoMinimo(double[][] U, int fila, int columna) {
        if(U[fila][columna] == VALOR_PERSONA) {
            return 0;
        }
        else {
            int value = movimiento(U, fila, columna);
            if(value == -1) {
                return -1;
            }
            else {
                int[] filas = { Math.abs(fila-1), fila, fila+1, fila };
                int[] columnas = { columna, columna+1, columna, Math.abs(columna-1) };
                return 1+calcularCaminoMinimo(U, filas[value], columnas[value]);
            }
        }
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
    // Devuelve true si se permite el movimiento, y false si no
    private boolean solicitarMoverse(int fila, int columna, boolean pasosNoSuperados){
        // Enviamos el mensaje al GestorTablero
        ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        m.addReceiver(((Taxi)this.myAgent).getGestorTablero());
        m.setContent(fila + "\n" + columna + "\n" + pasosNoSuperados);
        this.myAgent.send(m);
        m = this.myAgent.blockingReceive(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_PROPOSE));

        // Miramos la respuesta
        switch (m.getPerformative()) {
            case ACLMessage.ACCEPT_PROPOSAL:
                ((Taxi)this.myAgent).setFila(fila);
                ((Taxi)this.myAgent).setColumna(columna);
                return true;
            case ACLMessage.REJECT_PROPOSAL: return false;
            case ACLMessage.REFUSE: throw new RuntimeException("Solicitud rechazada (REFUSE)");
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    @Override
    public boolean done() {
        //Termina si ha llegado a un pasajero
        return !seguirBuscandoPasajero;
    }
}
