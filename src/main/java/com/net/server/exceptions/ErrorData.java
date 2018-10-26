package com.net.server.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ErrorData
{
  IErrorCode code;
  List<String> params;

  public ErrorData(IErrorCode code)
  {
    this.code = code;
    this.params = new ArrayList<String>();
  }

  public IErrorCode getCode()
  {
    return this.code;
  }

  public void setCode(IErrorCode code)
  {
    this.code = code;
  }

  public List<String> getParams()
  {
    return this.params;
  }

  public void setParams(List<String> params)
  {
    this.params = params;
  }

  public void addParameter(String parameter)
  {
    this.params.add(parameter);
  }
}