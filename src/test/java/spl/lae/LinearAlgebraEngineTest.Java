package spl.lae;

import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineRunTest {

    private static final double EPS = 1e-9;

    /// ===== run tests =====
    @Test
    void run_nullRoot() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        assertThrows(IllegalArgumentException.class, () -> lae.run(null));
    }

    @Test
    void run_rootAlreadyMatrix() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);

        double[][] m = {{1, 2}, {3, 4}};
        ComputationNode root = new ComputationNode(m);
        ComputationNode out = lae.run(root);

        assertSame(root, out);
        assertMatrixEquals(m, out.getMatrix());
    }

    @Test
    void run_addTwoMatrices() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode a = new ComputationNode(new double[][]{{1, 2}, {3, 4}});
        ComputationNode b = new ComputationNode(new double[][]{{5, 6}, {7, 8}});
        ComputationNode root = new ComputationNode("+", List.of(a, b));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{6, 8}, {10, 12}}, out.getMatrix());
        assertNull(out.getChildren(), "After resolve(), children should be null");
    }

    @Test
    void run_addSameMatrix() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);
        ComputationNode a = new ComputationNode(new double[][]{{1,2},{3,4}});
        ComputationNode root = new ComputationNode("+", List.of(a, a));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{2,4},{6,8}}, out.getMatrix());
    }

    @Test
    void run_negate() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode a = new ComputationNode(new double[][]{{1, -2}, {0, 4}});
        ComputationNode root = new ComputationNode("-", List.of(a));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{-1, 2}, {0, -4}}, out.getMatrix());
    }

    @Test
    void run_transpose() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        ComputationNode a = new ComputationNode(A);
        ComputationNode root = new ComputationNode("T", List.of(a));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{1, 4}, {2, 5}, {3, 6}}, out.getMatrix());
    }

    @Test
    void run_doubleTranspose_returnsOriginal() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        ComputationNode a = new ComputationNode(A);
        ComputationNode t1 = new ComputationNode("T", List.of(a));
        ComputationNode root = new ComputationNode("T", List.of(t1));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(A, out.getMatrix());
    }

    @Test
    void run_multiply() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(3);

        ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}, {4, 5, 6}});
        ComputationNode b = new ComputationNode(new double[][]{{7, 8}, {9, 10}, {11, 12}});
        ComputationNode root = new ComputationNode("*", List.of(a, b));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{58, 64}, {139, 154}}, out.getMatrix());
    }

    @Test
    void run_nestedOperations() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(4);

        ComputationNode A = new ComputationNode(new double[][]{{1, 2, 3}, {4, 5, 6}});
        ComputationNode B = new ComputationNode(new double[][]{{10, 20, 30}, {40, 50, 60}});
        ComputationNode add = new ComputationNode("+", List.of(A, B));
        ComputationNode tr = new ComputationNode("T", List.of(add));
        ComputationNode root = new ComputationNode("-", List.of(tr));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{-11, -44}, {-22, -55}, {-33, -66}}, out.getMatrix());
    }

    @Test
    void run_associativeNesting() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(3);

        ComputationNode A = new ComputationNode(new double[][]{{1}});
        ComputationNode B = new ComputationNode(new double[][]{{2}});
        ComputationNode C = new ComputationNode(new double[][]{{3}});
        ComputationNode root = new ComputationNode("+", new ArrayList<>(List.of(A, B, C)));
        ComputationNode out = lae.run(root);

        assertMatrixEquals(new double[][]{{6}}, out.getMatrix());
    }

    @Test
    void run_add_dimensionMismatch() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}});
        ComputationNode b = new ComputationNode(new double[][]{{4, 5}});
        ComputationNode root = new ComputationNode("+", List.of(a, b));

        assertThrows(IllegalArgumentException.class, () -> lae.run(root));
    }

    @Test
    void run_add_emptyMatrix() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        ComputationNode empty = new ComputationNode(new double[][]{});
        ComputationNode one = new ComputationNode(new double[][]{{1}});
        ComputationNode root = new ComputationNode("+", List.of(empty, one));

        assertThrows(Exception.class, () -> lae.run(root));
    }

    @Test
    void run_multiply_dimensionMismatch() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}, {4, 5, 6}});
        ComputationNode b = new ComputationNode(new double[][]{{1, 2}, {3, 4}});
        ComputationNode root = new ComputationNode("*", List.of(a, b));

        assertThrows(IllegalArgumentException.class, () -> lae.run(root));
    }

    @Test
    void run_multiply_emptyMatrix() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        ComputationNode empty = new ComputationNode(new double[][]{});
        ComputationNode one = new ComputationNode(new double[][]{{1}});
        ComputationNode root = new ComputationNode("*", List.of(empty, one));

        assertThrows(Exception.class, () -> lae.run(root));
    }


    /// ===== loadAndCompute tests =====
    @Test
    void loadAndCompute_nullNode() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(null));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_matrixNode() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode matrixNode = new ComputationNode(new double[][]{{1}});
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(matrixNode));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_noChildren() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode node = new ComputationNode("+", List.of());
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_unaryOperatorWithTwoChildren() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1}});
            ComputationNode b = new ComputationNode(new double[][]{{2}});
            ComputationNode node = new ComputationNode("-", List.of(a, b));
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_binaryOperator_withOneChild() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1}});
            ComputationNode node = new ComputationNode("+", List.of(a));
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_add_dimensionMismatch() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}});
            ComputationNode b = new ComputationNode(new double[][]{{4, 5}});
            ComputationNode node = new ComputationNode("+", List.of(a, b));
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_multiply_dimensionMismatch() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}, {4, 5, 6}});
            ComputationNode b = new ComputationNode(new double[][]{{1, 2}, {3, 4}});
            ComputationNode node = new ComputationNode("*", List.of(a, b));
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_addWithThreeChildren() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode A = new ComputationNode(new double[][]{{1}});
            ComputationNode B = new ComputationNode(new double[][]{{2}});
            ComputationNode C = new ComputationNode(new double[][]{{3}});

            ComputationNode node = new ComputationNode("+", new ArrayList<>(List.of(A, B, C)));
            assertThrows(IllegalArgumentException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_childNotMatrix() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(1);
        try {
            ComputationNode childOp = new ComputationNode("+", List.of(
                    new ComputationNode(new double[][]{{1}}),
                    new ComputationNode(new double[][]{{2}})
            ));
            ComputationNode node = new ComputationNode("-", List.of(childOp));
            assertThrows(IllegalStateException.class, () -> lae.loadAndCompute(node));
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_add() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1, 2}, {3, 4}});
            ComputationNode b = new ComputationNode(new double[][]{{5, 6}, {7, 8}});
            ComputationNode node = new ComputationNode("+", List.of(a, b));
            lae.loadAndCompute(node);

            assertEquals(ComputationNodeType.MATRIX, node.getNodeType());
            assertNull(node.getChildren());
            assertMatrixEquals(new double[][]{{6, 8}, {10, 12}}, node.getMatrix());
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_negate() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1, -2}, {0, 4}});
            ComputationNode node = new ComputationNode("-", List.of(a));
            lae.loadAndCompute(node);

            assertEquals(ComputationNodeType.MATRIX, node.getNodeType());
            assertNull(node.getChildren());
            assertMatrixEquals(new double[][]{{-1, 2}, {0, -4}}, node.getMatrix());
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_transpose() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}, {4, 5, 6}});
            ComputationNode node = new ComputationNode("T", List.of(a));
            lae.loadAndCompute(node);

            assertEquals(ComputationNodeType.MATRIX, node.getNodeType());
            assertNull(node.getChildren());
            assertMatrixEquals(new double[][]{{1, 4}, {2, 5}, {3, 6}}, node.getMatrix());
        } finally {
            shutdownLAE(lae);
        }
    }

    @Test
    void loadAndCompute_multiply() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(3);
        try {
            ComputationNode a = new ComputationNode(new double[][]{{1, 2, 3}, {4, 5, 6}});
            ComputationNode b = new ComputationNode(new double[][]{{7, 8}, {9, 10}, {11, 12}});
            ComputationNode node = new ComputationNode("*", List.of(a, b));
            lae.loadAndCompute(node);

            assertEquals(ComputationNodeType.MATRIX, node.getNodeType());
            assertNull(node.getChildren());
            assertMatrixEquals(new double[][]{{58, 64}, {139, 154}}, node.getMatrix());
        } finally {
            shutdownLAE(lae);
        }
    }


    /// ===== helper functions =====

    /**
     * helper function - comparing matrices
     * @param expected
     * @param actual
     */
    private static void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertNotNull(actual);
        assertEquals(expected.length, actual.length, "row count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].length, actual[i].length, "col count mismatch at row " + i);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], actual[i][j], EPS, "Mismatch at [" + i + "," + j + "]");
            }
        }
    }

    /**
     * helper function - shutting down the executor by calling run method
     * @param lae
     */
    private static void shutdownLAE(LinearAlgebraEngine lae) {
        try {
            lae.run(new ComputationNode(new double[][]{{0}}));
        } catch (Exception ignored) {
        }
    }
}
