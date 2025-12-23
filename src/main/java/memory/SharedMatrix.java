package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix

        // Handles null pointers gracefully by defaulting to  initializing it as an empty matrix
        if (matrix == null) {
            this.vectors = new SharedVector[0];
            return;
        }

        // Creating ROW_MAJOR matrix
        int len = matrix.length;
        SharedVector[] tmpMatrix = new SharedVector[len];

        for (int i = 0; i < len; i++) {
            tmpMatrix[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }

        this.vectors = tmpMatrix;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix

        // Handles null pointers gracefully by defaulting to  initializing it as an empty matrix
        if (matrix == null) {
            this.vectors = new SharedVector[0];
            return;
        }

        // Creating COLUMN_MAJOR matrix
        int mlen = matrix[0].length;
        int vlen = matrix.length;
        SharedVector[] tmpMatrix = new SharedVector[mlen];

        for (int i = 0; i < mlen; i++) {
            double[] tmpVector = new double[vlen];
            for (int j = 0; j < vlen; j++) {
                tmpVector[j] = matrix[j][i];
            }

            tmpMatrix[i] = new SharedVector(tmpVector, VectorOrientation.COLUMN_MAJOR);
        }
        this.vectors = tmpMatrix;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        return null;
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        return null;
    }

    public int length() {
        // TODO: return number of stored vectors
        return 0;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        return null;
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
    }
}
