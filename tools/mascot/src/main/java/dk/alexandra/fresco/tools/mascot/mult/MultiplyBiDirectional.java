package dk.alexandra.fresco.tools.mascot.mult;

import java.util.List;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MultiplyBiDirectional {

  protected MultiplyLeft left;
  protected MultiplyRight right;
  
  public MultiplyBiDirectional(MascotContext ctx, Integer otherId, int numLeftFactors) {
    this.left = new MultiplyLeft(ctx, otherId, numLeftFactors);
    this.right = new MultiplyRight(ctx, otherId, numLeftFactors);
  }

  public Integer getOtherId() {
    return left.getOtherId();
  }
  
  public List<List<FieldElement>> multiplyLeft(List<List<FieldElement>> leftFactors) {
    return left.multiply(leftFactors);
  }

  public List<List<FieldElement>> multiplyRight(List<FieldElement> rightFactor) {
    return right.multiply(rightFactor);
  }

}
