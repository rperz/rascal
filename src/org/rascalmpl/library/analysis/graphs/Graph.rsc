@license{
  Copyright (c) 2009-2015 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Paul Klint - Paul.Klint@cwi.nl - CWI}
@contributor{Arnold Lankamp - Arnold.Lankamp@cwi.nl - CWI}
@contributor{Bas Basten - Bas.Basten@cwi.nl - CWI}
@doc{
Synopsis: A `Graph` datatype with associated functions.

Types:
`alias Graph[&T] = rel[&T from, &T to];`

Description:
The Graph data type is a binary relation and all operators and functions defined
on [$Values/Relation] are also defined on Graph.

The `Graph` library provides the following functions:
<toc Rascal/Libraries/analysis/graphs/Graph 1>
}
module analysis::graphs::Graph

import Exception;
import Set;
import Relation;
import IO;      
   
alias Graph[&T] = rel[&T from, &T to];

@doc{
Synopsis: Compute topological order of the nodes in a graph.

Examples:
<screen>
import  analysis::graphs::Graph;
order({<3,4>, <1,2>, <2,4>, <1,3>});
</screen>
}
public list[&T] order(Graph[&T] g) {
  result = [];
  b = bottom(g);
  solve (g) {
    t = top(g);
    result = result + [e | e <- t];
    g = { <from,to> | <from,to> <- g, from notin t};
  }
  return result + [e | e <- b];
}

@doc{
Synopsis: Determine the bottom nodes (leaves) of a graph.

Description:
Returns the bottom nodes of Graph `G`, i.e., the leaf nodes that don't have any descendants.

Examples:
<screen>
import analysis::graphs::Graph;
bottom({<1,2>, <1,3>, <2,4>, <3,4>});
</screen>
}
public set[&T] bottom(Graph[&T] G)
{
  return range(G) - domain(G);
}

@doc{
Synopsis: Determine the direct predecessors of a graph node.

Description:
Returns the direct predecessors of node `From` in Graph `G`.

Examples:
<screen>
import analysis::graphs::Graph;
predecessors({<1,2>, <1,3>, <2,4>, <3,4>}, 4);
</screen>
}
public set[&T] predecessors(Graph[&T] G, &T From)
{
  //return G[_,From];
  return invert(G)[From];
}

@doc{
Synopsis: Determine the graph nodes reachable from a set of nodes.

Description:
Returns the set of nodes in Graph `G` that are reachable from any of the nodes
in the set `Start`.
}
public set[&T] reach(Graph[&T] G, set[&T] Start)
{
    set[&T] R = Start;
    set[&T] new = R;
	
	while (new != {}) {
		new = G[new] - R;
		R += new;
	}
    
	return R;
}

@doc{
Synopsis: Determine the graph nodes reachable from a set of nodes using a restricted set of intermediate nodes.

Description:
Returns the set of nodes in Graph `G` that are reachable from any of the nodes
in set `Start` using path that only use nodes in the set `Restr`.

Examples:
<screen>
import analysis::graphs::Graph;
reachR({<1,2>, <1,3>, <2,4>, <3,4>}, {1}, {1, 2, 3});
</screen>
}
public set[&T] reachR(Graph[&T] G, set[&T] Start, set[&T] Restr)
{
	return (carrierR(G, Restr)+)[Start];
}

@doc{
Synopsis: Determine the graph nodes reachable from a set of nodes excluding certain intermediate nodes.

Description:
Returns set of nodes in Graph `G` that are reachable from any of the nodes
in `Start` via path that exclude nodes in `Excl`.

Examples:
<screen>
import analysis::graphs::Graph;
reachX({<1,2>, <1,3>, <2,4>, <3,4>}, {1}, {2});
</screen>
}
public set[&T] reachX(Graph[&T] G, set[&T] Start, set[&T] Excl)
{
   return (carrierX(G, Excl)+)[Start];
}

@doc{
Synopsis: Determine the shortest path between two graph nodes.

Description:
Returns the shortest path between nodes `From` and `To` in Graph `G`.
}
@javaClass{org.rascalmpl.library.Prelude}
public java list[&T] shortestPathPair(Graph[&T] G, &T From, &T To);

@doc{
Synopsis: Determine the direct successors of a graph node.

Description:
Returns the direct successors of node `From` in Graph `G`.

Examples:
<screen>
import analysis::graphs::Graph;
successors({<1,2>, <1,3>, <2,4>, <3,4>}, 1);
</screen>
}
public set[&T] successors(Graph[&T] G, &T From)
{
  return G[From];
}

@doc{
Synopsis: Determine the set of top nodes (roots) of a graph.

Description:
Returns the top nodes of Graph `G`, i.e., the root nodes that do not have any predecessors.

Examples:
<screen>
import analysis::graphs::Graph;
top({<1,2>, <1,3>, <2,4>, <3,4>});
</screen>
}
public set[&T] top(Graph[&T] G)
{
  return domain(G) - range(G);
}

@doc{
Synopsis: Determine the connected components of a graph.

Description:
Returns the [connected components](http://en.wikipedia.org/wiki/Connected_component_(graph_theory)) of Graph `G`, as sets of nodes. All nodes within one component are all reachable from one another, there are no paths between two nodes from different components. The graph is assumed to be undirected.

Examples:
<screen>
import analysis::graphs::Graph;
connectedComponents({<1,2>, <1,3>, <4,5>, <5,6>});
</screen>
}
public set[set[&T]] connectedComponents(Graph[&T] G)
{
  set[set[&T]] components = {};

  Graph[&T] undirected = G + invert(G);

  set[&T] todo = domain(undirected);

  while (size(todo) > 0) {
    component = reach(undirected, {getOneFrom(todo)});
    components += {component};
    todo -= component;
  };

  return components;
}
