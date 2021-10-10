package ru.vzotov.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.ddd.shared.ValueObject;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

public class Money implements Comparable<Money>, ValueObject<Money> {

    private static final int[] cents = new int[]{1, 10, 100, 1000};
    private static final Currency RUR = Currency.getInstance("RUR"); // 810 code - internal payments
    private static final Currency RUB = Currency.getInstance("RUB"); // 643 code - international payments

    private long amount;

    private Currency currency;

    protected Money() {
        // for Hibernate
    }

    public Money(double amount, Currency currency) {
        Validate.notNull(currency);
        Validate.isTrue(!RUB.equals(currency), "Wrong currency: ", currency); // we use RUR only

        this.currency = currency;
        this.amount = Math.round(amount * centFactor());
    }

    public Money(long amount, Currency currency) {
        Validate.notNull(currency);
        Validate.isTrue(!RUB.equals(currency), "Wrong currency: ", currency); // we use RUR only

        this.currency = currency;
        this.amount = amount * centFactor();
    }

    public Money(BigDecimal amount, Currency currency) {
        Validate.notNull(amount);
        Validate.notNull(currency);
        Validate.isTrue(!RUB.equals(currency), "Wrong currency: ", currency); // we use RUR only

        this.currency = currency;
        this.amount = amount.longValue();
    }

    public static Money dollars(double amount) {
        return new Money(amount, Currency.getInstance(Locale.US));
    }

    public static Money rubles(double amount) {
        return new Money(amount, RUR);
    }

    public static Money kopecks(long amount) {
        return new Money(((double) amount) / 100d, RUR);
    }

    public static Money ofRaw(long rawAmount, Currency currency) {
        int cf = centFactorOfCurrency(currency);
        return new Money((double) rawAmount / (double) cf, currency);
    }

    private static int centFactorOfCurrency(Currency currency) {
        return cents[currency.getDefaultFractionDigits()];
    }

    public BigDecimal amount() {
        return BigDecimal.valueOf(amount, currency.getDefaultFractionDigits());
    }

    public long rawAmount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    private int centFactor() {
        return centFactorOfCurrency(currency);
    }

    public int hashCode() {
        return (int) (amount ^ (amount >>> 32));
    }

    public Money multiply(double multiplier) {
        return newMoney(Math.round(amount * multiplier));
    }

    /**
     * Round value up to 10^n
     *
     * @param n power of 10 for rounding up. 3 - up to 1000, 2 - up to 100, 1 - up to 10.
     * @return new Money value, rounded up to 10^n
     */
    public Money roundUp(int n) {
        long m = (long) Math.pow(10, n + currency.getDefaultFractionDigits());
        return newMoney((long) Math.ceil((double) amount / m) * m);
    }

    public Money add(Money other) {
        assertSameCurrencyAs(other);
        return newMoney(amount + other.amount);
    }

    public Money subtract(Money other) {
        assertSameCurrencyAs(other);
        return newMoney(amount - other.amount);
    }

    public boolean canCompare(Money other) {
        return other != null && Objects.equals(currency, other.currency);
    }

    public int compareTo(Money other) {
        assertSameCurrencyAs(other);
        if (amount < other.amount) return -1;
        else if (amount == other.amount) return 0;
        else return 1;
    }

    public boolean greaterThan(Money other) {
        return (compareTo(other) > 0);
    }

    private void assertSameCurrencyAs(Money arg) {
        if (!Objects.equals(currency, arg.currency)) {
            throw new IllegalArgumentException("money math mismatch");
        }
    }

    private Money newMoney(long rawAmount) {
        Money money = new Money();
        money.currency = this.currency;
        money.amount = rawAmount;
        return money;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "" + amount() + " " + currency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sameValueAs(Money money) {
        return money != null
                && Objects.equals(currency, money.currency)
                && amount == money.amount
                ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Money money = (Money) o;
        return sameValueAs(money);
    }
}
