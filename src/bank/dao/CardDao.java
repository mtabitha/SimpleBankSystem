package bank.dao;

import bank.model.Card;
import org.sqlite.SQLiteDataSource;

import java.sql.*;

public class CardDao {
    private Connection connect;
    private final String SQL_CREATE_DB      =   "CREATE TABLE card (" +
                                                "id INTEGER PRIMARY KEY," +
                                                "number VARCHAR(16) NOT NULL," +
                                                "pin VARCHAR(4) NOT NULL," +
                                                "balance INTEGER DEFAULT 0)";

    private final String SQL_FIND_BY_NUMBER =   "SELECT * FROM card WHERE number=?";
    private final String SQL_SAVE_NEW       =   "INSERT INTO card(number, pin) VALUES(?,?)";
    private final String SQL_UPDATE_BALANCE =   "UPDATE card SET balance = ? WHERE id=?";
    private final String SQL_DELETE_CARD    =   "DELETE FROM card WHERE id=?";

    public CardDao(String url) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try {
            connect = dataSource.getConnection();
            if (connect.isValid(5)) {
                try (Statement state = connect.createStatement()) {
                    state.executeUpdate(SQL_CREATE_DB);
                } catch (SQLException e) {
                    e.getStackTrace();
                }
            }
            else
                System.out.println("Error: no connection");
        }
        catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public Card findByNumber(String number) {
        Card    card =  null;
        try (PreparedStatement state = connect.prepareStatement(SQL_FIND_BY_NUMBER)) {
            state.setString(1, number);
            ResultSet result = state.executeQuery();
            if (result.next())
                card = new Card(   result.getInt("id"), result.getString("number"),
                                    result.getString("pin"), result.getInt("balance"));
        } catch (SQLException e) {
            e.getStackTrace();
        }
        return card;
    }

    public void save(String cardNumber, String cardPin) {
        try (PreparedStatement state = connect.prepareStatement(SQL_SAVE_NEW)) {
            state.setString(1, cardNumber);
            state.setString(2, cardPin);
            state.executeUpdate();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void addBalance(Card card) {
        try (PreparedStatement state = connect.prepareStatement(SQL_UPDATE_BALANCE)) {
            state.setInt(1, card.getBalance());
            state.setInt(2, card.getId());
            state.executeUpdate();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void delete(Card card) {
        try (PreparedStatement state = connect.prepareStatement(SQL_DELETE_CARD)) {
            state.setInt(1, card.getId());
            state.executeUpdate();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void transfer(Card card, Card transferCard) {
        try {
            connect.setAutoCommit(false);
            Savepoint save = connect.setSavepoint();
            try (PreparedStatement from = connect.prepareStatement(SQL_UPDATE_BALANCE);
                 PreparedStatement to = connect.prepareStatement(SQL_UPDATE_BALANCE)) {
                 from.setInt(1, card.getBalance());
                 from.setInt(2, card.getId());
                 from.executeUpdate();

                 to.setInt(1, transferCard.getBalance());
                 to.setInt(2, transferCard.getId());
                 to.executeUpdate();

            } catch (SQLException e) {
                connect.rollback(save);
            }
            connect.commit();
            connect.setAutoCommit(true);
        }
        catch (SQLException e) {
            e.getStackTrace();
        }
    }
}
