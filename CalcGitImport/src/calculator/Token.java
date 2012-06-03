package calculator;

//Various Tokens for arithmetic expressions based on integers 
//with identifiers and assignments

public class Token {
public int type;                   // token type
public double value;               // numerical value for NUM
public String str;                 // token string

public static final int EOL=0;     // End Of Line
public static final int PAL=1;     // Left Parenthesis
public static final int PAR=2;     // Right Parenthesis
public static final int ADD=3;     // operators
public static final int SUB=4;
public static final int MUL=5;
public static final int DIV=6;
public static final int NUM=7;     // number
public static final int EQU=8;     // equal 
public static final int LET=9;     // let
public static final int ID=10;     // identifier
public static final int END=11;    // exit
}