package com.mycompany.trainnetworkmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mxgraph.view.mxGraph;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.layout.mxFastOrganicLayout;

public class TrainApp {

    private static final Color BLUE_DARK = Color.decode("#1A252F");
    private static final Color BLUE_BUTTON = Color.decode("#2C3E50");    
    private static final Color BLUE_LIGHT = Color.decode("#1A252F");
    private static final Color BACKGROUND_COLOR = Color.decode("#ECF0F1"); 
    private static final Color WHITE = Color.WHITE;
    private static final Graph trainGraph = new Graph();
    
    private static JComboBox<String> srcCombo = new JComboBox<>();
    private static JComboBox<String> destCombo = new JComboBox<>();
    private static JComboBox<String> edgeSrcCombo = new JComboBox<>();
    private static JComboBox<String> edgeDestCombo = new JComboBox<>();
    private static JComboBox<String> deleteStationCombo = new JComboBox<>();
    private static JPanel graphContainerPanel = new JPanel(new BorderLayout());
    private static JLabel statusBar = new JLabel(" Ready");
    private static JTextArea nodeInfoArea = new JTextArea();
    
    private static boolean shouldApplyLayout = true;
    private static java.util.Map<String, Point> savedNodePositions = new java.util.HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TrainApp::showSplashScreen);
    }

    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        splash.setSize(700, 450);
        splash.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());

        JPanel bluePanel = new JPanel(new GridBagLayout());
        bluePanel.setBackground(BLUE_DARK);
        bluePanel.setPreferredSize(new Dimension(280, 450));
        java.net.URL gifURL = TrainApp.class.getResource("/Train.gif");
        JLabel gifLabel = (gifURL != null) ? new JLabel(new ImageIcon(gifURL)) : new JLabel("🚂 Train System");
        gifLabel.setForeground(WHITE);
        bluePanel.add(gifLabel);

        JPanel whitePanel = new JPanel(new GridBagLayout());
        whitePanel.setBackground(WHITE);
        
        JLabel titleLabel = new JLabel("Train Network Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(BLUE_DARK);
        whitePanel.add(titleLabel);

        content.add(bluePanel, BorderLayout.WEST);
        content.add(whitePanel, BorderLayout.CENTER);
        
        splash.setContentPane(content);
        splash.setVisible(true);

        Timer timer = new Timer(2000, e -> {
            splash.dispose();
            showMainFrame();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static void showMainFrame() {
        JFrame mainFrame = new JFrame("Train Network Management System ");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1250, 850);
        mainFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        CardLayout cardLayout = new CardLayout();
        JPanel contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(BACKGROUND_COLOR);

        statusBar.setPreferredSize(new Dimension(mainFrame.getWidth(), 25));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridBagLayout()); 
        sidebar.setBackground(BLUE_DARK);
        sidebar.setPreferredSize(new Dimension(260, 850));

        GridBagConstraints sGbc = new GridBagConstraints();
        sGbc.fill = GridBagConstraints.HORIZONTAL;
        sGbc.gridx = 0;
        sGbc.weightx = 1.0;
        JLabel menuTitle = new JLabel("MAIN MENU", SwingConstants.CENTER);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        menuTitle.setForeground(WHITE);
        menuTitle.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));
        sGbc.gridy = 0;
        sidebar.add(menuTitle, sGbc);

        JButton btnGraph = createSidebarButton("Interactive Graph");
        JButton btnEdit = createSidebarButton("Edit Network");
        JButton btnAlgorithms = createSidebarButton("Algorithms");
        JButton btnReports = createSidebarButton(" File IO");

        sGbc.gridy = 1; sidebar.add(btnGraph, sGbc);
        sGbc.gridy = 2; sidebar.add(btnEdit, sGbc);
        sGbc.gridy = 3; sidebar.add(btnAlgorithms, sGbc);
        sGbc.gridy = 4; sidebar.add(btnReports, sGbc);

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        sGbc.gridy = 5;
        sGbc.weighty = 1.0;
        sidebar.add(spacer, sGbc);

        btnGraph.addActionListener(e -> { 
            cardLayout.show(contentCardPanel, "GraphView"); 
            shouldApplyLayout = true;
            updateVisualGraph(null); 
        });
        btnEdit.addActionListener(e -> cardLayout.show(contentCardPanel, "EditView"));
        btnAlgorithms.addActionListener(e -> cardLayout.show(contentCardPanel, "AlgoView"));
        btnReports.addActionListener(e -> cardLayout.show(contentCardPanel, "ReportView"));

        contentCardPanel.add(createGraphPage(), "GraphView");
        contentCardPanel.add(createEditPage(), "EditView");
        contentCardPanel.add(createAlgorithmsPage(), "AlgoView");
        contentCardPanel.add(createReportsPage(), "ReportView");

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentCardPanel, BorderLayout.CENTER);

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);

        updateAllCombos();
        shouldApplyLayout = true;
        updateVisualGraph(null);
    }

    private static JPanel createGraphPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel topSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topSearchPanel.setBackground(WHITE);
        topSearchPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JLabel searchLabel = new JLabel("Search Station:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField searchField = new JTextField(15);
        JButton searchBtn = createStyledButton("Highlight");
        JButton resetBtn = createStyledButton("View");

        searchBtn.addActionListener(e -> {
            String name = searchField.getText().trim();
            if(!name.isEmpty()) {
                shouldApplyLayout = true;
                updateVisualGraph(name);
            }
        });
        resetBtn.addActionListener(e -> { 
            searchField.setText(""); 
            shouldApplyLayout = true;
            updateVisualGraph(null); 
        });

        topSearchPanel.add(searchLabel);
        topSearchPanel.add(searchField);
        topSearchPanel.add(searchBtn);
        topSearchPanel.add(resetBtn);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(260, 600));
        infoPanel.setBackground(WHITE);
        infoPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));

        JLabel infoTitle = new JLabel("Station Information", SwingConstants.CENTER);
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        infoTitle.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));
        infoPanel.add(infoTitle, BorderLayout.NORTH);

        nodeInfoArea.setEditable(false);
        nodeInfoArea.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        nodeInfoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nodeInfoArea.setText("Click on any station node\nto view its real-time network\nconnections and metadata.");
        infoPanel.add(new JScrollPane(nodeInfoArea), BorderLayout.CENTER);

        panel.add(topSearchPanel, BorderLayout.NORTH);
        panel.add(graphContainerPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.EAST);
        return panel;
    }

    private static JPanel createEditPage() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        JPanel stationAddPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        stationAddPanel.setBorder(BorderFactory.createTitledBorder(" Add Station"));
        stationAddPanel.setBackground(WHITE);
        stationAddPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JTextField nameField = new JTextField(12);
        JTextField codeField = new JTextField(6);
        JButton addBtn = createStyledButton("Add Station");
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            if(name.isEmpty() || code.isEmpty()) {
                showStatus("️ Please fill in both station name and code", true);
                return;
            }
            if(trainGraph.addStation(code, name)) {
                showStatus("Station [" + name + "] added successfully.", false);
                JOptionPane.showMessageDialog(panel, " Station [" + name + "] added successfully.");
                nameField.setText(""); codeField.setText("");
                updateAllCombos();
                shouldApplyLayout = true;
                updateVisualGraph(null);
            } else {
                showStatus(" Failed! Station already exists.", true);
            }
        });
        stationAddPanel.add(new JLabel("Name:")); stationAddPanel.add(nameField);
        stationAddPanel.add(new JLabel("Code:")); stationAddPanel.add(codeField);
        stationAddPanel.add(addBtn);

        JPanel stationDeletePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        stationDeletePanel.setBorder(BorderFactory.createTitledBorder("️ Delete Station"));
        stationDeletePanel.setBackground(WHITE);
        stationDeletePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton deleteStationBtn = createStyledButton("Delete Selected Station");
        deleteStationBtn.addActionListener(e -> {
            if(deleteStationCombo.getSelectedItem() == null) return;
            String selectedName = (String) deleteStationCombo.getSelectedItem();
            Station target = trainGraph.getStationByName(selectedName);
            if(target != null) {
                trainGraph.removeStation(target);
                savedNodePositions.remove(selectedName);
                showStatus("🗑️ Station [" + selectedName + "] deleted successfully.", false);
                JOptionPane.showMessageDialog(panel, "️ Station [" + selectedName + "] and all its tracks deleted successfully.");
                updateAllCombos();
                shouldApplyLayout = false;
                updateVisualGraph(null);
            }
        });
        stationDeletePanel.add(new JLabel("Select Station to Remove:"));
        stationDeletePanel.add(deleteStationCombo);
        stationDeletePanel.add(deleteStationBtn);

        JPanel edgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        edgePanel.setBorder(BorderFactory.createTitledBorder("Manage & Edit Edges"));
        edgePanel.setBackground(WHITE);
        edgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JTextField weightField = new JTextField(6);
        JButton addEdgeBtn = createStyledButton("Add/Update Edge");
        JButton removeEdgeBtn = createStyledButton("Remove Edge");
        addEdgeBtn.addActionListener(e -> {
            if(edgeSrcCombo.getSelectedItem() == null || edgeDestCombo.getSelectedItem() == null) return;
            Station src = trainGraph.getStationByName((String) edgeSrcCombo.getSelectedItem());
            Station dest = trainGraph.getStationByName((String) edgeDestCombo.getSelectedItem());
            try {
                int weight = Integer.parseInt(weightField.getText().trim());
                trainGraph.removeEdge(src, dest);
                if(trainGraph.addEdge(src, dest, weight)) {
                    showStatus("Path established/updated successfully.", false);
                    JOptionPane.showMessageDialog(panel, "Path linked from [" + src.getName() + "] to [" + dest.getName() + "] (" + weight + " km).");
                    weightField.setText("");
                    shouldApplyLayout = false;
                    updateVisualGraph(null);
                }
            } catch (NumberFormatException ex) {
                showStatus("Distance must be a valid integer!", true);
            }
        });

        removeEdgeBtn.addActionListener(e -> {
            if(edgeSrcCombo.getSelectedItem() == null || edgeDestCombo.getSelectedItem() == null) return;
            Station src = trainGraph.getStationByName((String) edgeSrcCombo.getSelectedItem());
            Station dest = trainGraph.getStationByName((String) edgeDestCombo.getSelectedItem());
            if(trainGraph.removeEdge(src, dest)) {
                showStatus(" Removed edge successfully.", false);
                JOptionPane.showMessageDialog(panel, "️ Path between [" + src.getName() + "] and [" + dest.getName() + "] removed.");
                shouldApplyLayout = false;
                updateVisualGraph(null);
            } else {
                showStatus("️ No existing direct path found to remove.", true);
            }
        });

        edgePanel.add(new JLabel("Source:")); edgePanel.add(edgeSrcCombo);
        edgePanel.add(new JLabel("Destination:")); edgePanel.add(edgeDestCombo);
        edgePanel.add(new JLabel("Distance:")); edgePanel.add(weightField);
        edgePanel.add(addEdgeBtn); edgePanel.add(removeEdgeBtn);

        container.add(stationAddPanel);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(stationDeletePanel);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(edgePanel);

        panel.add(container, BorderLayout.NORTH);
        return panel;
    }

    private static JPanel createAlgorithmsPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JTabbedPane algoTabs = new JTabbedPane();
        algoTabs.setFont(new Font("Segoe UI", Font.BOLD, 15)); 

        JPanel pathPanel = new JPanel(new BorderLayout(15, 15));
        pathPanel.setBackground(WHITE);
        pathPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pathControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pathControl.setOpaque(false);
        JButton calcPathBtn = createStyledButton(" Find Shortest Path");
        pathControl.add(new JLabel("From:")); pathControl.add(srcCombo);
        pathControl.add(new JLabel("To:")); pathControl.add(destCombo);
        pathControl.add(calcPathBtn);

        JTextArea pathResultArea = new JTextArea();
        pathResultArea.setEditable(false);
        pathResultArea.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pathResultArea.setBorder(BorderFactory.createTitledBorder("Routing Path output"));

        calcPathBtn.addActionListener(e -> {
            if(srcCombo.getSelectedItem() == null || destCombo.getSelectedItem() == null) return;
            Station src = trainGraph.getStationByName((String) srcCombo.getSelectedItem());
            Station dest = trainGraph.getStationByName((String) destCombo.getSelectedItem());
            
            List<Station> path = trainGraph.shortestPath(src, dest);
            if(path.isEmpty()) {
                pathResultArea.setText("No path exists between " + src.getName() + " and " + dest.getName());
                showStatus("No reachable path found.", true);
            } else {
                StringBuilder sb = new StringBuilder("Optimal Route Found:\n\n   ");
                for(int i=0; i<path.size(); i++) {
                    sb.append(path.get(i).getName()).append(i == path.size()-1 ? "" : " ➔ ");
                }
                pathResultArea.setText(sb.toString());
                showStatus(" Shortest path calculated successfully.", false);
                highlightVisualPath(path);
            }
        });
        pathPanel.add(pathControl, BorderLayout.NORTH);
        pathPanel.add(new JScrollPane(pathResultArea), BorderLayout.CENTER);

        JPanel cyclePanel = new JPanel(new GridBagLayout());
        cyclePanel.setBackground(WHITE);
        JButton checkCycleBtn = createStyledButton("Run Network Cycle Detection");
        JLabel cycleResultLabel = new JLabel("Status: Awaiting Scan", SwingConstants.CENTER);
        cycleResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkCycleBtn.addActionListener(e -> {
            boolean hasCycle = trainGraph.ifTheGraphHasCycle();
            if(hasCycle) {
                cycleResultLabel.setText("️ A circular loop (Cycle) was DETECTED in the network!");
                cycleResultLabel.setForeground(Color.RED);
            } else {
                cycleResultLabel.setText("  No circular loops detected in the train network.");
                cycleResultLabel.setForeground(Color.decode("#27AE60"));
            }
        });
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.insets = new Insets(15, 15, 15, 15);
        cGbc.gridx = 0; cGbc.gridy = 0; cyclePanel.add(checkCycleBtn, cGbc);
        cGbc.gridy = 1; cyclePanel.add(cycleResultLabel, cGbc);

        JPanel sortPanel = new JPanel(new BorderLayout(10, 10));
        sortPanel.setBackground(WHITE);
        sortPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        String[] columns = {"Station Name", "Station Code", "Active Connections Count"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable sortTable = new JTable(tableModel);
        sortTable.setRowHeight(25);
        sortTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton loadSortedBtn = createStyledButton(" Load & Sort Stations Dense Layout");
        loadSortedBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            ArrayList<Station> sorted = trainGraph.stationsOrderedByNumOfEdges();
            for(Station s : sorted) {
                tableModel.addRow(new Object[]{s.getName(), s.getCode(), trainGraph.numOfEdgesOfEveryStation(s)});
            }
            showStatus("Stations sorted by density of connections.", false);
        });
        sortPanel.add(loadSortedBtn, BorderLayout.NORTH);
        sortPanel.add(new JScrollPane(sortTable), BorderLayout.CENTER);

        algoTabs.addTab(" Route Router", pathPanel);
        algoTabs.addTab(" Cycle Detector", cyclePanel);
        algoTabs.addTab(" Density Sorting Table", sortPanel);

        panel.add(algoTabs, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createReportsPage() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel dashboardPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        dashboardPanel.setBackground(BACKGROUND_COLOR);
        JPanel card1 = createReportCard("Total Stations", "0");
        JPanel card2 = createReportCard("Total Rails Paths", "0");
        JPanel card3 = createReportCard("Network Typology", "Directed");

        dashboardPanel.add(card1);
        dashboardPanel.add(card2);
        dashboardPanel.add(card3);

        JPanel fileIOPanel = new JPanel(new GridLayout(2, 2, 20, 20)); 
        fileIOPanel.setBackground(WHITE);
        fileIOPanel.setBorder(BorderFactory.createTitledBorder(" Text File & Image Persistence Control Centre"));
        JButton importBtn = createStyledButton(" Import Network Text File");
        JButton exportBtn = createStyledButton(" Export Network Text File");
        JButton saveImageBtn = createStyledButton("️ Save Graph to PNG ");
        JButton refreshReportBtn = createStyledButton(" Refresh System Counters");
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                trainGraph.Import(file.getAbsolutePath());
                updateAllCombos();
                shouldApplyLayout = true;
                updateVisualGraph(null);
                showStatus(" Successfully loaded network from text data source.", false);
            }
        });
        exportBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("railway_network.txt"));
            if (chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String realPath = file.getAbsolutePath();
                if(!realPath.toLowerCase().endsWith(".txt")) realPath += ".txt";
                trainGraph.Export(realPath);
                showStatus(" Exported network topology successfully.", false);
            }
        });
        saveImageBtn.addActionListener(e -> {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setSelectedFile(new File("train_network_render.png"));
            if (saveChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    File targetImageFile = saveChooser.getSelectedFile();
                    String tempTxt = "temp_export_data.txt";
                    trainGraph.Export(tempTxt);
                    Graph.drawFromFile(tempTxt, targetImageFile.getAbsolutePath());
                    new File(tempTxt).delete();
                    showStatus(" Generated Graphviz PNG successfully.", false);
                } catch (IOException ex) {
                    showStatus(" Image render failed: " + ex.getMessage(), true);
                }
            }
        });
        refreshReportBtn.addActionListener(e -> {
            int stationsCount = trainGraph.getStations().size();
            int edgesCount = 0;
            for(List<Edge> edges : trainGraph.getGraph().values()) {
                edgesCount += edges.size();
            }
            ((JLabel) card1.getComponent(1)).setText(String.valueOf(stationsCount));
            ((JLabel) card2.getComponent(1)).setText(String.valueOf(edgesCount));
            showStatus(" Dashboard counters synchronized.", false);
        });
        fileIOPanel.add(importBtn); fileIOPanel.add(exportBtn);
        fileIOPanel.add(saveImageBtn); fileIOPanel.add(refreshReportBtn);

        panel.add(dashboardPanel, BorderLayout.NORTH);
        panel.add(fileIOPanel, BorderLayout.CENTER);
        return panel;
    }

    private static void updateVisualGraph(String highlightNodeName) {
        graphContainerPanel.removeAll();
        mxGraph mxGraphInstance = new mxGraph() {
            @Override
            public boolean isCellEditable(Object cell) { return false; }
        };

        Object graphParent = mxGraphInstance.getDefaultParent();
        mxGraphInstance.getModel().beginUpdate();

        java.util.Map<Station, Object> vertexMap = new java.util.HashMap<>();
        try {
            for (Station station : trainGraph.graph.keySet()) {
                String style = "shape=ellipse;fillColor=#579fd6;strokeColor=#3FABF2;fontColor=white;fontSize=13;fontStyle=1";
                if (highlightNodeName != null && station.getName().equalsIgnoreCase(highlightNodeName)) {
                    style = "shape=ellipse;fillColor=#F39C12;strokeColor=#D35400;fontColor=white;fontSize=14;fontStyle=1";
                }
                
                int posX = 0;
                int posY = 0;
                if (!shouldApplyLayout && savedNodePositions.containsKey(station.getName())) {
                    Point p = savedNodePositions.get(station.getName());
                    posX = p.x;
                    posY = p.y;
                }
                
                Object vertex = mxGraphInstance.insertVertex(graphParent, null, station.getName(), posX, posY, 115, 50, style);
                vertexMap.put(station, vertex);
            }

            for (java.util.Map.Entry<Station, java.util.List<Edge>> entry : trainGraph.graph.entrySet()) {
                Station source = entry.getKey();
                Object sourceVertex = vertexMap.get(source);

                for (Edge edge : entry.getValue()) {
                    Object destVertex = vertexMap.get(edge.getDestination());
                    if (sourceVertex != null && destVertex != null) {
                        mxGraphInstance.insertEdge(graphParent, null, String.valueOf(edge.getDistance()), sourceVertex, destVertex, 
                                "strokeColor=#3FABF2;strokeWidth=2.5;endArrow=classic;verticalAlign=bottom;fontStyle=1;fontSize=12;fontColor=#2C3E50");
                    }
                }
            }
        } finally {
            mxGraphInstance.getModel().endUpdate();
        }

        if (shouldApplyLayout) {
            mxFastOrganicLayout layout = new mxFastOrganicLayout(mxGraphInstance);
            layout.setForceConstant(160); 
            layout.execute(graphParent);
            
            for (Station station : trainGraph.graph.keySet()) {
                Object cell = vertexMap.get(station);
                if (cell != null) {
                    com.mxgraph.model.mxGeometry g = mxGraphInstance.getModel().getGeometry(cell);
                    if (g != null) {
                        savedNodePositions.put(station.getName(), new Point((int)g.getX(), (int)g.getY()));
                    }
                }
            }
        }

        mxGraphComponent graphComponent = new mxGraphComponent(mxGraphInstance);
        graphComponent.getViewport().setBackground(Color.WHITE);
        graphComponent.setBorder(BorderFactory.createEmptyBorder());
        
        graphComponent.setCenterZoom(true);
        SwingUtilities.invokeLater(() -> {
            graphComponent.zoomAndCenter();
        });

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                Object cell = graphComponent.getCellAt(event.getX(), event.getY());
                if (cell != null) {
                    if (mxGraphInstance.getModel().isVertex(cell)) {
                        String label = mxGraphInstance.getLabel(cell);
                        Station target = trainGraph.getStationByName(label);
                        
                        if (target != null) {
                            int connectionCount = trainGraph.numOfEdgesOfEveryStation(target);
                            StringBuilder infoText = new StringBuilder();
                            infoText.append("Station Name: ").append(target.getName()).append("\n");
                            infoText.append("Station Code: ").append(target.getCode()).append("\n");
                            infoText.append("Active Edges: ").append(connectionCount).append("\n\n");
                            infoText.append("Direct Destinations:\n");
                            List<Edge> edges = trainGraph.getGraph().get(target);
                            if(edges != null) {
                                for(Edge eg : edges) {
                                    infoText.append(" ➔ ").append(eg.getDestination().getName())
                                             .append(" (").append(eg.getDistance()).append(" km)\n");
                                }
                            }
                            nodeInfoArea.setText(infoText.toString());

                            int response = JOptionPane.showConfirmDialog(graphComponent, 
                                    "Do you want to delete station [" + target.getName() + "] and all its connections?", 
                                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (response == JOptionPane.YES_OPTION) {
                                trainGraph.removeStation(target);
                                savedNodePositions.remove(target.getName());
                                showStatus("Station [" + target.getName() + "] deleted.", false);
                                updateAllCombos();
                                shouldApplyLayout = false;
                                updateVisualGraph(null);
                            }
                        }
                    } else if (mxGraphInstance.getModel().isEdge(cell)) {
                        Object sourceCell = mxGraphInstance.getModel().getTerminal(cell, true);
                        Object destCell = mxGraphInstance.getModel().getTerminal(cell, false);
                        if (sourceCell != null && destCell != null) {
                            String srcLabel = mxGraphInstance.getLabel(sourceCell);
                            String destLabel = mxGraphInstance.getLabel(destCell);
                            Station srcStation = trainGraph.getStationByName(srcLabel);
                            Station destStation = trainGraph.getStationByName(destLabel);
                            
                            if (srcStation != null && destStation != null) {
                                int response = JOptionPane.showConfirmDialog(graphComponent, 
                                        "Do you want to delete the rail path from [" + srcLabel + "] to [" + destLabel + "]?", 
                                        "Confirm Path Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (response == JOptionPane.YES_OPTION) {
                                    if (trainGraph.removeEdge(srcStation, destStation)) {
                                        showStatus("Path from [" + srcLabel + "] to [" + destLabel + "] removed.", false);
                                        shouldApplyLayout = false;
                                        updateVisualGraph(null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        graphContainerPanel.add(graphComponent, BorderLayout.CENTER);
        graphContainerPanel.revalidate();
        graphContainerPanel.repaint();
    }

    private static void highlightVisualPath(List<Station> pathStations) {
        if(pathStations == null || pathStations.size() < 2) return;
        shouldApplyLayout = false;
        updateVisualGraph(null);
        
        mxGraphComponent comp = (mxGraphComponent) graphContainerPanel.getComponent(0);
        mxGraph mxGraphInstance = comp.getGraph();
        Object graphParent = mxGraphInstance.getDefaultParent();
        
        mxGraphInstance.getModel().beginUpdate();
        try {
            Object[] edges = mxGraphInstance.getChildEdges(graphParent);
            for(Object edgeCell : edges) {
                String sourceLabel = mxGraphInstance.getLabel(mxGraphInstance.getModel().getTerminal(edgeCell, true));
                String destLabel = mxGraphInstance.getLabel(mxGraphInstance.getModel().getTerminal(edgeCell, false));
                
                for(int i = 0; i < pathStations.size() - 1; i++) {
                    if(pathStations.get(i).getName().equals(sourceLabel) && pathStations.get(i+1).getName().equals(destLabel)) {
                        mxGraphInstance.setCellStyle("strokeColor=#E74C3C;strokeWidth=4;endArrow=classic;fontColor=#E74C3C;fontStyle=1;fontSize=13", new Object[]{edgeCell});
                    }
                }
            }
        } finally {
            mxGraphInstance.getModel().endUpdate();
        }
        graphContainerPanel.repaint();
    }

    private static void updateAllCombos() {
        srcCombo.removeAllItems();
        destCombo.removeAllItems();
        edgeSrcCombo.removeAllItems(); edgeDestCombo.removeAllItems();
        deleteStationCombo.removeAllItems();
        for(Station s : trainGraph.getStations()) {
            srcCombo.addItem(s.getName());
            destCombo.addItem(s.getName());
            edgeSrcCombo.addItem(s.getName()); edgeDestCombo.addItem(s.getName());
            deleteStationCombo.addItem(s.getName());
        }
    }

    private static void showStatus(String message, boolean isError) {
        statusBar.setText(" " + message);
        statusBar.setForeground(isError ? Color.RED : Color.decode("#27AE60"));
    }

    private static JPanel createReportCard(String title, String value) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.GRAY);
        
        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(BLUE_LIGHT);

        card.add(titleLbl); card.add(valLbl);
        return card;
    }

    private static JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(WHITE);
        btn.setBackground(BLUE_BUTTON); 
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(BLUE_BUTTON.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(BLUE_BUTTON); }
        });
        return btn;
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BLUE_LIGHT);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(BLUE_LIGHT.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(BLUE_LIGHT); }
        });
        return button;
    }
}
