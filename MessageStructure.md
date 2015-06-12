# Introduction #

This document is intended to describe the structure of the messages that are exchanged between HGDB peers.

The core attribute of a message are defined in the [FIPA ACL Message Structure Specification](http://www.fipa.org/specs/fipa00061/SC00061G.html).

The most important attribute is the performative which defines the "speech act". We attempt to follow the semantics of communication performatives which is described in the [FIPA Communicative Act Library Specification](http://www.fipa.org/specs/fipa00037/SC00037J.html)


# Message Structure #

At the top level of any HGDB message the following attributes will be available:

| **Attribute name** | **Type** | **Description** | **Mandatory** |
|:-------------------|:---------|:----------------|:--------------|
| perf               | String   | The name of the **performative** associated with the message.  | YES           |
| act                | String   | The name of the **action**. Together with _Performative_ this defines the intent of the message (ex. "CallForProposal" for "REMEMBER" or "Accept" for "REMEMBER") | ~~YES~~ NO    |
| ~~task~~ replyWith | UUID     | Identifies the **task** that generated the messages. Multiple peers can receive/send messages related to the same task. | YES           |
| conv               | UUID     | If the message is part of a **conversation** identifies the conversation.  | NO            |
| replyTo            | String   | A description of the **end point address** of the peer that is interested in an answer. This depends on the specific implementation of the transport protocol, for example, for JXTA, it could be the pipe advertisement.  | NO            |
| content            | Object   | This contains any **additional data** the destination peer needs to fulfill the intent of the message. | NO            |
| ont                | String   | The ontology of the message. If not specified it defaults to the HGDB ontology, but plug-ins might be defining their own set of performatives/actions. | NO            |

For the JXTA end point the following attributes are available:

| **Attribute name** | **Type** | **Description** | **Mandatory** |
|:-------------------|:---------|:----------------|:--------------|
| type               | String   | The name of the end point type. By default this is PipeAdvertisement, but other types could be used, depending on the implementation | NO            |
| id                 | String   | the resource identifier of the end point. | YES           |
| name               | String   | the name of the end point. This is not mandatory but should be used for logging purposes. | NO            |

Implementations can add other attributes depending on their specific needs.