package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    @Test
    void testEmpty() {
        SharedMatrix matrix = new SharedMatrix();
        assertEquals(3, matrix.length());
    }

}