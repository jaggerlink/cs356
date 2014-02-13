/*
 * by Sarmen Jordan
 */
package cs356gui;

import java.io.File;
import javax.swing.JFileChooser;

public class rvmGui extends javax.swing.JFrame {
   public rvmGui() {
      initComponents();
   }
   
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jButtonExit = new javax.swing.JButton();
      labelTitle = new java.awt.Label();
      labelChooseFile = new java.awt.Label();
      jButtonCancel = new javax.swing.JButton();
      jButtonClear = new javax.swing.JButton();
      jButtonRun = new javax.swing.JButton();
      jButtonBrowse = new javax.swing.JButton();
      fbText = new java.awt.TextField();
      jLabelProcess = new javax.swing.JLabel();
      jLabelSamplingRate = new javax.swing.JLabel();
      jLabelNumDet = new javax.swing.JLabel();
      jLabelRaceLog = new javax.swing.JLabel();
      jLabelDetRaceStat = new javax.swing.JLabel();
      textAreaProcess = new java.awt.TextArea();
      textAreaSamplingRate = new java.awt.TextArea();
      textAreaNumDet = new java.awt.TextArea();
      textAreaRaceLog = new java.awt.TextArea();
      textAreaDetRaceStat = new java.awt.TextArea();
      jFileChooser1 = new javax.swing.JFileChooser();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

      jButtonExit.setText("Exit");
      jButtonExit.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButtonExitActionPerformed(evt);
         }
      });

      labelTitle.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
      labelTitle.setText("Jikes Rvm");

      labelChooseFile.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
      labelChooseFile.setText("Choose File: ");

      jButtonCancel.setText("Cancel");

      jButtonClear.setText("Clear");
      jButtonClear.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButtonClearActionPerformed(evt);
         }
      });

      jButtonRun.setText("Run");
      jButtonRun.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButtonRunActionPerformed(evt);
         }
      });

      jButtonBrowse.setText("Browse");
      jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButtonBrowseActionPerformed(evt);
         }
      });

      fbText.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
      fbText.setText("click \"Browse\" to choose a file");
      fbText.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fbTextActionPerformed(evt);
         }
      });

      jLabelProcess.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelProcess.setText("Prosess of threads and outputs");

      jLabelSamplingRate.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelSamplingRate.setText("Effective Sampling Rate:");

      jLabelNumDet.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelNumDet.setText("Number of Races Detected:");

      jLabelRaceLog.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelRaceLog.setText("Race Log:");

      jLabelDetRaceStat.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelDetRaceStat.setText("Detected Race Statistics");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(354, 354, 354))
         .addGroup(layout.createSequentialGroup()
            .addGap(37, 37, 37)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonClear, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(labelChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fbText, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)))
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButtonBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButtonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabelProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textAreaProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGap(59, 59, 59)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(textAreaSamplingRate, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabelSamplingRate)
                     .addComponent(textAreaNumDet, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textAreaRaceLog, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textAreaDetRaceStat, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabelNumDet)
                     .addComponent(jLabelRaceLog)
                     .addComponent(jLabelDetRaceStat))))
            .addContainerGap(38, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(labelChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(fbText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(jButtonBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jButtonClear, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jButtonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(5, 5, 5)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabelProcess)
               .addComponent(jLabelSamplingRate))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(textAreaSamplingRate, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(21, 21, 21)
                  .addComponent(jLabelNumDet)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(textAreaNumDet, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(22, 22, 22)
                  .addComponent(jLabelRaceLog)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(textAreaRaceLog, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(20, 20, 20)
                  .addComponent(jLabelDetRaceStat)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(textAreaDetRaceStat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
               .addComponent(textAreaProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   //Closes the program when the Exit button is pushed
   private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
      System.exit(0);
   }//GEN-LAST:event_jButtonExitActionPerformed

   private void jButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunActionPerformed

   }//GEN-LAST:event_jButtonRunActionPerformed
  
   //initializing the fileBrowser  for later use
   JFileChooser jfc = new JFileChooser();
   
   private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
      //opens up the FileBrowser when "Browse" button is pressed
      int result = jfc.showDialog(null,"Select");
      if(result==JFileChooser.APPROVE_OPTION){
         File f = jfc.getSelectedFile();
         fbText.setText(f.getPath());
      }
   }//GEN-LAST:event_jButtonBrowseActionPerformed

   private void fbTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fbTextActionPerformed

   }//GEN-LAST:event_fbTextActionPerformed

   private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
      //clears the text in file broswer
      fbText.setText("");
   }//GEN-LAST:event_jButtonClearActionPerformed

   public static void main(String args[]) {
      try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               javax.swing.UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(rvmGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(rvmGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(rvmGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(rvmGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
            java.awt.EventQueue.invokeLater(new Runnable() {
         public void run() {
            new rvmGui().setVisible(true);
         }
      });
   }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private java.awt.TextField fbText;
   private javax.swing.JButton jButtonBrowse;
   private javax.swing.JButton jButtonCancel;
   private javax.swing.JButton jButtonClear;
   private javax.swing.JButton jButtonExit;
   private javax.swing.JButton jButtonRun;
   private javax.swing.JFileChooser jFileChooser1;
   private javax.swing.JLabel jLabelDetRaceStat;
   private javax.swing.JLabel jLabelNumDet;
   private javax.swing.JLabel jLabelProcess;
   private javax.swing.JLabel jLabelRaceLog;
   private javax.swing.JLabel jLabelSamplingRate;
   private java.awt.Label labelChooseFile;
   private java.awt.Label labelTitle;
   private java.awt.TextArea textAreaDetRaceStat;
   private java.awt.TextArea textAreaNumDet;
   private java.awt.TextArea textAreaProcess;
   private java.awt.TextArea textAreaRaceLog;
   private java.awt.TextArea textAreaSamplingRate;
   // End of variables declaration//GEN-END:variables
}
