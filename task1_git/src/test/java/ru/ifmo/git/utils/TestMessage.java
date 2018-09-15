package ru.ifmo.git.utils;

import org.junit.Test;
import ru.ifmo.git.util.Message;

import static junit.framework.TestCase.*;

public class TestMessage {

    @Test
    public void WriteThenReadTest() {
        Message msg = new Message();
        msg.write("kek!");
        assertEquals("kek!", msg.read());
    }

    @Test
    public void ConstructorWithStringThenReadTest() {
        Message msg = new Message("kek!");
        assertEquals("kek!", msg.read());
    }

    @Test
    public void WriteReadClearReadTest() {
        Message msg = new Message();
        msg.write("kek!");
        assertEquals("kek!", msg.read());
        msg.clear();
        assertEquals("", msg.read());
    }

}
