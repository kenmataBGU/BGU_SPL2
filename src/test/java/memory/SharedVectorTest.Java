package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    private static final double EPS = 1e-9;

    /**
     * get tests
     */
    @Test
    void get_returnsCorrectElement() {
        SharedVector v = new SharedVector(new double[]{10.0, -2.5, 7.25}, VectorOrientation.ROW_MAJOR);

        assertEquals(10.0, v.get(0), EPS);
        assertEquals(-2.5, v.get(1), EPS);
        assertEquals(7.25, v.get(2), EPS);
    }
    @Test
    void get_outOfBoundsNegative() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(-1));
    }
    @Test
    void get_outOfBoundsLength() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(3));
    }

    /**
     * length tests
     */
    @Test
    void length_returnsCorrectLength() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        assertEquals(3, v.length());
    }
    @Test
    void length_emptyVector_returnsZero() {
        SharedVector v = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        assertEquals(0, v.length());
    }

    /**
     * getOrientation tests
     */
    @Test
    void getOrientation_returnsOrientationGiven() {
        SharedVector row = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{1.0}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, row.getOrientation());
        assertEquals(VectorOrientation.COLUMN_MAJOR, col.getOrientation());
    }
    @Test
    void getOrientation_changesAfterTranspose() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
    }

    /**
     * transpose tests
     */
    @Test
    void transpose_togglesOrientationOnce() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
    }
    @Test
    void transpose_returnsToOriginal() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.COLUMN_MAJOR);
        v.transpose();
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
    }
    @Test
    void transpose_doesNotChangeVectorValues() {
        SharedVector v = new SharedVector(new double[]{10.0, -2.5, 7.25}, VectorOrientation.ROW_MAJOR);
        double a0 = v.get(0);
        double a1 = v.get(1);
        double a2 = v.get(2);

        v.transpose();

        assertEquals(a0, v.get(0), EPS);
        assertEquals(a1, v.get(1), EPS);
        assertEquals(a2, v.get(2), EPS);
    }

    /**
     * add tests
     */
    @Test
    void add_nullOther() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v.add(null));
    }
    @Test
    void add_self() {
        SharedVector v = new SharedVector(new double[]{1.0, -3.0, 2.5}, VectorOrientation.ROW_MAJOR);
        v.add(v);
        assertEquals(2.0, v.get(0), EPS);
        assertEquals(-6.0, v.get(1), EPS);
        assertEquals(5.0, v.get(2), EPS);
    }
    @Test
    void add_lengthMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 4.0, 5.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }
    @Test
    void add_orientationMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 4.0}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }
    @Test
    void add_valid() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{10.0, -2.0, 0.5}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);

        // v1 updated
        assertEquals(11.0, v1.get(0), EPS);
        assertEquals(0.0, v1.get(1), EPS);
        assertEquals(3.5, v1.get(2), EPS);

        // v2 unchanged
        assertEquals(10.0, v2.get(0), EPS);
        assertEquals(-2.0, v2.get(1), EPS);
        assertEquals(0.5, v2.get(2), EPS);
    }
    @Test
    void add_ColumnMajor() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.COLUMN_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 4.0}, VectorOrientation.COLUMN_MAJOR);

        v1.add(v2);
        assertEquals(4.0, v1.get(0), EPS);
        assertEquals(6.0, v1.get(1), EPS);
    }

    /**
     * negate tests
     */
    @Test
    void negate_valid() {
        SharedVector v = new SharedVector(new double[]{1.5, -2.0, 0.0}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(-1.5, v.get(0), EPS);
        assertEquals(2.0, v.get(1), EPS);
        assertEquals(-0.0, v.get(2), EPS);
    }
    @Test
    void negate_returnsOriginalValues() {
        SharedVector v = new SharedVector(new double[]{3.0, -4.5}, VectorOrientation.COLUMN_MAJOR);
        double a0 = v.get(0);
        double a1 = v.get(1);
        v.negate();
        v.negate();
        assertEquals(a0, v.get(0), EPS);
        assertEquals(a1, v.get(1), EPS);
    }
    @Test
    void negate_doesNotChangeOrientation() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    /**
     * dot tests
     */
    @Test
    void dot_nullOther() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v.dot(null));
    }
    @Test
    void dot_self() {
        SharedVector v = new SharedVector(new double[]{2.0, -3.0, 1.0}, VectorOrientation.ROW_MAJOR);
        double res = v.dot(v);
        assertEquals(14.0, res, EPS);
    }
    @Test
    void dot_lengthMismatch() {
        SharedVector row = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{3.0, 4.0, 5.0}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> row.dot(col));
    }
    @Test
    void dot_sameOrientation() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 4.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.dot(v2));
    }
    @Test
    void dot_rowDotCol() {
        SharedVector row = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{10.0, -1.0, 0.5}, VectorOrientation.COLUMN_MAJOR);
        double res = row.dot(col);
        assertEquals(9.5, res, EPS);
    }
    @Test
    void dot_colDotRow() {
        SharedVector col = new SharedVector(new double[]{10.0, -1.0, 0.5}, VectorOrientation.COLUMN_MAJOR);
        SharedVector row = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        double res = col.dot(row);
        assertEquals(9.5, res, EPS);
    }

    /**
     * vecMatMul tests
     */
    @Test
    void vecMatMul_nullMatrix() {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(null));
    }
    @Test
    void vecMatMul_emptyMatrix() {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m));
    }
    @Test
    void vecMatMul_vectorNotRowMajor() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.COLUMN_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{{1.0}, {2.0}});
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m));
    }
    @Test
    void vecMatMul_lengthMismatch() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{{1.0, 0.0}, {0.0, 1.0}});
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m));
    }
    @Test
    void vecMatMul_rowMajorMatrix() {
        SharedVector v = new SharedVector(new double[]{2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{
                {1.0, 0.0, 2.0},
                {-1.0, 3.0, 4.0}
        });

        v.vecMatMul(m);
        assertEquals(3, v.length());
        assertEquals(-1.0, v.get(0), EPS);
        assertEquals(9.0, v.get(1), EPS);
        assertEquals(16.0, v.get(2), EPS);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }
    @Test
    void vecMatMul_columnMajorMatrix() {
        SharedVector v = new SharedVector(new double[]{2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        double[][] data = new double[][]{
                {1.0, 0.0, 2.0},
                {-1.0, 3.0, 4.0}
        };
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        v.vecMatMul(m);
        assertEquals(3, v.length());
        assertEquals(-1.0, v.get(0), EPS);
        assertEquals(9.0, v.get(1), EPS);
        assertEquals(16.0, v.get(2), EPS);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }
    @Test
    void vecMatMul_identityMatrix() {
        SharedVector v = new SharedVector(new double[]{5.0, -2.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{
                {1.0, 0.0},
                {0.0, 1.0}
        });

        v.vecMatMul(m);
        assertEquals(2, v.length());
        assertEquals(5.0, v.get(0), EPS);
        assertEquals(-2.0, v.get(1), EPS);
    }
    @Test
    void vecMatMul_zeroVector() {
        SharedVector v = new SharedVector(new double[]{0.0, 0.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{
                {1.0, 2.0, 3.0, 4.0},
                {-5.0, 6.0, 7.0, 8.0}
        });

        v.vecMatMul(m);
        assertEquals(4, v.length());
        assertEquals(0.0, v.get(0), EPS);
        assertEquals(0.0, v.get(1), EPS);
        assertEquals(0.0, v.get(2), EPS);
        assertEquals(0.0, v.get(3), EPS);
    }

}
