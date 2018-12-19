package com.wfnex.app.sal.service;

public class Template {
	private String id;
    private String name;
    private String priority;
    private String initialBandWidth;
    private String maxBandWidth;
    private String minBandWidth;
    private String increaseStep;
    private String threshold2increase;
    private String window2increase;
    private String decreaseStep;
    private String threshold2decrease;
    private String window2decrease;

	public Template() {
		this.id = "-1";
		this.name = "NoName";
		this.priority = "0";
		this.initialBandWidth = "0";
		this.maxBandWidth = "0";
		this.minBandWidth = "0";
		this.increaseStep = "0";
		this.threshold2increase = "0";
		this.window2increase = "0";
		this.decreaseStep = "0";
		this.threshold2decrease = "0";
		this.window2decrease = "0";
	}
	
	public Template(String id, String name, String priority, String initialBandWidth, String maxBandWidth,
			String minBandWidth, String increaseStep, String threshold2increase, String window2increase,
			String decreaseStep, String threshold2decrease, String window2decrease) {
		this.id = id;
		this.name = name;
		this.priority = priority;
		this.initialBandWidth = initialBandWidth;
		this.maxBandWidth = maxBandWidth;
		this.minBandWidth = minBandWidth;
		this.increaseStep = increaseStep;
		this.threshold2increase = threshold2increase;
		this.window2increase = window2increase;
		this.decreaseStep = decreaseStep;
		this.threshold2decrease = threshold2decrease;
		this.window2decrease = window2decrease;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPriority() {
		return priority;
	}

	public String getInitialBandWidth() {
		return initialBandWidth;
	}

	public String getMaxBandWidth() {
		return maxBandWidth;
	}

	public String getMinBandWidth() {
		return minBandWidth;
	}

	public String getIncreaseStep() {
		return increaseStep;
	}

	public String getThreshold2increase() {
		return threshold2increase;
	}

	public String getWindow2increase() {
		return window2increase;
	}

	public String getDecreaseStep() {
		return decreaseStep;
	}

	public String getThreshold2decrease() {
		return threshold2decrease;
	}

	public String getWindow2decrease() {
		return window2decrease;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public void setInitialBandWidth(String initialBandWidth) {
		this.initialBandWidth = initialBandWidth;
	}

	public void setMaxBandWidth(String maxBandWidth) {
		this.maxBandWidth = maxBandWidth;
	}

	public void setMinBandWidth(String minBandWidth) {
		this.minBandWidth = minBandWidth;
	}

	public void setIncreaseStep(String increaseStep) {
		this.increaseStep = increaseStep;
	}

	public void setThreshold2increase(String threshold2increase) {
		this.threshold2increase = threshold2increase;
	}

	public void setWindow2increase(String window2increase) {
		this.window2increase = window2increase;
	}

	public void setDecreaseStep(String decreaseStep) {
		this.decreaseStep = decreaseStep;
	}

	public void setThreshold2decrease(String threshold2decrease) {
		this.threshold2decrease = threshold2decrease;
	}

	public void setWindow2decrease(String window2decrease) {
		this.window2decrease = window2decrease;
	}

	@Override
	public String toString() {
		return "Template [" + (id != null ? "id=" + id + ", " : "") + (name != null ? "name=" + name + ", " : "")
				+ (priority != null ? "priority=" + priority + ", " : "")
				+ (initialBandWidth != null ? "initialBandWidth=" + initialBandWidth + ", " : "")
				+ (maxBandWidth != null ? "maxBandWidth=" + maxBandWidth + ", " : "")
				+ (minBandWidth != null ? "minBandWidth=" + minBandWidth + ", " : "")
				+ (increaseStep != null ? "increaseStep=" + increaseStep + ", " : "")
				+ (threshold2increase != null ? "threshold2increase=" + threshold2increase + ", " : "")
				+ (window2increase != null ? "window2increase=" + window2increase + ", " : "")
				+ (decreaseStep != null ? "decreaseStep=" + decreaseStep + ", " : "")
				+ (threshold2decrease != null ? "threshold2decrease=" + threshold2decrease + ", " : "")
				+ (window2decrease != null ? "window2decrease=" + window2decrease : "") + "]";
	}
}
