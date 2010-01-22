package org.hypergraphdb.app.ann;

public class TanhFun implements ActivationFunction
{
    public double deval(double x)
    {
        return 1.0 - x*x;
    }
    
    public double eval(double x)
    {
        return Math.tanh(x);
    }
}