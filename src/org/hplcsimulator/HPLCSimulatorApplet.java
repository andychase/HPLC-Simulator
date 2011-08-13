package org.hplcsimulator;

import org.hplcsimulator.panels.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Vector;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.JApplet;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

class Compound
{
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

	static public int getCompoundNum()
	{
		return 22;
	}
	
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

public class HPLCSimulatorApplet extends JApplet implements ActionListener, ChangeListener, KeyListener, FocusListener, ListSelectionListener, AutoScaleListener, TableModelListener
{
	private static final long serialVersionUID = 1L;

	private boolean m_bSliderUpdate;

	TopPanel contentPane = null;
	public int m_iSecondPlotType = 0;
	public double m_dTemperature = 25;
	public boolean m_bGradientMode = false;
	public double m_dSolventBFraction = 0.5;
	public double m_dMixingVolume = 200; /* in uL */
	public double m_dNonMixingVolume = 200; /* in uL */
	public double m_dColumnLength = 100;
	public double m_dColumnDiameter = 4.6;
	public double m_dVoidFraction = 0.6;
	public double m_dFlowRate = 2; /* in mL/min */
	public double m_dVoidVolume;
	public double m_dVoidTime;
	public double m_dFlowVelocity;
	public double m_dParticleSize = 5;
	public double m_dDiffusionCoefficient = 0.00001;
	public double m_dATerm = 1;
	public double m_dBTerm = 5;
	public double m_dCTerm = 0.05;
	public double m_dMu;
	public double m_dReducedPlateHeight;
	public double m_dTheoreticalPlates;
	public double m_dHETP;
	public double m_dInjectionVolume = 5;
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
	public double[][] m_dRetentionFactorArray = null;
	public int m_iRetentionFactorArrayLength = 0;
	public double[][] m_dPositionArray = null;
	public int m_iPositionArrayLength = 0;
	public double m_dSelectedIsocraticRetentionFactor = 0;
	
	/**
	 * This is the xxx default constructor
	 */
	public HPLCSimulatorApplet() 
	{
	    super();
	    
		/*try {
	        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.nimbus");
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
	    }*/

		this.setPreferredSize(new Dimension(900, 650));
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
        try {
        //    SwingUtilities.invokeAndWait(new Runnable() 
        //    {
        //        public void run() {
                	createGUI();
        //        }
        //    });
        } catch (Exception e) { 
            System.err.println("createGUI didn't complete successfully");
            System.err.println(e.getMessage());
            System.err.println(e.getLocalizedMessage());
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
            System.err.println(e.getCause());
        }
        
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
        //Create and set up the content pane.
        contentPane = new TopPanel();
        contentPane.setOpaque(true); 
        setContentPane(contentPane);  
        
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
        contentPane.jxpanelColumnProperties.jtxtVoidFraction.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtVoidFraction.addFocusListener(this);        
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
        contentPane.m_GraphControl.addAutoScaleListener(this);
        contentPane.m_GraphControl.setSecondYAxisVisible(false);
        contentPane.m_GraphControl.setSecondYAxisRangeLimits(0, 100);
        contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.addActionListener(this);
        contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.addActionListener(this);
        contentPane.jxpanelGradientOptions.jbtnInsertRow.addActionListener(this);
        contentPane.jxpanelGradientOptions.jbtnRemoveRow.addActionListener(this);
        contentPane.jbtnContextHelp.addActionListener(new CSH.DisplayHelpAfterTracking(Globals.hbMainHelpBroker));
        contentPane.jxpanelGradientOptions.tmGradientProgram.addTableModelListener(this);
    }

    private void validateTemp()
    {
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
		double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtColumnDiameter.getText());
		
		if (dTemp < .001)
			dTemp = .001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dColumnDiameter = dTemp;
		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText(Float.toString((float)m_dColumnDiameter));    	
    }    

    private void validateVoidFraction()
    {
		double dTemp = (double)Float.valueOf(contentPane.jxpanelColumnProperties.jtxtVoidFraction.getText());
		
		if (dTemp < .001)
			dTemp = .001;
		if (dTemp > .999)
			dTemp = .999;
		
		this.m_dVoidFraction = dTemp;
		contentPane.jxpanelColumnProperties.jtxtVoidFraction.setText(Float.toString((float)m_dVoidFraction));    	
    }    

