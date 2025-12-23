package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    // _____fields_____
    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();


    // _____constructor_____
    public SharedVector(double[] vector, VectorOrientation orientation) {
        if (vector == null || orientation == null)
            throw new IllegalArgumentException("vector and orientation cannot be null");
        this.vector = vector;
        this.orientation = orientation;
    }

    // _____methods_____
    // return element at index (read-locked)
    public double get(int index) {
        readLock();
        try {
            if (index < 0 || index >= vector.length)
                throw new IndexOutOfBoundsException("Index: " + index + ", out of bounds for length " + vector.length);
            return vector[index];
        }
        finally {
            readUnlock();
        }

    }

    // return vector length
    public int length() {
        readLock();
        try {
            return vector.length;
        }
        finally {
            readUnlock();
        }
    }

    // return vector orientation
    public VectorOrientation getOrientation() {
        readLock();
        try {
            return orientation;
        }
        finally {
            readUnlock();
        }
    }

    // acquire write lock
    public void writeLock() {
        lock.writeLock().lock();
    }

    // release write lock
    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    // acquire read lock
    public void readLock() {
        lock.readLock().lock();
    }

    // release read lock
    public void readUnlock() {
        lock.readLock().unlock();
    }

    // transpose vector
    public void transpose() {
        writeLock();
        try {
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }
    }

    // add two vectors
    public void add(SharedVector other) {
        if (other == null)
            throw new IllegalArgumentException("other cannot be null");

        // case this = other
        if (other == this) {
            writeLock();
            try {
                for (int i = 0; i < vector.length; i++)
                    vector[i] += vector[i];
            }
            finally {
                writeUnlock();
            }
            return;
        }

        // resource ordering
        int thisIHC = System.identityHashCode(this);
        int otherIHC = System.identityHashCode(other);
        boolean firstIsThis = thisIHC < otherIHC;

        // locking by order
        if (firstIsThis) {
            this.writeLock();
            other.readLock();
        }
        else {
            other.readLock();
            this.writeLock();
        }

        // adding
        try {
            if (this.vector.length != other.vector.length)
                throw new IllegalArgumentException("vectors length do not match");
            if (this.orientation != other.orientation)
                throw new IllegalArgumentException("orientations do not match");
            for (int i = 0; i < vector.length; i++)
                this.vector[i] += other.vector[i];
        }
        finally {
            // unlocking by order
            if (firstIsThis) {
                other.readUnlock();
                this.writeUnlock();
            }
            else {
                this.writeUnlock();
                other.readUnlock();
            }
        }
    }

    // negate vector
    public void negate() {
        writeLock();
        try {
            for (int i = 0; i < vector.length; i++)
                vector[i] *= -1;
        }
        finally {
            writeUnlock();
        }
    }

    // compute dot product (row · column)
    public double dot(SharedVector other) {
        if (other == null)
            throw new IllegalArgumentException("other cannot be null");

        // case this = other
        if (this == other) {
            readLock();
            try {
                double result = 0.0;
                for (int i = 0; i < vector.length; i++)
                    result += vector[i] * vector[i];
                return result;
            }
            finally {
                readUnlock();
            }
        }

        // resource ordering
        int thisIHC = System.identityHashCode(this);
        int otherIHC = System.identityHashCode(other);
        boolean firstIsThis = thisIHC < otherIHC;

        // locking by order
        if (firstIsThis) {
            this.readLock();
            other.readLock();
        }
        else {
            other.readLock();
            this.readLock();
        }

        // multiplying
        try {
            if (this.vector.length != other.vector.length)
                throw new IllegalArgumentException("vectors length do not match");
            if (this.orientation == other.orientation)
                throw new IllegalArgumentException("orientations are the same");
            double result = 0.0;
            for (int i = 0; i < vector.length; i++)
                result += vector[i] * other.vector[i];
            return result;
        }
        finally {
            // unlocking by order
            if (firstIsThis) {
                other.readUnlock();
                this.readUnlock();
            }
            else {
                this.readUnlock();
                other.readUnlock();
            }
        }
    }

    // compute row-vector × matrix
    public void vecMatMul(SharedMatrix matrix) {
        if (matrix == null || matrix.length() == 0)
            throw new IllegalArgumentException("matrix cannot be null or empty");

        writeLock();
        try {
            if (orientation != VectorOrientation.ROW_MAJOR)
                throw new IllegalArgumentException("vector is not a row major");

            int matLength = matrix.length();
            VectorOrientation matOri = matrix.getOrientation();
            int matRows;
            int matCols;

            if (matOri == VectorOrientation.ROW_MAJOR) {
                matRows = matLength;
                matCols = matrix.get(0).length();
            } else {
                matCols = matLength;
                matRows = matrix.get(0).length();
            }

            if (vector.length != matRows)
                throw new IllegalArgumentException("vector length does not match matrix");

            double[] result = new double[matCols];


            if (matOri == VectorOrientation.ROW_MAJOR) {
                for (int i = 0; i < matCols; i++) {
                    for (int j = 0; j < matRows; j++) {
                        SharedVector row = matrix.get(j);
                        result[i] += vector[j] * row.get(i);
                    }
                }
            }
            else {
                for (int i = 0; i < matCols; i++) {
                    SharedVector col = matrix.get(i);
                    for (int j = 0; j < matRows; j++) {
                        result[i] += vector[j] * col.get(j);
                    }
                }
            }

            this.vector = result;
        }
        finally {
            writeUnlock();
        }
    }

}
