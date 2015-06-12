## Introduction ##

Flow-based programming is a powerful computational model that is becoming ever more relevant  in today's highly concurrent multi-core, distributed systems. It is more powerful and as scalable as something like map-reduce. The premise is very simple: computation is described in terms of black-box processes with well defined inputs and outputs. Each process runs in its own thread, reads inputs from a set of input channels, does something and then writes output to a set of output channels. There is no globally shared data and therefore there are no race conditions possible. Thus, parallelism is inherent to this model and arbitrary computations can be represented with it. There have been attempts in the past to develop the idea as a fundamental programming paradigm (a book, probably easily findable around the internet, that describe the idea in detail is called "Flow-Based Programming" as I remember). More recently, some research on live systems where computation is immediately effective as the programmer edits the program use the same idea. Programming lower-level logic in a flow-based manner can seem cumbersome and inefficient. But for a coarse-grained type of processing, especially in a distributed setting, the paradigm is powerful and convenient.

The basic idea is that a processing node has a bunch of incoming streams of data and bunch of outgoing streams. It continuously receives data from its input and it writes to one or more of its outputs. A simple way to view this is that the incoming streams provide a continuous source of parameters for a function to perform a computation and produce a continuous stream of results. The advantages of the flow-based approach are that parallel asynchronous processing and memory management (disposal of intermediary results that are no longer needed) are obtained for free as inherent to the model. Also, processing based on a flow-based model will scale seamlessly on many CPUs and machines. The disadvantage is that the programming logic may seem unnatural at first and it may prove difficult to live without a global variable namespace. In any case, in our implementation, each processing node has access to an arbitrary global context object provided by the application - this opens the door to working with global, shared state if need be in isolated and carefully controlled cases such as outputting the final results of the computation to a database, or accessing some global, read-only parameters.

The dataflow HyperGraphDB application component offers an implementation of this concept that is distributed across machines. The flow network of processes and channels can be thought of as a directed hypergraph, where each channel is a directed hyperedge connecting all processes writing to it and all processes reading from it. In the distributed version, it is in fact represented as such.

The core, in-process part of the framework, however, is **independent** of HyperGraphDB and can be used standalone. The distributed version uses the HyperGraphDB peer-to-peer framework for communication and also persists the topology in the local database instance of each peer. The intended use is data processing with HyperGraphDB as ultimate storage, but that doesn't have to be the case - processing results can be stored in files, RDBMs or whatever. It is in theory possible not to persist anything and dispense with having HGDB database instances altogether.

