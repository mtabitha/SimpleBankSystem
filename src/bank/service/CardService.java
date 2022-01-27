package bank.service;

import java.util.Arrays;
import java.util.Random;
import bank.dao.CardDao;
import bank.model.Card;


public class CardService {

    private final CardDao cardDao;
    private final Random  rand = new Random();

    public CardService(String dbName) {
        this.cardDao = new CardDao("jdbc:sqlite:" + dbName);
    }

    public Card getAuthCard(String cardNumber, String cardPIN) {
        Card cardFromDB = null;
        if (cardNumber.length() == 16 && cardPIN.length() == 4){
            cardFromDB = cardDao.findByNumber(cardNumber);
            if (cardFromDB == null || !cardPIN.equals(cardFromDB.getPin())) {
                cardFromDB = null;
            }
        }
        return cardFromDB;
    }

    public Card getCard(String cardNumber) {
        return cardDao.findByNumber(cardNumber);
    }

    public Card generateNewCard() {
        String cardNumber = generateCardNumber();
        String cardPin = generateCardPin();
        cardDao.save(cardNumber, cardPin);
        return cardDao.findByNumber(cardNumber);
    }

    public boolean addIncome(Card card, String amount) {
        if (!amount.matches("\\d+"))
            return false;
        card.setBalance(card.getBalance() + Integer.parseInt(amount));
        cardDao.addBalance(card);
        return true;
    }

    public void delete(Card card) {
        cardDao.delete(card);
    }

    private String generateCardPin() {
        int     pinI = rand.nextInt(10_000);
        String  pinS = Integer.toString(pinI);
        int     diff  = 4 - pinS.length();
        if (diff != 0 ) {
            pinS =  "000".substring(0, diff) + pinS;
        }
        return pinS;
    }

    private String generateCardNumber() {

        int[] mass = new int[15];
        mass[0] = 4;
        for (int i = 6; i < 14; i++)
            mass[i] = rand.nextInt(10);
        String cardNumber =  Arrays.toString(mass)
                .replaceAll("[\\[\\], ]", "");
        cardNumber += algorithmLuhn(mass);
        return cardNumber;
    }

    private int algorithmLuhn(int[] mass) {
        for (int i = 0; i < mass.length; i += 2) {
            mass[i] *= 2;
            if (mass[i] > 9)
                mass[i] -= 9;
        }
        int rem = Arrays.stream(mass).sum()  % 10;
        return rem == 0 ? 0 : 10 - rem;
    }

    private boolean isValidCardNumber(String cardNumber) {
        int[] mass = new int[15];
        for (int i = 0; i < 15; i++) {
            mass[i] = cardNumber.charAt(i) - 48;
        }
        int last = cardNumber.charAt(15) - 48;
        int rem = algorithmLuhn(mass);
        return last == rem;
    }

    public int validCardNumber(String cardNumber) {
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+"))
            return 2;
        if (cardDao.findByNumber(cardNumber) == null || !isValidCardNumber(cardNumber))
            return 1;
        return 0;
    }

    public void transferMoney(Card card, Card transferCard, int amount) {
        card.setBalance(card.getBalance() - amount);
        transferCard.setBalance(transferCard.getBalance() + amount);
        cardDao.transfer(card, transferCard);
    }
}
