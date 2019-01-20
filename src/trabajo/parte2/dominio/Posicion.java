package trabajo.parte2.dominio;

public class Posicion {
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
        //TODO
        return true;
    }
}
