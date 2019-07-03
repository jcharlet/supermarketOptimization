package ex1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ex1Test {


    @Test
    void testThatICanReturnHelloWorld() {
        //given class
        Ex1 ex1 = new Ex1();

        //when I execute function
        String output = ex1.returnHelloWorld();

        // then I get expected value
        assertEquals("Hello World",output);
    }



}