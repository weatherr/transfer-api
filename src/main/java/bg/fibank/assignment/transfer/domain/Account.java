package bg.fibank.assignment.transfer.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "iban", length = 34, nullable = false)
    private String iban;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Version 
    @Column(name = "version", nullable = false)
    private Long version;

    public Account() {}

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}