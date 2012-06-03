package calculator;


import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//Lexer for classical arithmetic expressions with identifiers and assignements.
// Scans a source string char by char.

public class CalculatorLexer {

	private Token currentToken;
	private String src; // source string for lexical analysis
	private int idx; // current index in source
	private int len; // length of source
	private String line;
	private static Map<String, String> map = new HashMap<String, String>();

	public CalculatorLexer() {
	}

	public void initLexer(String source) {
		this.src = source;
		idx = 0;
		len = src.length();
	}

	// Consumes letters only and builds an identifier
	private String identifier() {
		StringBuffer s = new StringBuffer();

		do {
			s.append(src.charAt(idx));
			idx++;
		} while (idx < len && Character.isLetter(src.charAt(idx)));
		return s.toString();
	}

	// Consumes digits and convert integer part and decimal part
	// Convert characters using the formula
	// "3456.253" = [(((3+0)*10+4)*10+5)*10+6]+[0.1*2+0.01*5+0.001*3]
	private double number() throws LexerException {
		double v = 0; // accumulates the result
		double factor = 0.1; // factor for decimal part

		do { // integer part
			v = v * 10 + Character.digit(src.charAt(idx), 30);
			idx++;
		} while (idx < len && Character.isDigit(src.charAt(idx)));
		if (idx < len && src.charAt(idx) == '.') { // decimal point
			idx++;
			if (idx < len && Character.isDigit(src.charAt(idx))) { // decimal
																	// part
				while (idx < len && Character.isDigit(src.charAt(idx))) {
					v = v + (factor * Character.digit(src.charAt(idx), 30));
					factor = factor * 0.1;
					idx++;
				}
			} else
				throw new LexerException("Illegal number: decimal part missing");
		}
		return v;
	}

	// Skips blanks, tabs, newlines
	private void skip() {
		char c;
		while (idx < len) {
			c = src.charAt(idx);
			if (c == ' ' || c == '\t' || c == '\n')
				idx++;
			else
				break;
		}
	}

	// returns next token
	public Token nextToken() throws LexerException {
		Token tok = new Token();

		skip();
		if (idx >= len) {
			tok.str = "EOL";
			tok.type = Token.EOL;
		} else
		// is it a positive number?
		if (Character.isDigit(src.charAt(idx))) {
			tok.value = number();
			tok.type = Token.NUM;
			tok.str = Double.toString(tok.value);
		} else if (Character.isLetter(src.charAt(idx))) {
			tok.value = 0;
			tok.type = Token.ID;
			tok.str = identifier();
			if (tok.str.compareTo("let") == 0)
				tok.type = Token.LET;
			if (tok.str.compareTo("exit") == 0)
				tok.type = Token.END;
		} else {
			switch (src.charAt(idx)) {
			case '+':
				tok.type = Token.ADD;
				tok.str = "+";
				break;
			case '-':
				tok.type = Token.SUB;
				tok.str = "-";
				break;
			case '*':
				tok.type = Token.MUL;
				tok.str = "*";
				break;
			case '/':
				tok.type = Token.DIV;
				tok.str = "/";
				break;
			case '(':
				tok.type = Token.PAL;
				tok.str = "(";
				break;
			case ')':
				tok.type = Token.PAR;
				tok.str = ")";
				break;
			case '=':
				tok.type = Token.EQU;
				tok.str = "=";
				break;
			default:
				throw new LexerException("Illegal Token: '" + src.charAt(idx)
						+ "'");
			}
			idx++;
		}
		return tok;
	}

	public static void main(String[] args) {
		CalculatorLexer cl = new CalculatorLexer();
		cl.start();
	}

	public void start() {
		while (true) {
			Scanner con = new Scanner(System.in);
			System.out.print("Type your expression: ");
			line = con.nextLine();

			// initLexer(line);
			// printOut();
			initLexer(line);
			statement();
		}
	}

	public double startWithExpression(String expr) {
		initLexer(expr);
		currentToken=nextToken();
		return expression();
	}

	public void statement() {
		double result=0;
		currentToken = nextToken();
		if (currentToken.type == Token.LET) {
			currentToken = nextToken();
			result = assignment();
		} else if (currentToken.type == Token.END)
			System.exit(1);// exit is supposed to mean quit?
		else {
			result = expression();
		}
		System.out.println("Result:"+result);

	}

