Each peer can be configured before start by either using a file with the settings in JSON format or by creating a map with the properties (see the [Structs](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/Structs.html) helper for some utility methods for creating such maps).

## Configuration Parameters ##

The following table lists all available, top-level configuration parameters. The relevant configurations for the networking interface or for the various bootstrapping operations are available elsewhere.

| **Property** | **Description** | **DefaultValue** |
|:-------------|:----------------|:-----------------|
| peerName     | A logical name for the peer. This is **part** of the peer's identity, but there's no requirement for it to be unique. | HGDBPeer         |
| bootstrap    | The list of bootstrapping operation to invoke upon startup. This list essentially defines what the peer will do. Each bootstrapping operations plugs in some behavior into the peer and custom bootstrapping is of course possible. More on the syntax and a list of predefined bootstrapping operations can be found below.  | none             |
| localDB      | The path to the local HGDB database this peer is bound to. When omitted, the peer will not be attached to a local database. | none             |
| interfaceType | The class that provides the peers interface. For the current XMPP implementation it should be `org.hypergraphdb.peer.xmpp.XMPPPeerInterface`|                  |
| interfaceConfig | A nested configuration object, specific to the `interfaceType` being used. The object is passed directly onto the `PeerInterface.configure` method. | null             |
| threadPoolSize | The number of threads to the peer. Note that this does not include threads normally created by HyperGraphDB itself for cache management and the like. Omit this parameter or specify a value <= 0 if you want to have an unbounded number of threads allocated on the fly - those threads will be cached, reused, and purged only if they remain idle for 60 seconds. | 10               |

## The `bootstrap` element ##

This configuration element lists implementations of the [BootstrapPeer](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/peer/BootstrapPeer.html) interface. It is an array of two element structures - one specifying the implementation class and the other an arbitrary nested configuration structure to be passed to the class's `bootstrap` method. For example:

```

{
...
"bootstrap" : [ {"class" : "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap", "config" : {}},
                {"class" : "org.hypergraphdb.peer.bootstrap.CACTBootstrap", "config" : {}},
              ]
...
}
```

This defines a list of two bootstrapping operations, the come with HyperGraphDB. The first is an almost mandatory operation that lets peers find each other is they come online, the configuration is empty in this case. The second, called CACTBootstrap, is used to initialize several primitive activities that allow peers to perform basic database operations against each other. Note that the configuration is assumed to be an object structure, it can't be a primitive literal or an array.