package org.smarthata.service.tm.command;


import java.util.*;
import java.util.stream.Collectors;

public class CommandRequest implements Iterator<String> {
    public final List<String> path;
    public final String chatId;
    public final Integer messageId;
    private int read = 0;

    public CommandRequest(List<String> path, String chatId, Integer messageId) {
        this.path = Collections.unmodifiableList(path);
        this.chatId = chatId;
        this.messageId = messageId;
    }

    public String next() {
        if (hasNext()) {
            return path.get(read++);
        }
        return "";
    }

    public boolean hasNext() {
        return path.size() > read;
    }

    public List<String> createPathRemoving(String... removing) {
        Set<String> set = new HashSet<>(Arrays.asList(removing));
        return path.stream()
                .filter(s -> !set.contains(s))
                .collect(Collectors.toList());
    }
}
