# Introduction #

This document is intended to give an example on how the work flow based framework can be used to create new tasks.

As a definition, a task is a unit of work that needs to be fulfilled by a peer. In order to complete the task, the peer will engage in (possibly) multiple conversations with its neighbors.

# Implementing a new task #

## Problem ##

Consider we use a ring structure (for whatever purpose - could be replication for example). The structure requires each peer to have a next and a previous neighbor and also requires that the last peer is followed by the first (and the first is preceded by the last).

The problem is how to implement a join mechanism - a peer wants "in" and, as such, must negotiate with two consecutive peers.

## Algorithm ##

The new peer (call it P) will send out message announcing that it wants to join the ring. Peers that are in the ring and are interested will reply with a proposal that will also contain the address of their successor. If peer P<sub>i</sub> replies and states that peer P<sub>j</sub> is its successor, and P decides to accept the proposal the following happens:

  1. P send an acknowledgment to P<sub>i</sub> asking it to not accept any other requests for a given period of time
  1. P sends a proposal to peer P<sub>j</sub> asking for permission to join as a predecessor
  1. If P<sub>j</sub> accepts, P will send acknowledgments to both P<sub>i</sub> and P<sub>j</sub> and everybody updates their data
  1. If P<sub>j</sub> rejects the request or fails to answer in a given time, P will send an acknowledgment to P<sub>i</sub> and start waiting for other peers.

## Work flow on peer P ##

The states of the task at peer P are:

| _Started_ | the peer is waiting for proposals to join the ring |
|:----------|:---------------------------------------------------|
| _FirstAccepted_ | the peer accepted a proposal from P<sub>i</sub> and started contacting P<sub>j</sub> |
| _Done_    | P<sub>j</sub> accepted and both P<sub>i</sub> and P<sub>j</sub> are acknowledged and the task is finished |


The states of the task at peer P<sub>i</sub> are:

| _Started_ | the peer received a call for proposal |
|:----------|:--------------------------------------|
| _Proposed_ | peer P was sent a proposal            |
| _Accepted_ | peer P accepted the proposal and requested to block for a given time |
| _Confirmed_ | peer P managed to contact P<sub>j</sub> |
| _Disconfirmed_ | peer P did not manage to contact P<sub>j</sub> or P<sub>j</sub> did not agree |
| _Done_    | The task is finished                  |

The states at peer P<sub>j</sub> are:

| _Started_ | the peer received a proposal from P |
|:----------|:------------------------------------|
| _Accepted_ | the peer accepted the proposal from P |
| _Confirmed_ / _Diconfirmed_ | P confirmed / disconfirmed          |
| _Done_    | Task is finished                    |

## Implementation ##

Because the steps in the conversations are allready implemented, we will use ProposalConversation

For the task on peer P the following need to be done:

1. Extend TaskActivity

2. Overwrite the startTask function. The implementation will have to declare when and how are conversation messages received + needs to send call for proposal messages to neighbors.

To declare workflow transitions based on conversation updates the registerConversationHandler function has to be called:

```
registerConversationHandler(State.Started, ProposalConversation.State.Proposed, "handleProposal", State.Working);
```

The line above states that whenever the task is in state _Started_ and a conversation sent a _Proposed_ message the _handleProposal_ function is called after the current state of the task is set to _Working_ (this ensures that no other messages are processed while executing the function). Note that if a _Proposed_ message arrives while the task is not in the _Started_ state, the message will be stored and considered if the task ever reaches the _Started_ state again.

The hadleProposal function will do the following:

```
public State handleProposal(AbstractActivity<?> fromActivity)
{
	ProposalConversation conversation = (ProposalConversation)fromActivity;
	
	//create reply message
	Object message = conversation.getMessage();
	Object reply = getReply(message);

	//ask to block for 1 second
	combine(reply, struct(BLOCK, 1000));
	conversation.accept(reply);
	
	//get the address of peer j from the message and send a proposal

	//return appropriate state
	return State.FirstAccepted;
}
   
```

At this point the task will accept _Accept_ and _Reject_ messages from P<sub>j</sub>

Add these lines to the _startTask- function.
```
registerConversationHandler(State.FirstAccepted, ProposalConversation.State.Accepted, "handleAccept", State.Working);

registerConversationHandler(State.FirstAccepted, ProposalConversation.State.Rejected, "handleReject", State.Working);

```_

```
public State handleAccept(AbstractActivity<?> fromActivity)
{
	ProposalConversation conversation = (ProposalConversation)fromActivity;
	
	//send confirm messages to Pi and Pj

	//update internal state

	//the task has ended
	return State.Done;
}
   
public State handleReject(AbstractActivity<?> fromActivity)
{
	ProposalConversation conversation = (ProposalConversation)fromActivity;
	
	//send disconfirm messages to Pi 

	//back to the begining...
	return State.Started;
}
```