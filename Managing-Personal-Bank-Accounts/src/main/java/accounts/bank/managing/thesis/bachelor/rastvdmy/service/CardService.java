package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CurrencyDataService currencyDataService;

    private final Generator generator;

    @Autowired
    public CardService(CardRepository cardRepository, UserRepository userRepository, CurrencyDataService currencyDataService, Generator generator) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.currencyDataService = currencyDataService;
        this.generator = generator;
    }

    @Cacheable(value = "cards")
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    @Cacheable(value = "cards")
    public Page<Card> filterAndSortCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    @Cacheable(value = "cards", key = "#cardId")
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card with id: " + cardId + " not found.")
        );
    }

    @Cacheable(value = "cards", key = "#cardNumber")
    public Card getCardByCardNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card with number: " + cardNumber + " not found.");
        }
        return cardRepository.findByCardNumber(cardNumber);
    }

    @CacheEvict(value = {"cards", "users"}, allEntries = true)
    public Card createCard(Long userId, String chosenCurrency, String type) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Creating card is unavailable for blocked user.");
        }
        long minCardLimit = 1_000_000_000_000_000L;
        long maxCardLimit = 9_999_999_999_999_999L;
        int minCvvLimit = 100;
        int maxCvvLimit = 999;
        int minPinLimit = 1000;
        int maxPinLimit = 9999;

        Card card = new Card();
        Random random = new Random();

        long generatedCardNumber = minCardLimit + ((long) (random.nextDouble() * (maxCardLimit - minCardLimit)));
        card.setCardNumber(String.valueOf(generatedCardNumber));

        int generateCvv = random.nextInt(maxCvvLimit - minCvvLimit + 1) + minCvvLimit;
        card.setCvv(generateCvv);

        int generatePin = random.nextInt(maxPinLimit - minPinLimit + 1) + minPinLimit;
        card.setPin(generatePin);

        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);
        card.setHolderName(user.getName() + " " + user.getSurname());
        card.setIban(generator.generateIban());
        card.setSwift(generator.generateSwift());
        if (chosenCurrency.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency must be filled.");
        }
        Currency currencyType;
        try {
            currencyType = Currency.valueOf(chosenCurrency.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency " + chosenCurrency + " does not exist.");
        }
        card.setAccountNumber(generator.generateAccountNumber());
        card.setCurrencyType(currencyType);
        cardTypeCheck(type, card);
        card.setCardExpirationDate(LocalDate.now().plusYears(5));
        return cardRepository.save(card);
    }

    private void cardTypeCheck(String type, Card card) {
        if (type.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card type must be filled.");
        }
        CardType cardType;
        try {
            cardType = CardType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card type " + type + " does not exist.");
        }
        card.setCardType(cardType);
    }

    @CacheEvict(value = "cards", allEntries = true)
    public void cardRefill(Long cardId, Integer pin, BigDecimal balance) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found.")
        );
        if (card.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable for blocked card.");
        }
        if (checkCardExpirationDate(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card is expired.");
        }
        if (card.getPin().equals(pin) && (card.getStatus().equals(CardStatus.STATUS_CARD_UNBLOCKED) ||
                card.getStatus().equals(CardStatus.STATUS_CARD_DEFAULT))) {
            conversationToCardCurrency(card, balance);
            card.setRecipientTime(LocalDateTime.now());
            cardRepository.save(card);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid pin or card is blocked.");
        }
    }

    private void conversationToCardCurrency(Card card, BigDecimal balance) {
        switch (card.getCurrencyType()) {
            case USD -> card.setBalance(card.getBalance().add(balance).multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.USD.toString()).getRate())
            ));
            case EUR -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.EUR.toString()).getRate()))
            ));
            case UAH -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.UAH.toString()).getRate()))
            ));
            case CZK -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.CZK.toString()).getRate()))
            ));
            case PLN -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.PLN.toString()).getRate()))
            ));
        }
    }

    @CacheEvict(value = "cards", allEntries = true)
    public void updateCardStatus(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card with id: " + id + " not found.")
        );
        if (checkCardExpirationDate(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card is expired.");
        }
        switch (card.getStatus()) {
            case STATUS_CARD_BLOCKED -> {
                card.setStatus(CardStatus.STATUS_CARD_UNBLOCKED);
                cardRepository.save(card);
            }
            case STATUS_CARD_DEFAULT, STATUS_CARD_UNBLOCKED -> {
                card.setStatus(CardStatus.STATUS_CARD_BLOCKED);
                cardRepository.save(card);
            }
        }
    }

    @CacheEvict(value = "cards", allEntries = true)
    public void changeCardType(Long cardId, String cardType) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found.")
        );
        if (checkCardExpirationDate(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card is expired.");
        }
        if (card.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable for blocked card.");
        }
        cardTypeCheck(cardType, card);
        cardRepository.save(card);
    }

    @Transactional
    @CacheEvict(value = {"cards", "users"}, allEntries = true)
    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found.")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "User with id: " + userId + " not found.")
        );
        if (card.getBalance().compareTo(BigDecimal.ZERO) == 0 && user.getCards().contains(card)) {
            user.getCards().remove(card);
            userRepository.save(user);
            cardRepository.delete(card);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Card is not empty or user does not contain this card.");
        }
    }

    private boolean checkCardExpirationDate(Card card) {
        return LocalDate.now().isAfter(card.getCardExpirationDate());
    }
}
