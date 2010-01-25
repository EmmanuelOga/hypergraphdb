package org.hypergraphdb.app.ann;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;

public class NeuralNet
{
    private static final double INITIAL_OUT_STRENGTH = 0.1;
    
    private HyperGraph graph;
    private ActivationFunction activationFunction = new TanhFun();
    Map<HGHandle, Double> inputLayer = null;
    Map<HGHandle, Double> hiddenLayer = null;
    Map<HGHandle, Double> outputLayer = null;
    private double learningRate = 0.5;
    
    private Map<HGHandle, Double> activateNextLayer(Map<HGHandle, Double> previousLayer, Map<HGHandle, Double> nextLayer)
    {
        for (Map.Entry<HGHandle, Double> in : previousLayer.entrySet())
        {
            Collection<Neuron> downstream = hg.getAll(graph, hg.and(hg.type(Neuron.class), 
                                                                    hg.incident(in.getKey())));
            for (Neuron n : downstream)
                if (!nextLayer.containsKey(n))
                    nextLayer.put(graph.getHandle(n), n.fire(previousLayer, activationFunction));
        }
        return nextLayer;
    }
    
    public NeuralNet(HyperGraph graph)
    {
        this.graph = graph;
    }
        
    public Map<Object, Double> feedforward(HGHandle...inputs)
    {
        Map<HGHandle, Double> inputMap = new ActivationMap(0.0);
        for (HGHandle in : inputs)
            inputMap.put(in, 1.0);
        feedforward(inputMap);
        Map<Object, Double> result = new HashMap<Object, Double>();
        for (Map.Entry<HGHandle, Double> e : outputLayer.entrySet())
            result.put(hg.getOne(graph, hg.apply(hg.targetAt(graph, 1), 
                                                    hg.and(hg.type(NeuronOutput.class), 
                                                           hg.incident(e.getKey())))), e.getValue());
        return result;
    }
    
    public void feedforward(Map<HGHandle, Double> inputs)
    {
        inputLayer = inputs;
        hiddenLayer = activateNextLayer(inputLayer, new ActivationMap(0.0));
        outputLayer = activateNextLayer(hiddenLayer, new ActivationMap(0.0));
    }    
    
    public void backpropagate(Map<HGHandle, Double> fromOutputs)
    {
        Map<HGHandle, Double> outputDeltas = new HashMap<HGHandle, Double>();
        for (Map.Entry<HGHandle, Double> node : outputLayer.entrySet())
        {
            double error = fromOutputs.get(node.getKey()) - node.getValue();
            outputDeltas.put(node.getKey(), activationFunction.deval(node.getValue())*error);
        }

        Map<HGHandle, Double> hiddenDeltas = new HashMap<HGHandle, Double>();        
        for (Map.Entry<HGHandle, Double> node : hiddenLayer.entrySet())
        {
            double error = 0.0;
            for (HGHandle out : outputLayer.keySet())
            {
                Neuron n = graph.get(out);
                double currentWeight = n.getWeights()[n.findInput(node.getKey())];
                error += outputDeltas.get(out)*currentWeight;
            }
            hiddenDeltas.put(node.getKey(), activationFunction.deval(node.getValue())*error);
        }
        
        for (Map.Entry<HGHandle, Double> node : outputLayer.entrySet())
        {
            Neuron n = graph.get(node.getKey());
            for (int i = 0; i < n.getArity(); i++)
            {
                n.getWeights()[i] += learningRate*(outputDeltas.get(node.getKey())*hiddenLayer.get(n.getTargetAt(i)));
            }                
            graph.update(n);
        }
        
        for (Map.Entry<HGHandle, Double> node : hiddenLayer.entrySet())
        {
            Neuron n = graph.get(node.getKey());
            for (int i = 0; i < n.getArity(); i++)
            {
                n.getWeights()[i] += learningRate*(hiddenDeltas.get(node.getKey())*
                        inputLayer.get(n.getTargetAt(i)));
            }    
            graph.update(n);
        }        
    }
        
