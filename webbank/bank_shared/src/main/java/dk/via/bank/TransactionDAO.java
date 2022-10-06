package dk.via.bank;

import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.AbstractTransaction;

import java.util.List;

public interface TransactionDAO {
    AbstractTransaction createTransaction(String cpr, AccountNumber accountNumber, TransactionSpecification transactionSpec);

    AbstractTransaction readTransaction(String cpr, AccountNumber accountNumber, int transactionId);

    List<AbstractTransaction> readTransactionsFor(String cpr, AccountNumber accountNumber);
}
