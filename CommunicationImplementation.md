# Introduction #

HyperGraph peers communicate through structured messages following a broad communication framework based on the Agent Communication Language (ACL) established by the FIPA standard (see http://www.fipa.org), which itself evolved from the Knowledge Query and Manipulation Language (KQML - see http://www.cs.umbc.edu/kqml/). In essence, the message framework defines a set of communication (or speech) acts that roughly formalise the intention of the communicating agent.

Following the agent metaphor, conceptually each communication act is performed within the context of accomplishing a given goal (or task). The "desire" to accomplish such a goal is always going to be initiated by one of the peers. The context of communication between peers is captured as an _activity_. Each such activity has a local representation at each peer participating in it, and is identified by a UUID (Universally Unique Identifier).

Thus, peers perform tasks (i.e. accomplish goals) by running asynchronous activities locally and communicating their state and results via speech act messages. The flow of those activities is orchestrated by a framework that provides the following elements:

  1. A peer-to-peer infrastructure based on the XMPP standard (see http://xmpp.org/), specifically using the Smack library from http://www.igniterealtime.org/.
  1. Message formatting and parsing using JSON syntax (see http://www.json.org). Serialization for common runtime HyperGraphDB objects (such as queries), Java beans as well as HyperGraphDB atoms is implemented and it is possible to plug in custom object serialization.
  1. Pluggable behaviour in the form of implementation of a `BootstrapPeer` interface.
  1. Management and scheduling of activities. Activities can be implemented either as "roll your own" general message handlers or as framework-managed finite-state machines. Activities can also be in a parent-child relationship.
  1. A set of predefined services such as replication and a straightforward client-server access to HyperGraphDB instances.


# Activities #

Activities in the HyperGraphDB peer-to-peer framework are the asynchronous processes through which peers communicate. An activity will generally represent a particular task to be accomplished or a sub-task of a larger task. An activity spans any number of peers at any given time.

Activities are implemented by extending the `org.hypergraphdb.peer.workflow.Activity` class and implementing the two abstract methods:

```
public class Activity
{
    // ...
    public abstract void initiate();
    public abstract void handleMessage(Message message);

    //...
}
```

The `initiate` method is called only at the peer first starting the activity and the `handleMessage` is called every time a message pertaining to that activity is received. The content and semantics of the `Message` argument is described in the MessageStructure topic.

Additionally, it is possible to override the `getType()` method to provide a custom type name for an activity's type. By default, it is the fully-qualified classname, which has several drawbacks: it is harder to read by humans, it is tied to a one class implementation at all peers and it is Java language dependent.

## Activity Types and Factories ##

Activity type names are not just for display purposes. The framework needs to be able to create activity instances given a type name and an incoming message. This is accomplished by providing the framework with an `ActivityFactory` for each activity type:

```
package org.hypergraphdb.peer.workflow;
public interface ActivityFactory
{
    Activity make(HyperGraphPeer thisPeer, UUID id, Message msg);
}
```

Activity types themselves are represented by the `org.hypergraphdb.peer.workflow.ActivityType` class whose purpose is to contain meta information about a particular activity type. In addition to the type name and activity factory, it holds a transition map needed for implementing FSM activities and ignored otherwise. The `ActivityType` may be extended in the future to contain further information.

Each activity type must be registered at bootstrap time by calling one of the `ActivityManager.registerActivityType`  methods. Note that some of the versions of this method do not take an `ActivityFactory` parameter. In those cases, the framework provides the factory itself using Java reflection. The Java Class of the activity is expected to have a constructor with one of 3 forms:

```
public MyActivity extends Activity
{
   public MyActivity(HyperGraphPeer peer) { }
  
   or 

   public MyActivity(HyperGraphPeer peer, UUID activityId) { }

   or

   public MyActivity(HyperGraphPeer peer, UUID activityId, Message msg) { }
}
```

The third form is used preferably, then the second, then the first. Given that an activity implementation has such a constructor, it can be registered simply by calling:

```
    thisPeer.getActivityManager().registerActivityType(MyActivity.class);
```

## Activities Lifecycle ##

Activities accomplish their task in discrete units of work that are scheduled to run in the peer thread pool. The lifetime and scheduling of activities is orchestrated by the peer-wide instance of `org.hypergraphdb.peer.workflow.ActivityManager` (accessible through `HyperGraphPeer.getActivityManager()`). In order to ensure proper sequencing/ordering of those units of work, each activity has an associated queue of actions to be performed. In general, an action simply amounts to the handling of incoming message and modifying the internal activity state somehow, but it is possible to add actions to the queue that are unrelated to messages.

The ActivityManager is responsible for handling all incoming messages and dispatching them according to the following algorithm:

  1. If the message refers to an activity already existing locally, then schedule an action to handle with that activity's own queue.
  1. Otherwise, create a new activity, **change its workflow state to Started** and schedule the message handling action only then.

Activities are identified between peers through a UUID that is created at the peer initiating the activity. Initiation itself must be done by calling one of the `ActivityManager.initiateActivity` methods - the different versions allow you to specify a parent activity and/or an event listener for the activity lifecycle (see `org.hypergraphdb.peer.workflow.ActivityListener`). The initiation process amounts to the following:

  1. Add the activity to the list of currently running activities and insert into the scheduler.
  1. Change its state to started.
  1. Call its `initiate` method.

Once an activity is started, the peer is free to send messages with it. In particular, initial messages should be send in the implementation of the `initiate` method. All messages sent in the context of that activity will carry its ID so that other peers can create and subsequently identify their own local representations.

Each activity is always in one and only one symbolic _workflow_ state that roughly defines its lifecycle. The state is accessible as `Activity.getState()`. Activity implementations then maintain any additional needed state as member variables of the implementing class. Workflow states are detailed in the next section.

The result of the computation performed by an activity is represented by the `org.hypergraphdb.peer.workflow.ActivityResult` class. The result essentially just holds a reference to the activity instance as well as the exception that possibly caused it to fail. When you call `ActivityManager.initiateActivity(...)`, you get back a `Future<ActivityResult>` which you can store somewhere, track when the activity has finished or do a `get()` and wait for the activity to finish. The latter is not advisable since an activity's completion will depend upon remote communication and a call to `Future.get()` would pause the current thread for a very long period of time. So, use it only when it really makes sense. Another way to track when an activity completes is to register a listener with its WorkflowState:

```
   activity.getState().addListener(new StateListener() { // etc.. see Javadocs for exact API });
```

Yet another way is to register an `ActivityListener` when calling `initiateActivity`. For example:

```
   thisPeer.geActivityManager().initiateActivity(myActivity, 
      new ActivityListener()
      {
           void activityFinished(ActivityResult result)
           {
              System.out.println("Activity " + result.getActivity().getId() + " finished.");
              if (result.getException() != null) 
                 System.out.println("With exception: " + result.getException());
           }
      }
```

The last method is most convenient when you are just interested to do something when an activity has finished executing and you don't want to keep track of the `Future` returned by the `initiateActivity` method.

[[TODO - document cancellation when implement. How does cancellation at one peer affect the activity at other peers, performative used to signal it etc.](.md)]

## Workflow States ##

Finite state machines (FSMs) are a generally good paradigm for implementing asynchronous processes. And even if a particular activity implementation is not entirely based on the FSM concept, its lifecycle will always follow a pattern where the activity is created, started and then it finishes (successfully or not). So the framework mandates the use of a few basic workflow states to capture this very pattern. And it also allows for the definition of an arbitrary number of custom ones so as to facilitate implementations following the FSM paradigm. The predefined states are summarized in the following table:

| **State Constant** | **Description** |
|:-------------------|:----------------|
| Limbo              | This state represents activities that have been created as Java objects, but are not actually running because they haven't been initiated. |
| Started            | Represents activities that are currently running, that have been initiated either originally by the current peer or through a message that was received by some other peer. This state is not the only possible state representing a running activity. All custom defined states also represent a currently running (i.e. "active") activity. |
| Completed          | Represents a state where the activity has completed successfully. Once an activity enters this state, it cannot be changed. |
| Failed             | Represents a state where the activity has failed to complete, most likely due to some exception. Once an activity enters this state, it cannot be changed. |
| Canceled           | Represents a state where the activity that was explicitly canceled. Once an activity enters this state, it cannot be changed. |

The `Limbo` state is in effect only after a new activity was created at a peer, but hasn't been scheduled to execute yet. As soon as it scheduled to execute, its state is changed to `Started` by the framework. Once an activity leaves the `Limbo` state, it can never return to it. The three states `Completed`, `Failed` and `Canceled` are terminal in that once an activity enters them, it can't go to another state and it will never be scheduled for execution again.

Note that workflow states, and for that matter any other internal implementation state data, are **local** to a peer. Thus while one peer may have an activity in a `Failed` state, another may have it in a `Started` state.

Workflow states are implemented by the `org.hypergraphdb.peer.workflow.WorkflowState` class which provides atomic operations for changing the state and allows for listeners of state changes to be registered:

```
public class WorkflowState
{
   // ....

   // Change the state the 'newState' only if it's currently 'oldState'
   public boolean compareAndAssign(WorkflowStateConstant oldState, WorkflowStateConstant newState)

   // Change the state to 'newState' regardless of current state
   public void assign(WorkflowStateConstant newState)
}
```

both of the above methods will throw an exception if the instance is currently in one of the terminal states.

The `WorkflowStateConstant` type of the parameters that you see above is that type of all workflow state constants. The implementation is akin to an open-ended enum - a pool of constants is maintained and any custom workflow state constant must be explicitly registered with the pool before it's being used by calling the static `WorkflowState.makeStateConstant(String name)` method. Each constant is identified by a name and this method ensures that there are no duplicate state constants so it is safe to compare them using the Java `==` operator. Given a name, you can retrieve the constant from the pool with `WorkflowState.toStateConstant(String name)` which will throw an exception if there's no constant with this name.

The `WorkflowStateConstant` is simply an immutable version (an extension) of `WorkflowState`. To create a brand new mutable `WorkflowState`, call one of the static methods `WorkflowState.makeState(WorkflowStateConstant c)` or `WorkflowState.makeState()`. The latter method assumes a default initial value of `WorkflowState.Limbo`.

To create a custom state constant, it is customary to simply declare them as final static fields in some class:

```
    public static final WorkflowStateConstant Started = WorkflowState.makeStateConstant("Started");
    public static final WorkflowStateConstant Completed = WorkflowState.makeStateConstant("Completed");
    // etc...
```

The names that you give are case-sensitive. That may be important if you are using Java annotations in combination with the provided `FSMActivity` implementation.

Each mutable WorkflowState can have any number of `StateListener`s attached to it. Whenever the value of the state changes, listeners are called in the order in which they were added. The framework itself relies on workflow state listeners to track an activity's state and decide when to remove it from scheduling etc.

## FSMActivity ##

The `FSMActivity` base class offers an easy way to implement activities using the finite-state-machines paradigm. Each FSMActivity moves from workflow state to workflow state based on the handling of external events. There are two types of such external events: (1) messages received from other peers and (2) State changes in sub-activities (i.e. activities to which the FSMActivity is a parent). Both kinds of events result in an action and a possible state change. Event handling is done by implementing the following general interface:

```
public interface Transition
{
    public WorkflowStateConstant apply(Activity activity, Object...args);
}
```

which represents a transition in the state-machine. Thus a transition takes as arguments the `Activity` instance as well as an arbitrary number of arguments depending on the kind of event that the transition handles.

There are two ways to define such transitions: by directly calling one of the `setTransition` methods in the `ActivityType`'s transition map, or by declaring methods in the implementation class that obey a predefined prototype and annotating those methods with appropriate Java annotations.

Transition methods that implement message-triggered transition have the following prototype:

```
    // Return the new state of the activity based on the message received.
    public WorkflowStateConstant onFoo(Message message);
```

and they can be annotated with the following annotations:

| @FromState | One or more input states. The transition will triggered only if the activity is in one of those states |
|:-----------|:-------------------------------------------------------------------------------------------------------|
| @OnMessage | A set of message attributes (name=value pairs). The transition is triggered only if the incoming message has those attributes with exactly those values.|
| @PossibleOutcome | A list of possible output states. This is purely for informative purposes and could be enforced at some point by the framework. |

Transition methods that implement a sub-activity triggered transition have the following prototype:

```
    // Return the new state of the activity based on the current state of the sub-activity
    public WorkflowStateConstant onFoo(Activity subActivity);
```

and they can be annotated with the `@FromState` and `@PossibleOutcome` annotation as well, but instead of `OnMessage`, two additional annotations are defined for them:

| @AtActivity | The typename of the sub-activity that will trigger this transitions. |
|:------------|:---------------------------------------------------------------------|
| @OnActivityState | A list of input states for the sub-activity. When a sub-activity with the type set by the @AtActivity annotation reaches one of those input states, this method transition will be called. |

Here is an example of a transition taken from the predefined `AffirmIdentity` activity:

```
    @FromState("Started")
    @OnMessage(performative="Inform")
    @PossibleOutcome("Completed")
    public WorkflowState onInform(Message msg)
    {
        HGPeerIdentity thisId = getThisPeer().getIdentity();
        HGPeerIdentity id = parseIdentity(getStruct(msg, CONTENT));
        Message reply = getReply(msg);        
        if (id.getId().equals(thisId.getId()))
            combine(reply, struct(PERFORMATIVE, Disconfirm));
        else
        {
            combine(reply, combine(struct(PERFORMATIVE, Confirm),
                                   struct(CONTENT, 
                                          makeIdentityStruct(getThisPeer().getIdentity()))));
            getThisPeer().bindIdentityToNetworkTarget(id, getPart(msg, REPLY_TO));
        }
        getPeerInterface().send(getSender(msg), reply);
        return WorkflowState.Completed;
    }

```

To implement an FSMActivity, extend the `org.hypergraphdb.peer.workflow.FSMActivity` class, define and annotated all your transition methods and define an `initiate` method if
needed.

## Scheduling Algorithm ##

The scheduler works by maintaining a global queue of activities prioritized according to their wait time (the number of milliseconds elapsed since the last an activity ran) and the number of actions in their queue. A special case are activities that are currently waited on by a call to the blocking `Future.get` method on the `Future` instance associated with them - such activities always take priority over activities that are NOT waited on.

The algorithm does the following:

  1. Take (remove from priority queue) the activity with the highest priority.
  1. If its action queue is empty, update a timestamp variable to adjust the waiting time for that activity and reinsert it back in the priority queue.
  1. Otherwise, execute the next action from the queue. The action is decorated with the additional behavior of updating the last execution timestamp and reinserting the activity back into the global queue once it is completed.

This algorithm ensures relative fairness in activity scheduling and offers a mechanism to modify their priority by explicitly waiting on them to finish. It would be easy to extend the algorithm with the ability to assign custom priorities, but this doesn't seem needed at this point.