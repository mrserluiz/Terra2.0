package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.AbstractIterator;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;

final class DirectedGraphConnections<N, V> implements GraphConnections<N, V> {
   private static final Object PRED = new Object();
   private final Map<N, Object> adjacentNodeValues;
   private final @Nullable List<DirectedGraphConnections.NodeConnection<N>> orderedNodeConnections;
   private int predecessorCount;
   private int successorCount;

   private DirectedGraphConnections(
      Map<N, Object> adjacentNodeValues,
      @Nullable List<DirectedGraphConnections.NodeConnection<N>> orderedNodeConnections,
      int predecessorCount,
      int successorCount
   ) {
      this.adjacentNodeValues = Preconditions.checkNotNull(adjacentNodeValues);
      this.orderedNodeConnections = orderedNodeConnections;
      this.predecessorCount = Graphs.checkNonNegative(predecessorCount);
      this.successorCount = Graphs.checkNonNegative(successorCount);
      Preconditions.checkState(predecessorCount <= adjacentNodeValues.size() && successorCount <= adjacentNodeValues.size());
   }

   static <N, V> DirectedGraphConnections<N, V> of(ElementOrder<N> incidentEdgeOrder) {
      int initialCapacity = 4;
      List<DirectedGraphConnections.NodeConnection<N>> orderedNodeConnections;
      switch (incidentEdgeOrder.type()) {
         case UNORDERED:
            orderedNodeConnections = null;
            break;
         case STABLE:
            orderedNodeConnections = new ArrayList<>();
            break;
         default:
            throw new AssertionError(incidentEdgeOrder.type());
      }

      return new DirectedGraphConnections<>(new HashMap<>(initialCapacity, 1.0F), orderedNodeConnections, 0, 0);
   }

   static <N, V> DirectedGraphConnections<N, V> ofImmutable(N thisNode, Iterable<EndpointPair<N>> incidentEdges, Function<N, V> successorNodeToValueFn) {
      Preconditions.checkNotNull(thisNode);
      Preconditions.checkNotNull(successorNodeToValueFn);
      Map<N, Object> adjacentNodeValues = new HashMap<>();
      ImmutableList.Builder<DirectedGraphConnections.NodeConnection<N>> orderedNodeConnectionsBuilder = ImmutableList.builder();
      int predecessorCount = 0;
      int successorCount = 0;

      for (EndpointPair<N> incidentEdge : incidentEdges) {
         if (incidentEdge.nodeU().equals(thisNode) && incidentEdge.nodeV().equals(thisNode)) {
            adjacentNodeValues.put(thisNode, new DirectedGraphConnections.PredAndSucc(successorNodeToValueFn.apply(thisNode)));
            orderedNodeConnectionsBuilder.add(new DirectedGraphConnections.NodeConnection.Pred<>(thisNode));
            orderedNodeConnectionsBuilder.add(new DirectedGraphConnections.NodeConnection.Succ<>(thisNode));
            predecessorCount++;
            successorCount++;
         } else if (incidentEdge.nodeV().equals(thisNode)) {
            N predecessor = incidentEdge.nodeU();
            Object existingValue = adjacentNodeValues.put(predecessor, PRED);
            if (existingValue != null) {
               adjacentNodeValues.put(predecessor, new DirectedGraphConnections.PredAndSucc(existingValue));
            }

            orderedNodeConnectionsBuilder.add(new DirectedGraphConnections.NodeConnection.Pred<>(predecessor));
            predecessorCount++;
         } else {
            Preconditions.checkArgument(incidentEdge.nodeU().equals(thisNode));
            N successor = incidentEdge.nodeV();
            V value = successorNodeToValueFn.apply(successor);
            Object existingValue = adjacentNodeValues.put(successor, value);
            if (existingValue != null) {
               Preconditions.checkArgument(existingValue == PRED);
               adjacentNodeValues.put(successor, new DirectedGraphConnections.PredAndSucc(value));
            }

            orderedNodeConnectionsBuilder.add(new DirectedGraphConnections.NodeConnection.Succ<>(successor));
            successorCount++;
         }
      }

      return new DirectedGraphConnections<>(adjacentNodeValues, orderedNodeConnectionsBuilder.build(), predecessorCount, successorCount);
   }

   @Override
   public Set<N> adjacentNodes() {
      return this.orderedNodeConnections == null ? Collections.unmodifiableSet(this.adjacentNodeValues.keySet()) : new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            final Iterator<DirectedGraphConnections.NodeConnection<N>> nodeConnections = DirectedGraphConnections.this.orderedNodeConnections.iterator();
            final Set<N> seenNodes = new HashSet<>();
            return new AbstractIterator<N>() {
               @Override
               protected @Nullable N computeNext() {
                  while (nodeConnections.hasNext()) {
                     DirectedGraphConnections.NodeConnection<N> nodeConnection = nodeConnections.next();
                     boolean added = seenNodes.add(nodeConnection.node);
                     if (added) {
                        return nodeConnection.node;
                     }
                  }

                  return (N)this.endOfData();
               }
            };
         }

