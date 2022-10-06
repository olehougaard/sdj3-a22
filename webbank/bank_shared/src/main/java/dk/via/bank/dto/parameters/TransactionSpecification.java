package dk.via.bank.dto.parameters;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.Money;
import dk.via.bank.dto.transaction.AbstractTransaction;
import dk.via.bank.dto.transaction.DepositTransaction;
import dk.via.bank.dto.transaction.TransferTransaction;
import dk.via.bank.dto.transaction.WithdrawTransaction;

@SuppressWarnings("unused")
public class TransactionSpecification {
    public static final String DEPOSIT = "deposit";
    public static final String WITHDRAW = "withdraw";
    public static final String TRANSFER = "transfer";

    private Integer id;
    private String type;
    private Money amount;
    private String text;
    private Account recipient;

    public TransactionSpecification() {
    }

    private TransactionSpecification(Integer id, String type, Money amount, String text, Account recipient) {
        id.toString();
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.text = text;
        this.recipient = recipient;
    }

    public static TransactionSpecification deposit(Integer id, Money amount, String text) {
        return new TransactionSpecification(id, DEPOSIT, amount, text, null);
    }

    public static TransactionSpecification withdraw(Integer id, Money amount, String text) {
        return new TransactionSpecification(id, WITHDRAW, amount, text, null);
    }

    public static TransactionSpecification transfer(Integer id, Money amount, String text, Account recipient) {
        return new TransactionSpecification(id, TRANSFER, amount, text, recipient);
    }

    public static TransactionSpecification from(AbstractTransaction transaction) {
        if (transaction instanceof DepositTransaction)
            return deposit(transaction.getId(), transaction.getAmount(), transaction.getText());
        else if (transaction instanceof WithdrawTransaction)
            return withdraw(transaction.getId(), transaction.getAmount(), transaction.getText());
        else if (transaction instanceof TransferTransaction)
            return transfer(transaction.getId(), transaction.getAmount(), transaction.getText(), ((TransferTransaction) transaction).getRecipient());
        else
            return null;
    }

    public AbstractTransaction toTransaction(Account account) {
        switch(type) {
            case DEPOSIT:
                return new DepositTransaction(id, amount, account, text);
            case WITHDRAW:
                return new WithdrawTransaction(id, amount, account, text);
            case TRANSFER:
                return new TransferTransaction(id, amount, account, recipient, text);
            default:
                throw new IllegalStateException("Not a valid transaction: " + type);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Account getRecipient() {
        return recipient;
    }

    public void setRecipient(Account recipient) {
        this.recipient = recipient;
    }
}
