**NOTE** : the JXTA interface was the first developed for the P2P framework, but is no longer maintained and it was in fact removed from SVN to eliminate compile-time dependencies on the JXTA libraries. The code is still archived and it's still possible to eventually retrieve and restart development if the JXTA project itself picks up and becomes more reliable and user-friendly.

# The `jxta` element #

| **Property** | **Description** | **DefaultValue** |
|:-------------|:----------------|:-----------------|
| peerGroup    | The group to which this peer belongs | HGDBGroup        |
| jxtaDir      | The directory where the jxta PlatformConfig file is stored | .jxta            |
| advertisementTTL | The time-to-live property for advertisements created by the peer (milliseconds) | 10000            |
| needsRelay   | True if the peer is required to establish a connection with a relay before starting | false            |
| needsRendezVous | True if the peer is required to establish a connection with a rendezvous before starting | false            |
| mode         | The mode in which the peer will start. Can be one of the following: _ADHOC_ (peers are part of an ad-hoc network), _EDGE_ (the peer will use relays if they are configured), _RELAY_ (the peer will act as a relay), _RENDEZVOUS_ (the peer will act as a rendezvous), _RENDEZVOUS\_RELAY_ (peer will be both a rendezvous and a relay) | ADHOC            |
| relays       | A list with relays that can be used | empty list       |
| rdvs         | A list with rendezvous that can be used | empty list       |
| tcp          | Controls TCP transport settings (see below) | null             |
| http         | Controls HTTP transport settings (see below) | null             |

For the transport type, the following properties are available:
| **Property** | **Description** | **DefaultValue** |
|:-------------|:----------------|:-----------------|
| enabled      | Controls if the transport is on or off | true             |
| incoming     | Use transport for incomming connections | true             |
| outgoing     | Use transport for outgoing connections | true             |
| port         | The transport listening port | 9701 for tcp, 9901 for http |
| startPort    | The lowest port on which the TCP Transport will listen if configured to do so. Valid values are -1, 0 and 1-65535. The -1 value is used to signify that the port range feature should be disabled. The 0 specifies that the Socket API dynamic port allocation should be used. | 9701             |
| endPort      | The highest port on which the TCP Transport will listen if configured to do so. Valid values are -1, 0 and 1-65535. The -1 value is used to signify that the port range feature should be disabled. The 0 specifies that the Socket API dynamic port allocation should be used | 9799             |

If a more fine grained control over jxta is required, a PlatformConfig file can be placed in the configured jxta directory. HGDB will load that file and override the values configured in the HGDB config file.

## Configuration file example ##

### Minimal configuration ###

```
{
"hasLocalStorage"	: true,
"interfaceType"		: "org.hypergraphdb.peer.jxta.JXTAPeerInterface"
}
```

### Peer with name ###

```
{
"hasLocalStorage"	: true,
"interfaceType"	: "org.hypergraphdb.peer.jxta.JXTAPeerInterface",

"jxta"		:
{
	"peerName"	: "MyPeer",
	"peerGroup"	: "MyGroup"
}
}
```

### Configure the directories used by hgdb ###
```
{
"hasLocalStorage"	: true,
"localDB"	: "./hgdb/mainDb",
"tempDB"	: "./hgdb/tempDb",

"interfaceType"	: "org.hypergraphdb.peer.jxta.JXTAPeerInterface",

"jxta"		:
{
	"jxtaDir"	: "./hgdb/.jxta",
	"peerName"	: "MyPeer",
	"peerGroup"	: "MyGroup"
}
}
```

### Configure tcp and http settings ###
```
{
"hasLocalStorage"	: true,
"localDB"	: "./hgdb/mainDb",
"tempDB"	: "./hgdb/tempDb",

"interfaceType"	: "org.hypergraphdb.peer.jxta.JXTAPeerInterface",

"jxta"		:
{
	"jxtaDir"	: "./hgdb/.jxta",
	"peerName"	: "MyPeer",
	"peerGroup"	: "MyGroup",
	"tcp"		:
	{
		"enabled"	: true,
		"port"		: 9702,
	},
	"http"		:
	{
		"enabled"	: true,
		"port"		: 9703
	}
}
}
```

### Peer uses relay ###
```
{
"hasLocalStorage"	: true,
"localDB"	: "./hgdb/mainDb",
"tempDB"	: "./hgdb/tempDb",

"interfaceType"	: "org.hypergraphdb.peer.jxta.JXTAPeerInterface",

"jxta"		:
{
	"jxtaDir"	: "./hgdb/.jxta",
	"peerName"	: "MyPeer",
	"peerGroup"	: "MyGroup",
	
	"mode"		: "EDGE",
	"needsRelay" 	: true,
	"relays" 	: ["tcp://127.0.0.1:9711"]
}
}
```

### Peer uses rendezvous ###
```
{
"hasLocalStorage": true,
"localDB"	: "./hgdb/mainDb",
"tempDB"	: "./hgdb/tempDb",

"interfaceType"	: "org.hypergraphdb.peer.jxta.JXTAPeerInterface",

"jxta"		:
{
	"jxtaDir"	: "./hgdb/.jxta",
	"peerName"	: "MyPeer",
	"peerGroup"	: "MyGroup",
	
	"mode"		: "EDGE",
	"needsRendezVous" : true,
	"rdvs" 		: ["tcp://127.0.0.1:9711"]
}
}
```

### Peer is configured as rendezvous and/or relay ###

For rendezvous replace the value of the **mode** property with _RENDEZVOUS_; for relay and rendezvous replace with _RENDEZVOUS\_RELAY_

```
{
"hasLocalStorage"	: true,
"localDB"	: "./hgdb/mainDb",
"tempDB"	: "./hgdb/tempDb",

"interfaceType"	: "org.hypergraphdb.peer.jxta.JXTAPeerInterface",

"jxta"		:
{
	"jxtaDir"	: "./hgdb/.jxta",
	"peerName"	: "MyPeer",
	"peerGroup"	: "MyGroup",

	"mode"		: "RELAY",
	"tcp"		:
	{
		"enabled"	: true,
		"port"		: 9702,
	},
	"http"		:
	{
		"enabled"	: true,
		"port"		: 9703
	}
}
}
```