package com.example.chatapp;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class ChatApp1 {

    // --------- User Class ---------
    static class User {

        private String fullname;
        private String gender;
        private String username;
        private String password;
        private String phone;
        private String profileImagePath;

        public User(String fullname,
                    String gender,
                    String username,
                    String password,
                    String phone,
                    String profileImagePath) {

            this.fullname = fullname;
            this.gender = gender;
            this.username = username;
            this.password = password;
            this.phone = phone;
            this.profileImagePath = profileImagePath;
        }

        public String getFullname() {
            return fullname;
        }

        public String getGender() {
            return gender;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getPhone() {
            return phone;
        }

        public String getProfileImagePath() {
            return profileImagePath;
        }
    }

    // --------- Login Logic ---------
    static class Login {

        protected static HashMap<String, User> users = new HashMap<>();

        public boolean checkUsername(String username) {
            return username.contains("_") && username.length() <= 5;
        }

        public boolean checkPasswordComplexity(String password) {
            return password != null && password.length() > 8;
        }

        public boolean checkCellphone(String phone) {
            return phone != null
                    && phone.startsWith("+")
                    && phone.length() >= 10
                    && phone.length() <= 13;
        }

        public String registerUser(String fullname,
                                   String gender,
                                   String username,
                                   String password,
                                   String confirmPassword,
                                   String phone,
                                   String imagePath) {

            StringBuilder missingFields = new StringBuilder();

            if (fullname == null || fullname.isEmpty())
                missingFields.append("Full Name, ");

            if (username == null || username.isEmpty())
                missingFields.append("Username, ");

            if (phone == null || phone.isEmpty())
                missingFields.append("Phone Number, ");

            if (password == null || password.isEmpty())
                missingFields.append("Password, ");

            if (confirmPassword == null || confirmPassword.isEmpty())
                missingFields.append("Confirm Password, ");

            if (gender == null || gender.isEmpty())
                missingFields.append("Gender, ");

            if (missingFields.length() > 0) {

                String fields = missingFields.substring(
                        0,
                        missingFields.length() - 2
                );

                return "Please fill in: " + fields;
            }

            if (!checkUsername(username)) {
                return "Username must contain an underscore and be no longer than 5 characters.";
            }

            if (!checkPasswordComplexity(password)) {
                return "Password must be at least 8 characters long.";
            }

            if (!checkCellphone(phone)) {
                return "Phone number incorrectly formatted.";
            }

            if (!password.equals(confirmPassword)) {
                return "Passwords do not match.";
            }

            if (users.containsKey(username)) {
                return "Username already exists.";
            }

            User user = new User(
                    fullname,
                    gender,
                    username,
                    password,
                    phone,
                    imagePath
            );

            users.put(username, user);

            return "Registration successful!";
        }

        public boolean loginUser(String username, String password) {

            User user = users.get(username);

            return user != null
                    && user.getPassword().equals(password);
        }

        public String returnLoginStatus(boolean loginSuccess,
                                        String username) {

            if (loginSuccess) {
                return "Welcome "
                        + username
                        + ", it is great to see you again.";
            }

            return "Username or password incorrect.";
        }
    }

    // ------------------ Message class ------------------

    public static class Message {

        private String messageID;
        private String recipientCell;
        private String messageText;
        private int messageNumber;
        private String messageHash;

        private static int sentCounter = 0;

        private static List<Message> sentMessages =
                new ArrayList<>();

        public Message(String messageID,
                       String recipientCell,
                       String messageText,
                       int messageNumber) {

            this.messageID = messageID;
            this.recipientCell = recipientCell;
            this.messageText = messageText;
            this.messageNumber = messageNumber;
            this.messageHash = createMessageHash();
        }

        public boolean checkMessageID() {
            return messageID != null
                    && messageID.length() <= 10;
        }

        public int checkRecipientCell() {

            if (recipientCell == null)
                return 0;

            if (!recipientCell.startsWith("+"))
                return 0;

            int len = recipientCell.length();

            if (len < 10 || len > 13)
                return 0;

            String rest = recipientCell.substring(1);

            return rest.matches("\\d+") ? 1 : 0;
        }

        public String createMessageHash() {

            String idPart =
                    messageID != null && messageID.length() >= 2
                            ? messageID.substring(0, 2)
                            : "00";

            String text =
                    messageText == null
                            ? ""
                            : messageText.trim();

            String[] words = text.split("\\s+");

            String first =
                    words.length >= 1
                            ? words[0]
                            : "";

            String last =
                    words.length >= 2
                            ? words[words.length - 1]
                            : first;

            return (idPart
                    + ":"
                    + messageNumber
                    + ":"
                    + first.toUpperCase()
                    + last.toUpperCase());
        }

        public String sentMessage(int choice) {

            switch (choice) {

                case 1:

                    sentCounter++;
                    sentMessages.add(this);

                    storeAllSentMessagesToJSON();

                    return "Message successfully sent.";

                case 2:

                    storeMessageAsDraft();

                    return "Message stored.";

                case 3:

                    return "Message disregarded.";

                default:

                    return "Invalid choice.";
            }
        }

        public static int returnTotalMessagess() {
            return sentCounter;
        }

        public void storeMessageAsDraft() {

            List<Map<String, String>> drafts =
                    readJsonListFromFile(
                            "stored_messages.json"
                    );

            Map<String, String> entry =
                    new HashMap<>();

            entry.put("MessageID", messageID);
            entry.put("MessageHash", messageHash);
            entry.put("Recipient", recipientCell);
            entry.put("Message", messageText);

            drafts.add(entry);

            writeJsonListToFile(
                    "stored_messages.json",
                    drafts
            );
        }

        private static void storeAllSentMessagesToJSON() {

            List<Map<String, String>> list =
                    new ArrayList<>();

            for (Message m : sentMessages) {

                Map<String, String> obj =
                        new HashMap<>();

                obj.put("MessageID", m.messageID);
                obj.put("MessageHash", m.messageHash);
                obj.put("Recipient", m.recipientCell);
                obj.put("Message", m.messageText);

                list.add(obj);
            }

            writeJsonListToFile("messages.json", list);
        }

        private static List<Map<String, String>>
        readJsonListFromFile(String filename) {

            try {

                Path p = Paths.get(filename);

                if (!Files.exists(p))
                    return new ArrayList<>();

                return new ArrayList<>();

            } catch (Exception ex) {

                return new ArrayList<>();
            }
        }

        private static void writeJsonListToFile(
                String filename,
                List<Map<String, String>> list) {

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 Paths.get(filename))) {

                writer.write("[\n");

                for (int i = 0; i < list.size(); i++) {

                    Map<String, String> map = list.get(i);

                    writer.write("  {\n");

                    int j = 0;

                    for (Map.Entry<String, String> e :
                            map.entrySet()) {

                        writer.write(
                                "    \""
                                        + e.getKey()
                                        + "\": \""
                                        + e.getValue()
                                        + "\""
                        );

                        j++;

                        if (j < map.size())
                            writer.write(",");

                        writer.write("\n");
                    }

                    writer.write("  }");

                    if (i < list.size() - 1)
                        writer.write(",");

                    writer.write("\n");
                }

                writer.write("]");

            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        public String getMessageID() {
            return messageID;
        }

        public String getRecipientCell() {
            return recipientCell;
        }

        public String getMessageText() {
            return messageText;
        }

        public String getMessageHash() {
            return messageHash;
        }
    }

    // --------- SIMPLE WINDOWS STYLE DIALOGS ---------

    static class RegisterForm {

        public RegisterForm() {

            Login loginLogic = new Login();

            String fullname =
                    JOptionPane.showInputDialog(
                            null,
                            "Enter Full Name:",
                            "Register",
                            JOptionPane.QUESTION_MESSAGE
                    );

            String username =
                    JOptionPane.showInputDialog(
                            null,
                            "Enter Username:",
                            "Register",
                            JOptionPane.QUESTION_MESSAGE
                    );

            String phone =
                    JOptionPane.showInputDialog(
                            null,
                            "Enter Phone Number:",
                            "Register",
                            JOptionPane.QUESTION_MESSAGE
                    );

            String password =
                    JOptionPane.showInputDialog(
                            null,
                            "Enter Password:",
                            "Register",
                            JOptionPane.QUESTION_MESSAGE
                    );

            String confirmPassword =
                    JOptionPane.showInputDialog(
                            null,
                            "Confirm Password:",
                            "Register",
                            JOptionPane.QUESTION_MESSAGE
                    );

            String[] genders = {
                    "Male",
                    "Female"
            };

            String gender =
                    (String) JOptionPane.showInputDialog(
                            null,
                            "Select Gender:",
                            "Register",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            genders,
                            genders[0]
                    );

            String result =
                    loginLogic.registerUser(
                            fullname,
                            gender,
                            username,
                            password,
                            confirmPassword,
                            phone,
                            ""
                    );

            JOptionPane.showMessageDialog(
                    null,
                    result
            );

            new LoginForm();
        }
    }

    static class LoginForm {

        public LoginForm() {

            Login loginLogic = new Login();

            String[] options = {
                    "Login",
                    "Register"
            };

            int choice =
                    JOptionPane.showOptionDialog(
                            null,
                            "Choose an option",
                            "ChatApp",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (choice == 1) {
                new RegisterForm();
                return;
            }

            String username =
                    JOptionPane.showInputDialog(
                            null,
                            "Enter Username:",
                            "Login",
                            JOptionPane.QUESTION_MESSAGE
                    );

            String password =
                    JOptionPane.showInputDialog(
                            null,
                            "Enter Password:",
                            "Login",
                            JOptionPane.QUESTION_MESSAGE
                    );

            boolean success =
                    loginLogic.loginUser(
                            username,
                            password
                    );

            String message =
                    loginLogic.returnLoginStatus(
                            success,
                            username
                    );

            JOptionPane.showMessageDialog(
                    null,
                    message
            );

            if (success) {
                runQuickChatLoop(username);
            } else {
                new LoginForm();
            }
        }
    }

    // --------- QuickChat loop ---------

    private static void runQuickChatLoop(
            String loggedInUsername) {

        JOptionPane.showMessageDialog(
                null,
                "Welcome to QuickChat."
        );

        String numStr =
                JOptionPane.showInputDialog(
                        "How many messages will you enter?"
                );

        int maxMessages = 0;

        try {

            maxMessages = Integer.parseInt(numStr);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    null,
                    "Invalid number."
            );

            return;
        }

        int entered = 0;

        while (true) {

            String[] options = {
                    "Send Messages",
                    "Show recently sent messages",
                    "Quit"
            };

            int choice =
                    JOptionPane.showOptionDialog(
                            null,
                            "Choose an option",
                            "QuickChat Menu",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (choice == 0) {

                if (entered >= maxMessages) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Message limit reached."
                    );

                    continue;
                }

                String messageID =
                        generateRandomDigitString(10);

                String recipient =
                        JOptionPane.showInputDialog(
                                "Enter Recipient:"
                        );

                String messageText =
                        JOptionPane.showInputDialog(
                                "Enter Message:"
                        );

                if (messageText == null)
                    messageText = "";

                if (messageText.length() > 250) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Message exceeds 250 characters."
                    );

                    continue;
                }

                int messageNumber =
                        Message.returnTotalMessagess();

                Message m =
                        new Message(
                                messageID,
                                recipient,
                                messageText,
                                messageNumber
                        );

                if (m.checkRecipientCell() == 0) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Invalid cellphone number."
                    );

                    continue;
                }

                String[] actions = {
                        "Send Message",
                        "Store Message",
                        "Disregard Message"
                };

                int action =
                        JOptionPane.showOptionDialog(
                                null,
                                "Choose action",
                                "Message Action",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                actions,
                                actions[0]
                        );

                String result =
                        m.sentMessage(action + 1);

                String details =
                        "Message Details:\n\n"
                                + "MessageID: "
                                + m.getMessageID()
                                + "\n"
                                + "MessageHash: "
                                + m.getMessageHash()
                                + "\n"
                                + "Recipient: "
                                + m.getRecipientCell()
                                + "\n"
                                + "Message: "
                                + m.getMessageText();

                JOptionPane.showMessageDialog(
                        null,
                        result + "\n\n" + details
                );

                entered++;

            } else if (choice == 1) {

                JOptionPane.showMessageDialog(
                        null,
                        "Coming Soon."
                );

            } else {

                JOptionPane.showMessageDialog(
                        null,
                        "Total messages sent: "
                                + Message.returnTotalMessagess()
                );

                break;
            }
        }
    }

    // helper

    private static String generateRandomDigitString(
            int n) {

        Random rnd = new Random();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < n; i++) {
            sb.append(rnd.nextInt(10));
        }

        return sb.toString();
    }

    // --------- MAIN ---------

    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        Login.users.put(
                "test_",
                new User(
                        "Test User",
                        "Male",
                        "test_",
                        "Password123",
                        "+27831234567",
                        ""
                )
        );

        SwingUtilities.invokeLater(
                () -> new LoginForm()
        );
    }
}