    public void train(Collection<HGHandle> inputs, Collection<HGHandle> outputs, HGHandle selectedOutput)
    {
        ensureHiddenNeuron(inputs, outputs);
        Map<HGHandle, Double> inputMap = new ActivationMap(0.0);
        for (HGHandle in : inputs)
            inputMap.put(in, 1.0);
        Map<HGHandle, Double> outputMap = new ActivationMap(0.0);
        // Map from output atom to its corresponding neuron:
        selectedOutput = hg.findOne(graph, hg.apply(hg.targetAt(graph, 0), 
                                                    hg.and(hg.type(NeuronOutput.class), 
                                                           hg.incident(selectedOutput)))); 
        outputMap.put(selectedOutput, 1.0);
        feedforward(inputMap);
        backpropagate(outputMap);
    }
    
    /**
     * <p>
     * Create a neuron in the hidden layers that connects a set of inputs (arbitrary atoms)
     * and a set of neuron outputs. The method first checks whether a neuron connecting
     * those inputs exists.
     * </p>
     * 
     * @param inputs A collection of arbitrary input atoms.
     * @param outputs A collection of arbitrary output atoms.
     */
    public void ensureHiddenNeuron(Collection<HGHandle> inputs, 
                                   Collection<HGHandle> outputs)
    {
        HGHandle [] ins = inputs.toArray(new HGHandle[0]);
        HGHandle existing = hg.findOne(graph, hg.and(hg.link(ins), hg.arity(ins.length)));
        if (existing == null)
        {
            Collection<HGHandle> outputNeurons = new HashSet<HGHandle>();
            for (HGHandle out : outputs)
            {
                NeuronOutput no = hg.getOne(graph, hg.and(hg.type(NeuronOutput.class), hg.incident(out)));
                if (no == null)
                {
                    no = new NeuronOutput(graph.add(new Neuron()), out);
                    graph.add(no);
                }
                outputNeurons.add(no.getNeuron());
            }            
            existing = graph.getHandle(createNeuron(inputs, 1.0/ins.length, outputNeurons, INITIAL_OUT_STRENGTH));
        }
    }
    
    /**
     * <p>
     * Create a neuron in the networks. 
     * </p>
     * 
     * @param inputs Input atoms to this neuron: those may or may not be Neuron atom themselves.
     * @param initialInputWeight The weight of all synapses between the new neuron and its inputs.
     * @param outputs A collection of output <b>neurons</b>. Unlike the inputs collection, those are
     * expected to be neurons.
     * @param initialOutputWeight The initial weight of all synapses between the new neuron and its
     * output connections.
     * @return The HyperGraph handle of the newly added neuron.
     */
    public HGHandle createNeuron(Collection<HGHandle> inputs, 
                                 double initialInputWeight, 
                                 Collection<HGHandle> outputs, 
                                 double initialOutputWeight)
    {
        Neuron n = new Neuron(inputs.toArray(new HGHandle[0]));
        n.setWeights(new double[inputs.size()]);
        for (int i = 0; i < n.getWeights().length; i++)
            n.getWeights()[i] = initialInputWeight;
        HGHandle nh = graph.add(n);        
        for (HGHandle outH : outputs)
        {
            Neuron out = graph.get(outH);
            out.addInput(nh, initialOutputWeight);
            graph.update(out);
        }
        return nh;
    }
    
    /**
     * <p>
     * A map HGHandle -> Double that will return a specified default on missing keys.
     * </p>
     */
    private static class ActivationMap extends HashMap<HGHandle, Double> 
    {
        private static final long serialVersionUID = 6911585894112990613L;
        
        private Double def;
        
        public ActivationMap(Double def)
        {
            this.def = def;
        }
        
        @Override
        public Double get(Object key) 
        { 
            Double value = super.get(key);
            return value == null ? def : value;
        }
    }    
}