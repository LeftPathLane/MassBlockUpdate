package net.erouax.blockupdate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Basic Block object containing x y z coords and an id and data value
 */
@Getter
@RequiredArgsConstructor
public class BasicBlock {
	private final short id, data;
	private final int x, y, z;

	public BasicBlock(short id, int x, int y, int z) {
		this(id, (short) 0, x, y, z);
	}
}