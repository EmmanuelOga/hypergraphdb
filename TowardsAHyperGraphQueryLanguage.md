When thinking about querying a database, the natural starting point is the SQL paradigm: given some data set, restrict it with some set of criteria to obtain a result. But there are more general views on querying, more close to the everyday meaning of "soliciting the answer to some question", given that we can express the question in a way meaningful to the system. Prolog is an example of this sort of generalized querying capabilities where rule-based inference of new facts are part of the process. Given the generality of HGDB, we are tempted to define similar mechanism where fairly complex queries can be expressed and where the query processing itself is highly customizable.

This would be a fairly long research project and a language would have to evolve based on experience with real-world applications and the development of auxiliary algorithms. So we restrict the scope for now to the already difficult problem of expressing and looking for structural/data patterns within a HyperGraph database.

The idea is to define a syntax that expresses some graph structure with constraints on atoms. The constraints specify how atoms are linked and/or typed and/or valued. The general form of a query looks like this:

`structure_pattern => result`

which is to be read "the structural pattern on the left yields the result on the right". The pattern on the left hand side may contain variables and the result expression on the right may use those variable to express the desired form of the result. An alternative syntax might be one where the result expression appears on the left. For example:

`result | structure_pattern`

could be read `we are looking for _result_ such as _structure_pattern_`. Or imitating Prolog:

`result :- structure_pattern`

or:

`result where structure_pattern`

Each query is evaluated in an environment containing variable bindings. When a variable inside the query pattern has no binding in the evaluation environment, it is used as a **generator** (like in the Icon programming language) and it will take on all possible values satisfying the pattern. All variables, environment-bound or free, can be used in the result expression. Initially the latter will only define a result set to be returned. In the future, it might be extended to support operators that modify the graph, similar to the insert/delete/update in SQL.

The lexical elements for constructing structural patterns are the following:

  1. Variables which are C/Java-like identifiers. The Prolog underscore denotation of anonymous variables (`_`) is adopted - those are always unbound variables whose value is to be ignored.
  1. Literal strings, numbers and booleans.
  1. An "is of type" operator denoted by semicolon ':'
  1. A subsumes operator denoted by '<:' (read "left-hand side is subsumed by right-hand side")
  1. A link operator denoted by square brackets [.md](.md)
  1. Logical operators & (and), | (or) and ~ (not).
  1. Commas as the usual "enumeration" operator of a sequence of things.
  1. Dot as the usual property dereferencing operator for complex values.
  1. Relational operators =, >, <, >=, <=

We may add other operators as we go. While I'd like the ability to extend querying by user-defined operators, I'd rather avoid any mechanisms for overloading or extending the grammar by defining new infix operators with their precedence etc. It's probably much simpler and just as user-friendly to offer the possibility of defining functions (e.g. arithmetic functions such add, sub, mul, bitwiseor etc). A function-like syntax would also cover the need for expressing type constructors. For example, a result expression of `pair(x,y)` would produce a set of pairs for all possible values of the variables x and y.

So far, so good. The simplest query is one asking for all atoms in a HyperGraphDB. It is expressed as:

`x => x`

provided x is unbound in the evaluation environment. Environment bindings are crucial for the interpretation of a query. Thus a  query that looks like this:

`x:T => T`

can mean one of several things:

  1. All atom types in the graph if both T and x are unbound
  1. The type of x if x is bound, but T unbound or if both x and T are bound and x is indeed of type T
  1. An empty set if both x and T are bound, but x is not of type T
  1. T if T is bound, but x unbound.

Atom values and parts of structured values can be accessed and compared using the usual dot notation. The query:

x:String & x = "HyperGraphDB"

will return are atoms of type string with value "HyperGraphDB". The and operator '&', and the or operator '|' can be used in combination with the usual convention that & preceeds |, and parenthesis can be used to group condition while enforcing a different precedence. For example:

x.weight > 4.25 & (x.value = 10 | x.value = 100) => x

yields all x's whose value is either 10 or 100, and whose weight is 4.25.

Not sure whether, the C-like and/or operators && and || should be adopted instead. Or maybe use the keyword **and** and **or**. It is unlikely that & could be a source of ambiguity when used for something else, but | is well-established as a comprehension operator (meaning "such that") which may very well find application in the query language.

Link pattern are defined using square brackets. The items in the target set of a link pattern are separated by commas for ordered links and by the & operator for unordered links. For example:

`x:R[a, b] & a.weight > 1 => a`

will find all atoms with weight > 1 and that are targets at position 0 of links of type R with arity 2, while the following:

`x:R[a & b] & a.weight > 1 => a`

will find all a's with weight > 1 and that are targets (regardless of position) of links of type R with arity 2. If the R links in the above can have arbitrary arity, one can use the star `*` operator applied to a don't care anonymous variable:

`x:R[a & _*] & a.weight > 1 => a`

The star operator means zero or more as in regular expressions. Naturally, it also works in ordered links:

`x:R[a, _, _*]`

means "an ordered link of type R whose first target is a and which 1 or more subsequent targets".

In the above example, we've used the construct `x:R[...]` in which we constrain the type of the link and we bind it to the name x. If we are not going to refer to the link in any other part of the query, it is better not to name it to avoid possible confusion:

`_:R[a, _, _*]`

Note that it would be ambiguous to write directly `R[...]` because the interpreter wouldn't know whether R is the name of the link or the name of the type of the link. Typing constraints are always to be qualified with the : operator.

Link patterns can be nested at arbitrary levels and circular reference are admitted. For instance, the following pattern

`x:R[a,b,y:T[x&y]]`

expresses a the structure of an ordered R link with arity 3 whose third element is an unordered link of type T having the R link as a target. As you can see, the variables inside the linking patterns can be type-qualified as well. However, constraints on the values of those variables must be stated outside the pattern, in a conjunction:

`x:R[a,b,y:T[x&y]] & a.weight < b.weight & T <: S & ...etc...`

to be continued...please comment on the ideas exposed so far. The language outlined above is already challenging enough to implement and quite expressive. A major feature missing is a nice syntax for specifying path patterns. It doesn't seem obvious how to do that in an elegant manner.