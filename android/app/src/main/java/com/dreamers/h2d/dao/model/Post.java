package com.dreamers.h2d.dao.model;

import com.dreamers.h2d.dao.MessageComparator;
import com.dreamers.h2d.sorteddata.SortedData;
import com.dreamers.h2d.sorteddata.SortedDataFactory;

import java.util.Iterator;
import java.util.UUID;

public class Post implements HasUUID {
	public final UUID id;
	public final UUID poster;
	public final String topic;
    public final SortedData<Message> messages;

	public Post(UUID id, UUID poster, String topic) {
		this.id = id;
		this.poster = poster;
		this.topic = topic;
		this.messages = SortedDataFactory.makeSortedData(MessageComparator.getInstance());
	}

	public Post(UUID id) {
		this(id, null, null);
	}

    /** If user is admin, it just returns the messages field directly*/
    /** If user is not admin, it needs to filter out hidden messages.*/
    public SortedData<Message> getVisibleMessages(boolean isAdmin) {
        if (isAdmin) return messages;

        SortedData<Message> visible = SortedDataFactory.makeSortedData(MessageComparator.getInstance());
        Iterator<Message> it = messages.getAll();
        while (it.hasNext()) {
            Message msg = it.next();
            if (!msg.isHidden()) {
                visible.insert(msg);
            }
        }
        return visible;
    }

	public UUID getUUID() { return id; }
}
