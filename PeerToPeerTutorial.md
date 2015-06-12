This short tutorial walks you through the basics of setting a P2P network, gives some background on the communication framework used and finishes with a simple example of moving atoms from one peer to another.

## Configuring the XMPP Server ##

Any standard compliant XMPP server can be used in principle. Since we relying on the Smack client libraries by [Ignite Realtime](http://www.igniterealtime.com), we have used their [OpenFire](http://www.igniterealtime.org/projects/openfire/index.jsp) server so far and we highly recommend it. To install the server:

  1. [Download Openfire](http://www.igniterealtime.org/downloads/index.jsp#openfire) for your platform of choice and install it locally to your machine.
  1. Under 'bin' in the installation directory, start the `openfired` executable.
  1. An HTTP server is now available locally on port 9090. Hit that port with the browser.
  1. Go through the configuration steps in the web interface: selected the _Embedded Database_ for simplicity.
  1. At the end of the configuration process, the current version 3.6.4 prompts you to login to the admin console, but the password you selected doesn't work. Just kill the server and start it again, it will work.

Once the server is properly installed, the next step is configuring users and/or chat rooms so that peers can see each other. There are two ways that peers A and B can connect:

  1. Both A and B are registered users and they are in each other's roster (i.e. list of "friends").
  1. There is a chat room to which both A and B are participants.

Users and chat rooms can be configured with the web interface. The names and passwords that you choose will have to be specified in HGDB configuration files. Since the HGDB API doesn't offer means to manage rosters, they need to be administered directly with the server web console, or by some other means. Hence, option 2 is easier to get started. But for a fine-grained connectivity control (who talks to who), option 1 is better.

By default, the sever allows peers to create their accounts automatically or to login anonymously. The HyperGraphDB lets you do that as well. That is, if automatic registration is enabled, you can just pass in a username and a password to the HGDB and if it can't login, it will create an account with that username and password.

For tutorial purposes, create a single chat room called 'play' and leave all options to their default values.

## Configuring a Single HyperGraphDB Peer ##

A HyperGraphDB peer is configured with a set of parameters that can be passed as a JSON file (or runtime resource), or as a Java Map. The parameter values are simple types or nested maps (mirroring JSON's object nesting). Here we'll go with the more readable JSON, but you can do the same by manually putting data into a Java map. So create a text file called `hgp2p.json` with the following content:

```
{
"interfaceType"	: "org.hypergraphdb.peer.xmpp.XMPPPeerInterface",
"localDB": "c:/temp/hgdbpeertest",
"peerName"	: "HGDBPeer",
"interfaceConfig"		:
  {
    "user" : "hgtest",
    "password" : "password",
    "serverUrl"	: "myhost",
    "room" : "play@conference.myhost",
    "autoRegister" : true
  }
}
```

Since the framework is designed to support P2P protocols, the first thing to configure is the actual protocol used. This is done with the `interfaceType` parameter whose value is the class of the implementation of the [PeerInterface](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/PeerInterface.html). Then normally a peer is bound to a local database instance, so you specify that instance location as well with the `localDB` parameter. The `peerName` parameter gives a user friendly, display name of the peer, it is part of the peer's identity within the network, but there's no requirement that it be unique. Finally, we have the detailed configuration of the XMPP network interface where we specify the user, password, server address (here we omit the port because we use the 5222 default for XMPP) and the chat room where peers should login. Replace "myhost" with the name of your machine in the above configuration: while putting "localhost" will work for the `serverUrl` parameter, the chat room JID (Jabber ID) needs the actual machine name.

For a complete list of top level configuration options, please see the PeerConfiguration page. For a list of XMPP specific configuration options, see the [ConfigurationXMPP](ConfigurationXMPP.md) page.

## Starting Up a Peer ##

With the XMPP server and configuration ready, starting up a peer is a simple API call. The main entry point is the [HyperGraphPeer](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/HyperGraphPeer.html) class. You construct an instance of that class with the desired configuration and call its `startup` method:

```
public static void main(String[] args)
{
    File configFile = new File("hgp2p.json"); // or whatever directory you put that file in.
    HyperGraphPeer peer = new HyperGraphPeer(configFile);
    Future<Boolean> startupResult = peer.start();		
    try
    {
        if (startupResult.get())
	{
	    System.out.println("Peer started successfully.");
	}
	else
	{
	    System.out.println("Peer failed to start.");
	    peer.getStartupFailedException().printStackTrace(System.err);
	}
    } 
    catch (Exception e)
    {
        e.printStackTrace(System.err);
    }
}

```

As can be seen, the `HyperGraphPeer.startup` returns a `Future` object that will hold a boolean once the startup process is complete. This is because startup is executed in a separate thread. Call `Future.get` block until the result is ready. Whenever the peer fails to start for whatever reason (usually due to a misconfiguration), that reason is recorded in the `startupFailedException` property of the `HyperGraphPeer` instance.

Note that all threads in the peer's thread pool are daemon threads, so the above application simply joins the network and then exits.

## Making Peers Work - Background ##

Ok, now that we've managed to start a HyperGraphDB peer and we can easily imagine starting many such peers over a network, local or the whole internet, what is it that a peer can do? With the above configuration, it cannot do anything. A peer in a P2P network is not the same as a server or a service exposing an interface and answering client requests, though it can be made to behave like this. Rather, a peer is more akin to an autonomous agent that participates in conversations with other peers. In a given conversation between peers `A` and `B`, `A` may play the role of a service while `B` is the client, but those roles may be reversed in a different conversation between the same two peers.

To make a peer do something, one must configure in what kinds of conversations it will be able to participate. More generally, a peer engages in _activities_ in conjunction with other peers. Those activities are asynchronous, they can be triggered, interrupted and resumed at different points in time. Several peers may participate in a single activity in order to accomplish something, but the activity is always _initiated_ by a single peer. An activity may be something as simple as transferring an atom from one peer to another or as complex as coordinating a distributed graph traversal algorithm. Peers coordinate their participation in a given activity by sending each other messages.

Now, activities are categorized by _type_ where each _activity type_ is simply a Java class implementation that defines the conversation protocol for all activities of that type. Think of the activity type as a specific function that a peer is able to perform. Only, that function is not necessarily some predefined request-response sequence, but rather a full conversation involving potentially many messages flowing back and forth. One common approach to implementing activity types is as _finite-state machines_ and there's support for those. A detailed description of the framework managing peer activities and messages can be found in the CommunicationImplementation page. Here we will show how peers are configured to participate in various activity types and we'll give a code sample showing how one peer can send an atom to another.

First, some API basics about activities. Those are managed by a class called [ActivityManager](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/workflow/ActivityManager.html), available as `HyperGraphPeer.getActivityManager()`. In order to initiate or respond to any activity of a given type, first a peer must register that activity type the with the manager, e.g.:

```
peer.getActivityManager().registerActivityType("define-atom", DefineAtom.class);
```

The first argument is the name of the activity type. This type name must be unique amongst activity types as it is used to identify the specific activity type of a message. The second argument is a class extending the [Activity](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/workflow/Activity.html) class. Once the type is registered, the peer can initiate new activities of this type or respond to ones started by other peers.

It is possible to register activity types at any time, but customarily they are registered at startup as part of the peer's configuration. The startup configuration of a peer allows you to list several _bootstrapping operations_ that will get performed before the peer connects to the network. A bootstrapping operation is anything implementing the [BootstrapPeer](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/BootstrapPeer.html) interface. This offers a bit more generality than simply listing activity types to pre-register - one can do any kind of initialization in the `BootstrapPeer.bootstrap(HyperGraphPeer peer, Map config)` method. Besides, in some cases activity types are logically linked together and must be registered as a single operation.

## Making Peers Work - An Example ##

Bootstrapping is configured as a top-level parameter, listing all bootstrapping implementations together with their configuration object (an arbitrary JSON object):

```
// file hgp2p2.json
{
"peerName" : "mysimplepeer",
"bootstrap" : [ {"class" : "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap", "config" : {}},
                {"class" : "org.hypergraphdb.peer.bootstrap.CACTBootstrap", "config" : {}},
              ]
...etc.
}
```

For a peer to do anything useful it must be made aware of other HyperGraphDB peers (by default it ignores any other XMPP presense). This is done by special built in activity called `AffirmIdentity` that needs to be bootstrapped in all configurations, as done here. The other bootstrapping listed above, `CACTBootstrap`, stands for "Common Activities Bootstrap" initializes several basic activity types, including the `DefineAtom` activity type mentioned in the previous section. As you can see, the class name of the `BootstrapPeer` implementation is required as well as the configuration to be passed to its sole method. In this case, the configuration is empty in both of the elements listed.

So, once we have some activities setup with the peer, we can put them to work. Let's take the simple example of the `DefineAtom` activity, startup two peers and have one of them send an atom to the other. First, add the above "bootstrap" configuration element to the JSON file you created, and then create a second JSON file as a modified copy of the first like this:

```
"interfaceType"	: "org.hypergraphdb.peer.xmpp.XMPPPeerInterface",
"localDB": "c:/temp/hgdbpeertest2",
"peerName"	: "HGDBPeer2",
"bootstrap" : [ {"class" : "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap", "config" : {}},
                {"class" : "org.hypergraphdb.peer.bootstrap.CACTBootstrap", "config" : {}},
              ]
"interfaceConfig"		:
  {
    "user" : "hgtest2",
    "password" : "password",
    "serverUrl"	: "myhost",
    "room" : "play@conference.myhost",
    "autoRegister" : true
  }
}
```

Note that here, we change the database location, peer name and XMPP username so as not conflict with our other peer. Then we can create a main program for each peer, or simply start them up both within a single main program (read the comments throughout):

```
// Start a peer out of a given configuration file. This will have as a side
// effect to open the underlying database if not already opened, execute
// all bootstrapping operations found in the configuration and connect to
// the network.
private static HyperGraphPeer startPeer(File configFile)
{
    HyperGraphPeer peer = new HyperGraphPeer(configFile);
    Future<Boolean> startupResult = peer.start();		
    try
    {
        if (startupResult.get())
	{
	    System.out.println("Peer started successfully.");
	}
	else
	{
	    System.out.println("Peer failed to start.");
	    peer.getStartupFailedException().printStackTrace(System.err);
	}
    } 
    catch (Exception e)
    {
        e.printStackTrace(System.err);
    }
}

public static void main(String[] args)
{
    // Let's startup our two peers. 
    HyperGraphPeer peer1 = startPeer(new File("hgp2p.json"));
    HyperGraphPeer peer2 = startPeer(new File("hgp2p2.json"));
  
    // Add some atom to the graph of the first peer.
    HGHandle fromPeer1 = peer1.getGraph().add("From Peer1");

    // Have our first peer initiate a "define-atom" activity which
    // will trigger a "HyperGraph.define" operation with the specified
    // atom at the other peer. 
    //     
    // The DefineAtom constructor takes the initiating peer as its
    // first argument, the handle of the atom to be send as its second
    // argument and the identity of the receiving peer.
    //
    // The the newly constructed activity is passed onto the ActivityManager's
    // initiate method which will take it from there.
    peer1.getActivityManager().initiateActivity(
    		new DefineAtom(peer1, fromPeer1, peer2.getIdentity()));

    // 2 seconds should be enough in a single machine to transfer the atom
    try { Thread.sleep(2000); } catch (Throwable t) { }

    // Let's check that the atom was properly transferred. 
    String received = peer2.getGraph().get(fromPeer1);
    if (received != null)
        System.out.println("Peer 2 received " + received);
    else
    	System.out.println("Peer 2 failed to receive anything.");

}

```