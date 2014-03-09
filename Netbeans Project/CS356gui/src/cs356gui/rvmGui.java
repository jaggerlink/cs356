/*
 * by Sarmen Jordan
 */
package cs356gui;

import java.io.IOException;
import java.io.File;
import javax.swing.JFileChooser;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.jikesrvm.rd.PacerData;

public class rvmGui extends javax.swing.JFrame {

    PacerData newData;
    String sourceDirectory;
    Process rvm;

    public rvmGui() {
        initComponents();
        this.setLocationRelativeTo(null);
    }

    public class SocketServer implements Runnable {

        PacerData test;
        rvmGui caller;

        public SocketServer(rvmGui caller) {
            this.caller = caller;
        }

        @Override
        public void run() {
            try {
                ServerSocket listener = new ServerSocket(8080);
                Socket socket = listener.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                try {
                    test = (PacerData) input.readObject();
                } catch (ClassNotFoundException e) {
                    System.out.println("Invalid Input.");
                    e.printStackTrace();
                }

                listener.close();
                caller.updateGui(test);
            } catch (IOException e) {
                System.out.println("Something went wrong.");
                e.printStackTrace();
            }
        }

        public PacerData getData() {
            run();
            return test;
        }
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
        jLabelRacesDet = new javax.swing.JLabel();
        jLabelRaceLog = new javax.swing.JLabel();
        jLabelDetRaceStat = new javax.swing.JLabel();
        textAreaDetRaceStat = new java.awt.TextArea();
        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jListRaceLog = new java.awt.List();
        jTextFieldRacesDet = new java.awt.TextField();
        jTextFieldSamplingRate = new java.awt.TextField();
        jButtonGetSource = new javax.swing.JButton();
        jButtonGetSource1 = new javax.swing.JButton();
        argsField = new javax.swing.JTextField();
        argsLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("rvmGui");

        jButtonExit.setText("Exit");
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        labelTitle.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        labelTitle.setText("Jikes RVM");

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

        jLabelRacesDet.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelRacesDet.setText("Number of Races Detected:");

        jLabelRaceLog.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelRaceLog.setText("Race Log:");

        jLabelDetRaceStat.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelDetRaceStat.setText("Detected Race Statistics:");

        textAreaDetRaceStat.setEditable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Sampling Rate:");

