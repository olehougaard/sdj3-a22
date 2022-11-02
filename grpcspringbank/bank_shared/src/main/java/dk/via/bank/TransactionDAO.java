package dk.via.bank;

import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.AbstractTransaction;

import java.util.List;

public interface TransactionDAO {
    AbstractTransaction createTransaction(AccountNumber accountNumber, TransactionSpecification transactionSpec);

    AbstractTransaction readTransaction(AccountNumber accountNumber, int transactionId);

    List<AbstractTransaction> readTransactionsFor(AccountNumber accountNumber);
}
