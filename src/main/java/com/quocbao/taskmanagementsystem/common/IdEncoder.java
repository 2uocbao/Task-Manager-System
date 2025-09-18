package com.quocbao.taskmanagementsystem.common;

import org.hashids.Hashids;
import org.springframework.stereotype.Component;

@Component
public class IdEncoder {

	private final Hashids hashids;

	public IdEncoder() {
		this.hashids = new Hashids("your-salt", 6);
	}

	public String encode(long id) {
		return hashids.encode(id);
	}

	public long decode(String hash) {
		long[] result = hashids.decode(hash);
		return result.length > 0 ? result[0] : -1;
	}
}
