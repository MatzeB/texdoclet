package org.wonderly.doclets.test;

/**
 * HŠllš this is just a class to test texdoclet...
 * @author matze
 */
public class TestClass {
	/**
	 * <b>Warning:</b> This is <em>NOT</em> considered good documentation style. It's way too verbose to be usefull in
	 * practice. Anyway this is just here to test texdoclet...
	 * 
	 * Adds 2 int numbers. I hope you're aware, that int numbers in java are not the same as natural
	 * numbers or integer numbers in mathematics. int numbers are restricted to the value range representable
	 * by 32 bits. If an addition produces an overflow or underflow, then modulo arithmetic is happening.
	 * So java int numbers as mathematical construct form a ring, with all the usual properties like
	 * have a zero and one element, respecting the rules of closure, associativity, closure, distributivity
	 * and inverse elements (for addition).
	 * 
	 * Well the talk above probably isn't 100% accurate but I just wanted to write a bigger block of text
	 * to test texdoclet...
	 * 
	 * <h1>About the mathematics of int numbers</h1>
	 * <p>
	 *   So I tried to write even more about that, but you should rather look at
	 *   <a href="http://en.wikipedia.org/wiki/Integer_%28computer_science%29">Wikipedia</a>.
	 * </p>
	 * 
	 * We can even have text in the
	 * <center>center</center>
	 * of the universe - I mean line.
	 * 
	 * @param a The first operand of the summation.
	 * 		This is the first of the 2 summands.
	 * 		There's the additional constraint, that both summand need ot be of the same type.
	 * 		To be precise both need to be integer.
	 * @param b the 2nd operand
	 * @return The sum of both summands. Note that because of overflow and underflow issues you can not
	 *         expect the sum of 2 positive numbers to be necessarily bigger than both parts.
	 *         Likewise the sum of 2 negative numbers is not necessarily smaller than both
	 *         summands.
	 */
	public int addints(int a, int b) {
		return a + b;
	}
}
