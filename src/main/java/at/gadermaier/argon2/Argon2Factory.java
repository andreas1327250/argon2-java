package at.gadermaier.argon2;

import at.gadermaier.argon2.model.Argon2Type;
import static java.lang.Integer.parseInt;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Argon2Factory {

    // parses an argon2 hash like this: $argon2i$v=19$m=4096,t=3,p=1$abcd$efgh
    // guide: $type$version$parameters(m,t,p)$salt$hash
    public static final Pattern ARGON2_ENCODING_PATTERN = Pattern.compile("^\\$(?<type>\\w*?)\\$v=(?<v>\\d*?)\\$m=(?<m>\\d*?),t=(?<t>\\d*?),p=(?<p>\\d*?)\\$(?<salt>[A-Za-z0-9\\+\\/\\=]*?)\\$(?<hash>[A-Za-z0-9\\+\\/\\=]*?)$");

    public static Argon2 createFromEncoding(String encoded) {
        Matcher m = ARGON2_ENCODING_PATTERN.matcher(encoded);
        if (m.matches()) {
            try {
                Argon2 argon2 = new Argon2();
                switch (m.group("type")) {
                    case "argon2i":
                        argon2.setType(Argon2Type.Argon2i);
                        break;
                    case "argon2id":
                    case "argon2di":
                        argon2.setType(Argon2Type.Argon2id);
                        break;
                    default:
                        throw new RuntimeException("Only compatible with argon2i or argon2id.");
                }
                argon2.setVersion(parseInt(m.group("v")))
                        .setMemoryInKiB(parseInt(m.group("m")))
                        .setIterations(parseInt(m.group("t")))
                        .setParallelism(parseInt(m.group("p")))
                        .setSalt(Base64.getDecoder().decode(m.group("salt")));
                return argon2;
            } catch (NumberFormatException n) {
                throw new RuntimeException("The provided encoding has a wrong number.", n);
            }
        }
        throw new IllegalArgumentException("The provided argon2 encoding is not formatted correctly.");
    }

    public static boolean verify(String encoded, String password) {
        Argon2 argon2 = createFromEncoding(encoded);
        Matcher m = ARGON2_ENCODING_PATTERN.matcher(encoded);
        m.matches();
        String base64input = m.group("hash");
        argon2.hash(password.getBytes(argon2.getCharset()), argon2.getSalt());
        String base64output = Base64.getEncoder().withoutPadding().encodeToString(argon2.getOutput());
        return base64input.equals(base64output);
    }

    public static Argon2 create() {
        return new Argon2();
    }
}
