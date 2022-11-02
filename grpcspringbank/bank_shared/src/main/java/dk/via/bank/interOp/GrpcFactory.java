package dk.via.bank.interOp;


import dk.via.bank.dto.Account;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.AbstractTransaction;
import dk.via.bank.dto.transaction.DepositTransaction;
import dk.via.bank.grpc.Transaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class GrpcFactory {
    public static dk.via.bank.grpc.Customer createCustomer(String cpr, String name, String address) {
        return dk.via.bank.grpc.Customer.newBuilder()
                .setCpr(cpr)
                .setName(name)
                .setAddress(address)
                .build();
    }


    public static dk.via.bank.grpc.Customer toGrpc(dk.via.bank.dto.Customer customer) {
        return dk.via.bank.grpc.Customer.newBuilder()
                .setCpr(customer.getCpr())
                .setName(customer.getName())
                .setAddress(customer.getAddress())
                .build();
    }

    public static dk.via.bank.grpc.CustomerRequest createCustomerRequest(String cpr) {
        return dk.via.bank.grpc.CustomerRequest.newBuilder().setCpr(cpr).build();
    }

    private static int getIntValue(BigDecimal amount, int precision) {
        return amount.multiply(BigDecimal.valueOf(precision)).intValue();
    }

    private static BigDecimal createDecimal(long amount, int precision) {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(precision));
    }

    public static dk.via.bank.dto.Customer fromGrpc(dk.via.bank.grpc.Customer customer) {
        return new dk.via.bank.dto.Customer(customer.getCpr(), customer.getName(), customer.getAddress());
    }

    public static dk.via.bank.grpc.AccountNumber toGrpc(dk.via.bank.dto.AccountNumber accountNumber) {
        return dk.via.bank.grpc.AccountNumber.newBuilder()
                .setRegNumber(accountNumber.getRegNumber())
                .setAcctNumber(accountNumber.getAccountNumber())
                .build();
    }

    public static dk.via.bank.dto.AccountNumber fromGrpc(dk.via.bank.grpc.AccountNumber accountNumber) {
        return new dk.via.bank.dto.AccountNumber(accountNumber.getRegNumber(), accountNumber.getAcctNumber());
    }

    public static dk.via.bank.grpc.Account toGrpc(dk.via.bank.dto.Account account) {
        return dk.via.bank.grpc.Account.newBuilder()
                .setAcctNumber(toGrpc(account.getAccountNumber()))
                .setAmount100(getIntValue(account.getBalance().getAmount(), 100))
                .setCurrency(account.getSettledCurrency())
                .setCustomerCpr(account.getCustomerCpr())
                .build();
    }

    public static dk.via.bank.grpc.CreateAccountRequest createAccountRequest(dk.via.bank.dto.Customer customer, String currency) {
        return dk.via.bank.grpc.CreateAccountRequest.newBuilder()
                .setCustomer(toGrpc(customer))
                .setCurrency(currency)
                .build();
    }

    public static dk.via.bank.dto.Account fromGrpc(dk.via.bank.grpc.Account account) {
        return new dk.via.bank.dto.Account(fromGrpc(account.getAcctNumber()), createMoney(account.getAmount100(), account.getCurrency()), account.getCustomerCpr());
    }

    public static Collection<dk.via.bank.grpc.Account> toGrpcAccounts(Collection<dk.via.bank.dto.Account> accounts) {
        return accounts.stream().map(GrpcFactory::toGrpc).collect(toList());
    }

    public static List<dk.via.bank.dto.Account> fromGrpcAccounts(dk.via.bank.grpc.Accounts accounts) {
        return accounts.getAccountList().stream().map(GrpcFactory::fromGrpc).collect(toList());
    }

    public static dk.via.bank.dto.transaction.AbstractTransaction fromGrpc(dk.via.bank.grpc.Transaction t) {
        dk.via.bank.dto.Account account = fromGrpc(t.getAccount());
        dk.via.bank.dto.Account recipient = t.getRecipient() == null ? null : fromGrpc(t.getRecipient());
        return fromGrpc(t, account, recipient);
    }

    public static AbstractTransaction fromGrpc(Transaction t, Account account, Account recipient) {
        long amount100 = t.getAmount100();
        String currency = t.getCurrency();
        dk.via.bank.dto.Money money = createMoney(amount100, currency);
        switch (t.getType()) {
            case TransactionSpecification.DEPOSIT:
                return new DepositTransaction(-1, money, account);
            case TransactionSpecification.WITHDRAW:
                return new dk.via.bank.dto.transaction.WithdrawTransaction(-1, money, account);
            case TransactionSpecification.TRANSFER:
                return new dk.via.bank.dto.transaction.TransferTransaction(-1, money, account, recipient);
            default:
                throw new IllegalArgumentException();
        }
    }

    public static dk.via.bank.grpc.Transaction toGrpc(dk.via.bank.dto.transaction.AbstractTransaction t) {
        dk.via.bank.grpc.Transaction[] trans = new dk.via.bank.grpc.Transaction[1];
        dk.via.bank.dto.transaction.TransactionVisitor visitor = new dk.via.bank.dto.transaction.TransactionVisitor() {
            @Override
            public void visit(dk.via.bank.dto.transaction.DepositTransaction t) {
                trans[0] = dk.via.bank.grpc.Transaction.newBuilder()
                        .setId(t.getId())
                        .setType(TransactionSpecification.DEPOSIT)
                        .setAmount100(getIntValue(t.getAmount().getAmount(), 100))
                        .setCurrency(t.getAmount().getCurrency())
                        .setAccount(GrpcFactory.toGrpc(t.getAccount()))
                        .build();
            }

            @Override
            public void visit(dk.via.bank.dto.transaction.WithdrawTransaction t) {
                trans[0] = dk.via.bank.grpc.Transaction.newBuilder()
                        .setId(t.getId())
                        .setType(TransactionSpecification.WITHDRAW)
                        .setAmount100(getIntValue(t.getAmount().getAmount(), 100))
                        .setCurrency(t.getAmount().getCurrency())
                        .setAccount(GrpcFactory.toGrpc(t.getAccount()))
                        .build();
            }

            @Override
            public void visit(dk.via.bank.dto.transaction.TransferTransaction t) {
                trans[0] = dk.via.bank.grpc.Transaction.newBuilder()
                        .setId(t.getId())
                        .setType(TransactionSpecification.TRANSFER)
                        .setAmount100(getIntValue(t.getAmount().getAmount(), 100))
                        .setCurrency(t.getAmount().getCurrency())
                        .setAccount(GrpcFactory.toGrpc(t.getAccount()))
                        .setRecipient(GrpcFactory.toGrpc(t.getRecipient()))
                        .build();
            }
        };
        t.accept(visitor);
        return trans[0];
    }


    public static dk.via.bank.dto.Money createMoney(long amount, String currency) {
        return new dk.via.bank.dto.Money(createDecimal(amount, 100), currency);
    }

    public static dk.via.bank.grpc.ExchangeResponse createExchangeResponse(dk.via.bank.dto.Money money) {
        return dk.via.bank.grpc.ExchangeResponse.newBuilder()
                .setAmount100(getIntValue(money.getAmount(), 100))
                .setCurrency(money.getCurrency())
                .build();
    }

    public static dk.via.bank.grpc.ExchangeRequest createExchangeRequest(dk.via.bank.dto.Money money, String targetCurrency) {
        return dk.via.bank.grpc.ExchangeRequest.newBuilder()
                .setAmount10000(getIntValue(money.getAmount(), 100))
                .setFromCurrency(money.getCurrency())
                .setToCurrency(targetCurrency)
                .build();
    }

    public static Collection<dk.via.bank.grpc.Transaction> toGrpcTransactions(Collection<dk.via.bank.dto.transaction.AbstractTransaction> transactions) {
        return transactions.stream().map(GrpcFactory::toGrpc).collect(toList());
    }

    public static List<AbstractTransaction> fromGrpcTransactions(dk.via.bank.grpc.Transactions transactions) {
        return transactions.getTransactionsList().stream().map(GrpcFactory::fromGrpc).collect(toList());
    }
}
