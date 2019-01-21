package trabajo.parte2.dominio;

import java.io.Serializable;

public class Posicion implements Serializable {
    private int fila, columna;

    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    // Devuelve true si y solo si las posiciones p1 y p2 son alcanzables en un (1) paso
    public static boolean esAlcanzable(Posicion p1, Posicion p2){
        return p1.getFila()==p2.getFila() && Math.abs(p1.getColumna()-p2.getColumna())<=1 ||
                p1.getColumna()==p2.getColumna() && Math.abs(p1.getFila()-p2.getFila())<=1;
    }
}
