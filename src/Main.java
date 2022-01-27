import java.util.Scanner;
import bank.model.Card;
import bank.service.CardService;

public class Main {
    public static void main(String[] args) {
        new Bank(args[1]).start();
    }
}

class Bank {

    private final CardService cardService;
    private final Scanner       scan = new Scanner(System.in);

    public Bank(String dbName) {
        cardService = new CardService(dbName);
    }

    public void start() {
        while (true) {
            authPanel();
            switch (validChoice()){
                case (2): if (log() == 1)   return; break;
                case (0):                   return;
                case (1): create();         break;
            }
        }
    }

    private int log() {
        System.out.print("\nEnter your card number:\n>"); String cardNumber = scan.nextLine();
        System.out.print("Enter your card PIN:\n>"); String cardPIN = scan.nextLine();

        Card card = cardService.getAuthCard(cardNumber, cardPIN);
        if (card == null) {
            System.out.print("\nWrong card number or PIN!\n>");
            return 0;
        }
        System.out.println("You have successfully logged in!\n");
        return successLog(card);
    }

    private int successLog(Card card) {
        while (true) {
            logPanel();
            switch (validChoice()) {
                case (0):   bye();          return 1;
                case (1):   balance(card);  break;
                case (2):   add(card);      break;
                case (3):   transfer(card); break;
                case (4):   closeAcc(card); return 0;
                case (5):   logOut();       return 0;
            }
        }
    }

    private void transfer(Card card) {
        System.out.print(   "\nTransfer\n" +
                            "Enter card number:\n>");
        String cardNumber = scan.nextLine();
        int ret = cardService.validCardNumber(cardNumber);
        if (ret > 0) {
            System.out.print( ret == 1  ?  "\nProbably you made a mistake in the card number. Please try again!\n"
                                        : "Such a card does not exist.\n\n");
            return ;
        }
        Card transferCard = cardService.getCard(cardNumber);
        if (transferCard != null) {
            System.out.print("Enter how much money you want to transfer:\n>");
            Integer amountI = Integer.parseInt(scan.nextLine());
            if (card.getBalance() >= amountI) {
                cardService.transferMoney(card, transferCard, amountI);
                System.out.println("Success!\n");
                return;
            }
            System.out.println("Not enough money!\n");
        }
    }

    private void closeAcc(Card card) {
        cardService.delete(card);
    }

    private void add(Card card) {
        System.out.print("\nEnter income:\n>");
        if (cardService.addIncome(card, scan.nextLine()))
            System.out.print("Income was added!\n\n");
    }

    private void create() {
        Card card = cardService.generateNewCard();
        createPrint(card);
    }

    private int  validChoice() {
        String line = scan.nextLine();
        if (line.length() > 1 || !line.matches("\\d+"))
            return -1;
        return Integer.parseInt(line);
    }

    private void createPrint(Card card) {
        System.out.println("\nYour card has been created\n" +
                            "Your card number:\n" +
                            card.getNumber() + "\n" +
                            "Your card PIN:\n" +
                            card.getPin() + "\n");
    }

    private void bye() {
        System.out.println("\nBye!");
    }

    private void logOut() {
        System.out.println("\nYou have successfully logged out!\n");
    }

    private void balance(Card card) {
        System.out.println("\nBalance: " + card.getBalance() + "\n");
    }

    private static void authPanel() {
        System.out.print(   "1. Create an account\n" +
                            "2. Log into account\n" +
                            "0. Exit\n>");
    }

    private void logPanel() {
        System.out.print(   "1. Balance\n" +
                            "2. Add income\n" +
                            "3. Do transfer\n" +
                            "4. Close account\n" +
                            "5. Log out\n" +
                            "0. Exit\n>");
    }
}
