package at.gadermaier.argon2;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        Argon2 argon2 = Argon2ArgumentFactory.parseArguments(args);
        char[] password;
        final Console console = System.console();

        if(console != null)
            password = console.readPassword();
        else{
            password = new Scanner(System.in).next().toCharArray();
            /* UNSAFE - only for testing purposes
            like piping input into argon2 - echo password | java -jar argon2.jar saltsalt
             */
        }

        argon2.setPassword(password)
            .hash();

        argon2.printSummary();
    }

}
