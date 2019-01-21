package trabajo.parte2.dominio;

import java.io.Serializable;
import java.util.Objects;

public class Casilla implements Serializable {
    private Estado e;
    private int numCochesPasados, fila, columna;

    public Casilla(Estado e, int numCochesPasados, int fila, int columna) {
        this.e = e;
        this.numCochesPasados = numCochesPasados;
        this.fila = fila;
        this.columna = columna;
    }

    public Estado getE() {
        return e;
    }

    public void setE(Estado e) {
        this.e = e;
    }

    public int getNumCochesPasados() {
        return numCochesPasados;
    }

    public void setNumCochesPasados(int numCochesPasados) {
        this.numCochesPasados = numCochesPasados;
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

    @Override
    public String toString() {
        return "Casilla{" +
                "e=" + e +
                ", numCochesPasados=" + numCochesPasados +
                ", fila=" + fila +
                ", columna=" + columna +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Casilla casilla = (Casilla) o;
        return numCochesPasados == casilla.numCochesPasados &&
                fila == casilla.fila &&
                columna == casilla.columna &&
                e == casilla.e;
    }

    @Override
    public int hashCode() {
        return Objects.hash(e, numCochesPasados, fila, columna);
    }
}
