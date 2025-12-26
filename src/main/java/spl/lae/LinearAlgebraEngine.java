package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    // _____fields_____
    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    // _____constructor_____
    public LinearAlgebraEngine(int numThreads) {
        if (numThreads < 1)
            throw new IllegalArgumentException("numThreads must be greater than 0");
        executor = new TiredExecutor(numThreads);
    }

    // _____methods_____
    // resolve computation tree step by step until the final matrix is produced
    public ComputationNode run(ComputationNode computationRoot) {
        if (computationRoot == null)
            throw new IllegalArgumentException("computationRoot must not be null");

        computationRoot.associativeNesting();

        // at the end of the loop, the root will be a matrix with no children.
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX){
            ComputationNode next = computationRoot.findResolvable();

            if (next == null)
                throw new IllegalStateException("No resolvable node found");

            loadAndCompute(next);
        }

        // shutting down and handling InterruptedException
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while shutting down executor");
        }

        return computationRoot;
    }

    // load operand matrices, create compute tasks & submit tasks to executor
    public void loadAndCompute(ComputationNode node) {
        // checks
        if (node == null || node.getNodeType() == ComputationNodeType.MATRIX)
            throw new IllegalArgumentException("node must not be null or matrix");

        List<ComputationNode> children = node.getChildren();
        if (children == null || children.isEmpty())
            throw new IllegalArgumentException("node must contain at least one child");

        ComputationNodeType type = node.getNodeType();
        if ((type == ComputationNodeType.NEGATE || type == ComputationNodeType.TRANSPOSE) && children.size() != 1)
            throw new IllegalArgumentException("node must contain exactly one child");
        if ((type == ComputationNodeType.ADD || type == ComputationNodeType.MULTIPLY) && children.size() != 2)
            throw new IllegalArgumentException("node must contain exactly two children");

        // extract matrices and sizes
        double[][] M1 = children.get(0).getMatrix();
        double[][] M2 = (children.size() == 2) ? children.get(1).getMatrix() : null;

        int aRows = M1.length;
        int aCols = M1[0].length;

        int bRows = (M2 != null) ? M2.length : 0;
        int bCols = (M2 != null) ? M2[0].length : 0;

        // check dimensions
        if (type == ComputationNodeType.ADD && (aRows != bRows || aCols != bCols))
            throw new IllegalArgumentException("dimensions do not match");
        else if (type == ComputationNodeType.MULTIPLY && (aCols != bRows))
            throw new IllegalArgumentException("dimensions do not match");

        // load matrices and create tasks
        List<Runnable> tasks;
        switch (type) {
            case ADD:
                leftMatrix.loadRowMajor(M1);
                rightMatrix.loadRowMajor(M2);
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                leftMatrix.loadRowMajor(M1);
                rightMatrix.loadColumnMajor(M2);
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                leftMatrix.loadRowMajor(M1);
                rightMatrix.loadRowMajor(null);
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                leftMatrix.loadColumnMajor(M1);
                rightMatrix.loadRowMajor(null);
                tasks = createTransposeTasks();
                break;
            default:
                throw new IllegalArgumentException("unknown computation node type");
        }

        // submit tasks and resolve
        executor.submitAll(tasks);
        double[][] result = leftMatrix.readRowMajor();
        node.resolve(result);
    }

    // return tasks that perform row-wise addition
    public List<Runnable> createAddTasks() {
        int rows = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            final int row = i;
            tasks.add(() -> {
                SharedVector M1Row = leftMatrix.get(row);
                SharedVector M2Row = rightMatrix.get(row);
                M1Row.add(M2Row);
            });
        }
        return tasks;
    }

    // return tasks that perform row × matrix multiplication
    public List<Runnable> createMultiplyTasks() {
        int rows = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            final int row = i;
            tasks.add(() -> {
                SharedVector M1Row = leftMatrix.get(row);
                M1Row.vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    // return tasks that negate rows
    public List<Runnable> createNegateTasks() {
        int rows = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            final int row = i;
            tasks.add(() -> {
                SharedVector M1Row = leftMatrix.get(row);
                M1Row.negate();
            });
        }
        return tasks;
    }

    // return tasks that transpose rows
    public List<Runnable> createTransposeTasks() {
        int vectors = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>(vectors);
        for (int i = 0; i < vectors; i++) {
            final int index = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(index);
                v.transpose();
            });
        }
        return tasks;
    }

    // return summary of worker activity
    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}
