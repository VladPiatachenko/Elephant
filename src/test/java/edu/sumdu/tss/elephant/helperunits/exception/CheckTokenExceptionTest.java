package edu.sumdu.tss.elephant.helperunits.exception;

import edu.sumdu.tss.elephant.helper.exception.CheckTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckTokenExceptionTest {
    @Test
    public void checkTokenException() {
        Assertions.assertThrows(RuntimeException.class, () -> { throw new CheckTokenException(); });
    }
}
