package com.net.server.controllers;

import com.net.engine.io.IRequest;
import com.net.server.exceptions.SFSRequestValidationException;

public abstract interface IControllerCommand {
	public abstract boolean validate(IRequest paramIRequest) throws SFSRequestValidationException;

	public abstract Object preProcess(IRequest paramIRequest) throws Exception;

	public abstract void execute(IRequest paramIRequest) throws Exception;
}