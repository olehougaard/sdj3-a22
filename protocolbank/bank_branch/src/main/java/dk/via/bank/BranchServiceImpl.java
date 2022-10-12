package dk.via.bank;

import dk.via.bank.dto.Money;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.AbstractTransaction;
import dk.via.bank.grpc.*;
import dk.via.bank.interOp.GrpcFactory;
import io.grpc.stub.StreamObserver;

import java.util.Collection;
import java.util.List;

public class BranchServiceImpl extends BranchGrpc.BranchImplBase {
    private final Branch branch;

    public BranchServiceImpl(Branch branch) {
        this.branch = branch;
    }

    @Override
    public void createCustomer(Customer request, StreamObserver<Customer> responseObserver) {
        dk.via.bank.dto.Customer customer = branch.createCustomer(request.getCpr(), request.getName(), request.getAddress());
        responseObserver.onNext(GrpcFactory.toGrpc(customer));
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomer(CustomerRequest request, StreamObserver<Customer> responseObserver) {
        dk.via.bank.dto.Customer customer = branch.getCustomer(request.getCpr());
        responseObserver.onNext(GrpcFactory.toGrpc(customer));
        responseObserver.onCompleted();
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<Account> responseObserver) {
        dk.via.bank.dto.Customer customer = GrpcFactory.fromGrpc(request.getCustomer());
        dk.via.bank.dto.Account account = branch.createAccount(customer, request.getCurrency());
        responseObserver.onNext(GrpcFactory.toGrpc(account));
        responseObserver.onCompleted();
    }

    @Override
    public void getAccount(AccountNumber request, StreamObserver<Account> responseObserver) {
        dk.via.bank.dto.Account account = branch.getAccount(GrpcFactory.fromGrpc(request));
        responseObserver.onNext(GrpcFactory.toGrpc(account));
        responseObserver.onCompleted();
    }

    @Override
    public void cancelAccount(Account request, StreamObserver<Confirmation> responseObserver) {
        branch.cancelAccount(GrpcFactory.fromGrpc(request));
        responseObserver.onNext(Confirmation.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getAccounts(Customer request, StreamObserver<Accounts> responseObserver) {
        Collection<dk.via.bank.dto.Account> accounts = branch.getAccounts(GrpcFactory.fromGrpc(request));
        Accounts grpcAccounts = Accounts.newBuilder()
                .addAllAccount(GrpcFactory.toGrpcAccounts(accounts))
                .build();
        responseObserver.onNext(grpcAccounts);
        responseObserver.onCompleted();
    }

    @Override
    public void execute(Transaction request, StreamObserver<Confirmation> responseObserver) {
        dk.via.bank.dto.Account account = branch.getAccount(GrpcFactory.fromGrpc(request.getAccount().getAcctNumber()));
        dk.via.bank.dto.Account recipient = null;
        if (request.getType().equals(TransactionSpecification.TRANSFER)) {
            recipient = branch.getAccount(GrpcFactory.fromGrpc(request.getRecipient().getAcctNumber()));
        }
        branch.execute(GrpcFactory.fromGrpc(request, account, recipient));
        responseObserver.onNext(Confirmation.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void exchange(ExchangeRequest request, StreamObserver<ExchangeResponse> responseObserver) {
        Money exchange = branch.exchange(GrpcFactory.createMoney(request.getAmount10000(), request.getFromCurrency()), request.getToCurrency());
        responseObserver.onNext(GrpcFactory.createExchangeResponse(exchange));
        responseObserver.onCompleted();
    }

    @Override
    public void getTransactionsFor(Account request, StreamObserver<Transactions> responseObserver) {
        List<AbstractTransaction> transactionsFor = branch.getTransactionsFor(GrpcFactory.fromGrpc(request));
        Transactions grpcTransactions = Transactions.newBuilder()
                .addAllTransactions(GrpcFactory.toGrpcTransactions(transactionsFor))
                .build();
        responseObserver.onNext(grpcTransactions);
        responseObserver.onCompleted();
    }
}
