/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package clipping;

import java.awt.BorderLayout;
import java.io.File;

/**
 *
 * @author majam
 */
public class Panel extends javax.swing.JFrame {

    /**
     * Creates new form Panel
     */
    public Panel() {
        initComponents();
        Local local = new Local();
        local.setSize(680,420);
        local.setLocation(0,0);
        
        contenido.removeAll();
        contenido.add(local, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LocalBoton = new javax.swing.JButton();
        OnlineBoton = new javax.swing.JButton();
        contenido = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        LocalBoton.setText("Local");
        LocalBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LocalBotonActionPerformed(evt);
            }
        });

        OnlineBoton.setText("Online");
        OnlineBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OnlineBotonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout contenidoLayout = new javax.swing.GroupLayout(contenido);
        contenido.setLayout(contenidoLayout);
        contenidoLayout.setHorizontalGroup(
            contenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        contenidoLayout.setVerticalGroup(
            contenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 268, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(130, Short.MAX_VALUE)
                .addComponent(LocalBoton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(OnlineBoton)
                .addGap(132, 132, 132))
            .addComponent(contenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OnlineBoton)
                    .addComponent(LocalBoton))
                .addGap(18, 18, 18)
                .addComponent(contenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   
    private void LocalBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LocalBotonActionPerformed
        Local local = new Local();
        local.setSize(680,420);
        local.setLocation(0,0);
        
        contenido.removeAll();
        contenido.add(local, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }//GEN-LAST:event_LocalBotonActionPerformed

    private void OnlineBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OnlineBotonActionPerformed
        Online online = new Online();
        online.setSize(680,420);
        online.setLocation(0,0);
        
        contenido.removeAll();
        contenido.add(online, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }//GEN-LAST:event_OnlineBotonActionPerformed
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Panel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton LocalBoton;
    private javax.swing.JButton OnlineBoton;
    private javax.swing.JPanel contenido;
    // End of variables declaration//GEN-END:variables
}
