package dk.via.bank.dto.transaction;

import java.io.Serializable;

public interface Transaction extends Serializable {
	String getText();
	void accept(TransactionVisitor visitor);
}