        jSpinner1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.05d));
        jSpinner1.setToolTipText("");
        jSpinner1.setName(""); // NOI18N

        jTextFieldRacesDet.setEditable(false);

        jTextFieldSamplingRate.setEditable(false);

        jButtonGetSource.setText("Get Source Line");
        jButtonGetSource.setToolTipText("");
        jButtonGetSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetSourceActionPerformed(evt);
            }
        });

        jButtonGetSource1.setText("Get Destination Line");
        jButtonGetSource1.setToolTipText("");
        jButtonGetSource1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetSource1ActionPerformed(evt);
            }
        });

        argsLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        argsLabel.setText("Args:");
        argsLabel.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(14, 14, 14)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(464, 464, 464))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fbText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)))
                        .addComponent(jButtonBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jListRaceLog, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                .addComponent(jTextFieldRacesDet, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldSamplingRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jButtonGetSource)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonGetSource1))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(argsLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(argsField)))
                            .addComponent(jLabelRaceLog)
                            .addComponent(jLabelSamplingRate)
                            .addComponent(jLabelRacesDet))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonClear, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                                .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabelDetRaceStat)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(textAreaDetRaceStat, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelDetRaceStat)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(argsLabel)
                            .addComponent(argsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(21, 21, 21)
                        .addComponent(jLabelSamplingRate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSamplingRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRacesDet)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldRacesDet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRaceLog)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jListRaceLog, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(textAreaDetRaceStat, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonGetSource, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonGetSource1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonClear, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Closes the program when the Exit button is pushed
   private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
       System.exit(0);
   }//GEN-LAST:event_jButtonExitActionPerformed

   private void jButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunActionPerformed
       jButtonRun.setEnabled(false);
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
           String rvmPath = "jikesrvm-3.1.0/dist/FastAdaptiveGenImmix_rdSamplingStats_ia32-linux/rvm";
           String rvmCommand = "-X:vm:raceDetSamplingRate=" + Float.parseFloat(jSpinner1.getValue().toString());

           File currentRelativePath = new File("");
           //gets the current directory path which rvmGui.java is located in
           String currGuiDir = currentRelativePath.getAbsolutePath();
           currGuiDir = currGuiDir.substring(0, currGuiDir.length() - 25);

           ProcessBuilder pb = new ProcessBuilder("xterm", "-e", currGuiDir + rvmPath, rvmCommand, currFileName);
           System.out.println(currGuiDir + rvmPath + " " + rvmCommand + " " + currFileName);
           File thisDir = jfc.getCurrentDirectory();
           pb.directory(thisDir);
           sourceDirectory = thisDir.toString();
           jButtonClearActionPerformed(null);
           try {
               SocketServer server = new SocketServer(this);
               rvm = pb.start();
               new Thread(server).start();
               //newData = server.getData();
               //Runtime.getRuntime().exec("xterm -e vi ./src/PBTest.java +5");
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
       } catch (Exception e) {
       }
   }//GEN-LAST:event_jButtonRunActionPerformed

    public void updateGui(PacerData newData) {
        this.newData = newData;
        jTextFieldSamplingRate.setText(newData.getSR()); //Insertion for gui display
        jTextFieldRacesDet.setText(newData.getNR());
        System.out.println(newData.getStatSize());
        for (int i = 0; i < newData.getStatSize(); i++) {
            textAreaDetRaceStat.append(newData.getDRS(i) + "\n");
        }
        for (int i = 0; i < newData.getRaceSize(); i++) {
            jListRaceLog.add(newData.getRace(i));
        }
        jButtonRun.setEnabled(true);
    }
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
       //fbText.setText("");
       textAreaDetRaceStat.setText(null);
       jTextFieldSamplingRate.setText(null);
       jTextFieldRacesDet.setText(null);
       jListRaceLog.removeAll();
   }//GEN-LAST:event_jButtonClearActionPerformed

   private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
       if (rvm != null) {
           rvm.destroy();
           jButtonRun.setEnabled(true);
       }
   }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonGetSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetSourceActionPerformed
        if (jListRaceLog.getSelectedIndex() != -1) {
            String testFix = newData.getPriorRaceDescriptor(jListRaceLog.getSelectedIndex()).substring(1, newData.getPriorRaceDescriptor(jListRaceLog.getSelectedIndex()).length() - 1) + ".java";
            ProcessBuilder pb = new ProcessBuilder("gedit", testFix, "+" + newData.getPriorRaceLine(jListRaceLog.getSelectedIndex()));
            pb.directory(new File(sourceDirectory));
            try {
                pb.start();
                //Runtime.getRuntime().exec("xterm -e vi ./src/PBTest.java +5");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButtonGetSourceActionPerformed

    private void jButtonGetSource1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetSource1ActionPerformed
        if (jListRaceLog.getSelectedIndex() != -1) {
            String testFix2 = newData.getCurrentRaceDescriptor(jListRaceLog.getSelectedIndex()).substring(1, newData.getCurrentRaceDescriptor(jListRaceLog.getSelectedIndex()).length() - 1) + ".java";
            ProcessBuilder pb2 = new ProcessBuilder("gedit", testFix2, "+" + newData.getCurrentRaceLine(jListRaceLog.getSelectedIndex()));
            pb2.directory(new File(sourceDirectory));
            try {
                pb2.start();
                //Runtime.getRuntime().exec("xterm -e vi ./src/PBTest.java +5");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButtonGetSource1ActionPerformed

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
    private javax.swing.JTextField argsField;
    private javax.swing.JLabel argsLabel;
    private java.awt.TextField fbText;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonGetSource;
    private javax.swing.JButton jButtonGetSource1;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelDetRaceStat;
    private javax.swing.JLabel jLabelRaceLog;
    private javax.swing.JLabel jLabelRacesDet;
    private javax.swing.JLabel jLabelSamplingRate;
    private java.awt.List jListRaceLog;
    private javax.swing.JSpinner jSpinner1;
    private java.awt.TextField jTextFieldRacesDet;
    private java.awt.TextField jTextFieldSamplingRate;
    private java.awt.Label labelChooseFile;
    private java.awt.Label labelTitle;
    private java.awt.TextArea textAreaDetRaceStat;
    // End of variables declaration//GEN-END:variables
}
