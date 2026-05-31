package com.dreamers.h2d.dao.model;

import java.util.UUID;

/** Report contains the message, user and timestamp*/
public record Report(
        UUID message,
        UUID user,
        long timestamp
) { }