package bg.fibank.assignment.transfer.controller;

import bg.fibank.assignment.transfer.domain.Account;
import bg.fibank.assignment.transfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @GetMapping("/{iban}")
    public ResponseEntity<Account> getAccountByIban(@PathVariable String iban) {
        return accountRepository.findById(iban)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}