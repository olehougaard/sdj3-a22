package dk.via.bank;

import dk.via.bank.dto.Customer;

public interface CustomerDAO {

    Customer createCustomer(Customer customer);

    Customer readCustomer(String cpr);

    void updateCustomer(String cpr, Customer customer);

    void deleteCustomer(String cpr);
}
