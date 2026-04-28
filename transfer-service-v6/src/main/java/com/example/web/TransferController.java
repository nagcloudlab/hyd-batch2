package com.example.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.model.Account;
import com.example.model.TxnHistory;
import com.example.repository.AccountRepository;
import com.example.repository.TxnHistoryRepository;
import com.example.service.TransferService;

import jakarta.validation.Valid;

// @Controller — returns view names (resolved by Thymeleaf to templates/*.html)
// Compare with @RestController — which returns data directly (JSON/XML), covered in v7
//
// Request flow: Browser -> DispatcherServlet -> @Controller -> ViewResolver -> Thymeleaf -> HTML
@Controller
public class TransferController {

    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;
    private final AccountRepository accountRepository;
    private final TxnHistoryRepository txnHistoryRepository;

    // Constructor injection — all dependencies are final and injected by Spring
    public TransferController(TransferService transferService,
            AccountRepository accountRepository,
            TxnHistoryRepository txnHistoryRepository) {
        this.transferService = transferService;
        this.accountRepository = accountRepository;
        this.txnHistoryRepository = txnHistoryRepository;
    }

    // =========================================================================
    // @InitBinder — customize data binding for this controller
    // Runs before every @RequestMapping method to configure WebDataBinder
    // Here: trims whitespace from all string fields (prevents " 123 " account
    // numbers)
    // =========================================================================
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // StringTrimmerEditor(true) — trims whitespace, converts empty strings to null
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // =========================================================================
    // @ModelAttribute at method level — runs BEFORE every handler method in THIS
    // controller
    // Adds shared data to the Model so all templates can access it
    // Here: populates "accounts" for the dropdown in the transfer form
    // Note: if the DB is unreachable, this will throw — so we handle it gracefully
    // =========================================================================
    @ModelAttribute("accounts")
    public List<Account> populateAccounts() {
        try {
            return accountRepository.findAll();
        } catch (Exception e) {
            logger.warn("Could not load accounts for dropdown: {}", e.getMessage());
            return List.of();
        }
    }

    // =========================================================================
    // @GetMapping — shorthand for @RequestMapping(method = RequestMethod.GET)
    // =========================================================================

    // Show transfer form — Model carries data from controller to Thymeleaf template
    @GetMapping("/transfer")
    public String showTransferForm(Model model) {
        // Add empty form-backing object for Thymeleaf th:object / th:field binding
        model.addAttribute("transferRequest", new TransferRequest());
        return "transfer-form";
    }

    // =========================================================================
    // @PostMapping — shorthand for @RequestMapping(method = RequestMethod.POST)
    // @Valid triggers Bean Validation (@NotBlank, @DecimalMin) on TransferRequest
    // BindingResult captures validation errors (MUST immediately follow @Valid
    // param)
    // =========================================================================

    @PostMapping("/transfer")
    public String processTransfer(
            @Valid @ModelAttribute("transferRequest") TransferRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Step 1: Bean Validation errors (@NotBlank, @DecimalMin) — auto-populated by
        // @Valid
        if (bindingResult.hasErrors()) {
            logger.warn("Validation failed: {}", bindingResult.getAllErrors());
            return "transfer-form";
        }

        // Step 2: Cross-field validation — business rules that span multiple fields
        // BindingResult.reject() adds a global error (not tied to a specific field)
        // Null-safe check: fromAccount/toAccount can be null if StringTrimmerEditor
        // converted empty to null
        if (request.getFromAccount() != null
                && request.getFromAccount().equals(request.getToAccount())) {
            bindingResult.reject("sameAccount", "Cannot transfer to the same account");
            return "transfer-form";
        }

        // Step 3: Call service — service may throw business exceptions
        // Catch known exceptions and show them as form-level errors (better UX than
        // error page)
        // Unknown exceptions propagate to @ControllerAdvice (GlobalExceptionHandler)
        try {
            transferService.transfer(request.getAmount(), request.getFromAccount(), request.getToAccount());
        } catch (IllegalArgumentException | com.example.exception.InsufficientFundsException
                | com.example.exception.AccountNotFoundException ex) {
            bindingResult.reject("transferError", ex.getMessage());
            return "transfer-form";
        }

        // Step 4: PRG pattern (Post-Redirect-Get) — redirect after POST to prevent
        // duplicate submission
        // Flash attributes survive the redirect and are available in the next GET
        // request
        redirectAttributes.addFlashAttribute("message", String.format(
                "Successfully transferred $%s from %s to %s",
                request.getAmount(), request.getFromAccount(), request.getToAccount()));

        return "redirect:/transfer-success"; // Location:
    }

    // Target of PRG redirect — shows success message from flash attributes
    @GetMapping("/transfer-success")
    public String showSuccess() {
        return "transfer-success";
    }

    // =========================================================================
    // @PathVariable — extracts values from URL path segments
    // URL: /accounts/123 -> accountNumber = "123"
    // =========================================================================

    @GetMapping("/accounts/{accountNumber}")
    public String showAccountDetail(@PathVariable String accountNumber, Model model) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        model.addAttribute("account", account);
        return "account-detail";
    }

    // =========================================================================
    // @RequestParam — binds query parameters from URL
    // URL: /accounts?sort=balance -> sortBy = "balance"
    // defaultValue prevents null when param is missing
    // =========================================================================

    @GetMapping("/accounts")
    public String listAccounts(@RequestParam(defaultValue = "number") String sort, Model model) {
        List<Account> accounts = accountRepository.findAll();
        logger.info("Listing accounts sorted by: {}", sort);
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentSort", sort);
        return "account-list";
    }

    // =========================================================================
    // Transfer history — demonstrates th:each on complex objects
    // =========================================================================

    @GetMapping("/transfer-history")
    public String showTransferHistory(Model model) {
        List<TxnHistory> history = txnHistoryRepository.findAll();
        model.addAttribute("history", history);
        return "transfer-history";
    }

}
