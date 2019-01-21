package trabajo.parte2.dominio;

import java.io.Serializable;
import java.util.Objects;

public class Casilla implements Serializable {
    private static final long serialVersionUID = 42L;
    private Estado e;
    private int numCochesPasados;

    public Casilla(Estado e, int numCochesPasados) {
        this.e = e;
        this.numCochesPasados = numCochesPasados;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Casilla casilla = (Casilla) o;
        return numCochesPasados == casilla.numCochesPasados &&
                e == casilla.e;
    }

    @Override
    public int hashCode() {
        return Objects.hash(e, numCochesPasados);
    }

    @Override
    public String toString() {
        return "Casilla{" +
                "e=" + e +
                ", numCochesPasados=" + numCochesPasados +
                '}';
    }
}
