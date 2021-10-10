package ru.vzotov.domain.model;


import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

public class MoneyTest {

    @Test
    public void testConstructor() {
        try {
            new Money(null, Currency.getInstance("RUR"));
            fail("Should not accept null arguments");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new Money(BigDecimal.valueOf(10), null);
            fail("Should not accept null arguments");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCompareDifferentCurrencies() {
        assertThatThrownBy(() -> {
            // Should not allow to compare different currencies
            Money.rubles(10.0d).compareTo(Money.dollars(10.0d));
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCompare() {
        assertThat(Money.rubles(10.0d).compareTo(Money.kopecks(1001L))).isLessThan(0);
        assertThat(Money.rubles(10.0d)).usingDefaultComparator().isLessThan(Money.rubles(10.01d));
    }

    @Test
    public void dollars() {
        final Money dollars = Money.dollars(10.0d);
        assertThat(dollars.rawAmount()).isEqualTo(1000L);
        assertThat(dollars.currency().getCurrencyCode()).isEqualToIgnoringCase("usd");
    }

    @Test
    public void rubles() {
        final Money rubles = Money.rubles(10.0d);
        assertThat(rubles.rawAmount()).isEqualTo(1000L);
        assertThat(rubles.currency().getCurrencyCode()).isEqualToIgnoringCase("rur");
    }

    @Test
    public void kopecks() {
        final Money kopecks = Money.kopecks(100L);
        assertThat(kopecks.rawAmount()).isEqualTo(100L);
        assertThat(kopecks.currency().getCurrencyCode()).isEqualToIgnoringCase("rur");
    }

    @Test
    public void amount() {
        final Money money = new Money(1000L, Currency.getInstance("RUR"));
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(1000L));
    }

    @Test
    public void rawAmount() {
        final Money money = new Money(1000L, Currency.getInstance("RUR"));
        assertThat(money.rawAmount()).isEqualTo(100000L);
    }

    @Test
    public void ofRaw() {
        final Money money = Money.ofRaw(1000L, Currency.getInstance("RUR"));
        assertThat(money.rawAmount()).isEqualTo(1000L);
    }

    @Test
    public void multiplyLowerHalf() {
        Money m = Money.kopecks(1L).multiply(2.4d);
        assertThat(m).isEqualTo(Money.rubles(.02d));
    }

    @Test
    public void multiplyHalf() {
        Money m = Money.kopecks(1L).multiply(2.5d);
        assertThat(m).isEqualTo(Money.rubles(.03d));
    }

    @Test
    public void multiplyUpperHalf() {
        Money m = Money.kopecks(1L).multiply(2.6d);
        assertThat(m).isEqualTo(Money.rubles(.03d));
    }

    @Test
    public void add() {
        Money m = Money.kopecks(1L).add(Money.kopecks(100L));
        assertThat(m).isEqualTo(Money.rubles(1.01d));
    }

    @Test
    public void subtract() {
        Money m = Money.kopecks(100L).subtract(Money.kopecks(5L));
        assertThat(m).isEqualTo(Money.rubles(0.95d));
    }

}
