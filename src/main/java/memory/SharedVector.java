package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // store vector data and its orientation
        if (vector == null || orientation == null)
            throw new IllegalArgumentException("vector and orientation cannot be null");
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // return element at index (read-locked)
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

    public int length() {
        // return vector length
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        // return vector orientation
        readLock();
        try {
            return orientation;
        }
        finally {
            readUnlock();
        }
    }

    public void writeLock() {
        // acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // release read lock
        lock.readLock().unlock();
    }

    public void transpose() {
        // transpose vector
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

    public void add(SharedVector other) {
        // add two vectors
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

        // copy other with readLock to prevent deadlock
        final double[] otherCopy;
        final VectorOrientation otherOrientation;
        other.readLock();
        try {
            if (this.vector.length != other.vector.length)
                throw new IllegalArgumentException("vector and other vector lengths do not match");
            otherCopy = other.vector.clone();
            otherOrientation = other.orientation;
        }
        finally {
            other.readUnlock();
        }

        // update this with writeLock
        this.writeLock();
        try {
            if (this.orientation != otherOrientation)
                throw new IllegalArgumentException("orientations do not match");
            for (int i = 0; i < vector.length; i++)
                vector[i] += otherCopy[i];
        }
        finally {
            this.writeUnlock();
        }

    }

    public void negate() {
        // TODO: negate vector
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        return 0;
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
    }
}
