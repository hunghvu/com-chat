package edu.uw.comchat.ui.chat.generatorfortesting;

import edu.uw.comchat.ui.chat.chatroom.ChatMessage;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to create dummy chats for development.
 * Actual chats will be obtained from our web server and possibly
 * local storage.
 *
 * @author Jerry Springer
 * @version 11 November 2020
 */
// TODO Do we still need to keep this class? - Hung Vu
public final class ChatMessageGenerator {

  private static final ChatMessage[] CHATS;
  public static final int COUNT = 20;

  static {
    CHATS = new ChatMessage[COUNT];
    for (int i = 0; i < CHATS.length; i++) {
      CHATS[i] = new ChatMessage((i + 1),
              "Message",
              "Sender " + (i + 1),
              "Time");
    }
  }

  public static List<ChatMessage> getChatList() {
    return Arrays.asList(CHATS);
  }

  private ChatMessageGenerator() {
  }
  // Checkstyle done, sprint 2 - Hung Vu. Ignore member name errors if they exist.
}