         @Override
         public int size() {
            return DirectedGraphConnections.this.adjacentNodeValues.size();
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.this.adjacentNodeValues.containsKey(obj);
         }
      };
   }

   @Override
   public Set<N> predecessors() {
      return new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            if (DirectedGraphConnections.this.orderedNodeConnections == null) {
               final Iterator<Entry<N, Object>> entries = DirectedGraphConnections.this.adjacentNodeValues.entrySet().iterator();
               return new AbstractIterator<N>() {
                  @Override
                  protected @Nullable N computeNext() {
                     while (entries.hasNext()) {
                        Entry<N, Object> entry = entries.next();
                        if (DirectedGraphConnections.isPredecessor(entry.getValue())) {
                           return entry.getKey();
                        }
                     }

                     return (N)this.endOfData();
                  }
               };
            } else {
               final Iterator<DirectedGraphConnections.NodeConnection<N>> nodeConnections = DirectedGraphConnections.this.orderedNodeConnections.iterator();
               return new AbstractIterator<N>() {
                  @Override
                  protected @Nullable N computeNext() {
                     while (nodeConnections.hasNext()) {
                        DirectedGraphConnections.NodeConnection<N> nodeConnection = nodeConnections.next();
                        if (nodeConnection instanceof DirectedGraphConnections.NodeConnection.Pred) {
                           return nodeConnection.node;
                        }
                     }

                     return (N)this.endOfData();
                  }
               };
            }
         }

         @Override
         public int size() {
            return DirectedGraphConnections.this.predecessorCount;
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.isPredecessor(DirectedGraphConnections.this.adjacentNodeValues.get(obj));
         }
      };
   }

   @Override
   public Set<N> successors() {
      return new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            if (DirectedGraphConnections.this.orderedNodeConnections == null) {
               final Iterator<Entry<N, Object>> entries = DirectedGraphConnections.this.adjacentNodeValues.entrySet().iterator();
               return new AbstractIterator<N>() {
                  @Override
                  protected @Nullable N computeNext() {
                     while (entries.hasNext()) {
                        Entry<N, Object> entry = entries.next();
                        if (DirectedGraphConnections.isSuccessor(entry.getValue())) {
                           return entry.getKey();
                        }
                     }

                     return (N)this.endOfData();
                  }
               };
            } else {
               final Iterator<DirectedGraphConnections.NodeConnection<N>> nodeConnections = DirectedGraphConnections.this.orderedNodeConnections.iterator();
               return new AbstractIterator<N>() {
                  @Override
                  protected @Nullable N computeNext() {
                     while (nodeConnections.hasNext()) {
                        DirectedGraphConnections.NodeConnection<N> nodeConnection = nodeConnections.next();
                        if (nodeConnection instanceof DirectedGraphConnections.NodeConnection.Succ) {
                           return nodeConnection.node;
                        }
                     }

                     return (N)this.endOfData();
                  }
               };
            }
         }

         @Override
         public int size() {
            return DirectedGraphConnections.this.successorCount;
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.isSuccessor(DirectedGraphConnections.this.adjacentNodeValues.get(obj));
         }
      };
   }

   @Override
   public Iterator<EndpointPair<N>> incidentEdgeIterator(N thisNode) {
      Preconditions.checkNotNull(thisNode);
      final Iterator<EndpointPair<N>> resultWithDoubleSelfLoop;
      if (this.orderedNodeConnections == null) {
         resultWithDoubleSelfLoop = Iterators.concat(
            Iterators.transform(this.predecessors().iterator(), predecessor -> EndpointPair.ordered((N)predecessor, thisNode)),
            Iterators.transform(this.successors().iterator(), successor -> EndpointPair.ordered(thisNode, (N)successor))
         );
      } else {
         resultWithDoubleSelfLoop = Iterators.transform(
            this.orderedNodeConnections.iterator(),
            connection -> connection instanceof DirectedGraphConnections.NodeConnection.Succ
               ? EndpointPair.ordered(thisNode, connection.node)
               : EndpointPair.ordered(connection.node, thisNode)
         );
      }

      final AtomicBoolean alreadySeenSelfLoop = new AtomicBoolean(false);
      return new AbstractIterator<EndpointPair<N>>() {
         protected @Nullable EndpointPair<N> computeNext() {
            while (resultWithDoubleSelfLoop.hasNext()) {
               EndpointPair<N> edge = resultWithDoubleSelfLoop.next();
               if (edge.nodeU().equals(edge.nodeV())) {
                  if (alreadySeenSelfLoop.getAndSet(true)) {
                     continue;
                  }

                  return edge;
               }

               return edge;
            }

            return this.endOfData();
         }
      };
   }

   @Override
   public @Nullable V value(N node) {
      Preconditions.checkNotNull(node);
      Object value = this.adjacentNodeValues.get(node);
      if (value == PRED) {
         return null;
      } else {
         return (V)(value instanceof DirectedGraphConnections.PredAndSucc ? ((DirectedGraphConnections.PredAndSucc)value).successorValue : value);
      }
   }

   @Override
   public void removePredecessor(N node) {
      Preconditions.checkNotNull(node);
      Object previousValue = this.adjacentNodeValues.get(node);
      boolean removedPredecessor;
      if (previousValue == PRED) {
         this.adjacentNodeValues.remove(node);
         removedPredecessor = true;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue);
         removedPredecessor = true;
      } else {
         removedPredecessor = false;
      }

      if (removedPredecessor) {
         Graphs.checkNonNegative(--this.predecessorCount);
         if (this.orderedNodeConnections != null) {
            this.orderedNodeConnections.remove(new DirectedGraphConnections.NodeConnection.Pred(node));
         }
      }
   }

   @Override
   public @Nullable V removeSuccessor(Object node) {
      Preconditions.checkNotNull(node);
      Object previousValue = this.adjacentNodeValues.get(node);
      Object removedValue;
      if (previousValue == null || previousValue == PRED) {
         removedValue = null;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put((N)node, PRED);
         removedValue = ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue;
      } else {
         this.adjacentNodeValues.remove(node);
         removedValue = previousValue;
      }

      if (removedValue != null) {
         Graphs.checkNonNegative(--this.successorCount);
         if (this.orderedNodeConnections != null) {
            this.orderedNodeConnections.remove(new DirectedGraphConnections.NodeConnection.Succ<>(node));
         }
      }

      return (V)(removedValue == null ? null : removedValue);
   }

   @Override
   public void addPredecessor(N node, V unused) {
      Object previousValue = this.adjacentNodeValues.put(node, PRED);
      boolean addedPredecessor;
      if (previousValue == null) {
         addedPredecessor = true;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, previousValue);
         addedPredecessor = false;
      } else if (previousValue != PRED) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(previousValue));
         addedPredecessor = true;
      } else {
         addedPredecessor = false;
      }

      if (addedPredecessor) {
         Graphs.checkPositive(++this.predecessorCount);
         if (this.orderedNodeConnections != null) {
            this.orderedNodeConnections.add(new DirectedGraphConnections.NodeConnection.Pred<>(node));
         }
      }
   }

   @Override
   public @Nullable V addSuccessor(N node, V value) {
      Object previousValue = this.adjacentNodeValues.put(node, value);
      Object previousSuccessor;
      if (previousValue == null) {
         previousSuccessor = null;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(value));
         previousSuccessor = ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue;
      } else if (previousValue == PRED) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(value));
         previousSuccessor = null;
      } else {
         previousSuccessor = previousValue;
      }

      if (previousSuccessor == null) {
         Graphs.checkPositive(++this.successorCount);
         if (this.orderedNodeConnections != null) {
            this.orderedNodeConnections.add(new DirectedGraphConnections.NodeConnection.Succ<>(node));
         }
      }

      return (V)(previousSuccessor == null ? null : previousSuccessor);
   }

   private static boolean isPredecessor(@Nullable Object value) {
      return value == PRED || value instanceof DirectedGraphConnections.PredAndSucc;
   }

   private static boolean isSuccessor(@Nullable Object value) {
      return value != PRED && value != null;
   }

   private abstract static class NodeConnection<N> {
      final N node;

      NodeConnection(N node) {
         this.node = Preconditions.checkNotNull(node);
      }

      static final class Pred<N> extends DirectedGraphConnections.NodeConnection<N> {
         Pred(N node) {
            super(node);
         }

         @Override
         public boolean equals(@Nullable Object that) {
            return that instanceof DirectedGraphConnections.NodeConnection.Pred
               ? this.node.equals(((DirectedGraphConnections.NodeConnection.Pred)that).node)
               : false;
         }

         @Override
         public int hashCode() {
            return DirectedGraphConnections.NodeConnection.Pred.class.hashCode() + this.node.hashCode();
         }
      }

      static final class Succ<N> extends DirectedGraphConnections.NodeConnection<N> {
         Succ(N node) {
            super(node);
         }

         @Override
         public boolean equals(@Nullable Object that) {
            return that instanceof DirectedGraphConnections.NodeConnection.Succ
               ? this.node.equals(((DirectedGraphConnections.NodeConnection.Succ)that).node)
               : false;
         }

         @Override
         public int hashCode() {
            return DirectedGraphConnections.NodeConnection.Succ.class.hashCode() + this.node.hashCode();
         }
      }
   }

   private static final class PredAndSucc {
      private final Object successorValue;

      PredAndSucc(Object successorValue) {
         this.successorValue = successorValue;
      }
   }
}
