package com.net.server.mmo;

import java.util.List;

import com.net.server.entities.User;

public class MMOUpdateDelta {
	private final List<User> plusUserList;
	private final List<User> minusUserList;
	private final List<BaseMMOItem> plusItemList;
	private final List<BaseMMOItem> minusItemList;
	private final User recipient;

	public MMOUpdateDelta(User recipient, List<User> plusUserList, List<User> minusUserList,
			List<BaseMMOItem> plusItemList, List<BaseMMOItem> minusItemList) {
		this.recipient = recipient;
		this.plusUserList = plusUserList;
		this.minusUserList = minusUserList;
		this.plusItemList = plusItemList;
		this.minusItemList = minusItemList;
	}

	public List<User> getPlusUserList() {
		return this.plusUserList;
	}

	public List<User> getMinusUserList() {
		return this.minusUserList;
	}

	public List<BaseMMOItem> getPlusItemList() {
		return this.plusItemList;
	}

	public List<BaseMMOItem> getMinusItemList() {
		return this.minusItemList;
	}

	public User getRecipient() {
		return this.recipient;
	}

	public String toString() {
		return String.format("{\n  Update: %s\n  -U: %s\n  +U: %s -I: %s\n +I: %s\n}\n",
				new Object[] { this.recipient.getName(), this.minusUserList, this.plusUserList, this.minusItemList,
						this.plusItemList });
	}
}