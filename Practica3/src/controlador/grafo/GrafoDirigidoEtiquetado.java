package controlador.grafo;

import controlador.listas.ListaEnlazada;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class GrafoDirigidoEtiquetado<E> extends GrafoDirigido {

    protected E etiquetas[];
    protected HashMap<E, Integer> dicVertices;
    private Class<E> clazz;
    private Boolean marcaEtiqueta[];

    public GrafoDirigidoEtiquetado(Integer numVertices, Class clazz) {
        super(numVertices);
        this.clazz = clazz;
        this.etiquetas = (E[]) Array.newInstance(clazz, numVertices + 1);
        this.dicVertices = new HashMap(numVertices);
        this.marcaEtiqueta = new Boolean[etiquetas.length];
    }

    public Boolean existeAristaE(E o, E d) throws Exception {
        return this.existeArista(obtenerCodigoE(o), obtenerCodigoE(d));
    }

    public void insertarAristaE(E o, E d, Double peso) throws Exception {
        insertarArista(obtenerCodigoE(o), obtenerCodigoE(d), peso);
    }

    public void insertarAristaE(E o, E d) throws Exception {
        insertarArista(obtenerCodigoE(o), obtenerCodigoE(d));
    }

    public ListaEnlazada<Adyacencia> adyacentesE(E o) {
        System.out.println(obtenerCodigoE(o));
        return adyacentes(obtenerCodigoE(o));
    }

    public Integer obtenerCodigoE(E etiqueta) {
        return dicVertices.get(etiqueta);
    }

    public E obtenerEtiqueta(Integer codigo) {
        return etiquetas[codigo];
    }

    public void etiquetarVertice(Integer codigo, E etiqueta) {
        etiquetas[codigo] = etiqueta;
        dicVertices.put(etiqueta, codigo);
    }

    public String toString() {
        StringBuffer grafo = new StringBuffer("");
        try {
            for (int i = 1; i <= nroVertices(); i++) {
                grafo.append("Vertice " + String.valueOf(etiquetas[i]));
                ListaEnlazada<Adyacencia> lista = adyacentes(i);
                if (lista != null) {
                    for (int j = 0; j < lista.getTamanio(); j++) {
                        Adyacencia a = lista.obtener(j);
                        if (a.getPeso().toString().equalsIgnoreCase(String.valueOf(Double.NaN))) {
                            grafo.append(" -- Vertice destino -- " + etiquetas[a.getDestino()] + " Sin Peso");
                        } else {
                            grafo.append(" -- Vertice destino -- " + etiquetas[a.getDestino()] + " Peso " + a.getPeso());
                        }
                    }
                }
                grafo.append("\n");
            }
        } catch (Exception e) {
            grafo.append(e);
            e.printStackTrace();
        }
        return grafo.toString();
    }

    public ListaEnlazada<E> floyd(E origen, E destino) {
        try {
            Double distancias[][] = new Double[nroVertices() + 1][nroVertices() + 1];
            Integer vertices[][] = new Integer[nroVertices() + 1][nroVertices() + 1];

            llenarMatrizDistancias(distancias);

            llenarMatrizVertices(vertices);

            operarMatrices(distancias, vertices);

            return recuperarCaminoCortoFloyd(origen, destino, distancias, vertices);

        } catch (Exception ex) {
            Logger.getLogger(GrafoDirigidoEtiquetado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private ListaEnlazada<E> recuperarCaminoCortoFloyd(E origen, E destino, Double distancias[][], Integer vertices[][]) {
        ListaEnlazada<E> camino = new ListaEnlazada<>();

        if (vertices[obtenerCodigoE(origen)][obtenerCodigoE(destino)] != null) {
            JOptionPane.showMessageDialog(null, "peso mínimo: " + distancias[obtenerCodigoE(origen)][obtenerCodigoE(destino)]);
            System.out.println("peso mínimo: " + distancias[obtenerCodigoE(origen)][obtenerCodigoE(destino)]);

            Integer aux = obtenerCodigoE(origen);
            camino.insertar(origen);
            while (vertices[aux][obtenerCodigoE(destino)] != null) {
                camino.insertar(obtenerEtiqueta(vertices[aux][obtenerCodigoE(destino)]));
                aux = vertices[aux][obtenerCodigoE(destino)];
            }
        } else {
            System.out.println("No hay camino");
        }

        return camino;
    }

    private void operarMatrices(Double distancias[][], Integer vertices[][]) throws Exception {
        for (int i = 1; i < distancias.length; i++) {
            for (int j = 1; j < distancias[i].length; j++) {
                for (int k = 1; k < distancias[i].length; k++) {
                    if ((distancias[j][i] + distancias[i][k]) < distancias[j][k]) {
                        distancias[j][k] = (distancias[j][i] + distancias[i][k]);
                        vertices[j][k] = i;
                    }
                    if (distancias[j][k] == 999.0) {
                        vertices[j][k] = null;
                    }
                }
            }
        }
    }

    private void llenarMatrizVertices(Integer matriz[][]) {
        for (int i = 1; i < matriz.length; i++) {
            for (int j = 1; j < matriz[i].length; j++) {
                if (i == j) {
                    matriz[i][j] = null;
                } else {
                    matriz[i][j] = j;
                }
            }
        }
    }

    private void llenarMatrizDistancias(Double matriz[][]) throws Exception {
        for (int i = 1; i < matriz.length; i++) {
            for (int j = 1; j < matriz[i].length; j++) {
                if (i == j) {
                    matriz[i][j] = 0.0;
                } else {
                    if (existeArista(i, j)) {
                        matriz[i][j] = pesoArista(i, j);
                    } else {
                        matriz[i][j] = 999.0;
                    }
                }
            }
        }
    }

    private void presentarMatriz(Object matriz[][]) {
        for (int i = 1; i < matriz.length; i++) {
            System.out.print(i + "\t");
            for (int j = 1; j < matriz[i].length; j++) {
                System.out.print(matriz[i][j] + "\t");
            }
            System.out.println("\n");
        }
    }
}
