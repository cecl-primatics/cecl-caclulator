package com.primatics.ignite.model;

import java.io.Serializable;
import java.util.Arrays;

public class Rates implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Rates() {}
	
	public Rates(Integer index, Double[] survival, Double[] lossRate, String scenario) {
		super();
		this.index = index;
		Arrays.fill(survival, 1.0);
		Arrays.fill(lossRate, 1.0);
		this.survival = survival;
		this.lossRate = lossRate;
		this.scenario = scenario;
	}

	private Double[] survival;
	private Double[] lossRate;
	private Integer index;
	private String scenario;
	
	public Double[] getSurvival() {
		return survival;
	}
	public void setSurvival(Double[] survival) {
		this.survival = survival;
	}
	public Double[] getLossRate() {
		return lossRate;
	}
	public void setLossRate(Double[] lossRate) {
		this.lossRate = lossRate;
	}
	
	@Override
	public String toString() {
		return "Rates [index=" + index +"survival=" + Arrays.toString(survival) + ", lossRate=" + Arrays.toString(lossRate) + ", scenario=" + scenario + "]";
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
	}

}
