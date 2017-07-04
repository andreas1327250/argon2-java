import at.gadermaier.argon2.Argon2ArgumentFactory;
import at.gadermaier.argon2.Argon2Factory;
import org.junit.Test;

public class DevTest {

    @Test
    public void basicTest(){
        long start = System.nanoTime();
        long start2 = System.currentTimeMillis();


        String result = Argon2Factory.create()
                //.setParallelism(2)
                //.setOutputLength(10)
                .hash("password".toCharArray(), "saltsalt");

//        System.out.println();
//        System.out.println(result);

        long end = System.nanoTime();
        long end2 = System.currentTimeMillis();

        long time = end - start;
        long time2 = end2 - start2;
        double timeSeconds = time / (1000*1000);
//        System.out.println(timeSeconds);
//        System.out.println(time2);
    }
}
