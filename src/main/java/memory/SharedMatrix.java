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

        // Handling cast of an empty matrix
        if (this.vectors.length == 0) {
            double[][] empty_array = {};
            return empty_array;
        }

        // Returning ROW_MAJOR matrix is a doubles array
        int m = this.vectors.length;
        int n = this.vectors[0].length();
        double[][] ret = new double[m][n];
        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++) {
                ret[i][j] = this.vectors[i].get(j);
            }
        }

        return ret;
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        return this.vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return this.vectors.length;
    }

    // ** Throws IllegalArgumentException when given empty matrix
    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (this.vectors.length == 0) {
            throw new IllegalArgumentException("Cannot determine orientation of empty matrix");
        }
        return this.vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (SharedVector v : vecs) {
            v.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for (SharedVector v : vecs) {
            v.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (SharedVector v : vecs) {
            v.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (SharedVector v : vecs) {
            v.writeUnlock();
        }
    }
}
