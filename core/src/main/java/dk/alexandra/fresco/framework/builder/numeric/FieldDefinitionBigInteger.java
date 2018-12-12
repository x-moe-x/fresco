package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class FieldDefinitionBigInteger implements FieldDefinition {

  private final ModulusBigInteger modulus;
  private final int modulusLength;
  private final BigInteger modulusHalf;

  public FieldDefinitionBigInteger(ModulusBigInteger modulus) {
    this.modulus = modulus;
    this.modulusHalf = modulus.getBigInteger().shiftRight(1);
    this.modulusLength = modulus.getBigInteger().toByteArray().length;
  }

  @Override
  public BigInteger convertRepresentation(FieldElement value) {
    BigInteger actual = FieldElementBigInteger.extractValue(value);
    if (actual.compareTo(modulusHalf) > 0) {
      return actual.subtract(getModulus());
    } else {
      return actual;
    }
  }

  @Override
  public BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public FieldElement createElement(int value) {
    return FieldElementBigInteger.create(value, modulus);
  }

  @Override
  public FieldElement createElement(String value) {
    return FieldElementBigInteger.create(value, modulus);
  }

  @Override
  public FieldElement createElement(BigInteger value) {
    return FieldElementBigInteger.create(value, modulus);
  }

  @Override
  public FieldElement deserialize(byte[] bytes) {
    return FieldElementBigInteger.create(bytes, modulus);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      byte[] copy = new byte[modulusLength];
      System.arraycopy(bytes, i, copy, 0, modulusLength);
      elements.add(FieldElementBigInteger.create(copy, modulus));
    }
    return elements;
  }

  @Override
  public byte[] serialize(FieldElement fieldElement) {
    return ((FieldElementBigInteger) fieldElement).toByteArray();
  }

  @Override
  public byte[] serialize(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      FieldElementBigInteger fieldElement = (FieldElementBigInteger) fieldElements.get(i);
      fieldElement.toByteArray(bytes, i * modulusLength, modulusLength);
    }
    return bytes;
  }
}
