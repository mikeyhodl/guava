/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.math;

import static com.google.common.math.IntMath.checkedAdd;
import static com.google.common.math.IntMath.checkedMultiply;
import static com.google.common.math.IntMath.checkedSubtract;
import static com.google.common.math.IntMath.sqrt;
import static com.google.common.math.MathTesting.ALL_INTEGER_CANDIDATES;
import static com.google.common.math.MathTesting.ALL_ROUNDING_MODES;
import static com.google.common.math.MathTesting.ALL_SAFE_ROUNDING_MODES;
import static com.google.common.math.MathTesting.EXPONENTS;
import static com.google.common.math.MathTesting.NEGATIVE_INTEGER_CANDIDATES;
import static com.google.common.math.MathTesting.NONZERO_INTEGER_CANDIDATES;
import static com.google.common.math.MathTesting.POSITIVE_INTEGER_CANDIDATES;
import static com.google.common.math.TestPlatform.intsCanGoOutOfRange;
import static java.lang.Math.min;
import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.UNNECESSARY;
import static org.junit.Assert.assertThrows;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.testing.NullPointerTester;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;
import junit.framework.TestCase;
import org.jspecify.annotations.NullUnmarked;

/**
 * Tests for {@link IntMath}.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
@NullUnmarked
@SuppressWarnings("IntMathMod") // We are testing IntMathMod against alternatives.
public class IntMathTest extends TestCase {
  public void testMaxSignedPowerOfTwo() {
    assertTrue(IntMath.isPowerOfTwo(IntMath.MAX_SIGNED_POWER_OF_TWO));

    // Extra work required to make GWT happy.
    long value = IntMath.MAX_SIGNED_POWER_OF_TWO * 2L;
    assertFalse(IntMath.isPowerOfTwo((int) value));
  }

  public void testCeilingPowerOfTwo() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      BigInteger expectedResult = BigIntegerMath.ceilingPowerOfTwo(bigInt(x));
      if (fitsInInt(expectedResult)) {
        assertEquals(expectedResult.intValue(), IntMath.ceilingPowerOfTwo(x));
      } else {
        assertThrows(ArithmeticException.class, () -> IntMath.ceilingPowerOfTwo(x));
      }
    }
  }

  public void testFloorPowerOfTwo() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      BigInteger expectedResult = BigIntegerMath.floorPowerOfTwo(bigInt(x));
      assertEquals(expectedResult.intValue(), IntMath.floorPowerOfTwo(x));
    }
  }

  public void testCeilingPowerOfTwoNegative() {
    for (int x : NEGATIVE_INTEGER_CANDIDATES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.ceilingPowerOfTwo(x));
    }
  }

  public void testFloorPowerOfTwoNegative() {
    for (int x : NEGATIVE_INTEGER_CANDIDATES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.floorPowerOfTwo(x));
    }
  }

  public void testCeilingPowerOfTwoZero() {
    assertThrows(IllegalArgumentException.class, () -> IntMath.ceilingPowerOfTwo(0));
  }

  public void testFloorPowerOfTwoZero() {
    assertThrows(IllegalArgumentException.class, () -> IntMath.floorPowerOfTwo(0));
  }

  @GwtIncompatible // BigIntegerMath // TODO(cpovirk): GWT-enable BigIntegerMath
  public void testConstantMaxPowerOfSqrt2Unsigned() {
    assertEquals(
        BigIntegerMath.sqrt(BigInteger.ZERO.setBit(2 * Integer.SIZE - 1), FLOOR).intValue(),
        IntMath.MAX_POWER_OF_SQRT2_UNSIGNED);
  }

  @GwtIncompatible // pow()
  public void testConstantsPowersOf10() {
    for (int i = 0; i < IntMath.powersOf10.length - 1; i++) {
      assertEquals(IntMath.pow(10, i), IntMath.powersOf10[i]);
    }
  }

  @GwtIncompatible // BigIntegerMath // TODO(cpovirk): GWT-enable BigIntegerMath
  public void testMaxLog10ForLeadingZeros() {
    for (int i = 0; i < Integer.SIZE; i++) {
      assertEquals(
          BigIntegerMath.log10(BigInteger.ONE.shiftLeft(Integer.SIZE - i), FLOOR),
          IntMath.maxLog10ForLeadingZeros[i]);
    }
  }

  @GwtIncompatible // BigIntegerMath // TODO(cpovirk): GWT-enable BigIntegerMath
  public void testConstantsHalfPowersOf10() {
    for (int i = 0; i < IntMath.halfPowersOf10.length; i++) {
      assertEquals(
          IntMath.halfPowersOf10[i],
          min(
              Integer.MAX_VALUE,
              BigIntegerMath.sqrt(BigInteger.TEN.pow(2 * i + 1), FLOOR).longValue()));
    }
  }

  public void testConstantsBiggestBinomials() {
    for (int k = 0; k < IntMath.biggestBinomials.length; k++) {
      assertTrue(fitsInInt(BigIntegerMath.binomial(IntMath.biggestBinomials[k], k)));
      assertTrue(
          IntMath.biggestBinomials[k] == Integer.MAX_VALUE
              || !fitsInInt(BigIntegerMath.binomial(IntMath.biggestBinomials[k] + 1, k)));
      // In the first case, any int is valid; in the second, we want to test that the next-bigger
      // int overflows.
    }
    assertFalse(
        fitsInInt(
            BigIntegerMath.binomial(
                2 * IntMath.biggestBinomials.length, IntMath.biggestBinomials.length)));
  }

  @GwtIncompatible // sqrt
  public void testPowersSqrtMaxInt() {
    assertEquals(sqrt(Integer.MAX_VALUE, FLOOR), IntMath.FLOOR_SQRT_MAX_INT);
  }

  @AndroidIncompatible // presumably slow
  public void testLessThanBranchFree() {
    for (int x : ALL_INTEGER_CANDIDATES) {
      for (int y : ALL_INTEGER_CANDIDATES) {
        if (LongMath.fitsInInt((long) x - y)) {
          int expected = (x < y) ? 1 : 0;
          int actual = IntMath.lessThanBranchFree(x, y);
          assertEquals(expected, actual);
        }
      }
    }
  }

  @GwtIncompatible // java.math.BigInteger
  public void testIsPowerOfTwo() {
    for (int x : ALL_INTEGER_CANDIDATES) {
      // Checks for a single bit set.
      BigInteger bigX = bigInt(x);
      boolean expected = (bigX.signum() > 0) && (bigX.bitCount() == 1);
      assertEquals(expected, IntMath.isPowerOfTwo(x));
    }
  }

  public void testLog2ZeroAlwaysThrows() {
    for (RoundingMode mode : ALL_ROUNDING_MODES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.log2(0, mode));
    }
  }

  public void testLog2NegativeAlwaysThrows() {
    for (int x : NEGATIVE_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_ROUNDING_MODES) {
        assertThrows(IllegalArgumentException.class, () -> IntMath.log2(x, mode));
      }
    }
  }

  // Relies on the correctness of BigIntegerMath.log2 for all modes except UNNECESSARY.
  public void testLog2MatchesBigInteger() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_SAFE_ROUNDING_MODES) {
        assertEquals(BigIntegerMath.log2(bigInt(x), mode), IntMath.log2(x, mode));
      }
    }
  }

  // Relies on the correctness of isPowerOfTwo(int).
  public void testLog2Exact() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      // We only expect an exception if x was not a power of 2.
      boolean isPowerOf2 = IntMath.isPowerOfTwo(x);
      try {
        assertEquals(x, 1 << IntMath.log2(x, UNNECESSARY));
        assertTrue(isPowerOf2);
      } catch (ArithmeticException e) {
        assertFalse(isPowerOf2);
      }
    }
  }

  @GwtIncompatible // log10
  public void testLog10ZeroAlwaysThrows() {
    for (RoundingMode mode : ALL_ROUNDING_MODES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.log10(0, mode));
    }
  }

  @GwtIncompatible // log10
  public void testLog10NegativeAlwaysThrows() {
    for (int x : NEGATIVE_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_ROUNDING_MODES) {
        assertThrows(IllegalArgumentException.class, () -> IntMath.log10(x, mode));
      }
    }
  }

  // Relies on the correctness of BigIntegerMath.log10 for all modes except UNNECESSARY.
  @GwtIncompatible // BigIntegerMath // TODO(cpovirk): GWT-enable BigIntegerMath
  public void testLog10MatchesBigInteger() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_SAFE_ROUNDING_MODES) {
        // The BigInteger implementation is tested separately, use it as the reference.
        assertEquals(BigIntegerMath.log10(bigInt(x), mode), IntMath.log10(x, mode));
      }
    }
  }

  // Relies on the correctness of log10(int, FLOOR) and of pow(int, int).
  @GwtIncompatible // pow()
  public void testLog10Exact() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      int floor = IntMath.log10(x, FLOOR);
      boolean expectSuccess = IntMath.pow(10, floor) == x;
      try {
        assertEquals(floor, IntMath.log10(x, UNNECESSARY));
        assertTrue(expectSuccess);
      } catch (ArithmeticException e) {
        assertFalse(expectSuccess);
      }
    }
  }

  @GwtIncompatible // log10
  public void testLog10TrivialOnPowerOfTen() {
    int x = 1000000;
    for (RoundingMode mode : ALL_ROUNDING_MODES) {
      assertEquals(6, IntMath.log10(x, mode));
    }
  }

  // Simple test to cover sqrt(0) for all types and all modes.
  @GwtIncompatible // sqrt
  public void testSqrtZeroAlwaysZero() {
    for (RoundingMode mode : ALL_ROUNDING_MODES) {
      assertEquals(0, sqrt(0, mode));
    }
  }

  @SuppressWarnings("EnumValuesLoopToEnumSet") // EnumSet.allOf isn't available under J2KT
  @GwtIncompatible // sqrt
  public void testSqrtNegativeAlwaysThrows() {
    for (int x : NEGATIVE_INTEGER_CANDIDATES) {
      for (RoundingMode mode : RoundingMode.values()) {
        assertThrows(IllegalArgumentException.class, () -> sqrt(x, mode));
      }
    }
  }

  /* Relies on the correctness of BigIntegerMath.sqrt for all modes except UNNECESSARY. */
  @GwtIncompatible // BigIntegerMath // TODO(cpovirk): GWT-enable BigIntegerMath
  public void testSqrtMatchesBigInteger() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_SAFE_ROUNDING_MODES) {
        // The BigInteger implementation is tested separately, use it as the reference.
        // Promote the int value (rather than using intValue() on the expected value) to avoid
        // any risk of truncation which could lead to a false positive.
        assertEquals(BigIntegerMath.sqrt(bigInt(x), mode), bigInt(sqrt(x, mode)));
      }
    }
  }

  /* Relies on the correctness of sqrt(int, FLOOR). */
  @GwtIncompatible // sqrt
  public void testSqrtExactMatchesFloorOrThrows() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      int floor = sqrt(x, FLOOR);
      // We only expect an exception if x was not a perfect square.
      boolean isPerfectSquare = floor * floor == x;
      try {
        assertEquals(floor, sqrt(x, UNNECESSARY));
        assertTrue(isPerfectSquare);
      } catch (ArithmeticException e) {
        assertFalse(isPerfectSquare);
      }
    }
  }

  @GwtIncompatible // 2147483646^2 expected=4
  public void testPow() {
    for (int i : ALL_INTEGER_CANDIDATES) {
      for (int pow : EXPONENTS) {
        assertEquals(i + "^" + pow, bigInt(i).pow(pow).intValue(), IntMath.pow(i, pow));
      }
    }
  }

  @AndroidIncompatible // slow
  @GwtIncompatible // Math.floorDiv gets wrong answers for negative divisors
  public void testDivNonZero() {
    for (int p : NONZERO_INTEGER_CANDIDATES) {
      for (int q : NONZERO_INTEGER_CANDIDATES) {
        for (RoundingMode mode : ALL_SAFE_ROUNDING_MODES) {
          // Skip some tests that fail due to GWT's non-compliant int implementation.
          // TODO(cpovirk): does this test fail for only some rounding modes or for all?
          if (p == -2147483648 && q == -1 && intsCanGoOutOfRange()) {
            continue;
          }
          int expected =
              new BigDecimal(bigInt(p)).divide(new BigDecimal(bigInt(q)), 0, mode).intValue();
          assertEquals(p + "/" + q, force32(expected), IntMath.divide(p, q, mode));
          // Check the assertions we make in the javadoc.
          if (mode == DOWN) {
            assertEquals(p + "/" + q, p / q, IntMath.divide(p, q, mode));
          } else if (mode == FLOOR) {
            assertEquals("⌊" + p + "/" + q + "⌋", Math.floorDiv(p, q), IntMath.divide(p, q, mode));
          }
        }
      }
    }
  }

  @AndroidIncompatible // presumably slow
  public void testDivNonZeroExact() {
    for (int p : NONZERO_INTEGER_CANDIDATES) {
      for (int q : NONZERO_INTEGER_CANDIDATES) {
        // Skip some tests that fail due to GWT's non-compliant int implementation.
        if (p == -2147483648 && q == -1 && intsCanGoOutOfRange()) {
          continue;
        }
        boolean dividesEvenly = (p % q) == 0;
        try {
          assertEquals(p + "/" + q, p, IntMath.divide(p, q, UNNECESSARY) * q);
          assertTrue(p + "/" + q + " not expected to divide evenly", dividesEvenly);
        } catch (ArithmeticException e) {
          assertFalse(p + "/" + q + " expected to divide evenly", dividesEvenly);
        }
      }
    }
  }

  public void testZeroDivIsAlwaysZero() {
    for (int q : NONZERO_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_ROUNDING_MODES) {
        assertEquals(0, IntMath.divide(0, q, mode));
      }
    }
  }

  public void testDivByZeroAlwaysFails() {
    for (int p : ALL_INTEGER_CANDIDATES) {
      for (RoundingMode mode : ALL_ROUNDING_MODES) {
        assertThrows(ArithmeticException.class, () -> IntMath.divide(p, 0, mode));
      }
    }
  }

  public void testMod() {
    for (int x : ALL_INTEGER_CANDIDATES) {
      for (int m : POSITIVE_INTEGER_CANDIDATES) {
        assertEquals(bigInt(x).mod(bigInt(m)).intValue(), IntMath.mod(x, m));
      }
    }
  }

  public void testModNegativeModulusFails() {
    for (int x : POSITIVE_INTEGER_CANDIDATES) {
      for (int m : NEGATIVE_INTEGER_CANDIDATES) {
        assertThrows(ArithmeticException.class, () -> IntMath.mod(x, m));
      }
    }
  }

  public void testModZeroModulusFails() {
    for (int x : ALL_INTEGER_CANDIDATES) {
      assertThrows(ArithmeticException.class, () -> IntMath.mod(x, 0));
    }
  }

  public void testGCD() {
    for (int a : POSITIVE_INTEGER_CANDIDATES) {
      for (int b : POSITIVE_INTEGER_CANDIDATES) {
        assertEquals(bigInt(a).gcd(bigInt(b)), bigInt(IntMath.gcd(a, b)));
      }
    }
  }

  public void testGCDZero() {
    for (int a : POSITIVE_INTEGER_CANDIDATES) {
      assertEquals(a, IntMath.gcd(a, 0));
      assertEquals(a, IntMath.gcd(0, a));
    }
    assertEquals(0, IntMath.gcd(0, 0));
  }

  public void testGCDNegativePositiveThrows() {
    for (int a : NEGATIVE_INTEGER_CANDIDATES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.gcd(a, 3));
      assertThrows(IllegalArgumentException.class, () -> IntMath.gcd(3, a));
    }
  }

  public void testGCDNegativeZeroThrows() {
    for (int a : NEGATIVE_INTEGER_CANDIDATES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.gcd(a, 0));
      assertThrows(IllegalArgumentException.class, () -> IntMath.gcd(0, a));
    }
  }

  @AndroidIncompatible // slow
  @SuppressWarnings("InlineMeInliner") // We need to test checkedAdd
  public void testCheckedAdd() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : ALL_INTEGER_CANDIDATES) {
        // TODO: cpovirk - Test against Math.addExact instead?
        BigInteger expectedResult = bigInt(a).add(bigInt(b));
        boolean expectedSuccess = fitsInInt(expectedResult);
        try {
          assertEquals(a + b, checkedAdd(a, b));
          assertTrue(expectedSuccess);
        } catch (ArithmeticException e) {
          assertFalse(expectedSuccess);
        }
      }
    }
  }

  @SuppressWarnings("InlineMeInliner") // We need to test checkedSubtract
  @AndroidIncompatible // slow
  public void testCheckedSubtract() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : ALL_INTEGER_CANDIDATES) {
        // TODO: cpovirk - Test against Math.subtractExact instead?
        BigInteger expectedResult = bigInt(a).subtract(bigInt(b));
        boolean expectedSuccess = fitsInInt(expectedResult);
        try {
          assertEquals(a - b, checkedSubtract(a, b));
          assertTrue(expectedSuccess);
        } catch (ArithmeticException e) {
          assertFalse(expectedSuccess);
        }
      }
    }
  }

  @SuppressWarnings("InlineMeInliner") // We need to test checkedMultiply
  @AndroidIncompatible // presumably slow
  public void testCheckedMultiply() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : ALL_INTEGER_CANDIDATES) {
        // TODO: cpovirk - Test against Math.multiplyExact instead?
        BigInteger expectedResult = bigInt(a).multiply(bigInt(b));
        boolean expectedSuccess = fitsInInt(expectedResult);
        try {
          assertEquals(a * b, checkedMultiply(a, b));
          assertTrue(expectedSuccess);
        } catch (ArithmeticException e) {
          assertFalse(expectedSuccess);
        }
      }
    }
  }

  public void testCheckedPow() {
    for (int b : ALL_INTEGER_CANDIDATES) {
      for (int k : EXPONENTS) {
        BigInteger expectedResult = bigInt(b).pow(k);
        boolean expectedSuccess = fitsInInt(expectedResult);
        try {
          assertEquals(b + "^" + k, force32(expectedResult.intValue()), IntMath.checkedPow(b, k));
          assertTrue(b + "^" + k + " should have succeeded", expectedSuccess);
        } catch (ArithmeticException e) {
          assertFalse(b + "^" + k + " should have failed", expectedSuccess);
        }
      }
    }
  }

  @AndroidIncompatible // slow
  @GwtIncompatible // TODO
  public void testSaturatedAdd() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : ALL_INTEGER_CANDIDATES) {
        assertOperationEquals(
            a, b, "s+", saturatedCast(bigInt(a).add(bigInt(b))), IntMath.saturatedAdd(a, b));
      }
    }
  }

  @AndroidIncompatible // slow
  @GwtIncompatible // TODO
  public void testSaturatedSubtract() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : ALL_INTEGER_CANDIDATES) {
        assertOperationEquals(
            a,
            b,
            "s-",
            saturatedCast(bigInt(a).subtract(bigInt(b))),
            IntMath.saturatedSubtract(a, b));
      }
    }
  }

  @AndroidIncompatible // slow
  @GwtIncompatible // TODO
  public void testSaturatedMultiply() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : ALL_INTEGER_CANDIDATES) {
        assertOperationEquals(
            a,
            b,
            "s*",
            saturatedCast(bigInt(a).multiply(bigInt(b))),
            IntMath.saturatedMultiply(a, b));
      }
    }
  }

  @GwtIncompatible // TODO
  public void testSaturatedPow() {
    for (int a : ALL_INTEGER_CANDIDATES) {
      for (int b : EXPONENTS) {
        assertOperationEquals(
            a, b, "s^", saturatedCast(bigInt(a).pow(b)), IntMath.saturatedPow(a, b));
      }
    }
  }

  private static final BigInteger MAX_INT = bigInt(Integer.MAX_VALUE);
  private static final BigInteger MIN_INT = bigInt(Integer.MIN_VALUE);

  private static int saturatedCast(BigInteger big) {
    if (big.compareTo(MAX_INT) > 0) {
      return Integer.MAX_VALUE;
    }
    if (big.compareTo(MIN_INT) < 0) {
      return Integer.MIN_VALUE;
    }
    return big.intValue();
  }

  private void assertOperationEquals(int a, int b, String op, int expected, int actual) {
    if (expected != actual) {
      fail("Expected for " + a + " " + op + " " + b + " = " + expected + ", but got " + actual);
    }
  }

  // Depends on the correctness of BigIntegerMath.factorial.
  public void testFactorial() {
    for (int n = 0; n <= 50; n++) {
      BigInteger expectedBig = BigIntegerMath.factorial(n);
      int expectedInt = fitsInInt(expectedBig) ? expectedBig.intValue() : Integer.MAX_VALUE;
      assertEquals(expectedInt, IntMath.factorial(n));
    }
  }

  public void testFactorialNegative() {
    for (int n : NEGATIVE_INTEGER_CANDIDATES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.factorial(n));
    }
  }

  // Depends on the correctness of BigIntegerMath.binomial.
  public void testBinomial() {
    for (int n = 0; n <= 50; n++) {
      for (int k = 0; k <= n; k++) {
        BigInteger expectedBig = BigIntegerMath.binomial(n, k);
        int expectedInt = fitsInInt(expectedBig) ? expectedBig.intValue() : Integer.MAX_VALUE;
        assertEquals(expectedInt, IntMath.binomial(n, k));
      }
    }
  }

  public void testBinomialOutside() {
    for (int i = 0; i <= 50; i++) {
      int n = i;
      assertThrows(IllegalArgumentException.class, () -> IntMath.binomial(n, -1));
      assertThrows(IllegalArgumentException.class, () -> IntMath.binomial(n, n + 1));
    }
  }

  public void testBinomialNegative() {
    for (int n : NEGATIVE_INTEGER_CANDIDATES) {
      assertThrows(IllegalArgumentException.class, () -> IntMath.binomial(n, 0));
    }
  }

  @AndroidIncompatible // slow
  @GwtIncompatible // java.math.BigInteger
  public void testMean() {
    // Odd-sized ranges have an obvious mean
    assertMean(2, 1, 3);

    assertMean(-2, -3, -1);
    assertMean(0, -1, 1);
    assertMean(1, -1, 3);
    assertMean((1 << 30) - 1, -1, Integer.MAX_VALUE);

    // Even-sized ranges should prefer the lower mean
    assertMean(2, 1, 4);
    assertMean(-3, -4, -1);
    assertMean(0, -1, 2);
    assertMean(0, Integer.MIN_VALUE + 2, Integer.MAX_VALUE);
    assertMean(0, 0, 1);
    assertMean(-1, -1, 0);
    assertMean(-1, Integer.MIN_VALUE, Integer.MAX_VALUE);

    // x == y == mean
    assertMean(1, 1, 1);
    assertMean(0, 0, 0);
    assertMean(-1, -1, -1);
    assertMean(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    assertMean(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    // Exhaustive checks
    for (int x : ALL_INTEGER_CANDIDATES) {
      for (int y : ALL_INTEGER_CANDIDATES) {
        assertMean(x, y);
      }
    }
  }

  /** Helper method that asserts the arithmetic mean of x and y is equal to the expectedMean. */
  private static void assertMean(int expectedMean, int x, int y) {
    assertEquals(
        "The expectedMean should be the same as computeMeanSafely",
        expectedMean,
        computeMeanSafely(x, y));
    assertMean(x, y);
  }

  /**
   * Helper method that asserts the arithmetic mean of x and y is equal to the result of
   * computeMeanSafely.
   */
  private static void assertMean(int x, int y) {
    int expectedMean = computeMeanSafely(x, y);
    assertEquals(expectedMean, IntMath.mean(x, y));
    assertEquals(
        "The mean of x and y should equal the mean of y and x", expectedMean, IntMath.mean(y, x));
  }

  /**
   * Computes the mean in a way that is obvious and resilient to overflow by using BigInteger
   * arithmetic.
   */
  private static int computeMeanSafely(int x, int y) {
    BigInteger bigX = bigInt(x);
    BigInteger bigY = bigInt(y);
    BigDecimal two = BigDecimal.valueOf(2); // Android doesn't have BigDecimal.TWO yet
    BigDecimal bigMean = new BigDecimal(bigX.add(bigY)).divide(two, RoundingMode.FLOOR);
    return bigMean.intValueExact();
  }

  private static boolean fitsInInt(BigInteger big) {
    return big.bitLength() <= 31;
  }

  @J2ktIncompatible
  @GwtIncompatible // NullPointerTester
  public void testNullPointers() {
    NullPointerTester tester = new NullPointerTester();
    tester.setDefault(int.class, 1);
    tester.testAllPublicStaticMethods(IntMath.class);
  }

  @GwtIncompatible // isPrime is GWT-incompatible
  public void testIsPrime() {
    // Defer correctness tests to Long.isPrime

    // Check the first 100,000 integers
    for (int i = 0; i < 100000; i++) {
      assertEquals(LongMath.isPrime(i), IntMath.isPrime(i));
    }

    // Then check 1000 deterministic pseudo-random int values.
    Random rand = new Random(1);
    for (int i = 0; i < 1000; i++) {
      int n = rand.nextInt(Integer.MAX_VALUE);
      assertEquals(LongMath.isPrime(n), IntMath.isPrime(n));
    }
  }

  public void testSaturatedAbs() {
    assertEquals(Integer.MAX_VALUE, IntMath.saturatedAbs(Integer.MIN_VALUE));
    assertEquals(Integer.MAX_VALUE, IntMath.saturatedAbs(Integer.MAX_VALUE));
    assertEquals(Integer.MAX_VALUE, IntMath.saturatedAbs(-Integer.MAX_VALUE));
    assertEquals(0, IntMath.saturatedAbs(0));
    assertEquals(1, IntMath.saturatedAbs(1));
    assertEquals(1, IntMath.saturatedAbs(-1));
    assertEquals(10, IntMath.saturatedAbs(10));
    assertEquals(10, IntMath.saturatedAbs(-10));
  }

  private static int force32(int value) {
    // GWT doesn't consistently overflow values to make them 32-bit, so we need to force it.
    // TODO: b/404577035 - Remove this unless it's needed for J2CL.
    // One of its users, testDivNonZero, is currently @GwtIncompatible, but maybe it WOULD need it?
    // And if it's needed, maybe use our usual trick of ~~ instead?
    return value & 0xffffffff;
  }

  private static BigInteger bigInt(long value) {
    return BigInteger.valueOf(value);
  }
}
