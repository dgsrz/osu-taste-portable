package com.dgsrz.osuTaste.exception;

/**
 * Created by: dgsrz
 * Date: 2014-01-31 16:40
 */
public class BeatmapParsingException extends Exception {

    public BeatmapParsingException() { }

    public BeatmapParsingException(String message) {
        super(message);
    }

    public BeatmapParsingException(String message, Exception cause) {
        super(message, cause);
    }

}