    private void validateFlowRate()
    {
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
		int iTemp = Integer.valueOf(contentPane.jxpanelGeneralProperties.jtxtNumPoints.getText());
		
		if (iTemp < 2)
			iTemp = 2;
		if (iTemp > 10000000)
			iTemp = 10000000;
		
		this.m_iNumPoints = iTemp;
		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText(Integer.toString(m_iNumPoints));    	
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
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("poise", "P");
	    	
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
		validateVoidFraction();
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
		
		m_dVoidVolume = Math.PI * Math.pow(((m_dColumnDiameter / 10) / 2), 2) * (m_dColumnLength / 10) * m_dVoidFraction;
		contentPane.jxpanelColumnProperties.jlblVoidVolume.setText(formatter.format(m_dVoidVolume));
		
		m_dVoidTime = (m_dVoidVolume / m_dFlowRate) * 60;
		contentPane.jxpanelColumnProperties.jlblVoidTime.setText(formatter.format(m_dVoidTime));
		
		m_dFlowVelocity = (m_dColumnLength / 10) / m_dVoidTime;
		contentPane.jxpanelChromatographyProperties.jlblFlowVelocity.setText(formatter.format(m_dFlowVelocity));
		
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
			// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30�44.
			m_dEluentViscosity = Math.exp((m_dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - m_dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (m_dSolventBFraction * (1 - m_dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
		}
		if (!m_bGradientMode)
			contentPane.jxpanelGeneralProperties.jlblEluentViscosity.setText(formatter.format(m_dEluentViscosity));
		else
			contentPane.jxpanelGeneralProperties.jlblEluentViscosity.setText("--");
			
		// Calculate backpressure (in pascals) (Darcy equation)
		// See Bird, R. B.; Stewart, W. E.; Lightfoot, E. N. Transport Phenomena; Wiley & Sons: New York, 1960.
		m_dBackpressure = 500 * (m_dEluentViscosity / 1000) * (((m_dFlowVelocity / 100) * (m_dColumnLength / 1000)) / Math.pow(m_dParticleSize / 1000000, 2));
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
		
		m_dMu = ((m_dParticleSize / 10000) * m_dFlowVelocity) / m_dDiffusionCoefficient;
		
		m_dReducedPlateHeight = m_dATerm + (m_dBTerm / m_dMu) + (m_dCTerm * m_dMu);
		contentPane.jxpanelColumnProperties.jlblReducedPlateHeight.setText(formatter.format(m_dReducedPlateHeight));
    	
		m_dHETP = (m_dParticleSize / 10000) * m_dReducedPlateHeight;
		contentPane.jxpanelChromatographyProperties.jlblHETP.setText(df.format(m_dHETP));
		
		NumberFormat NFormatter = new DecimalFormat("#0");
		m_dTheoreticalPlates = (m_dColumnLength / 10) / m_dHETP;
		contentPane.jxpanelChromatographyProperties.jlblTheoreticalPlates.setText(NFormatter.format(m_dTheoreticalPlates));

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
			m_dRetentionFactorArray = new double[1000][2];
			m_iRetentionFactorArrayLength = 0;
			m_dPositionArray = new double[1000][2];
			m_iPositionArrayLength = 0;
			
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
						m_dRetentionFactorArray[m_iRetentionFactorArrayLength][0] = dTotalTime;
						m_dRetentionFactorArray[m_iRetentionFactorArrayLength][1] = kprime;
						m_iRetentionFactorArrayLength++;
					}

					if (bRecordPosition)
					{
						m_dPositionArray[m_iPositionArrayLength][0] = dTotalTime;
						m_dPositionArray[m_iPositionArrayLength][1] = dXPosition * m_dColumnLength;
						m_iPositionArrayLength++;
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
		    	double dSigma = Math.sqrt(Math.pow((m_dVoidTime * (1 + kprime)) / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2)/* + Math.pow(0.017 * m_dInjectionVolume / m_dFlowRate, 2)*/);
		    	m_vectCompound.get(iCompound).dSigma = dSigma;	    	
		    	contentPane.vectChemicalRows.get(iCompound).set(4, formatter.format(dSigma));
		    	
		    	double dW = m_dInjectionVolume * m_vectCompound.get(iCompound).dConcentration;
		    	m_vectCompound.get(iCompound).dW = dW;
		    	contentPane.vectChemicalRows.get(iCompound).set(5, formatter.format(dW));		    	
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
			    	// Calculate lnk'w1
			    	double lnkprimew1 = (m_vectCompound.get(i).dLogkwvsTSlope * this.m_dTemperature) + m_vectCompound.get(i).dLogkwvsTIntercept;
			    	// Calculate S1
			    	double S1 = -1 * ((m_vectCompound.get(i).dSvsTSlope * this.m_dTemperature) + m_vectCompound.get(i).dSvsTIntercept);
					// Calculate k'
			    	double kprime = Math.pow(10, lnkprimew1 - (S1 * this.m_dSolventBFraction));
			    	contentPane.vectChemicalRows.get(i).set(2, formatter.format(kprime));
			    	
			    	if (contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex() == i)
			    	{
			    		m_dSelectedIsocraticRetentionFactor = kprime;	
			    	}
			    	
			    	double dRetentionTime = m_dVoidTime * (1 + kprime);
			    	m_vectCompound.get(i).dRetentionTime = dRetentionTime;
			    	contentPane.vectChemicalRows.get(i).set(3, formatter.format(dRetentionTime));
			    	
			    	double dSigma = Math.sqrt(Math.pow(dRetentionTime / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2) + Math.pow(0.017 * m_dInjectionVolume / m_dFlowRate, 2));
			    	m_vectCompound.get(i).dSigma = dSigma;	    	
			    	contentPane.vectChemicalRows.get(i).set(4, formatter.format(dSigma));
			    	
			    	double dW = m_dInjectionVolume * m_vectCompound.get(i).dConcentration;
			    	m_vectCompound.get(i).dW = dW;
			    	contentPane.vectChemicalRows.get(i).set(5, formatter.format(dW));
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
		}
    	
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
    	
    	// Clear the old chromatogram
    	contentPane.m_GraphControl.RemoveSeries(m_iChromatogramPlotIndex);
    	m_iChromatogramPlotIndex = -1;
    	
    	// Clear the single plot if it exists (the red plot that shows up if you click on a compound)
    	contentPane.m_GraphControl.RemoveSeries(m_iSinglePlotIndex);
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
	    		double dNoise = random.nextGaussian() * (m_dNoise / 1000);
	    		double dCTotal = (dNoise / Math.sqrt(m_dTimeConstant)) + m_dSignalOffset;
	    		
	    		// Add the contribution from each compound to the peak
	    		for (int j = 0; j < m_vectCompound.size(); j++)
	    		{
	    			Compound curCompound = m_vectCompound.get(j);
	    			double dCthis = ((curCompound.dW / 1000000) / (curCompound.dSigma * (m_dFlowRate / (60 * 1000)))) * Math.exp(-(0.5 * Math.pow((dTime - curCompound.dRetentionTime) / curCompound.dSigma, 2)));
	    			dCTotal += dCthis;
	    			
	    			// If a compound is selected, then show it in a different color and without noise.
	    			if (m_iSinglePlotIndex >= 0 && j == iRowSel)
	    		    	contentPane.m_GraphControl.AddDataPoint(m_iSinglePlotIndex, dTime, (dCthis + m_dSignalOffset) / 1000);
	    		}
	    		
		    	contentPane.m_GraphControl.AddDataPoint(m_iChromatogramPlotIndex, dTime, dCTotal / 1000);
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
			for (int i = 0; i < iNumPoints; i++)
			{
				double dViscosity = 0;
				double dSolventBFraction = m_dGradientArray[i][1] / 100;
				// Calculate eluent viscosity
				if (m_iSolventB == 0)
				{
					// This formula is for acetonitrile/water mixtures:
					// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
					dViscosity = .01 * Math.exp((dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
				}
				else if (m_iSolventB == 1)
				{
					// This formula is for methanol/water mixtures:
					// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30�44.
					dViscosity = .01 * Math.exp((dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
				}
				
				if (dViscosity < dViscosityMin)
					dViscosityMin = dViscosity;
				if (dViscosity > dViscosityMax)
					dViscosityMax = dViscosity;
				
				if (this.m_iSecondPlotType == 2)
				{
					// Calculate backpressure (in pascals) (Darcy equation)
					// See Bird, R. B.; Stewart, W. E.; Lightfoot, E. N. Transport Phenomena; Wiley & Sons: New York, 1960.
					double dBackpressure = 500 * (dViscosity / 10) * (((m_dFlowVelocity / 100) * (m_dColumnLength / 1000)) / Math.pow(m_dParticleSize / 1000000, 2));
				    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, dBackpressure / 100000);
				}
				else if (this.m_iSecondPlotType == 3)
				{
				    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, dViscosity);
				}
			}
			
			if (this.m_iSecondPlotType == 2)
			{
				// Calculate backpressure (in pascals) (Darcy equation)
				// See Bird, R. B.; Stewart, W. E.; Lightfoot, E. N. Transport Phenomena; Wiley & Sons: New York, 1960.
				double dBackpressureMin = 500 * (dViscosityMin / 10) * (((m_dFlowVelocity / 100) * (m_dColumnLength / 1000)) / Math.pow(m_dParticleSize / 1000000, 2));
				double dBackpressureMax = 500 * (dViscosityMax / 10) * (((m_dFlowVelocity / 100) * (m_dColumnLength / 1000)) / Math.pow(m_dParticleSize / 1000000, 2));
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dBackpressureMin / 100000, dBackpressureMax / 100000);
			}
			else if (this.m_iSecondPlotType == 3)
			{
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dViscosityMin, dViscosityMax);
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
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dEluentViscosity);
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dEluentViscosity);
		    	dViscosityMin = m_dEluentViscosity - (m_dEluentViscosity * 0.2);
		    	dViscosityMax = m_dEluentViscosity + (m_dEluentViscosity * 0.2);
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dViscosityMin, dViscosityMax);
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

	    	Double dRowValue1 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow, 0);
	    	Double dRowValue2 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow, 1);

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
	    	for (int i = 0; i < m_iRetentionFactorArrayLength; i++)
	    	{
	    		double dRetentionFactor = m_dRetentionFactorArray[i][1];
	    		
				if (dRetentionFactor < dRetentionFactorMin)
					dRetentionFactorMin = dRetentionFactor;
				if (dRetentionFactor > dRetentionFactorMax)
					dRetentionFactorMax = dRetentionFactor;
	
		    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dRetentionFactorArray[i][0], m_dRetentionFactorArray[i][1]);
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
	    	for (int i = 0; i < m_iPositionArrayLength; i++)
	    	{
	    		double dPosition = m_dPositionArray[i][1];
	    		contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dPositionArray[i][0], m_dPositionArray[i][1]);
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
