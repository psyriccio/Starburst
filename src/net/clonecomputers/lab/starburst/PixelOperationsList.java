package net.clonecomputers.lab.starburst;

import static java.lang.Math.*;

import java.util.*;

public class PixelOperationsList {
	private double removeOrderBias = .5; // default
	private Pair[] operations;
	private long start;
	private long end;
	private Random myRandom;
	
	public PixelOperationsList(int maxSize) {
		operations = new Pair[maxSize*2];// bigger in case of threading issues
		start = 0;
		end = 0;
		myRandom = new Random();
	}
	
	private int length() {
		return (int)(end-start);
	}
	
	private int wrap(long i) {
		return (int)(i % operations.length);
	}
	
	public synchronized void setRemoveOrderBias(double removeOrder) {
		this.removeOrderBias = removeOrder;
	}
	
	public double getRemoveOrderBias() {
		return removeOrderBias;
	}
	
	public synchronized boolean hasPoint() {
		return start != end;
	}
	
	public synchronized void addPoint(Pair p) {
		operations[wrap(end++)] = p;
	}

	public synchronized void addPoint(int x, int y) {
		operations[wrap(end++)] = new Pair(x,y);
	}

	public synchronized Pair getPoint() {
		if(!hasPoint()) return null;
		Pair retval = null;
		double removePoint = removeOrderBias==0? 0: pow(myRandom.nextDouble(), log(removeOrderBias)/log(.5));
		int i = wrap(start + (int)(removePoint*length()));
		retval = operations[i];
		operations[i] = operations[wrap(--end)];
		operations[wrap(end)] = null; // let the GC do it's work
		return retval;
	}
}