This module was initially developed for a NLP (natural language processing) project called [Disko](http://code.google.com/p/disko).

## API Overview ##

The API resides as a HyperGraphDB app called `dataflow` alongside the other HyperGraphDBApplications. The main package of the code is as expected:

`org.hypergraphdb.app.dataflow`


The main classes of interest are the following:

| **Class** | **Description** |
|:----------|:----------------|
| [DataFlowNetwork](http://www.hypergraphdb.org/docs/apps/dataflow/org/hypergraphdb/app/dataflow/DataFlowNetwork.html) | Represents a data flow network: essentially the program implemented using this computational model |
| [Processor](http://www.hypergraphdb.org/docs/apps/dataflow/org/hypergraphdb/app/dataflow/Processor.html) | The interface that a processing node in the network must implement. A processing node takes a bunch of inputs and produces a bunch of outputs. The logic that it implements can be arbitrarily complex and coarse grained, or something as simple as adding two numbers. |
| [Channel](http://www.hypergraphdb.org/docs/apps/dataflow/org/hypergraphdb/app/dataflow/Channel.html) | Represents a communication channel between processing nodes, or a _pipe_ if you will. A channel can be written to and read from by multiple processing nodes. It is also capable of buffering data that flows through it and blocking writes until that data is being processed. A channel carries a specific type of data and only that type of data. A special value represent EOS (end-of-stream) must be specified when the channel is constructed.  |
| [InputPort](http://www.hypergraphdb.org/docs/apps/dataflow/org/hypergraphdb/app/dataflow/InputPort.html) | Represents a _read_ connection between a channel and a processing node. A node reads from a channel through an `InputPort`. |
| [OutputPort](http://www.hypergraphdb.org/docs/apps/dataflow/org/hypergraphdb/app/dataflow/OutputPort.html) | Represents a _write_ connection between a channel and a processing node. A node writes to a channel via an `OutputPort`. |

To create a network, one needs to implement the processing nodes as implementations of the `Processor` interface. The `AbstractProcessor` class can be extended instead to get a default implementation of the `getName` method. This is convenient when troubleshooting because the processor name becomes the thread name of the thread in which it's executing, and this makes thread dumps more readable.

Then one needs to connect those processing nodes through communication channels. The connections are established through [ports](http://www.hypergraphdb.org/docs/apps/dataflow/org/hypergraphdb/app/dataflow/Port.html) - input ports or output ports. Let's take a look at the `Processor.process` method:

```
public void process(ContextType context, Ports ports) throws InterruptedException
{
   // Context is the global, arbitrary object that the network instance passes 
   // from the application onto all processing nodes. It's application specific and
   // can be anything.

   // The 'ports' parameter represents all input and output connections to channels 
   // in the network. Since each channel has a unique name, the ports are retrieved 
   // by name. Also, since each channel carries potentially a different type of data
   // ports are parameterized by this data type. 

   // To obtain an input port connected to a given input channel, named "inData" and
   // that carries data of type some class A:
   
   InputPort<A> in = ports.getInputPort("inData");

   // to get the next input blocking until there's some available:
   A a = in.take();

   // to check if there's no more input
   if (in.isEOS(a))
     .... do something...maybe simple return 

   // to check if there's data available
   a = in.poll();

   // An input is also an Iteratable so you can read all data in a for loop:
   for (A a : in)
     .... do something with a


   // To obtain an output port:
   OutputPort<B> b = ports.getOutputPort("outData");
   
   // to write to it:
   b.put(new B(a));

   // Any port can be closed at any time.
   in.close();
}
```

When a `Processor` implementation exits, the framework automatically closes all its ports (input and output). When all the input ports of a channel get closed, it will automatically close all its output ports, and vice-versa. That is, when a channel determines that nobody's going to write to it anymore, it starts sending EOS to its readers. Conversely, when it determines that nobody is going to read from it, it stops accepting writes and throws an exception when a write attempt is made. This behavior lets you manually shutdown some parts of network while other parts remain active.

## Sample Application ##

Let's take a look at a very simple network, available as the `sample.dataflow.DataFlowArithmetic` in the samples directory of the codebase.

This is a very simple example demonstrating the basic data flow API. The main program constructs a network that takes a single stream of integers input and calculates the sums of all its even numbers, all its odd numbers and the total sum. To do that, it splits the input into a stream of even numbers and a stream of odd numbers. The sum of each of those streams is accumulated separately and finally put back together.

The processing nodes here are implemented by a few very simple nested classes, for illustration purposes only. All channels have the same data type: Integer, and the network doesn't have any global context, so it's null. Here's the simplest processor that only reads a single input and prints it to stdout:

```
	public static class Printer extends AbstractProcessor<Object>
	{
		private String fromChannel;
		private String prefix;
		public Printer() { }
		public Printer(String prefix, String fromChannel) 
		{ 
			this.fromChannel = fromChannel;
			this.prefix = prefix;
		}
		
		public void process(Object ctx, Ports ports) throws InterruptedException
		{
			InputPort<Integer> in = ports.getInput(fromChannel);
			for (Integer x = in.take(); !in.isEOS(x); x = in.take())
				System.out.println(prefix + x);
		}		
	}
```

The processor is constructed with the name of input channel from which it gets its data and write it prefixed by another construction time parameter. Since the channel name is not hard-coded in the `process` method, this printer may be attached at different channels. The channel names can be hard-coded though to save some typing for processors that won't have multiple instances at different places in the network, like the processor that split numbers into even and odd:

```
	public static class SplitParity extends AbstractProcessor<Object>
	{
		public void process(Object ctx, Ports ports) throws InterruptedException
		{
			InputPort<Integer> in = ports.getInput("random-stream");
			OutputPort<Integer> outEven = ports.getOutput("even-numbers");
			OutputPort<Integer> outOdd = ports.getOutput("odd-numbers");
			for (Integer x : in)
			{
				if (x % 2 == 0)
					outEven.put(x);
				else
					outOdd.put(x);
			}
		}		
	}
```

So this processor has a single input and two outputs. A processor doesn't have to match every single "input event" with an "output event". The `Accumulator` processor for example reads all of its input, summing it up and finally writing it to its designed output channel:

```
	public static class Accumulator extends AbstractProcessor<Object>
	{
		private String inputChannel;
		private String outputChannel;
		
		public Accumulator() { }
		public Accumulator(String inputChannel, String outputChannel)
		{
			this.inputChannel = inputChannel;
			this.outputChannel = outputChannel;
		}
		
		public void process(Object ctx, Ports ports) throws InterruptedException
		{
			InputPort<Integer> in = ports.getInput(inputChannel);
			OutputPort<Integer> out = ports.getOutput(outputChannel);
			int total = 0;
			for (Integer x : in) total += x;
			out.put(total);
		}
	}
```

Finally, the `Sum` process, as trivial as the others:

```
	public static class Sum extends AbstractProcessor<Object>
	{
		private String left, right, output;
		
		public Sum() { }
		public Sum(String left, String right, String output)
		{
			this.left = left;
			this.right = right;
			this.output = output;			
		}
		
		public void process(Object ctx, Ports ports) throws InterruptedException
		{
			InputPort<Integer> inLeft = ports.getInput(left);
			InputPort<Integer> inRight = ports.getInput(right);
			OutputPort<Integer> sumOut = ports.getOutput(output);
			for (Integer x : inLeft)
			{
				Integer y = inRight.take();
				if (inLeft.isEOS(y))
					throw new RuntimeException("Sum is only defined on pair of numbers.");
				sumOut.put(x + y);
			}
		}
	}
```

Note how here we are checking that for every input from the "left" channel, we are checking that we do indeed have an input from the "right" channel. There's no mechanism in the framework that ensure this type of synchronization. The exception thrown from within the processor will simple cause the network to fail and close all channels.

Finally, here's how the main program looks. See comments inside:

```
	public static void main(String argv[])
	{
		// All channels work with the same data type (Integer) for all inputs and
		// outputs. And we postulate the value Integer.MIN_VALUE will serve as an
		// End-Of-Stream marker.
		Integer EOF = Integer.MIN_VALUE;
		
		// To construct a network, create a DataFlowNetwork instance, parameterized
		// by the type of global context that all processing nodes share. In this
		// case we are not using any global context, so we leave the type as Object.
		DataFlowNetwork<Object> network = new DataFlowNetwork<Object>();
		
		// First we add all the data channels to the network. Each channel is
		// uniquely identified by a name and it needs a special end-of-stream marker.
		
		// The channel that serves as the main input to the whole network.
		// There may be multiple such channels that get data from the "outside world".
		// Here we have only one.
		network.addChannel(new Channel<Integer>("random-stream", EOF));
		
		// The channel where the even number of the input stream go.
		network.addChannel(new Channel<Integer>("even-numbers", EOF));
		
		// The odd numbers channel.
		network.addChannel(new Channel<Integer>("odd-numbers", EOF));
		
		// etc..		
		network.addChannel(new Channel<Integer>("even-sum", EOF));
		network.addChannel(new Channel<Integer>("odd-sum", EOF));
		network.addChannel(new Channel<Integer>("total-sum", EOF));
		
		// Next, we add the processing nodes using network.addNode. 
		// The first argument of that method is
		// an arbitrary object implementing the Processor interface.
		// The second argument specifies all input channels that the processor
		// reads from and the third argument specifies all the output channels
		// that the processor writes to. The network will create a port
		// for each of those channels and pass it in the Ports argument 
		// the Processor.process implementation.
		
		network.addNode(new SplitParity(), 
						new String[]{"random-stream"}, 
						new String[]{"even-numbers", "odd-numbers"});
		network.addNode(new Accumulator("even-numbers", "even-sum"), 
						new String[]{"even-numbers"}, 
						new String[]{"even-sum"});
		network.addNode(new Accumulator("odd-numbers", "odd-sum"), 
				new String[]{"odd-numbers"}, 
				new String[]{"odd-sum"});
		network.addNode(new Sum("even-sum", "odd-sum", "total-sum"), 
				new String[]{"even-sum", "odd-sum"}, 
				new String[]{"total-sum"});
		
		// We now attach a printer to each of the "sum" channel to output the results.
		// Note that the results will be printed out in some random order depending on thread
		// scheduling.
		network.addNode(new Printer("Even sum=", "even-sum"), new String[]{"even-sum"}, new String[0]);		
		network.addNode(new Printer("Odd sum=", "odd-sum"), new String[]{"odd-sum"}, new String[0]);
		network.addNode(new Printer("Total sum=", "total-sum"), new String[]{"total-sum"}, new String[0]);
		
		try
		{
			// When we start the network, it creates a thread pool for the 
			// processor to use and it opens all channels for read and write. 
			// The network will remain in "working mode" until EOS is written
			// to all its ports. The returned Future from the start method can
			// be used to cancel processing, wait for it etc. The result is true/false
			// depending on whether it completed gracefully or not.
			Future<Boolean> f = network.start();
			
			// So we just write some random integers to the "entry" channel directly.
			Channel<Integer> ch = network.getChannel("random-stream");
			for (int i = 0; i < 1000; i++)
			{
				ch.put((int)(Math.random()*10000));
			}
			
			// And we close it, triggering a cascading close operation of all 
			// ports downstream in the network.
			ch.put(EOF);
			System.out.println("Network completed successfully: " + f.get());
			
			// At this point, the network is inactive, but it can be restarted again. 
			f = network.start();
			ch = network.getChannel("random-stream");
			for (int i = 0; i < 1000; i++)
			{
				ch.put((int)(Math.random()*10000));
			}
			ch.put(EOF);						
			System.out.println("Network completed successfully: " + f.get());
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
		finally
		{
			// shutdown the thread pool of the network so the application can exit gracefully.
			network.shutdown(); 
		}
	}
```


## Distributed Version ##

TODO -  the API needs some cleaning up and documentation as well as the sample app needs to have a distributed version as a showcase. When that's done, we'll add the appropriate description here.