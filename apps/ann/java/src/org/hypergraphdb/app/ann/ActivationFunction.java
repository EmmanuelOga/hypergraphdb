package org.hypergraphdb.app.ann;

public interface ActivationFunction
{
    double eval(double x);
    double deval(double x); 
}
