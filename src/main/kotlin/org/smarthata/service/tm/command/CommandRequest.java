package org.smarthata.service.tm.command;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class CommandRequest implements Iterator<String> {
    private final List<String> path;
    private final String chatId;
    private final Integer messageId;
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

    public List<String> getPathRemoving(String... removing) {
        Set<String> set = new HashSet<>(Arrays.asList(removing));
        return path.stream()
                .filter(s -> !set.contains(s))
                .collect(Collectors.toList());
    }
}
