package net.sxlver.jrpc.core.protocol;

import lombok.NoArgsConstructor;

import java.util.Random;

@NoArgsConstructor
public class ConversationUID {

    private static final Random rand = new Random();

    private long uid;

    public ConversationUID(final long uid) {
        this.uid = uid;
    }

    public long uid() {
        return uid;
    }

    @Override
    public String toString() {
        return String.valueOf(uid);
    }

    public static ConversationUID newUid() {
        return new ConversationUID(rand.nextLong());
    }

    public static long next() { return rand.nextLong(); }
}
