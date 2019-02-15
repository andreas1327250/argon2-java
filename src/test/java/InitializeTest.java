import at.gadermaier.argon2.Argon2;
import at.gadermaier.argon2.Argon2Factory;
import at.gadermaier.argon2.algorithm.Initialize;
import at.gadermaier.argon2.model.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class InitializeTest {

    private String instanceNull = "0011a4db4b4dc5422bfab9973caf41bd78d9b20f9a01c236e329c65ffa67c2e6d655eada0eddbc82fe29d0a8fc33ca267e76889ac563e5fcf2a61cb395bd32404a9f9f6bed4324328e4671614efd85ebaa3ff319a4b5996c3fb04e85188839580c945aa12081d3496c6f9a25bf3214d0f766e54c372361a0f3d62ad7acc5cb0306b8fca890e0fcdf932597e3f819ff1ae836976dae0af12b8d53036d1cff4b3bbf5a25de57828ed2de7baa364f0b32ff3ce0626c7cb1e3b76d9bf9a7857e051c1c7a3368792fa1a01a79a7b6c83f97a2cfb3c40c9a7c5f3616673f1f97716a10c6e27a950fb737aab6c544ab59bff3f328a13acf432942c0096420ac7a1ed2de34c181aaf71802ed9a56184476b983e011a50a6203f628d9028d1b442c006d95cbe819acc754451862eab50d7df1ec6ac6bcd55169a7c64f00a3305609df52a178498c44e1d11adf56661e600eeeaf5132864fb99e937cfd3173141e3e619451f1b774719fe6c9d16a73edad3fd494e7520cdefe9e77e210ae2bff00d398e91d6a387775e0a6d9806dc88fa4c481bf4968565cd82df21d135d3bba1cb64c6b65a99d56dd55143f6658d99a5e2c6768394a1036e9cf32fdbaaf704a28d6671423ceb5b54074eb0bde342d541b597a27bf6eced39c168f47da2650df61f96280c1c0f3b858d51534061180c7185ff5f0293a76be3a165ad547303c81c7a480e9eaeddf7f5770b90c3600535b1b3f4be981c357fe08f8db0405ff7d4bc7e5fa94653038aa72ea2f4d4bbb6eb7e5ac76166c57151f0f9c1d26e0307e0d037119e647d33399e8bd0a66b60d18bd0e53af50658e8288b3e829365743abb199c5c59cc910756eb6eacd2a1d73646e47215998d96458be9b00ebf04fc46cdd651d02339822400b5a9565ba11e4b368fbcbdc6ee047ef6e93e66fc08aa0b24349f929d6d3122f2b96b683d709a9c18f584914df5397cf106076fe63823c63470b4c641a7057cf6a690956b6c03db91133db7620cb5e929204a0771c17ac3edb4778e95cb41173c91db45740e1023493242b684cefa0544e384adc8913c4e7bd4711088c2b87d1b922c59bed69a5afbc241cc270268eab791969e3d4181199295af1562631cc5147f8e17607620d965509623ff65261319f2084fb670e473fa990f2b52a6a3fe9dd9b4630e124d7c1b21df4fe4db64b53d4e8c1be5f50093d271a2a7818518ecb786f3c5d36555d348eb3f26fe7031d491167f1c8cd1974e75eb3a205949b3f8b4be0d7dffdee49069f6fa23bd8dec2d950aad3bacae2d3208d543171868665f9d1d6837dab20672d25a32b6794cfc1492a8cf06dcc142a9ca91dc81f00d20038fd7e26e0af42e2a4727a3b4f7b49a17cf3f660635a18063773e3999c3bda22e749b4cb7da41c455ac3e261d64e992d58e28f8157687c5ace8d26349544b3";

    private Argon2 argon2;

    @BeforeEach
    public void setUp(){
        argon2 = Argon2Factory.create()
                              .setPassword("password".toCharArray())
                              .setSalt("saltsalt");
    }

    @Test
    public void basicTest(){

        Instance instance = new Instance(argon2);
        Initialize.initialize(instance, argon2);

        assertEquals(instanceNull, instance.memory[0].toString());
    }
}
