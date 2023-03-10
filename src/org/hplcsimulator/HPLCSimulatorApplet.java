package org.hplcsimulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Vector;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A Transferable able to transfer an AWT Image.
 * Similar to the JDK StringSelection class.
 */
class ImageSelection implements Transferable {
    private Image image;
   
    public static void copyImageToClipboard(Image image) {
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.getSystemClipboard().setContents(imageSelection, null);
    }
   
    public ImageSelection(Image image) {
        this.image = image;
    }
   
	@Override
    public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.imageFlavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
   
	@Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.imageFlavor);
    }
   
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {
            DataFlavor.imageFlavor
        };
    }
}

class Compound implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	String strCompoundName;
	double dConcentration;
	double dLogkwvsTSlope;
	double dLogkwvsTIntercept;
	double dSvsTSlope;
	double dSvsTIntercept;
	double dMolarVolume;
	
	double dRetentionTime;
	double dSigma;
	double dW;
	int iCompoundIndex;
	
	public boolean loadCompoundInfo(int iIndex, int iOrganicModifier)
	{
		iCompoundIndex = iIndex;
		
		strCompoundName = Globals.CompoundNameArray[iIndex];
		dLogkwvsTSlope = Globals.LSSTDataArray[iIndex][iOrganicModifier][0];
		dLogkwvsTIntercept = Globals.LSSTDataArray[iIndex][iOrganicModifier][1];
		dSvsTSlope = Globals.LSSTDataArray[iIndex][iOrganicModifier][2];
		dSvsTIntercept = Globals.LSSTDataArray[iIndex][iOrganicModifier][3];
		dMolarVolume = Globals.MolarVolumeArray[iIndex];
		
		return true;
	}
}

