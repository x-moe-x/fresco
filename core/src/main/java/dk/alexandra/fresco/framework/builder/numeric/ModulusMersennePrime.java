package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class ModulusMersennePrime implements Serializable {

  private final MersennePrimeInteger mersenne;
  private final BigInteger value;
  private final BigInteger halved;

  private ModulusMersennePrime(MersennePrimeInteger mersenne) {
    Objects.requireNonNull(mersenne);
    this.mersenne = mersenne;
    this.value = new BigInteger(mersenne.toString());
    this.halved = this.value.divide(BigInteger.valueOf(2));
  }

  MersennePrimeInteger getMersennePrimeInteger() {
    return mersenne;
  }

  public BigInteger getBigInteger() {
    return value;
  }

  public BigInteger getBigIntegerHalved() {
    return halved;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModulusMersennePrime that = (ModulusMersennePrime) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "ModulusMersennePrime{" +
        "value=" + value +
        '}';
  }
}