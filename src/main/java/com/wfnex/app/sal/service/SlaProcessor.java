package com.wfnex.app.sal.service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.io.PrintStream;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SlaProcessor {
	static boolean runSlaFlag = false;
	
	SlaMain slaMain;
	
	public SlaProcessor(SlaMain slaMain) {
		this.slaMain = slaMain;
	}
	
	protected int init() {
		System.out.println("SlaProcessor::init");
		
		return 0;
	}
	
	
	private LinkedList<FlowPriority> flowPriorityList = new LinkedList<FlowPriority>();
	
	private class FlowPriority implements Comparable<FlowPriority>{
		private int priority = -1;
		private String flowId = "-1";
		
		public FlowPriority(int priority, String flowId) {
			this.priority = priority;
			this.flowId = flowId;
		}

		public int getPriority() {
			return priority;
		}

		public String getFlowId() {
			return flowId;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public void setFlowId(String flowId) {
			this.flowId = flowId;
		}

		@Override
		public String toString() {
			return "FlowPriority [priority=" + priority + ", " + (flowId != null ? "flowId=" + flowId : "") + "]";
		}

		public int compareTo(FlowPriority o) {
			// TODO Auto-generated method stub
			return (priority - o.priority);
		}
	}
	
	private synchronized void addFlowPriority(FlowPriority item) {
		flowPriorityList.add(item);
	}
	
	private synchronized void sortFlowPriorityList() {
		Collections.sort(flowPriorityList);
	}
	
	private synchronized void clearFlowPriorityList() {
		flowPriorityList.clear();
	}
	
	private synchronized void dumpFlowPriorityList() {
		System.out.println("flowPriorityList: " + flowPriorityList);
	}
	
	private int fillFlowPriorityList() {
		LinkedList<String> flowIdList = slaMain.restClient.getFlowIdList();
		if (null == flowIdList) {
			System.err.println("fillFlowPriorityList, failed to get flowIdList from REST.");
			return -1;
		}
		
		for (String flowId : flowIdList) {
			int priority = slaMain.configMgr.getPriorityByFlowId(flowId);
			if (-1 == priority) {
				System.err.println("fillFlowPriorityList, failed to get priority for flow with id " + flowId);
				return -1;
			}
			addFlowPriority(new FlowPriority(priority, flowId));
		}
		
		sortFlowPriorityList();
		
		return 0;
	}
	
	HashMap<String, LinkedList<BandWidthUsageStats>> flowBandWidthUsageMap 
				= new HashMap<String, LinkedList<BandWidthUsageStats>>();
	private class BandWidthUsageStats {
		int maxBandWidth;
		int curBandWidth;
		double bandWidthUsage;
		Date statsTime;
		
		public BandWidthUsageStats() {
			maxBandWidth = -1;
			curBandWidth = -1;
			bandWidthUsage = 0.0;
			statsTime = new Date();
		}
		
		public BandWidthUsageStats(int maxBandWidth, int curBandWidth, double bandWidthUsage, Date statsTime) {
			this.maxBandWidth = maxBandWidth;
			this.curBandWidth = curBandWidth;
			this.bandWidthUsage = bandWidthUsage;
			this.statsTime = statsTime;
		}

		public int getMaxBandWidth() {
			return maxBandWidth;
		}

		public int getCurBandWidth() {
			return curBandWidth;
		}

		public double getBandWidthUsage() {
			return bandWidthUsage;
		}

		public Date getStatsTime() {
			return statsTime;
		}

		public void setMaxBandWidth(int maxBandWidth) {
			this.maxBandWidth = maxBandWidth;
		}

		public void setCurBandWidth(int curBandWidth) {
			this.curBandWidth = curBandWidth;
		}
		
		public void setBandWidthUsage(double bandWidthUsage) {
			this.bandWidthUsage = bandWidthUsage;
		}

		public void setStatsTime(Date statsTime) {
			this.statsTime = statsTime;
		}

		@Override
		public String toString() {
			return "BandWidthUsageStats [maxBandWidth=" + maxBandWidth + ", curBandWidth=" + curBandWidth
					+ ", bandWidthUsage=" + bandWidthUsage + ", " + (statsTime != null ? "statsTime=" + statsTime : "")
					+ "]";
		}

	}
	
	private synchronized LinkedList<BandWidthUsageStats> getBWUsageStatsByFlowId(String flowId) {
		return flowBandWidthUsageMap.get(flowId);
	}
	
	private synchronized void addBandWidthUsageStats(String flowId, BandWidthUsageStats stats) {
		LinkedList<BandWidthUsageStats> list = flowBandWidthUsageMap.get(flowId);
		if (list != null) {
			list.add(stats);
		} else {
			LinkedList<BandWidthUsageStats> newList = new LinkedList<BandWidthUsageStats>();
			newList.add(stats);
			flowBandWidthUsageMap.put(flowId, newList);
		}
	}
	
	private synchronized void clearBandWidthUsageStats(String flowId) {
		LinkedList<BandWidthUsageStats> list = flowBandWidthUsageMap.get(flowId);
		list.clear();
	}
	
	private synchronized void dumpFlowBandWidthUsageMap() {
		System.out.println("flowBandWidthUsageMap: " + flowBandWidthUsageMap);
	}
	
	private int fillFlowBandWidthUsageMap() {
		for (FlowPriority item : flowPriorityList) {
			String flowId = item.getFlowId();
			
			int maxBandWidth = slaMain.restClient.getMaxBandWidthByFlowId(flowId);
			if (-1 == maxBandWidth) {
				System.err.println("fillFlowBandWidthUsageMap, failed to get maxBandWidth for flow with id " + flowId);
				
				// If we fail to query flow's maximum bandwidth, we continue.
				//return -1;
			}
			
			// curBandWidthArray[0]: flow statistics(forward); curBandWidthArray[1]: flow statistics(reverse)
			int curBandWidthArray[] = new int[2];
			int retVal = slaMain.restClient.getCurBandWidthByFlowId(flowId, curBandWidthArray);
			if (-1 == retVal) {
				System.err.println("fillFlowBandWidthUsageMap, failed to get curBandWidth for flow with id " + flowId);
				
				// It is true that we fail to read flow statistics sometimes. So at this time, we continue.
				//return -1;				
			}
	        int curBandWidth = (curBandWidthArray[0] >= curBandWidthArray[1]) ? curBandWidthArray[0] : curBandWidthArray[1]; 
			
			Date statsTime = new Date();
			
			// Note: the last maxBandWidth in the csv is the maxBandWidth to set, NOT the current one read from RESTful API.
			//slaMain.statsLog.println(statsTime + "," + curBandWidthArray[0] + "," + curBandWidthArray[1] + "," + maxBandWidth);
			slaMain.getSLABandWidthLogByFlowId(flowId).print(statsTime + "," + curBandWidthArray[0] + "," + curBandWidthArray[1] + ",");
			
			double bandwidthUsage = -1.0;
			
			// Calculate bandwidth usage.
			LinkedList<BandWidthUsageStats> usageStatsList = getBWUsageStatsByFlowId(flowId);
			if ((maxBandWidth > 0) && (curBandWidth != -1)) {
				if (null == usageStatsList) {					
					bandwidthUsage = ( ((double) curBandWidth / 1000.0) / ((double) maxBandWidth) );
				} else {
					BandWidthUsageStats lastStats = usageStatsList.getLast();
					if (lastStats.getMaxBandWidth() == maxBandWidth) {
						if (lastStats.getCurBandWidth() != -1) {
							double averageUsedBandWidth = ( ((double) curBandWidth) + ((double) lastStats.getCurBandWidth()) ) / 2.0;
							bandwidthUsage = ( averageUsedBandWidth / 1000.0 / ((double) maxBandWidth) );
						} else {
							bandwidthUsage = ( ((double) curBandWidth / 1000.0) / ((double) maxBandWidth) );
						}
					} else {
						bandwidthUsage = ( ((double) curBandWidth / 1000.0) / ((double) maxBandWidth) );
					}
				}	
			}			
			
			addBandWidthUsageStats(flowId, new BandWidthUsageStats(maxBandWidth, curBandWidth, bandwidthUsage, statsTime));
		}
		
		return 0;
	}
	
	protected int start(int flowStatTimeWindow) {
		System.out.println("SlaProcessor::start, flowStatTimeWindow = " + flowStatTimeWindow);
		
		runSlaFlag = true;
		final int tidyInterval = 30;
		int i = 0;
		
		while (runSlaFlag) {
			int retVal = readFlowStats();
			if (retVal != 0) {
				System.err.println("SlaProcessor::start, failed to read flow stats. retVal = " + retVal);
				runSlaFlag = false;
				return -1;
			}
			
			runSlaPolicy();
			
			++i;
			if (i >= tidyInterval) {
				tidyBandWidthUsageStatsList();
				i = 0;
			}
			
	        try {
				TimeUnit.SECONDS.sleep(flowStatTimeWindow);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        	        
		}
		
		return 0;
	}
	
	// Update two collections: flowPriorityList and flowBandWidthUsageMap.
	// Note that we should sort flowPriorityList.
	private int readFlowStats() {
		// flowPriorityList
		clearFlowPriorityList();
		int retVal = fillFlowPriorityList();
		if (retVal != 0) {
			System.err.println("readFlowStats, failed to fill flowPriorityList. retVal = " + retVal);
			return retVal; 
		}
		
		dumpFlowPriorityList();
		
		// flowBandWidthUsageMap
		retVal = fillFlowBandWidthUsageMap();
		if (retVal != 0) {
			System.err.println("readFlowStats, failed to fill flowBandWidthUsageMap. retVal = " + retVal);
			return retVal; 
		}
		
		dumpFlowBandWidthUsageMap();
		
		return 0;
	}
	
	// to transfer values in template.xml to kilobyte per second
	// Currently, values in template.xml is megabit per second.
	// 1 kilobyte per second = 1 megabit per second * 1000 / 8
	private int unitConvert(int templateVal) {
		return (templateVal * 1000 / 8);
	}
	
	// SLA algorithm:
	// 1. decrease bandwidth of flows with low priority;
	// 2. decrease bandwidth of flows with high priority;
	// 3. increase bandwidth of flows with high priority;
	// 4. increase bandwidth of flows with low priority
	private void runSlaPolicy() {
		System.out.println("runSlaPolicy");
		
		int retVal = 0;
		Set<String> flowsAlreadyDecrBandWidthSet = new HashSet<String>();
		
		System.out.println("runSlaPolicy, step1: decrease bandwidth.");
		
		// 1. decrease bandwidth of flows with low priority;
		// 2. decrease bandwidth of flows with high priority;
		ListIterator<FlowPriority> iterator = flowPriorityList.listIterator(flowPriorityList.size());
		while (iterator.hasPrevious()) {
			FlowPriority item = iterator.previous();
			
			System.out.println("runSlaPolicy, " + item);
			
			String flowId = item.getFlowId();
			String templateId = slaMain.configMgr.getTemplateIdByFlowId(flowId);
			Template template = slaMain.configMgr.getTemplate(templateId);
			
			int flowBandWidth = slaMain.restClient.getMaxBandWidthByFlowId(flowId);
			
			System.out.println("runSlaPolicy, flowId = " + flowId + ", flowBandWidth = " + flowBandWidth);
			
			PrintStream ps = slaMain.getSLABandWidthLogByFlowId(flowId);
			
			// Check whether the bandwidth of the flow exceeds its maximum value.
			int maxBandWidth = Integer.parseInt(template.getMaxBandWidth());
			maxBandWidth = unitConvert(maxBandWidth);
			if (flowBandWidth > maxBandWidth) {
				System.out.println("runSlaPolicy, flow " + flowId + " 's bandwidth(" + flowBandWidth + ") exceeds maximum(" + maxBandWidth + ")."
								   + " Set it to the maximum.");
				
				retVal = slaMain.restClient.updateFlowMaxBandWidth(flowId, maxBandWidth);
				if (retVal != 0) {
					System.err.println("runSlaPolicy, failed to update flow max-bandwidth, flowId = " 
									   + flowId + ", bandwidth = " + maxBandWidth);
					
					// Currently, even if we failed to update flow max-bandwidth, we don't return to stop the process.
				}
				
				flowsAlreadyDecrBandWidthSet.add(flowId);
				
				ps.println(maxBandWidth);
				continue;
			}
			
			// If we cannot decrease the bandwidth of the flow any more, just skip it.
			int minBandWidth = Integer.parseInt(template.getMinBandWidth());
			minBandWidth = unitConvert(minBandWidth);
			int decreaseStep = Integer.parseInt(template.getDecreaseStep());
			decreaseStep = unitConvert(decreaseStep);
			if (flowBandWidth < (minBandWidth + decreaseStep)) {
				System.out.println("runSlaPolicy, cann't decrease the bandwidth of flow " + flowId + " any more."
								   + "flowBandWidth = " + flowBandWidth + ", minBandWidth = " + minBandWidth + ", decreaseStep = " + decreaseStep);
				// We don't log the maxBandWidth as we will do it in the very end of the function.
				continue;
			}
			
			// Check whether the bandwidth usage of the flow is below the threshold2decrease in the window2decrease.
			int window2decrease = Integer.parseInt(template.getWindow2decrease());
			double threshold2decrease = Double.parseDouble(template.getThreshold2decrease());
			int windowsNumBelowThres2decr = 0;
			LinkedList<BandWidthUsageStats> statsList = getBWUsageStatsByFlowId(flowId);
			
			System.out.println("runSlaPolicy, statsList = " + statsList);
			
			int newestMaxBandwidth = statsList.getLast().getMaxBandWidth();
			
			ListIterator<BandWidthUsageStats> iter = statsList.listIterator(statsList.size());

			// TBD!!! Currently, we ignore the statsTime field of the item in statsList.
			for (int i = 0; (i < window2decrease) && (iter.hasPrevious()); ++i) {
				BandWidthUsageStats stats = iter.previous();
				int itemMaxBandWidth = stats.getMaxBandWidth();				
				// Examine the windows at the same max-bandwidth level.
				if (itemMaxBandWidth != newestMaxBandwidth) {
					break;
				}
				
				double bwUsage = stats.getBandWidthUsage();
				if ((bwUsage != -1.0) && (bwUsage <= threshold2decrease)) {
					++windowsNumBelowThres2decr;
				}				
			}
			if (window2decrease == windowsNumBelowThres2decr) {
				System.out.println("runSlaPolicy, bandwidth usage of flow " + flowId + " below threshold2decrease " + threshold2decrease + " in "
								   + window2decrease + " windows. Set its bandwidth to " + (flowBandWidth - decreaseStep));
				
				retVal = slaMain.restClient.updateFlowMaxBandWidth(flowId, flowBandWidth - decreaseStep);
				if (retVal != 0) {
					System.err.println("runSlaPolicy, failed to update flow max-bandwidth, flowId = " 
									   + flowId + ", bandwidth = " + (flowBandWidth - decreaseStep));
					// Currently, even if we failed to update flow max-bandwidth, we don't return to stop the process.
				}
				
				flowsAlreadyDecrBandWidthSet.add(flowId);
				
				ps.println(flowBandWidth - decreaseStep);
			}
		}

		System.out.println("runSlaPolicy, step2: increase bandwidth.");
		
		// 3. increase bandwidth of flows with high priority;
		// 4. increase bandwidth of flows with low priority
		for (FlowPriority item : flowPriorityList) {
			System.out.println("runSlaPolicy, " + item);

			String flowId = item.getFlowId();
			
			// If the max-bandwidth of the flow is already decreased, skip it.
			if (flowsAlreadyDecrBandWidthSet.contains(flowId)) {
				System.out.println("runSlaPolicy, flow " + flowId + " already decreased, so skip it.");
				
				continue;
			}
			
			String templateId = slaMain.configMgr.getTemplateIdByFlowId(flowId);
			Template template = slaMain.configMgr.getTemplate(templateId);			
			
			int flowBandWidth = slaMain.restClient.getMaxBandWidthByFlowId(flowId);
			
			System.out.println("runSlaPolicy, bandwidth of flow " + flowId + " is " + flowBandWidth);
			
			PrintStream ps = slaMain.getSLABandWidthLogByFlowId(flowId);
			
			// If the bandwidth is below the minimum value, we need to set it to the minimum.
			int minBandWidth = Integer.parseInt(template.getMinBandWidth());
			minBandWidth = unitConvert(minBandWidth);
			if (flowBandWidth < minBandWidth) {
				System.out.println("runSlaPolicy, bandwidth of flow " + flowId + " is below the minimum(" + minBandWidth + ")."
								   + " So, set it to the minimum.");
				
				retVal = slaMain.restClient.updateFlowMaxBandWidth(flowId, minBandWidth);
				if (retVal != 0) {
					System.err.println("runSlaPolicy, failed to update flow max-bandwidth, flowId = " 
									   + flowId + ", bandwidth = " + minBandWidth);
					
					// Currently, even if we failed to update flow max-bandwidth, we don't return to stop the process.
				}
				
				ps.println(minBandWidth);
				
				continue;
			}
			
			// If we cannot increase the bandwidth of the flow any more, just skip it.
			int maxBandWidth = Integer.parseInt(template.getMaxBandWidth());
			maxBandWidth = unitConvert(maxBandWidth);
			int increaseStep = Integer.parseInt(template.getIncreaseStep());
			increaseStep = unitConvert(increaseStep);
			if ((flowBandWidth + increaseStep) > maxBandWidth) {
				System.out.println("runSlaPolicy, cann't increase bandwidth of flow " + flowId + " any more."
								   + " flowBandWidth = " + flowBandWidth + ", increaseStep = " + increaseStep
								   + ", maxBandWidth = " + maxBandWidth);
				ps.println(flowBandWidth);
				continue;
			}
			
			// Check whether the bandwidth usage of the flow is over the threshold2increase in the window2increase.
			int window2increase = Integer.parseInt(template.getWindow2increase());
			double threshold2increase = Double.parseDouble(template.getThreshold2increase());
			int windowsNumOverThres2incr = 0;
			LinkedList<BandWidthUsageStats> statsList = getBWUsageStatsByFlowId(flowId);
			
			System.out.println("runSlaPolicy, statsList = " + statsList);
			
			int newestMaxBandwidth = statsList.getLast().getMaxBandWidth();
			
			ListIterator<BandWidthUsageStats> iter = statsList.listIterator(statsList.size());

			// TBD!!! Currently, we ignore the statsTime field of the item in statsList.
			for (int i = 0; (i < window2increase) && (iter.hasPrevious()); ++i) {
				BandWidthUsageStats stats = iter.previous();
				int itemMaxBandWidth = stats.getMaxBandWidth();
				// We examine the bandwidth usage at the same max-bandwidth level.
				if (itemMaxBandWidth != newestMaxBandwidth) {
					break;
				}
				
				if (stats.getBandWidthUsage() >= threshold2increase) {
					++windowsNumOverThres2incr;
				}				
			}
			if (window2increase == windowsNumOverThres2incr) {
				System.out.println("runSlaPolicy, bandwidth usage of flow " + flowId + " is over threshold2ncrease " + threshold2increase 
								   + " in " + window2increase + "windows. Set its bandwidth to " + (flowBandWidth + increaseStep));
				
				retVal = slaMain.restClient.updateFlowMaxBandWidth(flowId, flowBandWidth + increaseStep);
				if (retVal != 0) {
					System.err.println("runSlaPolicy, failed to update flow max-bandwidth, flowId = " 
									   + flowId + ", bandwidth = " + (flowBandWidth + increaseStep));
					// Currently, even if we failed to update flow max-bandwidth, we don't return to stop the process.
				}
				
				ps.println(flowBandWidth + increaseStep);
			} else {			
				ps.println(flowBandWidth);
			}
		}
	}

	// if the size of BandWidthUsageStats-linkedList exceeds 30, we will decrease it to
	// the greater one of the window2decrease and window2increase.
	private synchronized void tidyBandWidthUsageStatsList() {
		final int maxSize = 30;
		
		for (Map.Entry<String, LinkedList<BandWidthUsageStats>> item : flowBandWidthUsageMap.entrySet()) {
			String flowId = item.getKey();
			LinkedList<BandWidthUsageStats> statsList = item.getValue();
			
			if (statsList.size() < maxSize) {
				continue;
			}
			
			String templateId = slaMain.configMgr.getTemplateIdByFlowId(flowId);
			Template template = slaMain.configMgr.getTemplate(templateId);			
			int window2increase = Integer.parseInt(template.getWindow2increase());
			int window2decrease = Integer.parseInt(template.getWindow2decrease());
			int window = (window2increase >= window2decrease) ? window2increase : window2decrease;
			
			int from = 0;
			int to = (statsList.size() - window);
			statsList.subList(from, to).clear();
		}
	}
}
