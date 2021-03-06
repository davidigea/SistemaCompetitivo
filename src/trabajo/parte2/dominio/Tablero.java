package trabajo.parte2.dominio;

import jade.core.AID;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/*
 * Clase que implementa un tablero
 */
public class Tablero implements Serializable {
    // Variables de la clase
    private Casilla[][] casillas;
    private HashMap<AID,Posicion> taxis;
    private HashMap<AID,Boolean> sigueEnOptimo;
    private int numFilas, numColumnas;
    private static final long serialVersionUID = 42L;


    // Constructor de la clase
    public Tablero(int n, int m) {
        taxis = new HashMap<>();
        casillas = new Casilla[n][m];
        sigueEnOptimo = new HashMap<>();
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                casillas[i][j] = new Casilla(Estado.LIBRE, 0, i, j);
            }
        }
        numFilas = n;
        numColumnas = m;
    }

    // Obtener el contenido de una casilla
    public Casilla getCasilla(int fila, int columna) {
        return casillas[fila][columna];
    }

    // Modificar una casilla
    public void setCasilla(int fila, int columna, Casilla c) {
        casillas[fila][columna] = c;
    }

    // Modificar una casilla
    public void setCasilla(int fila, int columna, Estado e, int numCoches) {
        Casilla c = casillas[fila][columna];
        c.setE(e);
        c.setNumCochesPasados(numCoches);
        casillas[fila][columna] = c;
    }

    // Modificar una casilla
    public void setCasilla(int fila, int columna, Estado e) {
        Casilla c = casillas[fila][columna];
        c.setE(e);
        casillas[fila][columna] = c;
    }

    // Obtener posiciones de taxis
    public HashMap<AID, Posicion> getTaxis() {
        return taxis;
    }

    // Obtener caminos optimos
    public HashMap<AID, Boolean> getSigueEnOptimo() { return sigueEnOptimo; }

    // Establecer posicion de un taxi
    public void moverTaxi(AID taxi, Posicion posicion) {
        // Si el taxi ya estaba en el tablero, dejar posición vacía
        Posicion c = taxis.get(taxi);
        if(c != null) {
            setCasilla(c.getFila(), c.getColumna(), Estado.LIBRE);
        }
        this.taxis.put(taxi,posicion);
        int numCochesPasados = casillas[posicion.getFila()][posicion.getColumna()].getNumCochesPasados();
        casillas[posicion.getFila()][posicion.getColumna()].setNumCochesPasados(numCochesPasados+1);
        casillas[posicion.getFila()][posicion.getColumna()].setE(Estado.COCHE);
    }

    // Actualizar si un taxi sigue en optimo
    public void setTaxiEnOptimo(AID t, Boolean b) {
        sigueEnOptimo.put(t,b);
    }

    public void borrarTaxi(AID t) {
        taxis.remove(t);
    }

    public int getNumFilas() {
        return numFilas;
    }

    public void setNumFilas(int numFilas) {
        this.numFilas = numFilas;
    }

    public int getNumColumnas() {
        return numColumnas;
    }

    public void setNumColumnas(int numColumnas) {
        this.numColumnas = numColumnas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tablero tablero = (Tablero) o;
        return numFilas == tablero.numFilas &&
                numColumnas == tablero.numColumnas &&
                Arrays.equals(casillas, tablero.casillas) &&
                Objects.equals(taxis, tablero.taxis);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(taxis, numFilas, numColumnas);
        result = 31 * result + Arrays.hashCode(casillas);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        for(int i=0; i<numFilas; i++) {
            for(int j=0; j<numColumnas; j++) {
                switch(casillas[i][j].getE()) {
                    case MURO: s.append("M | "); break;
                    case PERSONA: s.append("P | "); break;
                    case COCHE: s.append("C | "); break;
                    default: s.append("  | "); break;
                }
            }
            s.delete(s.length()-3, s.length());
            s.append("\n");
            if((i+1) != numFilas) {
                for(int j=0; j<numColumnas*4; j++) {
                    s.append("_");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
