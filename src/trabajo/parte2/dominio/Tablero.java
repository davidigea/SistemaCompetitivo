package trabajo.parte2.dominio;

import jade.core.AID;
import java.util.HashMap;

/*
 * Clase que implementa un tablero
 */
public class Tablero {
    // Variables de la clase
    private Casilla casillas[][];
    private HashMap<AID,Posicion> taxis;


    // Constructor de la clase
    public Tablero(int n, int m) {
        taxis = new HashMap<>();
        casillas = new Casilla[n][m];
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                casillas[i][j] = new Casilla(Estado.LIBRE, 0);
            }
        }
    }

    // Obtener el contenido de una casilla
    public Casilla getCasilla(int fila, int columna) {
        return casillas[fila][columna];
    }

    // Modificar una casilla
    public void setCasilla(int fila, int columna, Casilla c) {
        casillas[fila][columna] = c;
    }

    // Obtener posiciones de taxis
    public HashMap<AID, Posicion> getTaxis() {
        return taxis;
    }

    // Establecer posicion de un taxi
    public void moverTaxi(AID taxi, Posicion posicion) {
        this.taxis.put(taxi,posicion);
        int numCochesPasados = casillas[posicion.getFila()][posicion.getColumna()].getNumCochesPasados();
        casillas[posicion.getFila()][posicion.getColumna()].setNumCochesPasados(numCochesPasados+1);
    }
}
