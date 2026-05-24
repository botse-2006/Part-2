package com.example.chatapp;

import org.junit.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.*;

public class ChatApp1Test {

    private ChatApp1.Login login;

    @Before
    public void setup() throws IOException {

        login = new ChatApp1.Login();

        ChatApp1.Login.users.clear();

        Files.deleteIfExists(Paths.get("messages.json"));
        Files.deleteIfExists(Paths.get("stored_messages.json"));

        resetMessageStatics();
    }

    private void resetMessageStatics() {

        try {

            var sentCounter =
                    ChatApp1.Message.class.getDeclaredField("sentCounter");

            var sentMessages =
                    ChatApp1.Message.class.getDeclaredField("sentMessages");

            sentCounter.setAccessible(true);
            sentMessages.setAccessible(true);

            sentCounter.setInt(null, 0);

            ((List<?>) sentMessages.get(null)).clear();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    // ---------- LOGIN TESTS ----------

    @Test
    public void testCheckUsername_ValidAndInvalid() {

        assertTrue(login.checkUsername("ab_cd"));

        assertFalse(login.checkUsername("abcdef"));

        assertFalse(login.checkUsername("abcde"));
    }

    @Test
    public void testCheckPasswordComplexity() {

        assertTrue(login.checkPasswordComplexity("StrongPass1"));

        assertFalse(login.checkPasswordComplexity("short"));
    }

    @Test
    public void testCheckCellphone() {

        assertTrue(login.checkCellphone("+27831234567"));

        assertFalse(login.checkCellphone("27831234567"));

        assertFalse(login.checkCellphone("+123"));
    }

    @Test
    public void testRegisterUser_Successful() {

        String result =
                login.registerUser(
                        "John Doe",
                        "Male",
                        "jd_1",
                        "StrongPass1",
                        "StrongPass1",
                        "+27831234567",
                        "img.jpg"
                );

        assertEquals("Registration successful!", result);

        assertTrue(ChatApp1.Login.users.containsKey("jd_1"));
    }

    @Test
    public void testRegisterUser_MissingFields() {

        String result =
                login.registerUser(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                );

        assertTrue(result.contains("Please fill in"));
    }

    @Test
    public void testRegisterUser_PasswordMismatch() {

        String result =
                login.registerUser(
                        "John",
                        "Male",
                        "jo_1",
                        "pass12345",
                        "different",
                        "+27831234567",
                        ""
                );

        assertEquals("Passwords do not match.", result);
    }

    @Test
    public void testRegisterUser_DuplicateUsername() {

        login.registerUser(
                "Jane",
                "Female",
                "ja_1",
                "StrongPass1",
                "StrongPass1",
                "+27831234567",
                ""
        );

        String result =
                login.registerUser(
                        "Another",
                        "Male",
                        "ja_1",
                        "StrongPass1",
                        "StrongPass1",
                        "+27831234567",
                        ""
                );

        assertEquals("Username already exists.", result);
    }

    @Test
    public void testLoginUser_And_StatusMessages() {

        login.registerUser(
                "Alice",
                "Female",
                "al_1",
                "StrongPass1",
                "StrongPass1",
                "+27831234567",
                ""
        );

        assertTrue(login.loginUser("al_1", "StrongPass1"));

        assertFalse(login.loginUser("al_1", "wrongpass"));

        assertEquals(
                "Welcome al_1, it is great to see you again.",
                login.returnLoginStatus(true, "al_1")
        );

        assertEquals(
                "Username or password incorrect.",
                login.returnLoginStatus(false, "al_1")
        );
    }

    // ---------- MESSAGE TESTS ----------

    @Test
    public void testMessageIDAndRecipientValidation() {

        ChatApp1.Message msg =
                new ChatApp1.Message(
                        "AB12",
                        "+27831234567",
                        "Hi there",
                        0
                );

        assertTrue(msg.checkMessageID());

        assertEquals(1, msg.checkRecipientCell());

        ChatApp1.Message bad =
                new ChatApp1.Message(
                        "LONGMESSAGEID",
                        "0831234567",
                        "Invalid",
                        0
                );

        assertFalse(bad.checkMessageID());

        assertEquals(0, bad.checkRecipientCell());
    }

    @Test
    public void testMessageHashCreation() {

        ChatApp1.Message msg =
                new ChatApp1.Message(
                        "AB12",
                        "+27831234567",
                        "Hello World",
                        1
                );

        String hash = msg.createMessageHash();

        assertTrue(hash.startsWith("AB:1:HELLOWORLD"));
    }

    @Test
    public void testSendMessage_IncrementsCounterAndPersists()
            throws IOException {

        ChatApp1.Message msg =
                new ChatApp1.Message(
                        "ID01",
                        "+27831234567",
                        "Hello",
                        0
                );

        String result = msg.sentMessage(1);

        assertEquals("Message successfully sent.", result);

        assertEquals(
                1,
                ChatApp1.Message.returnTotalMessagess()
        );

        assertTrue(Files.exists(Paths.get("messages.json")));

        String content =
                Files.readString(Paths.get("messages.json"));

        assertTrue(content.contains("ID01"));
    }

    @Test
    public void testStoreMessage_SavesToStoredMessagesFile()
            throws IOException {

        ChatApp1.Message msg =
                new ChatApp1.Message(
                        "ID02",
                        "+27831234567",
                        "Draft message",
                        1
                );

        String result = msg.sentMessage(2);

        assertEquals("Message stored.", result);

        assertTrue(
                Files.exists(Paths.get("stored_messages.json"))
        );

        String content =
                Files.readString(Paths.get("stored_messages.json"));

        assertTrue(content.contains("Draft message"));
    }

    @Test
    public void testDisregardMessage() {

        ChatApp1.Message msg =
                new ChatApp1.Message(
                        "ID03",
                        "+27831234567",
                        "Ignore this",
                        2
                );

        String result = msg.sentMessage(3);

        assertEquals("Message disregarded.", result);
    }

    @After
    public void cleanup() throws IOException {

        Files.deleteIfExists(Paths.get("messages.json"));

        Files.deleteIfExists(Paths.get("stored_messages.json"));
    }
}