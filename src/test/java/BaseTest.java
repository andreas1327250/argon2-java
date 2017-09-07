import at.gadermaier.argon2.Argon2;
import at.gadermaier.argon2.Argon2Factory;
import org.junit.Before;

public abstract class BaseTest {

    protected Argon2 argon2;

    @Before
    public void setUp(){
        argon2 = Argon2Factory.create()
                .setPassword("password".toCharArray())
                .setSalt("saltsalt");
    }
}
