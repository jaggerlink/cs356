/*
 * by Sarmen Jordan
 */
package cs356gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
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
      jLabelSamplingRate = new javax.swing.JLabel();
      jLabelNumDet = new javax.swing.JLabel();
      jLabelRaceLog = new javax.swing.JLabel();
      jLabelDetRaceStat = new javax.swing.JLabel();
      textAreaSamplingRate = new java.awt.TextArea();
      textAreaNumDet = new java.awt.TextArea();
      textAreaRaceLog = new java.awt.TextArea();
      textAreaDetRaceStat = new java.awt.TextArea();
      jFileChooser1 = new javax.swing.JFileChooser();
      jLabel1 = new javax.swing.JLabel();
      jSpinner1 = new javax.swing.JSpinner();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setTitle("rvmGui");

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
      jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButtonCancelActionPerformed(evt);
         }
      });

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

      jLabelSamplingRate.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelSamplingRate.setText("Effective Sampling Rate:");

      jLabelNumDet.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelNumDet.setText("Number of Races Detected:");

      jLabelRaceLog.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelRaceLog.setText("Race Log:");

      jLabelDetRaceStat.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabelDetRaceStat.setText("Detected Race Statistics");

      jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
      jLabel1.setText("Sampling Rate:");

      jSpinner1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
      jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.5d, 1.0d, 0.05d));
      jSpinner1.setToolTipText("");
      jSpinner1.setName(""); // NOI18N

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(354, 354, 354))
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
               .addGroup(layout.createSequentialGroup()
                  .addGap(37, 37, 37)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                           .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addComponent(jLabelNumDet)
                              .addComponent(textAreaNumDet, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(textAreaSamplingRate, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(textAreaRaceLog, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(jLabelRaceLog))
                           .addGap(446, 446, 446))
                        .addGroup(layout.createSequentialGroup()
                           .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                              .addComponent(labelChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addGroup(layout.createSequentialGroup()
                                 .addGap(8, 8, 8)
                                 .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                              .addComponent(fbText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jButtonClear, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                           .addGap(18, 18, 18)
                           .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addComponent(jButtonBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(jButtonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))))
               .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                  .addGap(45, 45, 45)
                  .addComponent(jLabelSamplingRate)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabelDetRaceStat)
                     .addComponent(textAreaDetRaceStat, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(jButtonClear, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jButtonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabelDetRaceStat)
               .addComponent(jLabelSamplingRate))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(textAreaSamplingRate, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(16, 16, 16)
                  .addComponent(jLabelNumDet)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(textAreaNumDet, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabelRaceLog)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(textAreaRaceLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
               .addComponent(textAreaDetRaceStat, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
            .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   //Closes the program when the Exit button is pushed
   private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
      System.exit(0);
   }//GEN-LAST:event_jButtonExitActionPerformed

   private void jButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunActionPerformed
      //splits the of the selected file 
      try {
       //  double sampRate = (double) jSpinner1.getValue();
         String filePath = fbText.getText();
         String currDir = jfc.getCurrentDirectory().toString();
         File openedFile = new File(filePath);
         String currFile = jfc.getName(openedFile);
         String[] currFileArray = currFile.split("\\.");
         String currFileName = currFileArray[0];
         // textAreaDetRaceStat.setText(currFileName + "\n" + currFile + "\n" + currDir);

         //appends the strings together to form one long command line
         //that is going to be executed in the terminal
         String rvmPath = "/home/s/pacer/jikesrvm-3.1.0/dist/FastAdaptiveGenImmix_rdSamplingStats_ia32-linux/rvm";
         //String comm1 = "cd " + currDir;
         String comm2 = rvmPath + " -X:vm:raceDetSamplingRate=1.0 -cp ../:./:./* ";
         String comm3 = /*comm1 + "; " + */ comm2 + currFileName;

         Path currentRelativePath = Paths.get("");
         //gets the current directory path which rvmGui.java is located in
         String currGuiDir = currentRelativePath.toAbsolutePath().toString();
         
         //this is just a test line , comment it out later 
         textAreaDetRaceStat.setText(comm3);

         //String rvmPath = "/home/joshua/pacer/jikesrvm-3.1.0/dist/FastAdaptiveGenImmix_rdSamplingStats_ia32-linux/";
		   //ProcessBuilder pb = new ProcessBuilder("gedit", "./src/PBTest.java", "+10");
         //ProcessBuilder pb = new ProcessBuilder("xterm", "-e", "vi", "./src/PBTest.java", "+5");
         ProcessBuilder pb = new ProcessBuilder("xterm", "-hold", "-e", rvmPath, currFileName);
         File thisDir = jfc.getCurrentDirectory();
         pb.directory(thisDir);
         try {
            pb.start();
            //Runtime.getRuntime().exec("xterm -e vi ./src/PBTest.java +5");
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

         /*
          StringBuffer output = new StringBuffer();
 
          Process p;
          try {
          File thisDir = jfc.getCurrentDirectory();
          p = Runtime.getRuntime().exec(comm3,null,thisDir);
          p.waitFor();
          BufferedReader reader = 
          new BufferedReader(new InputStreamReader(p.getInputStream()));

          String line = "";			
          while ((line = reader.readLine())!= null) {
          output.append(line + "\n");
          }
          textAreaDetRaceStat.setText(line);
          } catch (Exception e) {
          e.printStackTrace();
          }
         
         
          //code below is supposed to execute a command in the terminal during java runtime
          /*
          String s = null;
          try {

          // run the Unix  command
          // using the Runtime exec method:
          Process p = Runtime.getRuntime().exec(comm3);

          BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

          BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

          // read the output from the command
          System.out.println("Here is the standard output of the command:\n");
          while ((s = stdInput.readLine()) != null) {
          textAreaDetRaceStat.setText(textAreaDetRaceStat.getText() + "\n" + s);
          }

          // read any errors from the attempted command
          System.out.println("Here is the standard error of the command (if any):\n");
          while ((s = stdError.readLine()) != null) {
          textAreaDetRaceStat.setText(textAreaDetRaceStat.getText() + "\n" + s);
          }

          //System.exit(0);
          } catch (Exception e) {
          }*/
      } catch (Exception e) {
      }
   }//GEN-LAST:event_jButtonRunActionPerformed

   //initializing the fileBrowser  for later use
   JFileChooser jfc = new JFileChooser();

   private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
      //opens up the FileBrowser when "Browse" button is pressed

      int result = jfc.showDialog(null, "Select");
      if (result == JFileChooser.APPROVE_OPTION) {
         File f = jfc.getSelectedFile();
         fbText.setText(f.getPath());
      }
   }//GEN-LAST:event_jButtonBrowseActionPerformed

   private void fbTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fbTextActionPerformed

   }//GEN-LAST:event_fbTextActionPerformed

   private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
      //clears the text in file broswer
      fbText.setText("");
      textAreaDetRaceStat.setText("");
      textAreaNumDet.setText("");
      textAreaRaceLog.setText("");
      textAreaSamplingRate.setText("");
   }//GEN-LAST:event_jButtonClearActionPerformed

   private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jButtonCancelActionPerformed

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
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabelDetRaceStat;
   private javax.swing.JLabel jLabelNumDet;
   private javax.swing.JLabel jLabelRaceLog;
   private javax.swing.JLabel jLabelSamplingRate;
   private javax.swing.JSpinner jSpinner1;
   private java.awt.Label labelChooseFile;
   private java.awt.Label labelTitle;
   private java.awt.TextArea textAreaDetRaceStat;
   private java.awt.TextArea textAreaNumDet;
   private java.awt.TextArea textAreaRaceLog;
   private java.awt.TextArea textAreaSamplingRate;
   // End of variables declaration//GEN-END:variables
}
