package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    private final double[][] data1 = {
            {1.0, 2.0},
            {3.0, 4.0}
    };

    private final double[][] data2 = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
    };

    private final double[][] data3 = {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
    };


    @Test
    void testEmpty() {
        SharedMatrix matrix = new SharedMatrix();
        assertEquals(0, matrix.length());
    }

    @Test
    void testNull() {
        // Checks illegal Argument exception is thrown when starting new matrix
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(null));
    }

    @Test
    void testConstructor() {
        // Constructing 3 new matrices
        SharedMatrix matrix1 = new SharedMatrix(data1);
        SharedMatrix matrix2 = new SharedMatrix(data2);
        SharedMatrix matrix3 = new SharedMatrix(data3);

        // Vector orientation should be ROW_MAJOR
        assertEquals(VectorOrientation.ROW_MAJOR, matrix1.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix2.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix3.getOrientation());

        // Testing output size
        assertEquals(2, matrix1.length());
        assertEquals(2, matrix2.length());
        assertEquals(3, matrix3.length());
    }

    @Test
    void testLoadColumnMajor() {
        SharedMatrix matrix1 = new SharedMatrix();
        SharedMatrix matrix2 = new SharedMatrix();
        matrix1.loadColumnMajor(data1);
        matrix2.loadColumnMajor(data2);

        // Checks matrices are properly column major
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix1.getOrientation());
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix2.getOrientation());

        // Checks lengths
        assertEquals(2, matrix1.length());
        assertEquals(3, matrix2.length());
    }

    @Test
    void testLoadRowMajor() {
        SharedMatrix matrix1 = new SharedMatrix();
        SharedMatrix matrix2 = new SharedMatrix();
        matrix1.loadRowMajor(data1);
        matrix2.loadRowMajor(data2);

        // Checks matrices are properly column major
        assertEquals(VectorOrientation.ROW_MAJOR, matrix1.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix2.getOrientation());

        // Checks lengths
        assertEquals(2, matrix1.length());
        assertEquals(2, matrix2.length());
    }

    @Test
    void testReadRowMajor() {
        // Constructing 3 new matrices
        SharedMatrix matrix1 = new SharedMatrix(data1);
        SharedMatrix matrix2 = new SharedMatrix(data2);
        SharedMatrix matrix3 = new SharedMatrix(data3);

        // Testing output
        assertArrayEquals(data1, matrix1.readRowMajor());
        assertArrayEquals(data2, matrix2.readRowMajor());
        assertArrayEquals(data3, matrix3.readRowMajor());
    }

    @Test
    void testGet() {
        // Gets the vector at the first index
        SharedMatrix matrix1 = new SharedMatrix(data1);
        SharedVector vector1 = matrix1.get(0);

        // Checks whether vector is correct
        assertEquals(2, vector1.length());
        assertEquals(1.0, vector1.get(0));
        assertEquals(2.0, vector1.get(1));
    }
}