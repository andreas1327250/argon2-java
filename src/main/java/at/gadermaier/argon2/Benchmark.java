package at.gadermaier.argon2;

import at.gadermaier.argon2.model.Argon2Type;

import static at.gadermaier.argon2.Constants.Defaults.ARGON2_VERSION_NUMBER;
import static at.gadermaier.argon2.model.Argon2Type.*;

public class Benchmark {

    public static void main(String[] args) {
        if (args.length > 0)
            benchmark(Integer.parseInt(args[0]));
        else
            benchmark(18);
    }

    /*
     * Benchmarks Argon2 with salt length 16, password length 16, t_cost 1,
     * and different m_cost and threads
     */
    private static void benchmark(int maxMemory) {

        System.out.println("threads;memory;seconds");

        int inlen = 16;
        int outlen = 16;

        int t_cost = 3;
        int m_cost;
        int[] thread_test = new int[]{1, 2, 4, 8};
        Argon2Type[] types = new Argon2Type[]{Argon2i, Argon2d, Argon2id};

        byte[] pwdBytes = new byte[inlen];
        byte[] saltBytes = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

        //warmup jit
        Argon2Factory.create()
                .setIterations(10)
                .setParallelism(4)
                .hash("password".toCharArray(), "saltsalt");

        for (m_cost = 10; m_cost <= maxMemory; m_cost++) {
            for (int i = 0; i < 4; i++) {


                double run_time = 0;
                int thread_n = thread_test[i];

                int runs = 3;
                run(outlen, t_cost, m_cost, types, pwdBytes, saltBytes, run_time, thread_n, runs);
                System.gc();
            }
        }
    }

    private static void run(int outlen, int t_cost, int m_cost, Argon2Type[] types, byte[] pwdBytes, byte[] saltBytes, double run_time, int thread_n, int runs) {
        for(int averageIndex=0; averageIndex<runs; averageIndex++){

            for (int typeIndex = 0; typeIndex < 3; ++typeIndex) {
                long start_time, stop_time;

                Argon2Type type = types[typeIndex];


                Argon2 argon2 =  Argon2Factory.create()
                        .setIterations(t_cost)
                        .setMemory(m_cost)
                        .setParallelism(thread_n)
                        .setType(type)
                        .setVersion(ARGON2_VERSION_NUMBER)
                        .setOutputLength(outlen);

                start_time = System.nanoTime();
                argon2.hash(pwdBytes, saltBytes);
                run_time += (double) System.nanoTime() - start_time;

            }
        }
        run_time /= 1000000000.0 * runs;

//                System.out.println(t_cost + " iterations " + (Math.pow(2,m_cost-10)) + " MB " +
//                        thread_n + " threads");
//                System.out.println(run_time + " seconds\n");

        System.out.println(thread_n + ";" + m_cost + ";" + run_time);
    }
}
