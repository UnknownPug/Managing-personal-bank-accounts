package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CurrencyDataService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/currency-data")
public class CurrencyDataController {
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyDataController.class);

    private final CurrencyDataService currencyDataService;

    @Autowired
    public CurrencyDataController(CurrencyDataService currencyDataService) {
        this.currencyDataService = currencyDataService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    public List<CurrencyData> updateAndFetchAllCurrencies() {
        LOG.debug("Updating currency data ...");
        currencyDataService.updateCurrencyData();
        return currencyDataService.findAllCurrencies();
    }

    @GetMapping(path = "/{currency}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CurrencyData> findByCurrency(@PathVariable(value = "currency") String currencyType) {
        LOG.debug("Getting currency {} ...", currencyType);
        return ResponseEntity.ok(currencyDataService.findByCurrency(currencyType));
    }
}
