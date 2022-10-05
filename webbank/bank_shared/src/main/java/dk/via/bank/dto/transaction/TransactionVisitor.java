package dk.via.bank.dto.transaction;

public interface TransactionVisitor {
	void visit(DepositTransaction transaction);
	void visit(WithdrawTransaction transaction);
	void visit(TransferTransaction transaction);
}
