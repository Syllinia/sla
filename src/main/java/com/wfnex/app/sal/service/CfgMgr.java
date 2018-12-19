package com.wfnex.app.sal.service;

import com.wfnex.app.sal.api.ICfgMgr;

import javax.xml.parsers.*; 
import org.xml.sax.*; 
import org.xml.sax.helpers.*; 
import org.w3c.dom.*; 
import java.io.*;

import java.util.HashMap;
import java.util.Map;

public class CfgMgr implements ICfgMgr{
    static PrintStream out = System.out;
    
    protected SlaMain slaMain;
    
    private String restSvrIP;
    
    private String minFlowStatWindowSize;
    private String maxFlowStatWindowSize;
    
    private HashMap<String, Template> templateMap = new HashMap<String, Template>();
    
    private HashMap<String, String> flowTemplateBindingMap = new HashMap<String, String>();

	public CfgMgr(SlaMain slaMain) {
		this.slaMain = slaMain;
		this.restSvrIP = "0.0.0.0";
		this.minFlowStatWindowSize = "0";
		this.maxFlowStatWindowSize = "0";
	}
	
	/*public CfgMgr(String minFlowStatWindowSize, String maxFlowStatWindowSize) {
		this.minFlowStatWindowSize = minFlowStatWindowSize;
		this.maxFlowStatWindowSize = maxFlowStatWindowSize;
	}*/
	
	public String getRestSvrIP() {
		return restSvrIP;
	}

	public String getMinFlowStatWindowSize() {
		return minFlowStatWindowSize;
	}

	public String getMaxFlowStatWindowSize() {
		return maxFlowStatWindowSize;
	}

	public void setMinFlowStatWindowSize(String minFlowStatWindowSize) {
		this.minFlowStatWindowSize = minFlowStatWindowSize;
	}

	public void setMaxFlowStatWindowSize(String maxFlowStatWindowSize) {
		this.maxFlowStatWindowSize = maxFlowStatWindowSize;
	}

	public int getFlowStatTimeWindow() {
		// TBD
		return Integer.parseInt(getMinFlowStatWindowSize());
	}
	
