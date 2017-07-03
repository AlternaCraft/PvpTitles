/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alternacraft.pvptitles.Misc.Formulas;

import java.util.Map;

/**
 *
 * @author AlternaCraft
 */
public class EvaluableExpression {

    private final String str;
    private final Map<String, Double> variables;

    private int pos = -1, ch;

    public EvaluableExpression(String str, Map<String, Double> variables) {
        this.str = str;
        this.variables = variables;
    }
    
    public String getFormula() {
        return this.str;
    }
    
    public Expression parse() throws RuntimeException {
        nextChar();
        Expression x = parseExpression();
        if (pos < str.length()) {
            throw new RuntimeException("Unexpected: " + (char) ch);
        }
        return x;
    }

    //<editor-fold defaultstate="collapsed" desc="INNER CODE">
    private void nextChar() {
        ch = (++pos < str.length()) ? str.charAt(pos) : -1;
    }

    private boolean eat(int charToEat) {
        while (ch == ' ') {
            nextChar();
        }
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

    private Expression parseExpression() {
        Expression x = parseTerm();
        for (;;) {
            if (eat('+')) { // addition
                final Expression a = x, b = parseTerm();
                x = () -> a.eval() + b.eval();
            } else if (eat('-')) { // subtraction
                final Expression a = x, b = parseTerm();
                x = () -> a.eval() - b.eval();
            } else {
                return x;
            }
        }
    }

    private Expression parseTerm() {
        Expression x = parseFactor();
        for (;;) {
            if (eat('*')) {
                final Expression a = x, b = parseFactor();
                x = () -> a.eval() * b.eval();
            } else if (eat('/')) {
                final Expression a = x, b = parseFactor();
                x = () -> a.eval() / b.eval();
            } else {
                return x;
            }
        }
    }

    private Expression parseFactor() {
        if (eat('+')) {
            return parseFactor(); // unary plus
        }
        if (eat('-')) {
            final Expression b = parseFactor(); // unary minus
            return () -> -1 * b.eval();
        }

        Expression x = null;
        int startPos = this.pos;

        if (eat('(')) { // parentheses
            x = parseExpression();
            eat(')');
        } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
            while ((ch >= '0' && ch <= '9') || ch == '.') {
                nextChar();
            }
            final double xx = Double.parseDouble(str.substring(startPos, this.pos));
            x = () -> xx;
        } else if (ch >= 'a' && ch <= 'z') { // functions and variables
            while (ch >= 'a' && ch <= 'z') {
                nextChar();
            }
            final String name = str.substring(startPos, this.pos);

            final ClassMethod cm = new ClassMethod(Math.class, double.class, name);
            if (cm.validMethod(0, 1, 2)) { // Valid for 0, 1 or 2 argument/s
                Expression arg = parseFactor();
                if (arg != null) {
                    cm.add(arg);
                }
                while (eat(',')) { // Multiple parameters
                    cm.add(parseFactor());
                }

                x = () -> {
                    try {
                        Object result = cm.applyAsObject();
                        if (result instanceof Long) {
                            return ((Long) result).doubleValue();
                        } else if (result instanceof Float) {
                            return ((Float) result).doubleValue();
                        }
                        return (double) result;
                    } catch (RuntimeException ex) {
                        throw new RuntimeException("Error parsing Math function: " + ex.getMessage());
                    }
                };
            } else {
                x = () -> variables.get(name);
            }
        } else if (ch != ')') {
            throw new RuntimeException("Unexpected: " + (char) ch);
        }

        if (eat('^')) {
            final Expression e = x;
            final double d = parseFactor().eval();
            x = () -> Math.pow(e.eval(), d);
        }

        return x;
    }
    //</editor-fold>
}
