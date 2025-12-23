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
        readLock();
        try {
            return vector.length;
        }
        finally {
            readUnlock();
        }
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
            otherCopy = other.vector.clone();
            otherOrientation = other.orientation;
        }
        finally {
            other.readUnlock();
        }

        // update this with writeLock
        this.writeLock();
        try {
            if (this.vector.length != otherCopy.length)
                throw new IllegalArgumentException("vector and other vector lengths do not match");
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
        // negate vector
        writeLock();
        try {
            for (int i = 0; i < vector.length; i++)
                vector[i] *= -1;
        }
        finally {
            writeUnlock();
        }
    }


    public double dot(SharedVector other) {
        // compute dot product (row · column)
        if (other == null)
            throw new IllegalArgumentException("other cannot be null");

        // copy vectors with readLock to prevent deadlock
        final double[] thisCopy;
        final VectorOrientation thisOrientation;
        this.readLock();
        try {
            thisCopy = this.vector.clone();
            thisOrientation = this.orientation;
        }
        finally {
            this.readUnlock();
        }
        final double[] otherCopy;
        final VectorOrientation otherOrientation;
        other.readLock();
        try {
            otherCopy = other.vector.clone();
            otherOrientation = other.orientation;
        }
        finally {
            other.readUnlock();
        }

        if (thisCopy.length != otherCopy.length)
            throw new IllegalArgumentException("vector and other vector lengths do not match");

        if (thisOrientation != VectorOrientation.ROW_MAJOR || otherOrientation != VectorOrientation.COLUMN_MAJOR)
            throw new IllegalArgumentException("wrong orientations");

        double sum = 0.0;
        for (int i = 0; i < thisCopy.length; i++)
            sum += thisCopy[i] * otherCopy[i];

        return sum;
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
    }
}
