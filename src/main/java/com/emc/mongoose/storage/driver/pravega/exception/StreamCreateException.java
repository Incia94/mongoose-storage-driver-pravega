package com.emc.mongoose.storage.driver.pravega.exception;

public class StreamCreateException
extends StreamException {

	public StreamCreateException(final String streamName, final Throwable cause) {
		super(streamName, cause);
	}
}
