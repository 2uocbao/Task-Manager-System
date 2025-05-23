package com.quocbao.taskmanagementsystem.common;

import org.hashids.Hashids;
import org.springframework.stereotype.Component;

@Component
public class IdEncoder {

	private final Hashids hashids;

	public IdEncoder() {
		this.hashids = new Hashids("2uocbao.0106", 6);
	}

	public String endcode(long id) {
		return hashids.encode(id);
	}

	public long decode(String hash) {
		long[] result = hashids.decode(hash);
		return result.length > 0 ? result[0] : -1;
	}
}
