package com.app;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;

public class BasicAppTest {
    private App app = new App();

    private static String getPath(String tcase, String type) {
        String property = System.getProperty("user.dir");
        return property + "/src/test/resources/" + tcase + "/" + type + ".csv";
    }

    private void testImpl(String tc) throws IOException ,InterruptedException {
        StringBuilder bboPath = new StringBuilder();
        StringBuilder tradePath = new StringBuilder();

        app.run(getPath(tc, "input"), bboPath, tradePath);

//        Process p = Runtime.getRuntime().exec("diff " + getPath(tc, "output_bbos") +  " " + bboPath.toString());
//        assertEquals(0, p.waitFor());
//
//        p = Runtime.getRuntime().exec("diff " + getPath(tc, "output_trades") + " " + tradePath.toString());
//        assertEquals(0, p.waitFor());
    }

    @Test
    public void test01() throws IOException, InterruptedException {
        testImpl("sample_test01");
    }

    @Test
    public void test02() throws IOException, InterruptedException {
        testImpl("sample_test02");
    }

    //cancel
    @Test
    public void test03() throws IOException, InterruptedException {
        testImpl("sample_test03");
    }

    @Test
    public void test04() throws IOException, InterruptedException {
        testImpl("sample_test04");
    }

    @Test
    public void test05() throws IOException, InterruptedException {
        testImpl("sample_test05");
    }

//    //my own tests below
//    @Test
//    public void test06() throws IOException, InterruptedException {
//        testImpl("sample_test06");
//    }

//    //testing the example they gave on page 3 with the 4 sells and 2 buys
//    @Test
//    public void test07() throws IOException, InterruptedException {
//        testImpl("sample_test07");
//    }

//      //test 2 buys and then a sell
//    @Test
//    public void test08() throws IOException, InterruptedException {
//        testImpl("sample_test08");
//    }

//    //test a few buys and then a sell that has a really large size in order to exhaust all the buys
//    @Test
//    public void test09() throws IOException, InterruptedException {
//        testImpl("sample_test09");
//    }

//    //test all buys and sells are exhausted (empty order book) *
//    @Test
//    public void test10() throws IOException, InterruptedException {
//        testImpl("sample_test10");
//    }

    //cancel
    @Test
    public void test11() throws IOException, InterruptedException {
        testImpl("sample_test11");
    }
    //cancel
    @Test
    public void test12() throws IOException, InterruptedException {
        testImpl("sample_test12");
    }


    //test cancelling all orders using the example from page 3 that has the 4 sells and 2 buys initially
    @Test
    public void test13() throws IOException, InterruptedException {
        testImpl("sample_test13");
    }
}
