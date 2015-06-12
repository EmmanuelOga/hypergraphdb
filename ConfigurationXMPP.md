The XMPP-based network layer for the HyperGraphDB P2P framework uses the Smack library from [Ignite Realtime](http://www.igniterealtime.org/). It is specified by setting the full classname as the `interfaceType` property and configured with the `interfaceConfig` property of the top-level P2P configuration:

```
{
"hasLocalStorage"	: false,
"localDB": "c:/temp/hgdbpeertest",
"bootstrap" : [ {"class" : "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap", "config" : {}},
                {"class" : "org.hypergraphdb.peer.bootstrap.CACTBootstrap", "config" : {}},
                {"class" : "org.disco.utils.ScriptingBootstrap",
                        "config": {}
                }
                
              ],
"interfaceType"	: "org.hypergraphdb.peer.xmpp.XMPPPeerInterface",
"interfaceConfig"		:
  {
    "user" : "bolerio",
    "password" : "password",
    "serverUrl"	: "localhost",
    "ignoreRoster" : true,
    "room" : "testroom@conference.localhost",
    "port"	: 5222
  }
}
```

The `interfaceConfig` holds all options that XMPPPeerInterface recognizes. They are:

|<b>Name</b>|<b>Description</b>|<b>Default</b>|<b>Mandatory</b>|
|:----------|:-----------------|:-------------|:---------------|
|serverUrl  |The hostname of the XMPP server.|none          |Y               |
|port       |The port at the XMPP server.|5222          |N               |
|user       | The username at the XMPP server.|none          | N              |
|password   | The password at the XMPP server.|none          |N               |
|anonymous  |Whether to login as anonymous user.|false         |N               |
|autoRegister|Whether to attempt registration with user/password if login fails.|false         |N               |
|room       |A chat room to join and get the peer list from there.|none          |N               |
|ignoreRoster|Whether to ignore the peer in this user's roster.|false         |N               |
|fileTransferThreshold|The number of bytes above which messages will be sent as file transfers via XMPP.|100\*1024     |N               |

### Login Behavior ###

A peer needs to login to an XMPP server. This can be done simply by settings `anonymous` to true, but it's usually better to have a named user for system monitoring and debugging. If you put a `user` you need to put a `password`. If your XMPP server is configured to allow registration, you can set `autoRegister` to true for users that haven't been preregistered. A common practice is to use the hostname of the computer running the peer.

### Who Are the Peers? ###

Normally, an XMPP user has a roster which is essentially a list of friends - other XMPP users that are notified about this user's presence. By default, the peers available will be precisely the peers in the current user's roster. This gives fine-grained control of who sees and can communicate with whom. We don't provide an API to manage rosters. This is done either from your XMPP server's administrative interface or by using directly the Smack API. For two peers to see each other under this model, they must each be in each other's rosters.

Another option is to configure a chat room and let everybody that joins in be peers. The chat room must be created in your server's admin or with the Smack API, but once it is created you simply specify its JID (Jabber ID) as the `room` parameter. When this parameter is present the XMPPPeerInterface will try to join the room and monitor all peers in it.

If a user has both a roster and it joins a room, the peers from both the roster and the room form the HGDB peers of the user. If you want only the room peer, set the `ignoreRoster` parameter to true.

### What server can I connect to? ###

This depends on the application at hand etc. For local development or experimentation, check if there's an XMPP server at your organization that you could use, or, better, get the OpenFire server [Ignite Realtime](http://www.igniterealtime.org), start it up locally and have fun. It has a very nice and easy to use admin interface.