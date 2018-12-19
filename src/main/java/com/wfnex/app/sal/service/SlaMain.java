package com.wfnex.app.sal.service;

import java.io.PrintStream;
import java.util.HashMap;

public class SlaMain {
	protected CfgMgr configMgr;
	protected RestClient restClient;
	protected SlaProcessor sla;
    private HashMap<String, PrintStream> flow2SLABandWidthLogMap = new HashMap<String, PrintStream>();
	
	public SlaMain() {
		configMgr = new CfgMgr(this);
		restClient = new RestClient(this);
		sla = new SlaProcessor(this);
	}
	
	protected int init() {
		int retVal = -1;
		
		System.out.println("SlaMain::init");
		
		try {
			// Read configuration file.
			retVal = configMgr.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (retVal != 0) {
			System.out.println("SalMain::init, configMgr failed to init. retVal = " + retVal);
			return retVal;
		}
		
		retVal = restClient.init();
		if (retVal != 0) {
			System.out.println("SalMain::init, restClient failed to init. retVal = " + retVal);
			return retVal;
		}
		
		retVal = sla.init();
		if (retVal != 0) {
			System.out.println("SalMain::init, sal failed to init. retVal = " + retVal);
			return retVal;
		}
		
		return 0;
	}
	
	protected int start() {
		System.out.println("SlaMain::start");
		
		int retVal = sla.start(configMgr.getFlowStatTimeWindow());
		return retVal;
	}
	
	public synchronized void addFlow2SLABandWidthLog(String flowId, PrintStream ps) {
		flow2SLABandWidthLogMap.put(flowId, ps);
	}
	
	public synchronized PrintStream getSLABandWidthLogByFlowId(String flowId) {
		return flow2SLABandWidthLogMap.get(flowId);
	}
	
	public static void main(String[] args) {
		SlaMain salMain = new SlaMain();
		int retVal = salMain.init();
		if (retVal != 0) {
			System.out.println("Failed to init.");
			return;
		}
		
		retVal = salMain.start();
		if (retVal != 0) {
			System.out.println("Failed to start.");
		}
	}

}
