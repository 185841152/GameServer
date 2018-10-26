package com.net.engine.controllers;

import java.util.Comparator;

import com.net.engine.io.IRequest;

public class RequestComparator implements Comparator<IRequest> {
	public int compare(IRequest r1, IRequest r2) {
		int res = 0;

		if (r1.getPriority().getValue() < r2.getPriority().getValue()) {
			res = -1;
		} else if (r1.getPriority() == r2.getPriority()) {
			if (r1.getTimeStamp() < r2.getTimeStamp())
				res = -1;
			else if (r1.getTimeStamp() > r2.getTimeStamp())
				res = 1;
			else
				res = 0;
		} else {
			res = 1;
		}
		return res;
	}
}