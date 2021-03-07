package UI;

import DownloadLogic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class MainForm {
    private JTextField urlTextField;
    private JButton goButton;
    private JTextArea detailsTextArea;
    private JPanel mainPanel;
    private JTree videoUrlTree;
    private JProgressBar progressBar1;

    public MainForm() {
        // Setup iniziale
        videoUrlTree.setModel(new DefaultTreeModel(null));
        detailsTextArea.setText("");
        urlTextField.setText("");
        progressBar1.setVisible(false);

        // DEBUG
        // TODO
        urlTextField.setText("https://www.davvero.tv/tgbyoblu24/videos/tg-byoblu24-26-febbraio-2021-edizione-19-00");
        ////////

        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = videoUrlTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = videoUrlTree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        //System.out.println(String.format("%d - %s",selRow, selPath.toString()));
                    }
                    else if(e.getClickCount() == 2) {
                        if (selPath == null) {
                            return;
                        }

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        Object nodeInfo = node.getUserObject();

                        if (node.isLeaf()) {
                            detailsTextArea.append(String.format("Url copiata nella clipboard: %s - %s\n",
                                    ((VideoUrlAndQuality) nodeInfo).getQuality(),
                                    ((VideoUrlAndQuality) nodeInfo).getVideoUrl()
                            ));
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            StringSelection stringSelection = new StringSelection(((VideoUrlAndQuality) nodeInfo).getVideoUrl());
                            clipboard.setContents(stringSelection, null);
                            JOptionPane.showMessageDialog(mainPanel, "Url copiata nella clipboard: "+((VideoUrlAndQuality) nodeInfo).getQuality()
                                    ,"Dati copiati", JOptionPane.INFORMATION_MESSAGE);
                        }


                    }
                }
            }
        };

        videoUrlTree.addMouseListener(ml);

        videoUrlTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) videoUrlTree.getLastSelectedPathComponent();
                if (node==null){
                    return;
                }

                Object nodeInfo = node.getUserObject();

                if (node.isLeaf()) {
                    detailsTextArea.append(String.format("%s - %s\n",
                            ((VideoUrlAndQuality) nodeInfo).getQuality(),
                            ((VideoUrlAndQuality) nodeInfo).getVideoUrl()
                    ));
                }
            }
        });

        videoUrlTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ParserWorker parserWorker = new ParserWorker();
                parserWorker.execute();

            }
        });

    }

    private void populateTree(VideoDetails videoDetails) {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(videoDetails.getTitle());

        for (VideoUrlAndQuality videoUrlAndQuality : videoDetails.getVideoUrlQualityList()){
            top.add(new DefaultMutableTreeNode(videoUrlAndQuality));
        }

        videoUrlTree.setModel(new DefaultTreeModel(top));
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Download DavveroTV");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    private class ParserWorker extends SwingWorker<VideoDetails, WorkerUpdateCallback.UpdateInfo>{
        WorkerUpdateCallback workerUpdateCallback;

        public ParserWorker() {
            this.workerUpdateCallback = new WorkerUpdateCallback() {
                @Override
                public void update(UpdateEvent updateEvent, String message) {
                    //detailsTextArea.append(String.format("%s, - %s",updateEvent,message));
                    publish(new UpdateInfo(updateEvent,message));
                }
            };
        }

        @Override
        protected VideoDetails doInBackground() throws Exception {
            String urlText = urlTextField.getText().trim();
            progressBar1.setVisible(true);

            ParseVideoPage parseVideoPage = new ParseVideoPage(workerUpdateCallback);
            VideoDetails videoDetails = parseVideoPage.start(urlText);

            if (videoDetails==null){
                workerUpdateCallback.update(UpdateEvent.Error,"Errore nel download / parsing");
                return null;
            }

            List<VideoUrlAndQuality> videoMP4Url = videoDetails.getVideoUrlQualityList();

            populateTree(videoDetails);

            workerUpdateCallback.update(UpdateEvent.VideoDownloadCanStart,
                    String.format("%s - %s",videoDetails.getTitle(), videoDetails.getMainUrl()));

            for (VideoUrlAndQuality videoUrlAndQuality : videoMP4Url){
                workerUpdateCallback.update(UpdateEvent.VideoDownloadCanStart,
                        String.format("%s - %s",
                        videoUrlAndQuality.getQuality(),
                        videoUrlAndQuality.getVideoUrl()
                ));
            }

            return videoDetails;
        }

        @Override
        protected void process(List<WorkerUpdateCallback.UpdateInfo> chunks) {
            super.process(chunks);
            for (WorkerUpdateCallback.UpdateInfo chunk : chunks) {
                detailsTextArea.append(chunk.getMessage() + "\n");
            }
        }

        @Override
        protected void done() {
            super.done();
            progressBar1.setVisible(false);
        }
    }
}