	protected int init() {
		int retVal = -1;
		
		out.println("CfgMgr::init");

		try {
			retVal = readSLACfg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		if (retVal != 0) {
			System.err.println("CfgMgr::init, failed to read SLA configuration file. retVal = " + retVal);
			return retVal;
		}
		
		try {
			retVal = readTemplateCfg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		if (retVal != 0) {
			System.err.println("CfgMgr::init, failed to read template configuration file. retVal = " + retVal);
			return retVal;
		}
		
		try {
			retVal = readFlowTemplateBindingsCfg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (retVal != 0) {
			System.err.println("CfgMgr::init, failed to read flow-template-bindings file. retVal = " + retVal);
			return retVal;
		}		
		
		return 0;
	}

	protected int readSLACfg() throws Exception {
		out.println("readSLACfg");
		
        // Step 1: create a DocumentBuilderFactory 
        DocumentBuilderFactory dbf = 
            DocumentBuilderFactory.newInstance(); 
        // We can set various configuration choices on dbf now 
        // (to ignore comments, do validation, etc) 

        // Step 2: create a DocumentBuilder  
        DocumentBuilder db = null; 
        try { 
            db = dbf.newDocumentBuilder(); 
        } catch (ParserConfigurationException pce) { 
            System.err.println(pce); 
            return -1; 
        } 
        // Step 3: parse the input file 
        Document doc = null; 
        try { 
            doc = db.parse(new File("config.xml")); 
        } catch (SAXException se) { 
            System.err.println(se.getMessage()); 
            return -1; 
        } catch (IOException ioe) { 
            System.err.println(ioe); 
            return -1; 
        } 

        // flowStatWindowSize
        NodeList list = doc.getElementsByTagName("restServerIP");
        if (null == list) {
        	System.err.println("readSLACfg, try to search restServerIP element, however, we only get null list.");
        	return -1;
        }        
        if (0 == list.getLength()) {
        	System.err.println("readSLACfg, failed to find restServerIP element.");
        	return -1;
        }
        Node restServerIPNode = list.item(0);
        if (null == restServerIPNode) {
        	System.err.println("readSLACfg, restServerIPNode is null.");
        	return -1;
        }
        
        restSvrIP = restServerIPNode.getFirstChild().getNodeValue();
        
		return 0;
	}
	
	protected int readTemplateCfg() throws Exception {
		out.println("readTemplateCfg");
		
        // Step 1: create a DocumentBuilderFactory 
        DocumentBuilderFactory dbf = 
            DocumentBuilderFactory.newInstance(); 
        // We can set various configuration choices on dbf now 
        // (to ignore comments, do validation, etc) 

        // Step 2: create a DocumentBuilder  
        DocumentBuilder db = null; 
        try { 
            db = dbf.newDocumentBuilder(); 
        } catch (ParserConfigurationException pce) { 
            System.err.println(pce); 
            return -1; 
        } 
        // Step 3: parse the input file 
        Document doc = null; 
        try { 
            doc = db.parse(new File("template.xml")); 
        } catch (SAXException se) { 
            System.err.println(se.getMessage()); 
            return -1; 
        } catch (IOException ioe) { 
            System.err.println(ioe); 
            return -1; 
        } 

        // flowStatWindowSize
        NodeList list = doc.getElementsByTagName("flowStatWindowSize");
        if (null == list) {
        	System.err.println("readTemplateCfg, try to search flowStatWindowSize element, however, we only get null list.");
        	return -1;
        }
        if (0 == list.getLength()) {
        	System.err.println("readTemplateCfg, failed to find flowStatWindowSize element.");
        	return -1;
        }
        Node flowStatWindowSizeNode = list.item(0);
        if (null == flowStatWindowSizeNode) {
        	System.err.println("readTemplateCfg, flowStatWindowSizeNode is null.");
        	return -1;
        }
        for (Node child = flowStatWindowSizeNode.getFirstChild();
    		 child != null; 
             child = child.getNextSibling()) {
        	if (child.getNodeName().equals("minimum")) {
        		minFlowStatWindowSize = child.getFirstChild().getNodeValue();
        		System.out.println("readTemplateCfg, minFlowStatWindowSize = " + minFlowStatWindowSize);
        	}
        	if (child.getNodeName().equals("maximum")) {
        		maxFlowStatWindowSize = child.getFirstChild().getNodeValue();
        		System.out.println("readTemplateCfg, maxFlowStatWindowSize = " + maxFlowStatWindowSize);
        	}       	
        }
        
        // template
        list = doc.getElementsByTagName("template");
        if (null == list) {
        	System.err.println("readTemplateCfg, try to search template element, however, we only get null list.");
        	return -1;
        }
        for (int i = 0; i < list.getLength(); ++i) {
            out.println("readTemplateCfg, i = " + i);

        	String id = "-1";
            String name = "NoName";
            String priority = "0";
            String initialBandWidth = "0";
            String maxBandWidth = "0";
            String minBandWidth = "0";
            String increaseStep = "0";
            String threshold2increase = "0";
            String window2increase = "0";
            String decreaseStep = "0";
            String threshold2decrease = "0";
            String window2decrease = "0";
            
            Node templateNode = list.item(i);
        	if (null == templateNode) {
        		System.err.println("readTemplateCfg, templateNode is null. i = " + i);
        		return -1;
        	}
            
        	NodeList templateSubElemList = templateNode.getChildNodes();
        	if (null == templateSubElemList) {
        		System.err.println("readTemplateCfg, templateSubElemList is null.");
        		return -1;
        	}
            
        	for (int j = 0; j < templateSubElemList.getLength(); ++j) {
        		Node child = templateSubElemList.item(j);
        		if (null == child) {
        			continue;
        		}
                
            	if (child.getNodeName().equals("id")) {
	        		id = child.getFirstChild().getNodeValue();
            	}            	
            	if (child.getNodeName().equals("name")) {
            		name = child.getFirstChild().getNodeValue();
            	}            	
            	if (child.getNodeName().equals("priority")) {
            		priority = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("initialBandWidth")) {
            		initialBandWidth = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("maxBandWidth")) {
            		maxBandWidth = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("minBandWidth")) {
            		minBandWidth = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("increaseStep")) {
            		increaseStep = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("threshold2increase")) {
            		threshold2increase = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("window2increase")) {
            		window2increase = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("decreaseStep")) {
            		decreaseStep = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("threshold2decrease")) {
            		threshold2decrease = child.getFirstChild().getNodeValue();
            	}
            	if (child.getNodeName().equals("window2decrease")) {
            		window2decrease = child.getFirstChild().getNodeValue();
            	}
        	}
        	
    		out.println("readTemplateCfg, id = " + id
    					+ ", name = " + name
    					+ ", priority = " + priority
    					+ ", initialBandWidth = " + initialBandWidth
    					+ ", maxBandWidth = " + maxBandWidth
    					+ ", minBandWidth = " + minBandWidth
    					+ ", increaseStep = " + increaseStep
    					+ ", threshold2increase = " + threshold2increase
    					+ ", window2increase = " + window2increase
    					+ ", decreaseStep = " + decreaseStep
    					+ ", threshold2decrease = " + threshold2decrease
    					+ ", window2decrease = " + window2decrease);
    		
        	Template template = new Template(id,
											 name,
											 priority,
											 initialBandWidth,
											 maxBandWidth,
									 		 minBandWidth,
									 		 increaseStep,
									 		 threshold2increase,
									 		 window2increase,
									 		 decreaseStep,
									 		 threshold2decrease,
									 		 window2decrease);
        	addTemplate(id, template);
        }
        
		return 0;
	}

	private synchronized void addTemplate(String id, Template template) {
		templateMap.put(id, template);
	}
	
	public synchronized Template getTemplate(String id) {
		return templateMap.get(id);
	}
    
	protected int readFlowTemplateBindingsCfg()  throws Exception {
		out.println("readFlowTemplateBindingsCfg");
		
        // Step 1: create a DocumentBuilderFactory 
        DocumentBuilderFactory dbf = 
            DocumentBuilderFactory.newInstance(); 
        // We can set various configuration choices on dbf now 
        // (to ignore comments, do validation, etc) 

        // Step 2: create a DocumentBuilder  
        DocumentBuilder db = null; 
        try { 
            db = dbf.newDocumentBuilder(); 
        } catch (ParserConfigurationException pce) { 
            System.err.println(pce); 
            return -1; 
        } 
        // Step 3: parse the input file 
        Document doc = null; 
        try { 
            doc = db.parse(new File("flowTemplateBindings.xml")); 
        } catch (SAXException se) { 
            System.err.println(se.getMessage()); 
            return -1; 
        } catch (IOException ioe) { 
            System.err.println(ioe); 
            return -1; 
        } 
        
        // template
        NodeList list = doc.getElementsByTagName("binding");
        if (null == list) {
        	System.err.println("readFlowTemplateBindingsCfg, try to search binding element, however, we only get null list.");
        	return -1;
        }
        for (int i = 0; i < list.getLength(); ++i) {
            out.println("readFlowTemplateBindingsCfg, i = " + i);

        	String flowId = "-1";
            String templateId = "-1";
            
            Node bindingNode = list.item(i);
        	if (null == bindingNode) {
        		System.err.println("readFlowTemplateBindingsCfg, bindingNode is null. i = " + i);
        		return -1;
        	}
        	
        	for (Node child = bindingNode.getFirstChild();
    			 child != null; 
                 child = child.getNextSibling()) {
                
            	if (child.getNodeName().equals("flowId")) {
            		flowId = child.getFirstChild().getNodeValue();
            	}            	
            	if (child.getNodeName().equals("templateId")) {
            		templateId = child.getFirstChild().getNodeValue();
            	}            	
        	}
        	
    		out.println("readFlowTemplateBindingsCfg, flowId = " + flowId
    					+ ", templateId = " + templateId);
    		

        	addFlowTemplateBinding(flowId, templateId);
        	
    		try {
    			PrintStream slaBandWidthLog = new PrintStream ("slaBandWidthlog_" + flowId + ".csv");
    			slaBandWidthLog.println("timestamp,flow statistics(forward),flow statistics(reverse),maximum bandwidth");
    			slaMain.addFlow2SLABandWidthLog(flowId, slaBandWidthLog);
    		} catch (IOException x) {
    			System.err.println("Exception: " + x.getMessage());
    		}
        }
        
		return 0;
	}
	
	private synchronized void addFlowTemplateBinding(String flowId, String templateId) {
		flowTemplateBindingMap.put(flowId, templateId);
	}
	
	public synchronized String getTemplateIdByFlowId(String flowId) {
		return flowTemplateBindingMap.get(flowId);
	}
	
	public int getPriorityByFlowId(String flowId) {
		int priority = -1;
		
		String templateId = getTemplateIdByFlowId(flowId);
		if (null == templateId) {
			System.err.println("getPriorityByFlowId, failed to get templateId for flow with id " + flowId);
			return -1;
		}
		
		Template template = getTemplate(templateId);
		if (null == template) {
			System.err.println("getPriorityByFlowId, failed to get template for flow with id " + flowId
								+ ", templateId = " + templateId);
			return -1;
		}
		
		priority = Integer.parseInt(template.getPriority());
		return priority;
	}
	
}