public class HPLCSimulatorApplet extends JFrame implements ActionListener, ChangeListener, KeyListener, FocusListener, ListSelectionListener, AutoScaleListener, TableModelListener
 {
	
	private static final long serialVersionUID = 1L;
	double dFileVersion = 1.13;

	private boolean m_bSliderUpdate;

	TopPanel contentPane = null;
	public JScrollPane jMainScrollPane = null;
	public int m_iSecondPlotType = 0;
	public double m_dTemperature = 25;
	public boolean m_bGradientMode = false;
	public double m_dSolventBFraction = 0.5;
	public double m_dMixingVolume = 200; /* in uL */
	public double m_dNonMixingVolume = 200; /* in uL */
	public double m_dColumnLength = 100;
	public double m_dColumnDiameter = 4.6;
	public double m_dInterparticlePorosity = 0.4;
	public double m_dIntraparticlePorosity = 0.4;
	public double m_dTotalPorosity = 0.64;
	public double m_dFlowRate = 2; /* in mL/min */
	public double m_dVoidVolume;
	public double m_dVoidTime;
	public double m_dOpenTubeVelocity;
	public double m_dInterstitialVelocity;
	public double m_dChromatographicVelocity;
	public double m_dReducedVelocity;
	public double m_dParticleSize = 5;
	public double m_dDiffusionCoefficient = 0.00001;
	public double m_dATerm = 1;
	public double m_dBTerm = 5;
	public double m_dCTerm = 0.05;
	public double m_dReducedFlowVelocity;
	public double m_dReducedPlateHeight;
	public double m_dTheoreticalPlates;
	public double m_dHETP;
	public double m_dInjectionVolume = 5; //(in uL)
	public double m_dTimeConstant = 0.5;
	public double m_dStartTime = 0;
	public double m_dEndTime = 0;
	public double m_dNoise = 3;
	public double m_dSignalOffset = 30;
	public int m_iNumPoints = 3000;
	public double m_dEluentViscosity = 1;
	public double m_dBackpressure = 400;
	public int m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
	public boolean m_bDoNotChangeTable = false;
	public Vector<Compound> m_vectCompound = new Vector<Compound>();
	public double[][] m_dGradientArray;
	public LinearInterpolationFunction m_lifGradient = null;
	public int m_iChromatogramPlotIndex = -1;
	public int m_iSinglePlotIndex = -1;
	public int m_iSecondPlotIndex = -1;
	public Vector<double[]> m_vectRetentionFactorArray;
	public Vector<double[]> m_vectPositionArray;
	public double m_dSelectedIsocraticRetentionFactor = 0;
	public double m_dTubingLength = 0; /* in cm */
	public double m_dTubingDiameter = 5; /* in mil */
	
	// Menu items
    JMenuItem menuLoadSettingsAction = new JMenuItem("Load Settings");
    JMenuItem menuSaveSettingsAction = new JMenuItem("Save Settings");
    JMenuItem menuSaveSettingsAsAction = new JMenuItem("Save Settings As...");
    JMenuItem menuResetToDefaultValuesAction = new JMenuItem("Reset To Default Settings");
    JMenuItem menuExitAction = new JMenuItem("Exit");
    
    JMenuItem menuHelpTopicsAction = new JMenuItem("Help Topics");
    JMenuItem menuAboutAction = new JMenuItem("About HPLC Simulator");
    
    File m_currentFile = null;
    boolean m_bDocumentChangedFlag = false;
    
    public static void main(String args[]){
    	HPLCSimulatorApplet sim = new HPLCSimulatorApplet(); // create frame with title

    	sim.pack(); // set window to appropriate size (for its elements)
    	sim.init();	
    	sim.setVisible(true); // usual step to make frame visible
     }

    public class JFileChooser2 extends JFileChooser
    {
		@Override
		public void approveSelection()
		{
		    File f = getSelectedFile();
		    if(f.exists() && getDialogType() == SAVE_DIALOG)
		    {
		        int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
		        switch(result){
		            case JOptionPane.YES_OPTION:
		                super.approveSelection();
		                return;
		            case JOptionPane.NO_OPTION:
		                return;
		            case JOptionPane.CANCEL_OPTION:
		                cancelSelection();
		                return;
		        }
		    }
		    super.approveSelection();
		}
	}
    
	/**
	 * This is the xxx default constructor
	 */
	public HPLCSimulatorApplet() 
	{
	    super();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    addWindowListener(new WindowAdapter()
	    {
	       public void windowClosing(WindowEvent e)
	       {
	         System.exit(0); //calling the method is a must
	       }
	    });
	    
		try {
	        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.metal");
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }

		this.setPreferredSize(new Dimension(910, 650));
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init()
	{
		
        // Load the JavaHelp
		String helpHS = "org/hplcsimulator/help/HPLCSimHelp.hs";
		ClassLoader cl = TopPanel.class.getClassLoader();
		try {
			URL hsURL = HelpSet.findHelpSet(cl, helpHS);
			Globals.hsMainHelpSet = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			System.out.println( "HelpSet " + ee.getMessage());
			System.out.println("HelpSet "+ helpHS +" not found");
			return;
		}
		Globals.hbMainHelpBroker = Globals.hsMainHelpSet.createHelpBroker();

		//Execute a job on the event-dispatching thread; creating this applet's GUI.

        //    SwingUtilities.invokeAndWait(new Runnable() 
        //    {
        //        public void run() {
                	createGUI();
        //        }
        //    });

        
    	Compound compound1 = new Compound();
    	compound1.loadCompoundInfo(2, m_iSolventB);
    	compound1.dConcentration = 5;
    	this.m_vectCompound.add(compound1);
    	
    	Compound compound2 = new Compound();
    	compound2.loadCompoundInfo(3, m_iSolventB);
    	compound2.dConcentration = 25;
    	this.m_vectCompound.add(compound2);
    	
    	Compound compound3 = new Compound();
    	compound3.loadCompoundInfo(4, m_iSolventB);
    	compound3.dConcentration = 40;
    	this.m_vectCompound.add(compound3);

    	Compound compound4 = new Compound();
    	compound4.loadCompoundInfo(6, m_iSolventB);
    	compound4.dConcentration = 15;
    	this.m_vectCompound.add(compound4);

    	Compound compound5 = new Compound();
    	compound5.loadCompoundInfo(11, m_iSolventB);
    	compound5.dConcentration = 10;
    	this.m_vectCompound.add(compound5);

    	updateCompoundComboBoxes();

    	for (int i = 0; i < m_vectCompound.size(); i++)
    	{
        	// Add the table space for the compound. Fill it in later with performCalculations().
        	Vector<String> vectNewRow = new Vector<String>();
        	vectNewRow.add(m_vectCompound.get(i).strCompoundName);
        	vectNewRow.add(Float.toString((float)m_vectCompound.get(i).dConcentration));
        	vectNewRow.add("");
        	vectNewRow.add("");
        	vectNewRow.add("");
        	vectNewRow.add("");
        	vectNewRow.add("");
        	
        	contentPane.vectChemicalRows.add(vectNewRow);    		
    	}
    	
    	calculateGradient();
        performCalculations();
    }
    
    private void createGUI()
    {
    	// Creates a menubar for a JFrame
        JMenuBar menuBar = new JMenuBar();
        
        // Add the menubar to the frame
        setJMenuBar(menuBar);
        
        // Define and add two drop down menu to the menubar
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        // Create and add simple menu item to one of the drop down menu
        menuLoadSettingsAction.addActionListener(this);
        menuSaveSettingsAction.addActionListener(this);
        menuSaveSettingsAsAction.addActionListener(this);
        menuResetToDefaultValuesAction.addActionListener(this);
        menuExitAction.addActionListener(this);

        menuHelpTopicsAction.addActionListener(this);
        menuAboutAction.addActionListener(this);
        
        fileMenu.add(menuLoadSettingsAction);
        fileMenu.add(menuSaveSettingsAction);
        fileMenu.add(menuSaveSettingsAsAction);
        fileMenu.addSeparator();
        fileMenu.add(menuResetToDefaultValuesAction);
        fileMenu.addSeparator();
        fileMenu.add(menuExitAction);
        
        helpMenu.add(menuHelpTopicsAction);
        helpMenu.addSeparator();
        helpMenu.add(menuAboutAction);

        //Create and set up the content pane
    	jMainScrollPane = new JScrollPane();
    	jMainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	jMainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    	contentPane = new TopPanel();
        contentPane.setOpaque(true);
        jMainScrollPane.setViewportView(contentPane);
    	setContentPane(contentPane);
    	jMainScrollPane.revalidate();
        
        contentPane.jbtnAddChemical.addActionListener(this);
        contentPane.jbtnEditChemical.addActionListener(this);
        contentPane.jbtnRemoveChemical.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoNoPlot.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoRetentionFactor.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoSolventBFraction.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoPosition.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoBackpressure.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.addActionListener(this);
        contentPane.jxpanelPlotOptions.jcboPositionCompounds.addActionListener(this);
        contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.addActionListener(this);
        contentPane.jxpanelMobilePhaseComposition.jcboSolventB.addActionListener(this);
        contentPane.jxpanelChromatographyProperties.jsliderTemp.addChangeListener(this);
        contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.addChangeListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtTemp.addKeyListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtTemp.addFocusListener(this);
        contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.addKeyListener(this);
        contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.addFocusListener(this);
        contentPane.jxpanelGradientOptions.jtxtMixingVolume.addKeyListener(this);
        contentPane.jxpanelGradientOptions.jtxtMixingVolume.addFocusListener(this);
        contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.addKeyListener(this);
        contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnLength.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnLength.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnDiameter.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnDiameter.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.addFocusListener(this);        
        contentPane.jxpanelChromatographyProperties.jtxtFlowRate.addKeyListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtFlowRate.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtParticleSize.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtParticleSize.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtATerm.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtATerm.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtBTerm.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtBTerm.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtCTerm.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtCTerm.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.addFocusListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.addKeyListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.addFocusListener(this);        
        contentPane.jxpanelGeneralProperties.jtxtTimeConstant.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtTimeConstant.addFocusListener(this);        
        contentPane.jxpanelGeneralProperties.jtxtNoise.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtNoise.addFocusListener(this);        
        contentPane.jxpanelGeneralProperties.jtxtSignalOffset.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtSignalOffset.addFocusListener(this);   
        contentPane.jxpanelGeneralProperties.jtxtInitialTime.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtInitialTime.addFocusListener(this);   
        contentPane.jxpanelGeneralProperties.jtxtFinalTime.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtFinalTime.addFocusListener(this);   
        contentPane.jxpanelGeneralProperties.jtxtNumPoints.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtNumPoints.addFocusListener(this);   
        contentPane.jtableChemicals.getSelectionModel().addListSelectionListener(this);
        contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.addActionListener(this);
        contentPane.jbtnPan.addActionListener(this);
        contentPane.jbtnZoomIn.addActionListener(this);
        contentPane.jbtnZoomOut.addActionListener(this);
        contentPane.jbtnAutoscale.addActionListener(this);
        contentPane.jbtnAutoscaleX.addActionListener(this);
        contentPane.jbtnAutoscaleY.addActionListener(this);
        contentPane.jbtnHelp.addActionListener(this);
        contentPane.jbtnTutorials.addActionListener(this);
        contentPane.jbtnCopyImage.addActionListener(this);
        contentPane.m_GraphControl.addAutoScaleListener(this);
        contentPane.m_GraphControl.setSecondYAxisVisible(false);
        contentPane.m_GraphControl.setSecondYAxisRangeLimits(0, 100);
        contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.addActionListener(this);
        contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.addActionListener(this);
        contentPane.jxpanelGradientOptions.jbtnInsertRow.addActionListener(this);
        contentPane.jxpanelGradientOptions.jbtnRemoveRow.addActionListener(this);
        contentPane.jbtnContextHelp.addActionListener(new CSH.DisplayHelpAfterTracking(Globals.hbMainHelpBroker));
        contentPane.jxpanelGradientOptions.tmGradientProgram.addTableModelListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.addKeyListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.addFocusListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.addKeyListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.addFocusListener(this);        
    }

    private void validateTemp()
    {
    	if (contentPane.jxpanelChromatographyProperties.jtxtTemp.getText().length() == 0)
    		contentPane.jxpanelChromatographyProperties.jtxtTemp.setText("0");

		double dTemp = (double)Float.valueOf(contentPane.jxpanelChromatographyProperties.jtxtTemp.getText());
		dTemp = Math.floor(dTemp);
		
		if (dTemp < 10)
			dTemp = 10;
		if (dTemp > 150)
			dTemp = 150;
		
		m_dTemperature = dTemp;
		m_bSliderUpdate = false;
		contentPane.jxpanelChromatographyProperties.jsliderTemp.setValue((int)m_dTemperature);
		contentPane.jxpanelChromatographyProperties.jtxtTemp.setText(Integer.toString((int)m_dTemperature));    	
    }
    
    private void validateSolventBFraction()
    {
    	if (contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.getText().length() == 0)
    		contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText("0");
    	
		double dTemp = (double)Float.valueOf(contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.getText());
		dTemp = Math.floor(dTemp);
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 100)
			dTemp = 100;
		
		this.m_dSolventBFraction = dTemp / 100;
		m_bSliderUpdate = false;
		contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.setValue((int)(m_dSolventBFraction * 100));
		contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText(Integer.toString((int)(m_dSolventBFraction * 100)));    	
    }    
 
    private void validateMixingVolume()
    {
    	if (contentPane.jxpanelGradientOptions.jtxtMixingVolume.getText().length() == 0)
    		contentPane.jxpanelGradientOptions.jtxtMixingVolume.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGradientOptions.jtxtMixingVolume.getText());
		
		if (dTemp < 0.01)
			dTemp = 0.01;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dMixingVolume = dTemp;
		contentPane.jxpanelGradientOptions.jtxtMixingVolume.setText(Float.toString((float)m_dMixingVolume));    	
    }    

    private void validateNonMixingVolume()
    {
    	if (contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.getText().length() == 0)
    		contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.getText());
		
		if (dTemp < 0.01)
			dTemp = 0.01;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dNonMixingVolume = dTemp;
		contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.setText(Float.toString((float)m_dNonMixingVolume));    	
    }
    
    private void validateColumnLength()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtColumnLength.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtColumnLength.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtColumnLength.getText());
		
		if (dTemp < .01)
			dTemp = .01;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dColumnLength = dTemp;
		contentPane.jxpanelColumnProperties.jtxtColumnLength.setText(Float.toString((float)m_dColumnLength));    	
    }    

    private void validateColumnDiameter()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtColumnDiameter.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtColumnDiameter.getText());
		
		if (dTemp < .001)
			dTemp = .001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dColumnDiameter = dTemp;
		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText(Float.toString((float)m_dColumnDiameter));    	
    }    

    private void validateInterparticlePorosity()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.getText());
		
		if (dTemp < .001)
			dTemp = .001;
		if (dTemp > .999)
			dTemp = .999;
		
		this.m_dInterparticlePorosity = dTemp;
		contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.setText(Float.toString((float)m_dInterparticlePorosity));    	
    }    

    private void validateIntraparticlePorosity()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.getText());
		
		if (dTemp < .001)
			dTemp = .001;
		if (dTemp > .999)
			dTemp = .999;
		
		this.m_dIntraparticlePorosity = dTemp;
		contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.setText(Float.toString((float)m_dIntraparticlePorosity));    	
    }    

    private void validateFlowRate()
    {
    	if (contentPane.jxpanelChromatographyProperties.jtxtFlowRate.getText().length() == 0)
    		contentPane.jxpanelChromatographyProperties.jtxtFlowRate.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelChromatographyProperties.jtxtFlowRate.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dFlowRate = dTemp;
		contentPane.jxpanelChromatographyProperties.jtxtFlowRate.setText(Float.toString((float)m_dFlowRate));    	
    }    

    private void validateParticleSize()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtParticleSize.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtParticleSize.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtParticleSize.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dParticleSize = dTemp;
		contentPane.jxpanelColumnProperties.jtxtParticleSize.setText(Float.toString((float)m_dParticleSize));    	
    }    

    private void validateATerm()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtATerm.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtATerm.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtATerm.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dATerm = dTemp;
		contentPane.jxpanelColumnProperties.jtxtATerm.setText(Float.toString((float)m_dATerm));    	
    }    

    private void validateBTerm()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtBTerm.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtBTerm.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtBTerm.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dBTerm = dTemp;
		contentPane.jxpanelColumnProperties.jtxtBTerm.setText(Float.toString((float)m_dBTerm));    	
    } 
    
    private void validateCTerm()
    {
    	if (contentPane.jxpanelColumnProperties.jtxtCTerm.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtCTerm.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtCTerm.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dCTerm = dTemp;
		contentPane.jxpanelColumnProperties.jtxtCTerm.setText(Float.toString((float)m_dCTerm));    	
    } 

    private void validateInjectionVolume()
    {
    	if (contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.getText().length() == 0)
    		contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dInjectionVolume = dTemp;
		contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.setText(Float.toString((float)m_dInjectionVolume));    	
    } 

    private void validateTimeConstant()
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtTimeConstant.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtTimeConstant.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGeneralProperties.jtxtTimeConstant.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dTimeConstant = dTemp;
		contentPane.jxpanelGeneralProperties.jtxtTimeConstant.setText(Float.toString((float)m_dTimeConstant));    	
    } 

    private void validateNoise()
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtNoise.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtNoise.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGeneralProperties.jtxtNoise.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dNoise = dTemp;
		contentPane.jxpanelGeneralProperties.jtxtNoise.setText(Float.toString((float)m_dNoise));    	
    } 

    private void validateSignalOffset()
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtSignalOffset.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtSignalOffset.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGeneralProperties.jtxtSignalOffset.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dSignalOffset = dTemp;
		contentPane.jxpanelGeneralProperties.jtxtSignalOffset.setText(Float.toString((float)m_dSignalOffset));    	
    } 

    private void validateStartTime()
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtInitialTime.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGeneralProperties.jtxtInitialTime.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > m_dEndTime)
			dTemp = m_dEndTime - .000001;
		
		this.m_dStartTime = dTemp;
		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText(Float.toString((float)m_dStartTime));    	
    } 

    private void validateEndTime()
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtFinalTime.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelGeneralProperties.jtxtFinalTime.getText());
		
		if (dTemp < m_dStartTime)
			dTemp = m_dStartTime + .000001;
		if (dTemp > 99999999)
			dTemp = 99999999;
		
		this.m_dEndTime = dTemp;
		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(Float.toString((float)m_dEndTime));    	
    } 

    private void validateNumPoints()
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtNumPoints.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText("0");

    	int iTemp = Integer.valueOf(contentPane.jxpanelGeneralProperties.jtxtNumPoints.getText());
		
		if (iTemp < 2)
			iTemp = 2;
		if (iTemp > 100000)
			iTemp = 100000;
		
		this.m_iNumPoints = iTemp;
		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText(Integer.toString(m_iNumPoints));    	
    } 

    private void validateTubingLength()
    {
    	if (contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.getText().length() == 0)
    		contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 99999999)
			dTemp = 99999999;
		
		this.m_dTubingLength = dTemp;
		contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.setText(Float.toString((float)m_dTubingLength));    	
    } 
    
    private void validateTubingDiameter()
    {
    	if (contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.getText().length() == 0)
    		contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 99999999)
			dTemp = 99999999;
		
		this.m_dTubingDiameter = dTemp;
		contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.setText(Float.toString((float)m_dTubingDiameter));    	
    } 
    
    public boolean writeToOutputStream()
    {
    	try 
		{
            FileOutputStream fos = new FileOutputStream(m_currentFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

	    	oos.writeDouble(dFileVersion);
	    	oos.writeObject(contentPane.jxpanelGradientOptions.tmGradientProgram.getDataVector());
            oos.writeBoolean(m_bGradientMode);
	    	oos.writeBoolean(contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.isSelected());
	        oos.writeDouble(m_dTemperature);
	        oos.writeDouble(m_dSolventBFraction);
	        oos.writeDouble(m_dMixingVolume);
	        oos.writeDouble(m_dNonMixingVolume);
	        oos.writeDouble(m_dColumnLength);
	        oos.writeDouble(m_dColumnDiameter);
	        oos.writeDouble(m_dInterparticlePorosity);
	        oos.writeDouble(m_dIntraparticlePorosity);
	        oos.writeDouble(m_dFlowRate);
	        oos.writeDouble(m_dParticleSize);
	        oos.writeDouble(m_dATerm);
	        oos.writeDouble(m_dBTerm);
	        oos.writeDouble(m_dCTerm);
	        oos.writeDouble(m_dInjectionVolume);
	        oos.writeDouble(m_dTimeConstant);
	        oos.writeDouble(m_dStartTime);
	        oos.writeDouble(m_dEndTime);
	        oos.writeDouble(m_dNoise);
	        oos.writeDouble(m_dSignalOffset);
	        oos.writeInt(m_iNumPoints);
	        oos.writeInt(m_iSolventB);
	        oos.writeObject(m_vectCompound);
	        oos.writeDouble(m_dTubingLength);
	        oos.writeDouble(m_dTubingDiameter);
	        
            oos.flush();
			oos.close();
			this.m_bDocumentChangedFlag = false;
    	}
    	catch (IOException e) 
		{
			e.printStackTrace();
	        JOptionPane.showMessageDialog(this, "The file could not be saved.", "Error saving file", JOptionPane.ERROR_MESSAGE);
	        return false;
		}
    	
    	return true;
    }
    
    public boolean saveFile(boolean bSaveAs)
    {
		if (bSaveAs == false && m_currentFile != null)
		{
			if (writeToOutputStream())
				return true;
		}
		else
		{	
			JFileChooser2 fc = new JFileChooser2();
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter("HPLC Simulator Files (*.hplcsim)", "hplcsim");
			fc.setFileFilter(filter);
			fc.setDialogTitle("Save As...");
			int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_currentFile = fc.getSelectedFile();
                String path = m_currentFile.getAbsolutePath();
                if (path.lastIndexOf(".") >= 0)
                	path = path.substring(0, path.lastIndexOf("."));
                	
                m_currentFile = new File(path + ".hplcsim");

               	if (writeToOutputStream())
               		return true;
				
            }
		}
		return false;
    }
    
    public boolean loadFile(File fileToLoad)
    {
    	// Set all variables to default values here
    	
    	int iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
    	for (int i = 0; i < iNumRows; i++)
    	{
    		m_bDoNotChangeTable = true;
    		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
    	}

		m_bDoNotChangeTable = true;
    	contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {0.0, 5.0});
		m_bDoNotChangeTable = true;
    	contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {5.0, 95.0});
    	
    	m_bGradientMode = false;
    	boolean bAutomaticTimeRange = true;
    	m_dTemperature = 25;
    	m_dSolventBFraction = 0.5;
    	m_dMixingVolume = 200; /* in uL */
    	m_dNonMixingVolume = 200; /* in uL */
    	m_dColumnLength = 100;
    	m_dColumnDiameter = 4.6;
    	m_dInterparticlePorosity = 0.4;
    	m_dIntraparticlePorosity = 0.4;
    	m_dFlowRate = 2; /* in mL/min */
    	m_dParticleSize = 3.0;
    	m_dATerm = 1;
    	m_dBTerm = 5;
    	m_dCTerm = 0.05;
    	m_dInjectionVolume = 5; //(in uL)
    	m_dTimeConstant = 0.1;
    	m_dStartTime = 0;
    	m_dEndTime = 277;
    	m_dNoise = 2.0;
    	m_dSignalOffset = 0;
    	m_iNumPoints = 3000;
    	m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
    	m_vectCompound.clear();
    	Compound compound1 = new Compound();
    	compound1.loadCompoundInfo(2, m_iSolventB);
    	compound1.dConcentration = 5;
    	this.m_vectCompound.add(compound1);
    	
    	Compound compound2 = new Compound();
    	compound2.loadCompoundInfo(3, m_iSolventB);
    	compound2.dConcentration = 25;
    	this.m_vectCompound.add(compound2);
    	
    	Compound compound3 = new Compound();
    	compound3.loadCompoundInfo(4, m_iSolventB);
    	compound3.dConcentration = 40;
    	this.m_vectCompound.add(compound3);

    	Compound compound4 = new Compound();
    	compound4.loadCompoundInfo(6, m_iSolventB);
    	compound4.dConcentration = 15;
    	this.m_vectCompound.add(compound4);

    	Compound compound5 = new Compound();
    	compound5.loadCompoundInfo(11, m_iSolventB);
    	compound5.dConcentration = 10;
    	this.m_vectCompound.add(compound5);
    	
    	m_dTubingLength = 0; /* in cm */
    	m_dTubingDiameter = 5; /* in mil */
    	
    	if (fileToLoad != null)
    	{
    		try 
            {
                FileInputStream fis = new FileInputStream(m_currentFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                double dFileVersion = ois.readDouble();
                if (dFileVersion >= 1.13)
                {
                	iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
                	for (int i = 0; i < iNumRows; i++)
                	{
                		m_bDoNotChangeTable = true;
                		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
                	}
              
                	Vector<Vector<Double>> rowVector = (Vector<Vector<Double>>)ois.readObject();

                	for (int i = 0; i < rowVector.size(); i++)
                	{
                		if (rowVector.elementAt(i) != null)
                		{
                    		m_bDoNotChangeTable = true;
                			contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(rowVector.elementAt(i));
                		}
                	}
                	m_bGradientMode = ois.readBoolean();
                	bAutomaticTimeRange = ois.readBoolean();
                	m_dTemperature = ois.readDouble();
                	m_dSolventBFraction = ois.readDouble();
                	m_dMixingVolume = ois.readDouble();
                	m_dNonMixingVolume = ois.readDouble();
                	m_dColumnLength = ois.readDouble();
                	m_dColumnDiameter = ois.readDouble();
                	m_dInterparticlePorosity = ois.readDouble();
                	m_dIntraparticlePorosity = ois.readDouble();
                	m_dFlowRate = ois.readDouble();
                	m_dParticleSize = ois.readDouble();
                	m_dATerm = ois.readDouble();
                	m_dBTerm = ois.readDouble();
                	m_dCTerm = ois.readDouble();
                	m_dInjectionVolume = ois.readDouble();
                	m_dTimeConstant = ois.readDouble();
                	m_dStartTime = ois.readDouble();
                	m_dEndTime = ois.readDouble();
                	m_dNoise = ois.readDouble();
                	m_dSignalOffset = ois.readDouble();
                	m_iNumPoints = ois.readInt();
                	m_iSolventB = ois.readInt();
                	m_vectCompound = (Vector<Compound>) ois.readObject();
                	m_dTubingLength = ois.readDouble();
                	m_dTubingDiameter = ois.readDouble();
                }
    	        
                ois.close();
			} 
            catch (IOException e) 
            {
				e.printStackTrace();
		        JOptionPane.showMessageDialog(this, "The file is not a valid HPLC Simulator file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
		        m_currentFile = null;
		        return false;
			} 
            catch (ClassNotFoundException e) 
            {
				e.printStackTrace();
		        JOptionPane.showMessageDialog(this, "The file is not a valid HPLC Simulator file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
		        m_currentFile = null;
		        return false;
            }
    	}
    	
    	// Now set each parameter in the controls
    	if (m_bGradientMode)
	    {
    		contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(true);

	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelIsocraticOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelGradientOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jControlPanel.validate();
	    }
	    else
	    {
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(true);
	    	
	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelGradientOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelIsocraticOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jControlPanel.validate();
	    }
    	
    	m_bSliderUpdate = false;
    	contentPane.jxpanelChromatographyProperties.jtxtTemp.setText(Integer.toString((int)m_dTemperature));
		contentPane.jxpanelChromatographyProperties.jsliderTemp.setValue((int)m_dTemperature);
		m_bSliderUpdate = false;
		contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.setValue((int)(m_dSolventBFraction * 100));
		contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText(Integer.toString((int)(m_dSolventBFraction * 100)));    	
		contentPane.jxpanelChromatographyProperties.jtxtFlowRate.setText(Float.toString((float)m_dFlowRate));    	
		contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.setText(Float.toString((float)m_dInjectionVolume));    	
		contentPane.jxpanelColumnProperties.jtxtATerm.setText(Float.toString((float)m_dATerm));    	
		contentPane.jxpanelColumnProperties.jtxtBTerm.setText(Float.toString((float)m_dBTerm));    	
		contentPane.jxpanelColumnProperties.jtxtCTerm.setText(Float.toString((float)m_dCTerm));    	
		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText(Float.toString((float)m_dColumnDiameter));    	
		contentPane.jxpanelColumnProperties.jtxtColumnLength.setText(Float.toString((float)m_dColumnLength));    	
		contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.setText(Float.toString((float)m_dInterparticlePorosity));    	
		contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.setText(Float.toString((float)m_dIntraparticlePorosity));    	
		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText(Float.toString((float)m_dStartTime));    	
		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(Float.toString((float)m_dEndTime));    	
		contentPane.jxpanelGradientOptions.jtxtMixingVolume.setText(Float.toString((float)m_dMixingVolume));    	
		contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.setText(Float.toString((float)m_dNonMixingVolume));    	
		contentPane.jxpanelGeneralProperties.jtxtNoise.setText(Float.toString((float)m_dNoise));    	
		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText(Integer.toString(m_iNumPoints));    	
		contentPane.jxpanelColumnProperties.jtxtParticleSize.setText(Float.toString((float)m_dParticleSize));    	
		contentPane.jxpanelGeneralProperties.jtxtSignalOffset.setText(Float.toString((float)m_dSignalOffset));    	
		contentPane.jxpanelGeneralProperties.jtxtTimeConstant.setText(Float.toString((float)m_dTimeConstant));    	
		contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.setText(Float.toString((float)m_dTubingDiameter));    	
		contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.setText(Float.toString((float)m_dTubingLength));    	
		contentPane.jxpanelMobilePhaseComposition.jcboSolventA.setSelectedIndex(0);
		contentPane.jxpanelMobilePhaseComposition.jcboSolventB.setSelectedIndex(m_iSolventB);
		
		
    	// Add the table space for the compound. Fill it in later with performCalculations().
		contentPane.vectChemicalRows.clear();
		for (int i = 0; i < m_vectCompound.size(); i++)
		{
			Vector<String> vectNewRow = new Vector<String>();
	    	vectNewRow.add(Globals.CompoundNameArray[m_vectCompound.get(i).iCompoundIndex]);
	    	vectNewRow.add(Float.toString((float)m_vectCompound.get(i).dConcentration));
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	contentPane.vectChemicalRows.add(vectNewRow);
		}
    	updateCompoundComboBoxes();
    	
		contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.setSelected(bAutomaticTimeRange);
    	if (bAutomaticTimeRange)
    	{
    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(false);
    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(false);
    	}
    	else
    	{
    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(true);
    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(true);	    		
    	}
    	
		performCalculations();
		/*
    	m_iSecondPlotType = 0;
    	m_dTemperature = 25;
    	m_bGradientMode = false;
    	m_dSolventBFraction = 0.5;
    	m_dMixingVolume = 200;
    	m_dNonMixingVolume = 200;
    	m_dColumnLength = 100;
    	m_dColumnDiameter = 4.6;
    	m_dInterparticlePorosity = 0.4;
    	m_dIntraparticlePorosity = 0.4;
    	m_dTotalPorosity = 0.64;
    	m_dFlowRate = 2;
    	m_dParticleSize = 5;
    	m_dDiffusionCoefficient = 0.00001;
    	m_dATerm = 1;
    	m_dBTerm = 5;
    	m_dCTerm = 0.05;
    	m_dInjectionVolume = 5; //(in uL)
    	m_dTimeConstant = 0.5;
    	m_dStartTime = 0;
    	m_dEndTime = 0;
    	m_dNoise = 3;
    	m_dSignalOffset = 30;
    	m_iNumPoints = 3000;
    	m_dEluentViscosity = 1;
    	m_dBackpressure = 400;
    	m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
    	m_vectCompound.clear();
    	Compound compound1 = new Compound();
    	compound1.loadCompoundInfo(2, m_iSolventB);
    	compound1.dConcentration = 5;
    	this.m_vectCompound.add(compound1);
    	
    	Compound compound2 = new Compound();
    	compound2.loadCompoundInfo(3, m_iSolventB);
    	compound2.dConcentration = 25;
    	this.m_vectCompound.add(compound2);
    	
    	Compound compound3 = new Compound();
    	compound3.loadCompoundInfo(4, m_iSolventB);
    	compound3.dConcentration = 40;
    	this.m_vectCompound.add(compound3);

    	Compound compound4 = new Compound();
    	compound4.loadCompoundInfo(6, m_iSolventB);
    	compound4.dConcentration = 15;
    	this.m_vectCompound.add(compound4);

    	Compound compound5 = new Compound();
    	compound5.loadCompoundInfo(11, m_iSolventB);
    	compound5.dConcentration = 10;
    	this.m_vectCompound.add(compound5);
    	
    	m_iChromatogramPlotIndex = -1;
    	m_iSinglePlotIndex = -1;
    	m_iSecondPlotIndex = -1;
    	m_dTubingLength = 0;
    	m_dTubingDiameter = 5;*/
    	
    	return true;
    }
    
    public void actionPerformed(ActionEvent evt) 
	{
	    String strActionCommand = evt.getActionCommand();
	    if (strActionCommand == "Add Chemical")
	    {
	    	Frame[] frames = Frame.getFrames();
	    	ChemicalDialog dlgChemical = new ChemicalDialog(frames[0], false, m_iSolventB);
	    	
	    	// Make a list of the chemical indices already used
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		Integer k = new Integer(m_vectCompound.get(i).iCompoundIndex);
	    		dlgChemical.m_vectCompoundsUsed.add(k);
	    	}
	    	
	    	// Show the dialog.
	    	dlgChemical.setVisible(true);
	    	
	    	if (dlgChemical.m_bOk == false)
	    		return;
	    	
	    	// Add the compound properties to the m_vectCompound array
	    	Compound newCompound = new Compound();
	    	newCompound.strCompoundName = dlgChemical.m_strCompoundName;
	    	newCompound.dConcentration = dlgChemical.m_dConcentration;
	    	newCompound.dLogkwvsTSlope = dlgChemical.m_dLogkwvsTSlope;
	    	newCompound.dLogkwvsTIntercept = dlgChemical.m_dLogkwvsTIntercept;
	    	newCompound.dSvsTSlope = dlgChemical.m_dSvsTSlope;
	    	newCompound.dSvsTIntercept = dlgChemical.m_dSvsTIntercept;
	    	newCompound.dMolarVolume = dlgChemical.m_dMolarVolume;
	    	newCompound.iCompoundIndex = dlgChemical.m_iCompound;
	    	
	    	this.m_vectCompound.add(newCompound);
	    	
	    	// Add the table space for the compound. Fill it in later with performCalculations().
	    	Vector<String> vectNewRow = new Vector<String>();
	    	vectNewRow.add(dlgChemical.m_strCompoundName);
	    	vectNewRow.add(Float.toString((float)dlgChemical.m_dConcentration));
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	
	    	contentPane.vectChemicalRows.add(vectNewRow);
	    	updateCompoundComboBoxes();
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Edit Chemical")
	    {
	    	int iRowSel = contentPane.jtableChemicals.getSelectedRow();
	    	if (iRowSel < 0 || iRowSel >= contentPane.vectChemicalRows.size())
	    		return;

	    	Frame[] frames = Frame.getFrames();
	    	ChemicalDialog dlgChemical = new ChemicalDialog(frames[0], true, m_iSolventB);
	    	
	    	dlgChemical.setSelectedCompound(this.m_vectCompound.get(iRowSel).iCompoundIndex);
	    	dlgChemical.setCompoundConcentration(this.m_vectCompound.get(iRowSel).dConcentration);
	    	
	    	// Make a list of the chemical indices already used
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		// Don't add the currently selected row to the list
	    		if (i == iRowSel)
	    			continue;
	    		
	    		Integer k = new Integer(m_vectCompound.get(i).iCompoundIndex);
	    		dlgChemical.m_vectCompoundsUsed.add(k);
	    	}
	    	
	    	// Show the dialog.
	    	dlgChemical.setVisible(true);
	    	
	    	if (dlgChemical.m_bOk == false)
	    		return;
	    	
	    	// Add the compound properties to the m_vectCompound array
	    	Compound newCompound = new Compound();
	    	newCompound.strCompoundName = dlgChemical.m_strCompoundName;
	    	newCompound.dConcentration = dlgChemical.m_dConcentration;
	    	newCompound.dLogkwvsTSlope = dlgChemical.m_dLogkwvsTSlope;
	    	newCompound.dLogkwvsTIntercept = dlgChemical.m_dLogkwvsTIntercept;
	    	newCompound.dSvsTSlope = dlgChemical.m_dSvsTSlope;
	    	newCompound.dSvsTIntercept = dlgChemical.m_dSvsTIntercept;
	    	newCompound.dMolarVolume = dlgChemical.m_dMolarVolume;
	    	newCompound.iCompoundIndex = dlgChemical.m_iCompound;
	    	
	    	this.m_vectCompound.set(iRowSel, newCompound);
	    	
	    	// Add the table space for the compound. Fill it in later with performCalculations().
	    	Vector<String> vectNewRow = new Vector<String>();
	    	vectNewRow.add(dlgChemical.m_strCompoundName);
	    	vectNewRow.add(Float.toString((float)dlgChemical.m_dConcentration));
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	
	    	contentPane.vectChemicalRows.set(iRowSel, vectNewRow);
	    	updateCompoundComboBoxes();

	    	performCalculations();
	    }
	    else if (strActionCommand == "Remove Chemical")
	    {
	    	int iRowSel = contentPane.jtableChemicals.getSelectedRow();
	    	if (iRowSel < 0 || iRowSel >= contentPane.vectChemicalRows.size())
	    		return;
	    	
	    	contentPane.vectChemicalRows.remove(iRowSel);
	    	contentPane.jtableChemicals.addNotify();
	    	
	    	if (iRowSel >= m_vectCompound.size())
	    		return;
	    	
	    	m_vectCompound.remove(iRowSel);	    		
	    	updateCompoundComboBoxes();

	    	performCalculations();
	    }
	    else if (strActionCommand == "Automatically determine time span")
	    {
	    	if (contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.isSelected() == true)
	    	{
	    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(false);
	    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(false);
	    	}
	    	else
	    	{
	    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(true);
	    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(true);	    		
	    	}
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Autoscale")
	    {
	    	if (contentPane.jbtnAutoscale.isSelected() == true)
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(true);
	    		contentPane.m_GraphControl.setAutoScaleY(true);
	    		contentPane.m_GraphControl.repaint();
	    	}
	    	else
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(false);
    			contentPane.m_GraphControl.setAutoScaleY(false);
	    	}
	    }
	    else if (strActionCommand == "Autoscale X")
	    {
	    	if (contentPane.jbtnAutoscaleX.isSelected() == true)
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(true);
	    		contentPane.m_GraphControl.repaint();
	    	}
	    	else
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(false);
	    	}
	    }
	    else if (strActionCommand == "Autoscale Y")
	    {
	    	if (contentPane.jbtnAutoscaleY.isSelected() == true)
	    	{
	    		contentPane.m_GraphControl.setAutoScaleY(true);
	    		contentPane.m_GraphControl.repaint();
	    	}
	    	else
	    	{
	    		contentPane.m_GraphControl.setAutoScaleY(false);
	    	}
	    }
	    else if (strActionCommand == "Pan")
	    {
	    	contentPane.jbtnPan.setSelected(true);
	    	contentPane.jbtnZoomIn.setSelected(false);
	    	contentPane.jbtnZoomOut.setSelected(false);
	    	contentPane.m_GraphControl.selectPanMode();
	    }
	    else if (strActionCommand == "Zoom in")
	    {
	    	contentPane.jbtnPan.setSelected(false);
	    	contentPane.jbtnZoomIn.setSelected(true);
	    	contentPane.jbtnZoomOut.setSelected(false);	    	
	    	contentPane.m_GraphControl.selectZoomInMode();
	    }
	    else if (strActionCommand == "Zoom out")
	    {
	    	contentPane.jbtnPan.setSelected(false);
	    	contentPane.jbtnZoomIn.setSelected(false);
	    	contentPane.jbtnZoomOut.setSelected(true);	    		    	
	    	contentPane.m_GraphControl.selectZoomOutMode();
	    }
	    else if (strActionCommand == "Help")
	    {
			Globals.hbMainHelpBroker.setCurrentID("getting_started");
			Globals.hbMainHelpBroker.setDisplayed(true);
	    }
	    else if (strActionCommand == "Tutorials")
	    {
			Globals.hbMainHelpBroker.setCurrentID("tutorials");
			Globals.hbMainHelpBroker.setDisplayed(true);
	    }
	    else if (strActionCommand == "SolventBComboBoxChanged")
	    {
	    	m_iSolventB = contentPane.jxpanelMobilePhaseComposition.jcboSolventB.getSelectedIndex();
	    	// Change the organic modifier fraction label 
	    	String strLabel = "Solvent B fraction (% v/v):";
	    	contentPane.jxpanelIsocraticOptions.jlblSolventBFraction.setText(strLabel);
	    	
	    	// Change all the Compound information
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		m_vectCompound.get(i).loadCompoundInfo(m_vectCompound.get(i).iCompoundIndex, m_iSolventB);
	    	}
	    	
	    	// Update all the indicators
	    	performCalculations();
	    }
	    else if (strActionCommand == "Isocratic elution mode")
	    {
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(true);
	    	
	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelGradientOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelIsocraticOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jControlPanel.validate();
	    	
	    	this.m_bGradientMode = false;
	    	performCalculations();
	    }
	    else if (strActionCommand == "Gradient elution mode")
	    {
	    	contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(true);

	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelIsocraticOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelGradientOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jControlPanel.validate();

	    	this.m_bGradientMode = true;
	    	performCalculations();
	    }
	    else if (strActionCommand == "Insert Row")
	    {
	    	int iSelectedRow = contentPane.jxpanelGradientOptions.jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount()-1;
	    	
	    	Double dRowValue1 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iSelectedRow, 0);
	    	Double dRowValue2 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iSelectedRow, 1);
	    	Double dRowData[] = {dRowValue1, dRowValue2};
	    	contentPane.jxpanelGradientOptions.tmGradientProgram.insertRow(iSelectedRow+1, dRowData);
	    }
	    else if (strActionCommand == "Remove Row")
	    {
	    	int iSelectedRow = contentPane.jxpanelGradientOptions.jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount()-1;
	    	
	    	if (contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() >= 3)
	    	{
	    		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (strActionCommand == "No plot")
	    {
	    	m_iSecondPlotType = 0;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(false);
	    	contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
	    	m_iSecondPlotIndex = -1;
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot solvent B fraction")
	    {
	    	m_iSecondPlotType = 1;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(true);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Solvent B Fraction");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("% v/v", "%");
	    	contentPane.m_GraphControl.setSecondYAxisRangeLimits(0.0, 100.0);
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot backpressure")
	    {
	    	m_iSecondPlotType = 2;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Backpressure");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("bar", "bar");
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot mobile phase viscosity")
	    {
	    	m_iSecondPlotType = 3;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Mobile Phase Viscosity");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("Poise", "P");
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot retention factor")
	    {
	    	m_iSecondPlotType = 4;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("k", "");
	    	int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex();
	    	String strCompoundName = "";
	    	
	    	if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
	    		strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    	
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Retention factor of " + strCompoundName);

	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot position")
	    {
	    	m_iSecondPlotType = 5;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex();
	    	String strCompoundName = "";
	    	
	    	if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
	    		strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    	
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Position of " + strCompoundName + " along column");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("millimeters", "mm");
	    	contentPane.m_GraphControl.setSecondYAxisRangeLimits(0.0, m_dColumnLength);
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "RetentionFactorCompoundChanged")
	    {
	    	if (this.m_iSecondPlotType == 4)
	    	{
	    		int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex();
	    		String strCompoundName = "";
	    		
	    		if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
		    		strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    		
		    	contentPane.m_GraphControl.setSecondYAxisTitle("Retention factor of " + strCompoundName);
	    	}

	    	performCalculations();  	
	    }
	    else if (strActionCommand == "PositionCompoundChanged")
	    {
	    	if (this.m_iSecondPlotType == 5)
	    	{
	    		int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex();
	    		String strCompoundName = "";
	    		
	    		if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
	    			strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    		
		    	contentPane.m_GraphControl.setSecondYAxisTitle("Position of " + strCompoundName + " along column");
	    	}
	    	
	    	performCalculations();  	
	    }
	    else if (strActionCommand == "Copy Image")
	    {
	    	ByteBuffer bytePixels = contentPane.m_GraphControl.getPixels();
	    	Image image;
	    	int h = contentPane.m_GraphControl.getHeight();
	    	int w = contentPane.m_GraphControl.getWidth();
	    	if (w % 4 > 0)
        		w += 4 - (w % 4);
	    	
	    	byte[] flippedPixels = new byte[bytePixels.array().length];

	    	for (int y = 0; y < h; y++)
	    	{
	    		for (int x = 0; x < w * 4; x++)
	    		{
	    			flippedPixels[(y * w * 4) + x] = bytePixels.array()[((h - y - 1) * w * 4) + x];
	    		}
	    	}

	    	DataBuffer dbuf = new DataBufferByte(flippedPixels, flippedPixels.length, 0);

	    	int[] bandOffsets = {0,1,2,3};
	    	SampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, contentPane.m_GraphControl.getWidth(), contentPane.m_GraphControl.getHeight(), 4, w * 4, bandOffsets);
	    	WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf, null);
	    	ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
	    					 new int[] {8,8,8,8},
	    					 true,
	    					 false,
	    					 ComponentColorModel.OPAQUE,
	    					 DataBuffer.TYPE_BYTE);
	    	image = new BufferedImage(colorModel, raster, false, null);
	        
	        new javax.swing.ImageIcon(image); // Force load.
	        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	        newImage.createGraphics().drawImage(image, 0, 0, null);
	        image = newImage;
	        
	        ImageSelection imageSelection = new ImageSelection(image);
	        Toolkit toolkit = Toolkit.getDefaultToolkit();
	        toolkit.getSystemClipboard().setContents(imageSelection, null);
	    }
	    else if (evt.getSource() == this.menuResetToDefaultValuesAction)
		{
			if (this.m_bDocumentChangedFlag)
			{
				String fileName;
				if (this.m_currentFile == null)
					fileName = "Untitled";
				else
					fileName = m_currentFile.getName();
				
		        int result = JOptionPane.showConfirmDialog(this,"Do you want to save changes to " + fileName + "?", "HPLC Simulator", JOptionPane.YES_NO_CANCEL_OPTION);
		        
		        if (result == JOptionPane.YES_OPTION)
		        {
		        	if (!saveFile(false))
		        		return;
		        }
		        else if (result == JOptionPane.CANCEL_OPTION)
		        {
		        	return;
		        }
			}
			m_currentFile = null;
			
			loadFile(null);
		}
		else if (evt.getSource() == this.menuLoadSettingsAction)
		{
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("HPLC Simulator Files (*.hplcsim)", "hplcsim");
			fc.setFileFilter(filter);
			fc.setDialogTitle("Open");
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_currentFile = fc.getSelectedFile();

                loadFile(m_currentFile);
            }
		}
		else if (evt.getSource() == this.menuSaveSettingsAction)
		{
			saveFile(false);
		}
		else if (evt.getSource() == this.menuSaveSettingsAsAction)
		{
			saveFile(true);
		}
		else if (evt.getSource() == this.menuExitAction)
		{
			if (this.m_bDocumentChangedFlag)
			{
				String fileName;
				if (this.m_currentFile == null)
					fileName = "Untitled";
				else
					fileName = m_currentFile.getName();
				
		        int result = JOptionPane.showConfirmDialog(this,"Do you want to save changes to " + fileName + "?", "HPLC Simulator", JOptionPane.YES_NO_CANCEL_OPTION);
		        
		        if (result == JOptionPane.YES_OPTION)
		        {
		        	if (!saveFile(false))
		        		return;
		        }
		        else if (result == JOptionPane.CANCEL_OPTION)
		        {
		        	return;
		        }
			}
			
			this.setVisible(false);
			System.exit(0); 
		}
		else if (evt.getSource() == this.menuAboutAction)
		{
			Frame[] frames = Frame.getFrames();
			AboutDialog aboutDialog = new AboutDialog(frames[0]);
	    	Point dialogPosition = new Point(this.getSize().width / 2, this.getSize().height / 2);
	    	dialogPosition.x -= aboutDialog.getWidth() / 2;
	    	dialogPosition.y -= aboutDialog.getHeight() / 2;
	    	aboutDialog.setLocation(dialogPosition);
	    	
	    	// Show the dialog.
	    	aboutDialog.setVisible(true);

		}
		else if (evt.getSource() == this.menuHelpTopicsAction)
		{
			Globals.hbMainHelpBroker.setCurrentID("getting_started");
			Globals.hbMainHelpBroker.setDisplayed(true);
		}
	}

	//@Override
	public void stateChanged(ChangeEvent e) 
	{
		JSlider source = (JSlider)e.getSource();
		if (source.getName() == "Temperature Slider")
		{
			if (m_bSliderUpdate == false)
			{
				m_bSliderUpdate = true;
				return;
			}
			
			m_dTemperature = contentPane.jxpanelChromatographyProperties.jsliderTemp.getValue();
			contentPane.jxpanelChromatographyProperties.jtxtTemp.setText(Integer.toString((int)m_dTemperature));
			performCalculations();
		}
		else if (source.getName() == "Solvent B Slider")
		{
			if (m_bSliderUpdate == false)
			{
				m_bSliderUpdate = true;
				return;
			}
			
			m_dSolventBFraction = ((double)contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.getValue() / (double)100);
			contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText(Integer.toString((int)(m_dSolventBFraction * 100)));		
			performCalculations();
		}
	}

	//@Override
	public void keyPressed(KeyEvent arg0) 
	{
		
	}

	//@Override
	public void keyReleased(KeyEvent e) 
	{

	}

	//@Override
	public void keyTyped(KeyEvent e) 
	{
		//JTextField source = (JTextField)e.getSource();
		
		if (!((Character.isDigit(e.getKeyChar()) ||
				(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
				(e.getKeyChar() == KeyEvent.VK_DELETE) ||
				(e.getKeyChar() == KeyEvent.VK_PERIOD))))
		{
	        e.consume();
		}
		
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			performCalculations();
		}
		
	}

	public void performCalculations()
	{
		NumberFormat formatter = new DecimalFormat("#0.0000");
		
		validateTemp();
		validateSolventBFraction();
		validateMixingVolume();
		validateNonMixingVolume();
		validateColumnLength();
		validateColumnDiameter();
		validateInterparticlePorosity();
		validateIntraparticlePorosity();
		validateFlowRate();
		validateParticleSize();
		validateATerm();
		validateBTerm();
		validateCTerm();
		validateInjectionVolume();
		validateTimeConstant();
		validateNoise();
		validateSignalOffset();
		validateStartTime();
		validateEndTime();
		validateNumPoints();
		validateTubingDiameter();
		validateTubingLength();
		
		m_dTotalPorosity = this.m_dInterparticlePorosity + this.m_dIntraparticlePorosity * (1 - this.m_dInterparticlePorosity);
		contentPane.jxpanelColumnProperties.jlblTotalPorosityOut.setText(formatter.format(m_dTotalPorosity));
		
		m_dVoidVolume = Math.PI * Math.pow(((m_dColumnDiameter / 10) / 2), 2) * (m_dColumnLength / 10) * m_dTotalPorosity;
		contentPane.jxpanelColumnProperties.jlblVoidVolume.setText(formatter.format(m_dVoidVolume));
		
		m_dVoidTime = (m_dVoidVolume / m_dFlowRate) * 60;
		contentPane.jxpanelColumnProperties.jlblVoidTime.setText(formatter.format(m_dVoidTime));
		
		m_dOpenTubeVelocity = (m_dFlowRate / 60) / (Math.PI * Math.pow(((m_dColumnDiameter / 10) / 2), 2));
		contentPane.jxpanelChromatographyProperties.jlblOpenTubeVelocity.setText(formatter.format(m_dOpenTubeVelocity));

		m_dInterstitialVelocity = m_dOpenTubeVelocity / this.m_dInterparticlePorosity;
		contentPane.jxpanelChromatographyProperties.jlblInterstitialVelocity.setText(formatter.format(m_dInterstitialVelocity));
		
		m_dChromatographicVelocity = m_dOpenTubeVelocity / this.m_dTotalPorosity;
		contentPane.jxpanelChromatographyProperties.jlblChromatographicVelocity.setText(formatter.format(m_dChromatographicVelocity));
			
		NumberFormat bpFormatter = new DecimalFormat("#0.00");
		contentPane.jxpanelGradientOptions.jlblDwellVolumeIndicator.setText(bpFormatter.format(m_dMixingVolume + m_dNonMixingVolume));
		contentPane.jxpanelGradientOptions.jlblDwellTimeIndicator.setText(bpFormatter.format(((m_dMixingVolume + m_dNonMixingVolume) / 1000) / m_dFlowRate));

		double dTempKelvin = m_dTemperature + 273.15;

		// Calculate eluent viscosity
		if (m_iSolventB == 0)
		{
			// This formula is for acetonitrile/water mixtures:
			// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
			m_dEluentViscosity = Math.exp((m_dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - m_dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (m_dSolventBFraction * (1 - m_dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
		}
		else if (m_iSolventB == 1)
		{
			// This formula is for methanol/water mixtures:
			// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30???44.
			m_dEluentViscosity = Math.exp((m_dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - m_dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (m_dSolventBFraction * (1 - m_dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
		}
		if (!m_bGradientMode)
			contentPane.jxpanelGeneralProperties.jlblEluentViscosity.setText(formatter.format(m_dEluentViscosity));
		else
			contentPane.jxpanelGeneralProperties.jlblEluentViscosity.setText("--");
			
		/*m_dBackpressure = 500 * (m_dEluentViscosity / 1000) * (((m_dOpenTubeVelocity / 100) * (m_dColumnLength / 1000)) / Math.pow(m_dParticleSize / 1000000, 2));
		if (!m_bGradientMode)
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText(bpFormatter.format(m_dBackpressure / 100000));
		else
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText("--");
			*/
		
		// Calculate backpressure (in pascals) (Darcy equation)
		// See Thompson, J. D.; Carr, P. W. Anal. Chem. 2002, 74, 4150-4159.
		// Backpressure in units of Pa
		m_dBackpressure = ((this.m_dOpenTubeVelocity / 100.0) * (this.m_dColumnLength / 1000.0) * (m_dEluentViscosity / 1000.0) * 180.0 * Math.pow(1 - this.m_dInterparticlePorosity, 2)) / (Math.pow(this.m_dInterparticlePorosity, 3) * Math.pow(m_dParticleSize / 1000000, 2));
		if (!m_bGradientMode)
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText(bpFormatter.format(m_dBackpressure / 100000));
		else
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText("--");
		
		// Calculate the average diffusion coefficient using Wilke-Chang empirical determination
		// See Wilke, C. R.; Chang, P. AICHE J. 1955, 1, 264-270.
		
		// First, determine association parameter
		double dAssociationParameter = ((1 - m_dSolventBFraction) * (2.6 - 1.9)) + 1.9;
		
		// Determine weighted average molecular weight of solvent
		double dSolventBMW;
		if (this.m_iSolventB == 0)
			dSolventBMW = 41;
		else
			dSolventBMW = 32;
		
		double dSolventMW = (m_dSolventBFraction * (dSolventBMW - 18)) + 18;
		
		// Determine the average molar volume
		double dAverageMolarVolume = 0;
		for (int i = 0; i < m_vectCompound.size(); i++)
		{
			dAverageMolarVolume += m_vectCompound.get(i).dMolarVolume;
		}
		dAverageMolarVolume = dAverageMolarVolume / m_vectCompound.size();
		
		// Now determine the average diffusion coefficient
		m_dDiffusionCoefficient = 0.000000074 * (Math.pow(dAssociationParameter * dSolventMW, 0.5) * dTempKelvin) / (m_dEluentViscosity * Math.pow(dAverageMolarVolume, 0.6));
		DecimalFormat df = new DecimalFormat("0.000E0");
		contentPane.jxpanelGeneralProperties.jlblDiffusionCoefficient.setText(df.format(m_dDiffusionCoefficient));
		
		// Determine the reduced flow velocity
		m_dReducedFlowVelocity = ((m_dParticleSize / 10000) * m_dInterstitialVelocity) / m_dDiffusionCoefficient;
		contentPane.jxpanelChromatographyProperties.jlblReducedVelocity.setText(formatter.format(m_dReducedFlowVelocity));
		
		// Calculate reduced plate height
		m_dReducedPlateHeight = m_dATerm + (m_dBTerm / m_dReducedFlowVelocity) + (m_dCTerm * m_dReducedFlowVelocity);
		contentPane.jxpanelColumnProperties.jlblReducedPlateHeight.setText(formatter.format(m_dReducedPlateHeight));
    	
		// Calculate HETP
		m_dHETP = (m_dParticleSize / 10000) * m_dReducedPlateHeight;
		contentPane.jxpanelChromatographyProperties.jlblHETP.setText(df.format(m_dHETP));
		
		// Calculate number of theoretical plates
		NumberFormat NFormatter = new DecimalFormat("#0");
		m_dTheoreticalPlates = (m_dColumnLength / 10) / m_dHETP;
		contentPane.jxpanelChromatographyProperties.jlblTheoreticalPlates.setText(NFormatter.format(m_dTheoreticalPlates));

		// Calculate post-column tubing volume
		double dTubingVolume = (this.m_dTubingLength / 100) * (Math.PI * Math.pow((this.m_dTubingDiameter * 0.0000254) / 2, 2) * 1000000000);
		this.contentPane.jxpanelExtraColumnTubing.jlblTubingVolume.setText(formatter.format(dTubingVolume));

		// Get extra-column tubing radius in units of cm
		double dTubingRadius = (this.m_dTubingDiameter * 0.00254) / 2;

		// Open tube velocity in cm/s
		double dTubingOpenTubeVelocity = (m_dFlowRate / 60) / (Math.PI * Math.pow(dTubingRadius, 2));

		// Calculate dispersion that will result from extra-column tubing
		// in cm^2
		double dTubingZBroadening = (2 * m_dDiffusionCoefficient * this.m_dTubingLength / dTubingOpenTubeVelocity) + ((Math.pow(dTubingRadius, 2) * m_dTubingLength * dTubingOpenTubeVelocity) / (24 * m_dDiffusionCoefficient));
		
		// convert to mL^2
		double dTubingVolumeBroadening = Math.pow(Math.sqrt(dTubingZBroadening) * Math.PI * Math.pow(dTubingRadius, 2), 2);
		
		// convert to s^2
		double dTubingTimeBroadening = Math.pow((Math.sqrt(dTubingVolumeBroadening) / m_dFlowRate) * 60, 2);
		
		// Calculate retention factors
		int iNumCompounds = m_vectCompound.size();
		
		if (this.m_bGradientMode)
		{
	    	// Calculate the time period we're going to be looking at:
	    	if (contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.isSelected() == true)
	    	{
				m_dStartTime = 0;
				int iLastRow = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() - 1;
				m_dEndTime = ((Double)contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iLastRow, 0)) * 60;
				m_dEndTime += (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) * 60;
		    	contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(Float.toString((float)m_dEndTime));
		    	contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText("0");
			}
	    	
			calculateGradient();
			
	    	// Scale dtstep correctly for long and short runs - use the total gradient time as a reference
			double dtstep = (m_dEndTime - m_dStartTime) / 1000;
			this.m_vectRetentionFactorArray = new Vector<double[]>();
			this.m_vectPositionArray = new Vector<double[]>();
			
			for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
			{
				double dIntegral = 0;
				double dtRFinal = 0;
				double dD = 0;
				double dTotalTime = 0;
				double dTotalDeadTime = 0;
				double dXPosition = 0;
				double[] dLastXPosition = {0,0};
				double[] dLastko = {0,0};
				double dXMovement = 0;
				Boolean bIsEluted = false;
				double dPhiC = 0;
				double dCurVal = 0;
				boolean bRecordRetentionFactor = false;
				boolean bRecordPosition = false;
				
				if (contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex() == iCompound)
					bRecordRetentionFactor = true;
				
				if (contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex() == iCompound)
					bRecordPosition = true;
				
		    	// Calculate logk'w1
		    	double logkprimew1 = (m_vectCompound.get(iCompound).dLogkwvsTSlope * this.m_dTemperature) + m_vectCompound.get(iCompound).dLogkwvsTIntercept;
		    	// Calculate S1
		    	double S1 = -1 * ((m_vectCompound.get(iCompound).dSvsTSlope * this.m_dTemperature) + m_vectCompound.get(iCompound).dSvsTIntercept);
				
				double t = 0;
		    	double kprime = 1;
		    	
				while (bIsEluted == false)// (double t = 0; t <= (Double) m_vectCompound.get(m_vectCompound.size() - 1)[1] * 1.5; t += dtstep)
				{
					t += dtstep;
					dPhiC = this.m_lifGradient.getAt((dTotalTime - dIntegral) / 60) / 100;
					// Calculate k'
			    	kprime = Math.pow(10, logkprimew1 - (S1 * dPhiC));
					dCurVal = dtstep / kprime;
					double dt0 = m_dVoidTime;
					dXMovement = dCurVal / dt0;

					if (bRecordRetentionFactor)
					{
						double[] temp = {dTotalTime,kprime};
						m_vectRetentionFactorArray.add(temp);
					}

					if (bRecordPosition)
					{
						double[] temp = {dTotalTime,dXPosition * m_dColumnLength};
						m_vectPositionArray.add(temp);
					}

					if (dXPosition >= 1)
					{
						dD = ((1 - dLastXPosition[0])/(dXPosition - dLastXPosition[0])) * (dTotalDeadTime - dLastXPosition[1]) + dLastXPosition[1]; 
					}
					else
					{
						dLastXPosition[0] = dXPosition;
						dLastXPosition[1] = dTotalDeadTime;
					}
					
					dTotalDeadTime += dXMovement * dt0;
					
					if (dXPosition >= 1)
					{
						dtRFinal = ((dD - dLastko[0])/(dIntegral - dLastko[0]))*(dTotalTime - dLastko[1]) + dLastko[1];
					}
					else
					{
						dLastko[0] = dIntegral;
						dLastko[1] = dTotalTime;
					}
					
					dTotalTime += dtstep + dCurVal;
					dIntegral += dCurVal;
										
					if (dXPosition > 1 && bIsEluted == false)
					{
						bIsEluted = true;
						break;
					}
					
					dXPosition += dXMovement;
				}

				contentPane.vectChemicalRows.get(iCompound).set(2, "--");
				
		    	double dRetentionTime = dtRFinal;
		    	m_vectCompound.get(iCompound).dRetentionTime = dRetentionTime;
		    	contentPane.vectChemicalRows.get(iCompound).set(3, formatter.format(dRetentionTime));
		    	
		    	// TODO: The following equation does not account for peak broadening due to injection volume.
		    	// Use the final value of k to determine the peak width.
		    	double dSigma = Math.sqrt(Math.pow((m_dVoidTime * (1 + kprime)) / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2)/* + Math.pow(0.017 * m_dInjectionVolume / m_dFlowRate, 2)*/ + dTubingTimeBroadening);
		    	m_vectCompound.get(iCompound).dSigma = dSigma;	    	
		    	contentPane.vectChemicalRows.get(iCompound).set(4, formatter.format(dSigma));
		    	
		    	double dW = (m_dInjectionVolume / 1000000) * m_vectCompound.get(iCompound).dConcentration;
		    	m_vectCompound.get(iCompound).dW = dW;
		    	contentPane.vectChemicalRows.get(iCompound).set(5, formatter.format(dW * 1000000));		    	
			}
		}
		else
		{
			// Isocratic mode
			
			// Make sure the table is initialized
			if (contentPane.vectChemicalRows.size() == iNumCompounds)
			{
				for (int i = 0; i < iNumCompounds; i++)
				{
			    	// Calculate logk'w1
			    	double logkprimew1 = (m_vectCompound.get(i).dLogkwvsTSlope * this.m_dTemperature) + m_vectCompound.get(i).dLogkwvsTIntercept;
			    	// Calculate S1
			    	double S1 = -1 * ((m_vectCompound.get(i).dSvsTSlope * this.m_dTemperature) + m_vectCompound.get(i).dSvsTIntercept);
					// Calculate k'
			    	double kprime = Math.pow(10, logkprimew1 - (S1 * this.m_dSolventBFraction));
			    	contentPane.vectChemicalRows.get(i).set(2, formatter.format(kprime));
			    	
			    	if (contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex() == i)
			    	{
			    		m_dSelectedIsocraticRetentionFactor = kprime;	
			    	}
			    	
			    	// In seconds
			    	double dRetentionTime = m_dVoidTime * (1 + kprime);
			    	m_vectCompound.get(i).dRetentionTime = dRetentionTime;
			    	contentPane.vectChemicalRows.get(i).set(3, formatter.format(dRetentionTime));
			    	
			    	// 9/22/11 - Peak broadening due to sample injection volume is underestimated.
			    	//double dSigma = Math.sqrt(Math.pow(dRetentionTime / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2) + Math.pow(0.017 * m_dInjectionVolume / m_dFlowRate, 2));
			    	double dSigma = Math.sqrt(Math.pow(dRetentionTime / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2) + (1.0/12.0) * Math.pow((m_dInjectionVolume / 1000.0) / (m_dFlowRate / 60.0), 2) + dTubingTimeBroadening);
			    	m_vectCompound.get(i).dSigma = dSigma;	    	
			    	contentPane.vectChemicalRows.get(i).set(4, formatter.format(dSigma));
			    	
			    	double dW = (m_dInjectionVolume / 1000000) * m_vectCompound.get(i).dConcentration;
			    	m_vectCompound.get(i).dW = dW;
			    	contentPane.vectChemicalRows.get(i).set(5, formatter.format(dW * 1000000));
				}
			}
		}

    	// Now calculate the time period we're going to be looking at:
    	if (contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.isSelected() == true)
    	{
	    	// Find the compound with the longest tR
	    	double dLongestRetentionTime = 0;
	    	
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		if (m_vectCompound.get(i).dRetentionTime > dLongestRetentionTime)
	    		{
	    			dLongestRetentionTime = m_vectCompound.get(i).dRetentionTime;
	    		}
	    	}
	    	
	    	m_dEndTime = dLongestRetentionTime * 1.1;
	    	
	    	contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(Float.toString((float)m_dEndTime));
	    	
	    	contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText("0");
	    	
	    	m_dStartTime = 0;
    	}

    	// Clear the old chromatogram
    	contentPane.m_GraphControl.RemoveAllSeries();

		if (m_iSecondPlotType == 1)
			plotGradient();
		if (m_iSecondPlotType == 2 || m_iSecondPlotType == 3)
			plotViscosityOrBackpressure();
		if (m_iSecondPlotType == 4)
			plotRetentionFactor();
		if (m_iSecondPlotType == 5)
			plotPosition();

    	// Calculate each data point
    	Random random = new Random();
    	
    	//contentPane.m_GraphControl.RemoveSeries(m_iChromatogramPlotIndex);
    	m_iChromatogramPlotIndex = -1;
    	
    	// Clear the single plot if it exists (the red plot that shows up if you click on a compound)
    	//contentPane.m_GraphControl.RemoveSeries(m_iSinglePlotIndex);
    	m_iSinglePlotIndex = -1;
    	
    	if (this.m_vectCompound.size() > 0)
    	{
    		m_iChromatogramPlotIndex = contentPane.m_GraphControl.AddSeries("Chromatogram", new Color(98, 101, 214), 1, false, false);
	    	// Find if a chemical is selected
	    	int iRowSel = contentPane.jtableChemicals.getSelectedRow();
	    	if (iRowSel >= 0 && iRowSel < contentPane.vectChemicalRows.size())
	    	{
	    		m_iSinglePlotIndex = contentPane.m_GraphControl.AddSeries("Single", new Color(206, 70, 70), 1, false, false);	    		
	    	}
	    	
	    	for (int i = 0; i < this.m_iNumPoints; i++)
	    	{
	    		double dTime = m_dStartTime + (double)i * ((m_dEndTime - m_dStartTime) / (double)this.m_iNumPoints);
	    		double dNoise = random.nextGaussian() * (m_dNoise / 1000000000);
	    		double dCTotal = (dNoise / Math.sqrt(m_dTimeConstant)) + m_dSignalOffset;
	    		
	    		// Add the contribution from each compound to the peak
	    		for (int j = 0; j < m_vectCompound.size(); j++)
	    		{
	    			Compound curCompound = m_vectCompound.get(j);
	    			//double dCthis = ((curCompound.dW / 1000000) / (curCompound.dSigma * (m_dFlowRate / (60 * 1000)))) * Math.exp(-0.5*Math.pow((dTime - curCompound.dRetentionTime) / (curCompound.dSigma), 2));
	    			double dCthis = ((curCompound.dW / 1000000) / (Math.sqrt(2 * Math.PI) * curCompound.dSigma * (m_dFlowRate / (60 * 1000)))) * Math.exp(-Math.pow(dTime - curCompound.dRetentionTime, 2) / (2 * Math.pow(curCompound.dSigma, 2)));
	    			dCTotal += dCthis;
	    			
	    			// If a compound is selected, then show it in a different color and without noise.
	    			if (m_iSinglePlotIndex >= 0 && j == iRowSel)
	    		    	contentPane.m_GraphControl.AddDataPoint(m_iSinglePlotIndex, dTime, (dCthis + m_dSignalOffset));
	    		}
	    		
		    	contentPane.m_GraphControl.AddDataPoint(m_iChromatogramPlotIndex, dTime, dCTotal);
	    	}
	    	
	    	if (contentPane.jbtnAutoscaleX.isSelected() == true)
	    		contentPane.m_GraphControl.AutoScaleX();	//AutoScaleToSeries(iTotalPlotIndex);
	    	if (contentPane.jbtnAutoscaleY.isSelected() == true)
	    		contentPane.m_GraphControl.AutoScaleY();
    	}
    	
    	contentPane.m_GraphControl.repaint();   	
    	contentPane.jtableChemicals.addNotify();
	}

	public void plotViscosityOrBackpressure()
	{
		// TODO: Considers only viscosity of solvent entering column, not the average viscosity of all solvent in the column
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
		
		double dViscosityMin = 999999999;
		double dViscosityMax = 0;
		
		int iNumPoints = m_dGradientArray.length;
		
		double dTempKelvin = m_dTemperature + 273.15;
		if (this.m_bGradientMode)
		{
			double dFinalValue = 0;
			
			//double[][] dPoints = new double[iNumPoints][2];
			
			for (int i = 0; i < iNumPoints; i++)
			{
				double dViscosity = 0;
				double dSolventBFraction = m_dGradientArray[i][1] / 100;
				// Calculate eluent viscosity
				if (m_iSolventB == 0)
				{
					// This formula is for acetonitrile/water mixtures:
					// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
					dViscosity = Math.exp((dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
				}
				else if (m_iSolventB == 1)
				{
					// This formula is for methanol/water mixtures:
					// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30???44.
					dViscosity = Math.exp((dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
				}
				
				if (dViscosity < dViscosityMin)
					dViscosityMin = dViscosity;
				if (dViscosity > dViscosityMax)
					dViscosityMax = dViscosity;
				
				if (this.m_iSecondPlotType == 2)
				{
					// Calculate backpressure (in pascals) (Darcy equation)
					// See Thompson, J. D.; Carr, P. W. Anal. Chem. 2002, 74, 4150-4159.
					// Backpressure in units of Pa
					double dBackpressure = ((this.m_dOpenTubeVelocity / 100.0) * (this.m_dColumnLength / 1000.0) * (dViscosity / 1000.0) * 180.0 * Math.pow(1 - this.m_dInterparticlePorosity, 2)) / (Math.pow(this.m_dInterparticlePorosity, 3) * Math.pow(m_dParticleSize / 1000000, 2));
				    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, dBackpressure / 100000);
				    //dPoints[i][0] = m_dGradientArray[i][0] * 60;
				    //dPoints[i][1] = dBackpressure / 100000;
				    dFinalValue = dBackpressure / 100000;
				}
				else if (this.m_iSecondPlotType == 3)
				{
				    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, dViscosity / 100);
				    dFinalValue = dViscosity / 100;
				}
			}
			
		    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, 9999999999d, dFinalValue);

			if (this.m_iSecondPlotType == 2)
			{
				// Calculate backpressure (in pascals) (Darcy equation)
				// See Bird, R. B.; Stewart, W. E.; Lightfoot, E. N. Transport Phenomena; Wiley & Sons: New York, 1960.
				double dBackpressureMin = ((this.m_dOpenTubeVelocity / 100.0) * (this.m_dColumnLength / 1000.0) * (dViscosityMin / 1000.0) * 180.0 * Math.pow(1 - this.m_dInterparticlePorosity, 2)) / (Math.pow(this.m_dInterparticlePorosity, 3) * Math.pow(m_dParticleSize / 1000000, 2));
				double dBackpressureMax = ((this.m_dOpenTubeVelocity / 100.0) * (this.m_dColumnLength / 1000.0) * (dViscosityMax / 1000.0) * 180.0 * Math.pow(1 - this.m_dInterparticlePorosity, 2)) / (Math.pow(this.m_dInterparticlePorosity, 3) * Math.pow(m_dParticleSize / 1000000, 2));
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dBackpressureMin / 100000, dBackpressureMax / 100000);
			}
			else if (this.m_iSecondPlotType == 3)
			{
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dViscosityMin / 100, dViscosityMax / 100);
			}	
		}
		else
		{
			// Isocratic Mode
			if (this.m_iSecondPlotType == 2)
			{
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dBackpressure / 100000);
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dBackpressure / 100000);
				double dBackPressureMin = m_dBackpressure - (m_dBackpressure * 0.2);
				double dBackPressureMax = m_dBackpressure + (m_dBackpressure * 0.2);
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dBackPressureMin / 100000, dBackPressureMax / 100000);
			}
			else if (this.m_iSecondPlotType == 3)
			{
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dEluentViscosity / 100);
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dEluentViscosity / 100);
		    	dViscosityMin = m_dEluentViscosity - (m_dEluentViscosity * 0.2);
		    	dViscosityMax = m_dEluentViscosity + (m_dEluentViscosity * 0.2);
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dViscosityMin / 100, dViscosityMax / 100);
			}

		}
	}
	
	//@Override
	public void focusGained(FocusEvent e) 
	{
		
	}

	//@Override
	public void focusLost(FocusEvent e) 
	{
		performCalculations();
	}

	//@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		performCalculations();
		
	}

	//@Override
	public void autoScaleChanged(AutoScaleEvent event) 
	{
		if (event.getAutoScaleXState() == true)
			contentPane.jbtnAutoscaleX.setSelected(true);
		else
			contentPane.jbtnAutoscaleX.setSelected(false);
		
		if (event.getAutoScaleYState() == true)
			contentPane.jbtnAutoscaleY.setSelected(true);
		else
			contentPane.jbtnAutoscaleY.setSelected(false);
		
		if (event.getAutoScaleXState() == true && event.getAutoScaleYState() == true)
			contentPane.jbtnAutoscale.setSelected(true);			
		else
			contentPane.jbtnAutoscale.setSelected(false);						
	}

	@Override
	public void tableChanged(TableModelEvent e) 
	{
		if (e.getSource() == contentPane.jxpanelGradientOptions.tmGradientProgram)
		{
			if (m_bDoNotChangeTable)
			{
				m_bDoNotChangeTable = false;
				return;
			}
			
			int iChangedRow = e.getFirstRow();
			int iChangedColumn = e.getColumn();

			Double dRowValue1 = 0.0;
			Double dRowValue2 = 0.0;
			
			if (iChangedRow < contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount())
			{
				dRowValue1 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow, 0);
				dRowValue2 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow, 1);
			}
			
	    	if (iChangedColumn == 0)
			{
				// If the column changed was the first, then make sure the time falls in the right range
				if (iChangedRow == 0)
				{
					// No changes allowed in first row - must be zero min
					dRowValue1 = 0.0;
				}
				else if (iChangedRow == contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() - 1)
				{
					Double dPreviousTime = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() - 2, 0);
					// If it's the last row, just make sure the time is greater than or equal to the time before it.
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
				}
				else
				{
					Double dPreviousTime = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow - 1, 0);
					Double dNextTime = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow + 1, 0);
					
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
					
					if (dRowValue1 > dNextTime)
						dRowValue1 = dNextTime;
				}
				
		    	m_bDoNotChangeTable = true;
		    	contentPane.jxpanelGradientOptions.tmGradientProgram.setValueAt(dRowValue1, iChangedRow, iChangedColumn);
			}
			else if (iChangedColumn == 1)
			{
				// If the column changed was the second, then make sure the solvent composition falls between 0 and 100
				if (dRowValue2 > 100)
					dRowValue2 = 100.0;
				
				if (dRowValue2 < 0)
					dRowValue2 = 0.0;
				
		    	m_bDoNotChangeTable = true;
		    	contentPane.jxpanelGradientOptions.tmGradientProgram.setValueAt(dRowValue2, iChangedRow, iChangedColumn);
			}
	    	
	    	performCalculations();
		}		
	}
	
	public void calculateGradient()
	{
		int iNumPoints = 1000;
		m_dGradientArray = new double[iNumPoints][2];
		int iGradientTableLength = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
		
		// Initialize the solvent mixer composition to that of the initial solvent composition
		double dMixerComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(0, 1)).doubleValue();
		//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
		double dFinalTime = this.m_dEndTime / 60;
		double dTimeStep = dFinalTime / (iNumPoints - 1);
		
		// Start at time 0
		double dTime = 0;
		
		for (int i = 0; i < iNumPoints; i++)
		{
			dTime = i * dTimeStep;
			
			m_dGradientArray[i][0] = dTime;
			m_dGradientArray[i][1] = dMixerComposition;
			
			//if (((m_dFlowRate * 1000) * dTimeStep) < m_dMixingVolume)
			//{
				double dSolventBInMixer = dMixerComposition * m_dMixingVolume;
							
				// Now push out a step's worth of volume from the mixer
				dSolventBInMixer -= ((m_dFlowRate * 1000) * dTimeStep) * dMixerComposition;
				
				// Now add a step's worth of new volume from the pump
				// First, find which two data points we are between
				// Find the last data point that isn't greater than our current time
				double dIncomingSolventComposition = 0;
				if (dTime < (m_dNonMixingVolume / 1000) / m_dFlowRate)
				{
					dIncomingSolventComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(0, 1)).doubleValue();
				}
				else
				{
					int iRowBefore = 0;
					for (int j = 0; j < iGradientTableLength; j++)
					{
						double dRowTime = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(j, 0)).doubleValue();
						if (dRowTime <= (dTime - ((m_dNonMixingVolume / 1000) / m_dFlowRate)))
							iRowBefore = j;
						else
							break;
					}
					
					// Now interpolate between the solvent composition at iRowBefore and the next row (if it exists)
					double dRowBeforeTime = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore, 0)).doubleValue();
					
					if (iRowBefore <= iGradientTableLength - 2)
					{
						double dRowAfterTime = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore + 1, 0)).doubleValue();
						double dPositionBetween = ((dTime - ((m_dNonMixingVolume / 1000) / m_dFlowRate)) - dRowBeforeTime) / (dRowAfterTime - dRowBeforeTime);
						double dRowBeforeComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore, 1)).doubleValue();
						double dRowAfterComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore + 1, 1)).doubleValue();
						dIncomingSolventComposition = (dPositionBetween * (dRowAfterComposition - dRowBeforeComposition)) + dRowBeforeComposition;
					}
					else
						dIncomingSolventComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore, 1)).doubleValue();
				}
				
				dSolventBInMixer += ((m_dFlowRate * 1000) * dTimeStep) * dIncomingSolventComposition;
				
				// Calculate the new solvent composition in the mixing volume
				if (((m_dFlowRate * 1000) * dTimeStep) < m_dMixingVolume)
					dMixerComposition = dSolventBInMixer / m_dMixingVolume;
				else
					dMixerComposition = dIncomingSolventComposition;
		}
		
		m_lifGradient = new LinearInterpolationFunction(m_dGradientArray);
	}
	
	public void plotGradient()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
    	
		if (this.m_bGradientMode)
		{
	    	for (int i = 0; i < m_dGradientArray.length; i++)
	    	{
		    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, m_dGradientArray[i][1]);
	    	}
	    	
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, 9999999999d, m_dGradientArray[m_dGradientArray.length - 1][1]);
		}
		else
		{
			// Isocratic mode
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, this.m_dStartTime, this.m_dSolventBFraction * 100);			
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, this.m_dEndTime, this.m_dSolventBFraction * 100);			
		}
	}
	
	public void plotRetentionFactor()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
		
		double dRetentionFactorMin = 999999999;
		double dRetentionFactorMax = 0;
   	
		if (this.m_bGradientMode)
		{
	    	for (int i = 0; i < this.m_vectRetentionFactorArray.size(); i++)
	    	{
	    		double dRetentionFactor = m_vectRetentionFactorArray.get(i)[1];
	    		
				if (dRetentionFactor < dRetentionFactorMin)
					dRetentionFactorMin = dRetentionFactor;
				if (dRetentionFactor > dRetentionFactorMax)
					dRetentionFactorMax = dRetentionFactor;
	
		    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_vectRetentionFactorArray.get(i)[0], m_vectRetentionFactorArray.get(i)[1]);
	    	}
	    	
		}
		else
		{
			// Isocratic Mode
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dSelectedIsocraticRetentionFactor);
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dSelectedIsocraticRetentionFactor);
	    	dRetentionFactorMin = m_dSelectedIsocraticRetentionFactor - (m_dSelectedIsocraticRetentionFactor * 0.5);
	    	dRetentionFactorMax = m_dSelectedIsocraticRetentionFactor + (m_dSelectedIsocraticRetentionFactor * 0.5);
		}
    	
		contentPane.m_GraphControl.setSecondYAxisRangeLimits(dRetentionFactorMin, dRetentionFactorMax);
	}
	
	public void plotPosition()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		

		if (this.m_bGradientMode)
		{
	    	for (int i = 0; i < m_vectPositionArray.size(); i++)
	    	{
	    		contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_vectPositionArray.get(i)[0], m_vectPositionArray.get(i)[1]);
	    	}
		}
		else
		{
			// Isocratic Mode
			int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex();
			if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
			{
				double dRetentionTime = m_vectCompound.get(iSelectedCompound).dRetentionTime;
			
				contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dStartTime * (m_dColumnLength / dRetentionTime));
				contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dEndTime * (m_dColumnLength / dRetentionTime));
			}
		}
    	
		contentPane.m_GraphControl.setSecondYAxisRangeLimits(0, m_dColumnLength);
	}
	
	public void updateCompoundComboBoxes()
	{
		int iNumCompounds = m_vectCompound.size();
		
		contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.removeAllItems();
		contentPane.jxpanelPlotOptions.jcboPositionCompounds.removeAllItems();
		
		for (int i = 0; i < iNumCompounds; i++)
		{
			contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.addItem(m_vectCompound.get(i).strCompoundName);
			contentPane.jxpanelPlotOptions.jcboPositionCompounds.addItem(m_vectCompound.get(i).strCompoundName);
		}
	}
}
