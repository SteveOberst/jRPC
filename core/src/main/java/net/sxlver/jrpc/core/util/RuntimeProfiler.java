package net.sxlver.jrpc.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sxlver.jrpc.core.InternalLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class RuntimeProfiler {
    private static RuntimeProfiler globalInstance;

    private final Map<String, ProfilingNode> nodes = Maps.newConcurrentMap();
    private final Stack<ProfilingNode> currentNodes = new Stack<>();

    private RuntimeProfiler() {
    }

    public static RuntimeProfiler getInstance() {
        if (globalInstance == null) {
            synchronized (RuntimeProfiler.class) {
                if (globalInstance == null) {
                    globalInstance = new RuntimeProfiler();
                }
            }
        }
        return globalInstance;
    }

    public static ProfilingNode startNode(String nodeName) {
        return getInstance().startNodeInternal(nodeName);
    }

    public static void endNode() {
        getInstance().endNodeInternal();
    }

    public ProfilingNode startNodeInternal(final String nodeName) {
        final ProfilingNode parent;
        synchronized (currentNodes) {
            parent = currentNodes.isEmpty() ? null : currentNodes.peek();
            ProfilingNode node = new ProfilingNode(nodeName, parent);
            nodes.put(nodeName, node);
            currentNodes.push(node);
            return node;
        }
    }

    public void endNodeInternal() {
        synchronized (currentNodes) {
            if (!currentNodes.isEmpty()) {
                currentNodes.pop();
            } else {
                System.out.println("RuntimeProfiler Error: Mismatched startNode and endNode.");
            }
        }
    }

    public ProfilingNode getNode(final String nodeName) {
        return nodes.get(nodeName);
    }

    public void printAndResetStatistics() {
        System.out.println("Profiling Statistics:");
        for (final ProfilingNode rootNode : nodes.values()) {
            if (rootNode.isRoot()) {
                rootNode.printStatistics("");
            }
        }
        resetStatistics();
    }

    public List<ProfilingNode> getRootNodes() {
        final List<ProfilingNode> rootNodes = Lists.newArrayList();
        for (final ProfilingNode rootNode : nodes.values()) {
            if (rootNode.isRoot()) {
                rootNodes.add(rootNode);
            }
        }
        return rootNodes;
    }


    public void resetStatistics() {
        synchronized (currentNodes) {
            nodes.clear();
            currentNodes.clear();
        }
    }

    public static class ProfilingNode {
        private final String name;
        private final ProfilingNode parent;
        private final Map<String, ProfilingNode> childNodes = Maps.newConcurrentMap();
        private long totalRuntime = 0;
        private int executionCount = 0;
        private long startTime = 0;

        public ProfilingNode(final String name, final ProfilingNode parent) {
            this.name = name;
            this.parent = parent;
        }

        public void start() {
            startTime = System.nanoTime();
        }

        public void stop() {
            long elapsedTime = System.nanoTime() - startTime;
            totalRuntime += elapsedTime;
            executionCount++;
        }

        public void printStatistics(String prefix) {
            System.out.printf("%sNode: %s%n", prefix, name);
            System.out.printf("%sExecuted %d times%n", prefix, executionCount);
            System.out.printf("%sTotal time: %d ns%n", prefix, totalRuntime);
            System.out.println();
            for (final ProfilingNode child : childNodes.values()) {
                child.printStatistics(prefix + "\t");
            }
        }

        public ProfilingNode createChildNode(final String nodeName) {
            final ProfilingNode child = new ProfilingNode(nodeName, this);
            childNodes.put(nodeName, child);
            return child;
        }

        public boolean isRoot() {
            return parent == null;
        }
    }
}
