package vista;

import controlador.EscuelaController;
import controlador.grafo.Adyacencia;
import controlador.grafo.GrafoDirigidoEtiquetado;
import controlador.listas.ListaEnlazada;
import controlador.listas.exepciones.ListaVaciaException;
import controlador.listas.exepciones.PosicionNoEncontradaException;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import modelo.Escuela;
import vista.Utilidades.Utilidades;
import vista.modelo.ModeloTablaAdyacencias;
import vista.modelo.ModeloTablaEscuelas;

public class PrincipalDialog extends javax.swing.JDialog {

    private EscuelaController ec = new EscuelaController();
    private ModeloTablaEscuelas mte = new ModeloTablaEscuelas();
    private ModeloTablaAdyacencias mta = new ModeloTablaAdyacencias();

    /**
     * Creates new form PrincipalDialog
     */
    public PrincipalDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ec = Utilidades.leerJson();
        cargarTablaEscuelas();
        cargarCombos();
        cargarMatriz();
    }

    private void cargarCombos() {
        try {
            cbxDestinoAdyacencia = Utilidades.cargarCombo(cbxDestinoAdyacencia, ec.getListaEscuela());
            cbxDestinoBusqueda = Utilidades.cargarCombo(cbxDestinoBusqueda, ec.getListaEscuela());
            cbxOrigenAdyacencia = Utilidades.cargarCombo(cbxOrigenAdyacencia, ec.getListaEscuela());
            cbxOrigenBusqueda = Utilidades.cargarCombo(cbxOrigenBusqueda, ec.getListaEscuela());
        } catch (ListaVaciaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PosicionNoEncontradaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cargarTablaEscuelas() {
        mte.setLista(ec.getListaEscuela());
        tblEscuelas.setModel(mte);
        tblEscuelas.updateUI();
        cargarCombos();
    }

    private void eliminarEscuela() throws ListaVaciaException, PosicionNoEncontradaException, FileNotFoundException {
        if (tblEscuelas.getSelectedRow() >= 0) {
            ec.getListaEscuela().eliminarPosicion(tblEscuelas.getSelectedRow());
            for (int i = 0; i < ec.getListaEscuela().getTamanio(); i++) {
                ec.getListaEscuela().obtener(i).setId(i + 1);
                
                if(ec.getGrafoEscuela().adyacentes(i + 1).estaVacia()) continue;
                
                var aux = ec.getGrafoEscuela().adyacentes(i + 1).toArray();
                
                var nueva = new ListaEnlazada<Adyacencia>();
                
                int contador = 0;
                
                for(var d : aux){
                    if(d.getDestino() > ec.getListaEscuela().getTamanio()){
                        ec.getGrafoEscuela().adyacentes(i + 1).eliminarPosicion(contador);
                    }
                    contador++;
                }
                
            }
        }

        Utilidades.guardarJSON(ec);
        cargarTablaEscuelas();
        cargarMatriz();
    }

    private void fijarAdyacencia() throws Exception {
        Integer inicio = cbxOrigenAdyacencia.getSelectedIndex();
        Integer destino = cbxDestinoAdyacencia.getSelectedIndex();
        if (inicio == destino) {
            JOptionPane.showMessageDialog(null, "No se debe escoger la misma ubicación");
        } else if (txtDistanciaAdyacencia.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Indique la distancia de la adyacencia");
        } else {
            ec.insertarAdyacenciaPeso(ec.getListaEscuela().obtener(inicio), ec.getListaEscuela().obtener(destino), Double.parseDouble(txtDistanciaAdyacencia.getText()));
            Utilidades.guardarJSON(ec);
        }
        cargarMatriz();
    }

    private void cargarMatriz() {
        mta = new ModeloTablaAdyacencias();
        mta.setEc(ec);
        tblAdyacencias.setModel(mta);
        tblAdyacencias.updateUI();
    }

    private GrafoDirigidoEtiquetado<Escuela> crearGrafoAux(ListaEnlazada<Escuela> lista) throws ListaVaciaException, PosicionNoEncontradaException, Exception {
        EscuelaController ecAux = new EscuelaController();
        ecAux.setListaEscuela(lista);
        ecAux.crearGrafo();
        for (int i = 0; i < lista.getTamanio() - 1; i++) {
            if (ec.getGrafoEscuela().existeAristaE(lista.obtener(i), lista.obtener(i + 1))) {
                ecAux.insertarAdyacenciaPeso(lista.obtener(i), lista.obtener(i + 1), ec.getGrafoEscuela().pesoArista(ec.getGrafoEscuela().obtenerCodigoE(lista.obtener(i)), ec.getGrafoEscuela().obtenerCodigoE(lista.obtener(i + 1))));
            } else {
                ecAux.insertarAdyacencia(lista.obtener(i), lista.obtener(i + 1));
            }
        }
        return ecAux.getGrafoEscuela();
    }

    private GrafoDirigidoEtiquetado<Escuela> crearGrafoAux(EscuelaController nueva) throws ListaVaciaException, PosicionNoEncontradaException, Exception {
        EscuelaController ecAux = Utilidades.leerJson();
        var lista = nueva.getListaEscuela();
        for (int i = 0; i < nueva.getListaEscuela().getTamanio() - 1; i++) {
            if (ecAux.getGrafoEscuela().existeAristaE(lista.obtener(i), lista.obtener(i + 1))) {
                nueva.insertarAdyacenciaPeso(lista.obtener(i), lista.obtener(i + 1), ecAux.getGrafoEscuela().pesoArista(ecAux.getGrafoEscuela().obtenerCodigoE(lista.obtener(i)), ecAux.getGrafoEscuela().obtenerCodigoE(lista.obtener(i + 1))));
            } else {
                nueva.insertarAdyacencia(lista.obtener(i), lista.obtener(i + 1));
            }
        }
        return nueva.getGrafoEscuela();
    }

    private void verGrafo(GrafoDirigidoEtiquetado<Escuela> grafo) {
        new FrmGrafo(null, true, grafo).setVisible(true);
    }

    private void buscar() throws ListaVaciaException, PosicionNoEncontradaException {
        LocalTime tiempoInicio = null;
        ListaEnlazada<Escuela> aux = null;
        Escuela origen = ec.getListaEscuela().obtener(cbxOrigenBusqueda.getSelectedIndex());
        Escuela destino = ec.getListaEscuela().obtener(cbxDestinoBusqueda.getSelectedIndex());

        if (!rdbtnFloyd.isSelected()) { //agregar el método bellman-ford 
            JOptionPane.showMessageDialog(this, "Seleccione el método de búsqueda");
        } else {
            tiempoInicio = LocalTime.now();
            aux = ec.floyd(origen, destino);
        }
        calcularTiempo(tiempoInicio);
        aux.imprimir();
        try {
            if (aux.getTamanio() != null) {
                verGrafo(crearGrafoAux(aux));
            } else {
                JOptionPane.showMessageDialog(this, "No existe Camino");
            }
        } catch (ListaVaciaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PosicionNoEncontradaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void calcularTiempo(LocalTime tiempoInicio) {
        Long[] tiempos = Utilidades.tiempoTranscurrido(tiempoInicio, LocalTime.now());
        String tiempo = "";
        String data = "";
        for (int i = 0; i < tiempos.length; i++) {
            switch (i) {
                case 0: {
                    tiempo = "minutos: " + tiempos[i] + " ";
                    data = tiempos[i] + ":";
                    continue;
                }
                case 1: {
                    tiempo = tiempo + "segundos: " + tiempos[i] + " ";
                    data = data + tiempos[i] + ":";
                    continue;
                }
                case 2: {
                    tiempo = tiempo + "milisegundos: " + tiempos[i] + " ";
                    data = data + tiempos[i] + "";
                    continue;
                }
                default:
                    break;
            }
        }
        txtTiempo.setText(data);
        System.out.println(tiempo);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngMetodosBusqueda = new javax.swing.ButtonGroup();
        btngRecorridoGrafo = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbxDestinoAdyacencia = new javax.swing.JComboBox<>();
        cbxOrigenAdyacencia = new javax.swing.JComboBox<>();
        btnAdyacencia = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAdyacencias = new javax.swing.JTable();
        btnVerGrafo = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtDistanciaAdyacencia = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        cbxOrigenBusqueda = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        cbxDestinoBusqueda = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        rdbtnFloyd = new javax.swing.JRadioButton();
        txtTiempo = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblEscuelas = new javax.swing.JTable();
        btnAgregarEscuela = new javax.swing.JButton();
        btnModificarEscuela = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(50, 720));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setMinimumSize(new java.awt.Dimension(50, 760));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Adyacencias"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Destino:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, -1));

        jLabel2.setText("Origen:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        cbxDestinoAdyacencia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cbxDestinoAdyacencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 70, 230, -1));

        cbxOrigenAdyacencia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cbxOrigenAdyacencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 210, -1));

        btnAdyacencia.setText("ADYACENCIA");
        btnAdyacencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdyacenciaActionPerformed(evt);
            }
        });
        jPanel2.add(btnAdyacencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 80, -1, -1));

        tblAdyacencias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblAdyacencias);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 570, 340));

        btnVerGrafo.setText("VER GRAFO");
        btnVerGrafo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerGrafoActionPerformed(evt);
            }
        });
        jPanel2.add(btnVerGrafo, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 470, -1, -1));

        jLabel5.setText("Distancia:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 40, -1, -1));
        jPanel2.add(txtDistanciaAdyacencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 40, 140, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 630, 510));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Camino Corto"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cbxOrigenBusqueda.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cbxOrigenBusqueda, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 320, -1));

        jLabel3.setText("Origen:");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        btnBuscar.setText("BUSCAR");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });
        jPanel3.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 110, -1));

        cbxDestinoBusqueda.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cbxDestinoBusqueda, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 320, -1));

        jLabel4.setText("Destino:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 70, -1, -1));

        btngMetodosBusqueda.add(rdbtnFloyd);
        rdbtnFloyd.setText("FLOYD");
        jPanel3.add(rdbtnFloyd, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 90, -1, -1));

        txtTiempo.setText("00:00:00");
        jPanel3.add(txtTiempo, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 70, 100, -1));

        jLabel6.setText("Tiempo empleado (mm:ss:ms):");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 40, 220, -1));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 520, 630, 140));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("EscuelasCrud"));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblEscuelas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tblEscuelas);

        jPanel4.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 430, -1));

        btnAgregarEscuela.setText("AGREGAR ESCUELA");
        btnAgregarEscuela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarEscuelaActionPerformed(evt);
            }
        });
        jPanel4.add(btnAgregarEscuela, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 490, -1, -1));

        btnModificarEscuela.setText("MODIFICAR ESCUELA");
        btnModificarEscuela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarEscuelaActionPerformed(evt);
            }
        });
        jPanel4.add(btnModificarEscuela, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 510, -1, -1));

        btnEliminar.setText("ELIMINAR ESCUELA");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        jPanel4.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, 170, -1));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 10, 480, 640));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1147, 661));
        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        try {
            // TODO add your handling code here:
            buscar();
        } catch (ListaVaciaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PosicionNoEncontradaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnAgregarEscuelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarEscuelaActionPerformed
        try {
            // TODO add your handling code here:
            EscuelaController aux = Utilidades.leerJson();
            var grafoAux = aux.getGrafoEscuela();
            EscuelaDialog ed = new EscuelaDialog(null, true, ec);
            ed.setVisible(true);
            ed.dispose();
            cargarTablaEscuelas();
            ec.crearGrafo();
            for (int i = 1; i <= aux.getListaEscuela().getTamanio(); i++) {
                for (int j = 1; j <= aux.getListaEscuela().getTamanio(); j++) {
                    if (grafoAux.existeArista(i, j)) {
                        ec.insertarAdyacenciaPeso(ec.getListaEscuela().obtener(i - 1), ec.getListaEscuela().obtener(j - 1), aux.getGrafoEscuela().pesoArista(i, j));
                    }
                }
            }
            Utilidades.guardarJSON(ec);
            cargarMatriz();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAgregarEscuelaActionPerformed

    private void btnModificarEscuelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarEscuelaActionPerformed
        // TODO add your handling code here:

        if (tblEscuelas.getSelectedRow() >= 0) {
            EscuelaController aux = Utilidades.leerJson();
            var grafoAux = aux.getGrafoEscuela();
            EscuelaDialog ed = new EscuelaDialog(null, true, ec, tblEscuelas.getSelectedRow());
            ed.setVisible(true);
            ed.dispose();
            cargarTablaEscuelas();
            ec.crearGrafo();
            for (int i = 1; i <= aux.getListaEscuela().getTamanio(); i++) {
                for (int j = 1; j <= aux.getListaEscuela().getTamanio(); j++) {
                    try {
                        if (grafoAux.existeArista(i, j)) {
                            ec.insertarAdyacenciaPeso(ec.getListaEscuela().obtener(i - 1), ec.getListaEscuela().obtener(j - 1), aux.getGrafoEscuela().pesoArista(i, j));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            try {
                Utilidades.guardarJSON(ec);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
            cargarMatriz();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione la escuela a modificar");
        }

    }//GEN-LAST:event_btnModificarEscuelaActionPerformed

    private void btnAdyacenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdyacenciaActionPerformed
        try {
            // TODO add your handling code here:
            fijarAdyacencia();
            cargarMatriz();
        } catch (Exception ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAdyacenciaActionPerformed

    private void btnVerGrafoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerGrafoActionPerformed
        // TODO add your handling code here:
        verGrafo(ec.getGrafoEscuela());
    }//GEN-LAST:event_btnVerGrafoActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        try {
            eliminarEscuela();
        } catch (ListaVaciaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PosicionNoEncontradaException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrincipalDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PrincipalDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PrincipalDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PrincipalDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PrincipalDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PrincipalDialog dialog = new PrincipalDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdyacencia;
    private javax.swing.JButton btnAgregarEscuela;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnModificarEscuela;
    private javax.swing.JButton btnVerGrafo;
    private javax.swing.ButtonGroup btngMetodosBusqueda;
    private javax.swing.ButtonGroup btngRecorridoGrafo;
    private javax.swing.JComboBox<String> cbxDestinoAdyacencia;
    private javax.swing.JComboBox<String> cbxDestinoBusqueda;
    private javax.swing.JComboBox<String> cbxOrigenAdyacencia;
    private javax.swing.JComboBox<String> cbxOrigenBusqueda;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton rdbtnFloyd;
    private javax.swing.JTable tblAdyacencias;
    private javax.swing.JTable tblEscuelas;
    private javax.swing.JTextField txtDistanciaAdyacencia;
    private javax.swing.JTextField txtTiempo;
    // End of variables declaration//GEN-END:variables
}