	public double expression() {
		double left = term();
		// currentToken = nextToken();
		if (currentToken.type == Token.END || currentToken.type == Token.EOL
				|| currentToken.type == Token.PAR) {
			printTokenInfo("exprENDeol");
			return left;
		}

		else if (currentToken.type == Token.ADD) {
			printTokenInfo("exprADD");
			currentToken = nextToken();
			left += term();
		} else {
			// (currentToken.type == Token.SUB) {
			printTokenInfo("exprSUB");
			currentToken = nextToken();
			left -= term();
		}

		while (currentToken.type == Token.ADD || currentToken.type == Token.SUB) {
			if (currentToken.type == Token.ADD) {
				printTokenInfo("exprADD");
				currentToken = nextToken();
				left += term();
			} else {
				// (currentToken.type == Token.SUB) {
				printTokenInfo("exprSUB");
				currentToken = nextToken();
				left -= term();
			}
		}
		return left;
		// } else
		// throw new LexerException("Expected + or - as operator, but found "
		// + currentToken.str);

	}

	public double term() {
		double left = factor();

		if (currentToken.type == Token.DIV) {
			printTokenInfo("termDIV");
			currentToken = nextToken();
			left /= factor();
		} else if (currentToken.type == Token.MUL) {
			printTokenInfo("termMUL");
			currentToken = nextToken();
			left *= factor();
		}

		while (currentToken.type == Token.DIV || currentToken.type == Token.MUL) {
			if (currentToken.type == Token.DIV) {
				printTokenInfo("termDIV");
				currentToken = nextToken();
				left /= factor();
			} else if (currentToken.type == Token.MUL) {
				printTokenInfo("termMUL");
				currentToken = nextToken();
				left *= factor();
			}
		}
		return left;
	}

	public double factor() {
		String storedExpression;
		// if it is a negative number

		if (currentToken.type == Token.SUB) {
			printTokenInfo("factorNEG");
			currentToken = nextToken();
			return -1 * factor();
		} else if (currentToken.type == Token.NUM) {
			double value = currentToken.value;
			printTokenInfo("factorNUM");
			currentToken = nextToken();
			return value;
		} else if (currentToken.type == Token.ID) {
			printTokenInfo("factorID");
			storedExpression = map.get(currentToken.str);
			if (storedExpression==null)
				throw new LexerException(currentToken.str+ " is not yet defined");
//			System.out.println("Stored Expression for " + currentToken.str
//					+ " is " + storedExpression);
			currentToken = nextToken();
			CalculatorLexer cl = new CalculatorLexer();
			return cl.startWithExpression(storedExpression);
		} else if (currentToken.type == Token.PAL) {
			printTokenInfo("factorPAL");
			currentToken = nextToken();
			double result = expression();
			if (currentToken.type == Token.PAR) {
				printTokenInfo("factorPAR");
				currentToken = nextToken();
			} else
				throw new LexerException(
						"Expected an end parentheses, but found "
								+ currentToken.str);
			return result;
		}

		// .println(currentToken.str + " has value ");
		// System.out.print(currentToken.value + "has type " +
		// currentToken.type);
		return currentToken.value;
	}

	public double assignment() {
		if (currentToken.type != Token.ID)
			throw new LexerException(
					"identifier expected after let, but found "
							+ currentToken.str);
		String id = currentToken.str;
		currentToken = nextToken();
		if (currentToken.type != Token.EQU)
			throw new LexerException(
					"equals sign expected after the identifier, but found "
							+ currentToken.str);
		currentToken = nextToken();
		double result = expression();
//		System.out.println(id + " = " + result);
		initLexer(line);
		for (int i = 0; i < 4; i++) {
			currentToken = nextToken();
		}
		// System.out.println("current token before passing to expression: "+
		// currentToken.str);
		StringBuilder str = new StringBuilder();
		while (currentToken.type != Token.EOL) {
			str.append(currentToken.str);
			currentToken = nextToken();
		}
//		System.out.println("EXPRESSION TO STORE:" + str.toString());
		initLexer(str.toString());
		currentToken = nextToken();
		result = expression();
//		System.out.println("EVALUATES TO : " + result);
		map.put(id, str.toString());
		return result;
	}

	public void printOut() {
		Token t = nextToken();
		while (t.type != Token.EOL) {
			System.out.print(" Token:" + t.str + " "
			// + t.type
					);
			t = nextToken();
		}
		System.out.println("");
	}

	public void printTokenInfo() {
		printTokenInfo("No Referal");
	}

	public void printTokenInfo(String origin) {
		// System.out.println(origin + ": " + currentToken.str + " is type "
		// + currentToken.type);
	}
}