package com.dfsek.paralithic.eval.parser;

import com.dfsek.paralithic.Expression;
import com.dfsek.paralithic.eval.ExpressionBuilder;
import com.dfsek.paralithic.eval.ParserUtil;
import com.dfsek.paralithic.eval.tokenizer.ParseError;
import com.dfsek.paralithic.eval.tokenizer.ParseException;
import com.dfsek.paralithic.eval.tokenizer.Token;
import com.dfsek.paralithic.eval.tokenizer.Tokenizer;
import com.dfsek.paralithic.functions.Function;
import com.dfsek.paralithic.functions.dynamic.DynamicFunction;
import com.dfsek.paralithic.functions.natives.NativeFunction;
import com.dfsek.paralithic.functions.natives.NativeMath;
import com.dfsek.paralithic.functions.node.NodeFunction;
import com.dfsek.paralithic.functions.node.TernaryIfFunction;
import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.paralithic.node.special.InvocationVariableNode;
import com.dfsek.paralithic.node.special.LocalVariableBindingNode;
import com.dfsek.paralithic.node.special.LocalVariableNode;
import com.dfsek.paralithic.node.special.function.FunctionNode;
import com.dfsek.paralithic.node.special.function.NativeFunctionNode;
import com.dfsek.paralithic.node.unary.AbsoluteValueNode;
import com.dfsek.paralithic.node.unary.NegationNode;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Parser {
   private static final double[] D0 = new double[0];
   private Scope scope;
   private int maxLocalVariableIndex = 0;
   private final List<ParseError> errors = new ArrayList<>();
   private final Tokenizer tokenizer;
   private final Map<String, Function> functionTable = new TreeMap<>();
   private final Parser.ParseOptions options;

   public Parser() {
      this(new StringReader(""), new Scope(), new TreeMap<>(), new Parser.ParseOptions());
   }

   public Parser(Parser.ParseOptions options) {
      this(new StringReader(""), new Scope(), new TreeMap<>(), options);
   }

   protected Parser(Reader input, Scope scope, Map<String, Function> functionTable, Parser.ParseOptions options) {
      this.registerFunction("sin", NativeMath.SIN);
      this.registerFunction("cos", NativeMath.COS);
      this.registerFunction("tan", NativeMath.TAN);
      this.registerFunction("floor", NativeMath.FLOOR);
      this.registerFunction("ceil", NativeMath.CEIL);
      this.registerFunction("round", NativeMath.ROUND);
      this.registerFunction("pow", NativeMath.POW);
      this.registerFunction("min", NativeMath.MIN);
      this.registerFunction("max", NativeMath.MAX);
      this.registerFunction("sqrt", NativeMath.SQRT);
      this.registerFunction("sinh", NativeMath.SINH);
      this.registerFunction("cosh", NativeMath.COSH);
      this.registerFunction("tanh", NativeMath.TANH);
      this.registerFunction("asin", NativeMath.ASIN);
      this.registerFunction("acos", NativeMath.ACOS);
      this.registerFunction("atan", NativeMath.ATAN);
      this.registerFunction("atan2", NativeMath.ATAN2);
      this.registerFunction("rad", NativeMath.RAD);
      this.registerFunction("deg", NativeMath.DEG);
      this.registerFunction("abs", NativeMath.ABS);
      this.registerFunction("log", NativeMath.LOG);
      this.registerFunction("ln", NativeMath.LN);
      this.registerFunction("exp", NativeMath.EXP);
      this.registerFunction("sign", NativeMath.SIGN);
      this.registerFunction("sigmoid", NativeMath.SIGMOID);
      this.registerFunction("if", new TernaryIfFunction());
      this.scope = scope;
      this.tokenizer = new Tokenizer(input);
      this.tokenizer.setProblemCollector(this.errors);
      if (options.useLetExpressions) {
         this.tokenizer.addKeyword("let");
         this.tokenizer.addKeyword("in");
      }

      this.functionTable.putAll(functionTable);
      this.options = options;
   }

   public Scope getScope() {
      return this.scope;
   }

   public void registerFunction(String name, Function function) {
      this.functionTable.put(name, function);
   }

   public Expression parse(String input) throws ParseException {
      return new Parser(new StringReader(input), new Scope(), this.functionTable, this.options).parse();
   }

   public Expression parse(Reader input) throws ParseException {
      return new Parser(input, new Scope(), this.functionTable, this.options).parse();
   }

   public Expression parse(String input, Scope scope) throws ParseException {
      return new Parser(new StringReader(input), scope, this.functionTable, this.options).parse();
   }

   public Expression parse(Reader input, Scope scope) throws ParseException {
      return new Parser(input, scope, this.functionTable, this.options).parse();
   }

   public Expression parse() throws ParseException {
      Node result = this.parseExpression();
      Map<String, DynamicFunction> dynamicFunctionMap = new TreeMap<>();
      this.functionTable.forEach((id, f) -> {
         if (f instanceof DynamicFunction) {
            dynamicFunctionMap.put(id, (DynamicFunction)f);
         }
      });
      return new ExpressionBuilder(dynamicFunctionMap).get(result);
   }

   public double eval(String expression) throws ParseException {
      return this.eval(expression, D0);
   }

   public double eval(String expression, double... args) throws ParseException {
      return this.eval(expression, new Scope(), args);
   }

   public double eval(String expression, Scope scope, double... args) throws ParseException {
      return new Parser(new StringReader(expression), scope, this.functionTable, this.options).eval(args);
   }

   public double eval(double... args) throws ParseException {
      return this.parseExpression().eval(new double[this.maxLocalVariableIndex + 1], args);
   }

   public Node parseExpression() throws ParseException {
      Node result = this.expression();
      if (this.tokenizer.current().isNotEnd()) {
         Token token = this.tokenizer.consume();
         this.errors.add(ParseError.error(token, String.format("Unexpected token: '%s'. Expected an expression.", token.getSource())));
      }

      if (!this.errors.isEmpty()) {
         throw ParseException.create(this.errors);
      } else {
         return result;
      }
   }

   protected Node expression() {
      Node left = this.relationalExpression();
      if (this.tokenizer.current().isSymbol("&&")) {
         this.tokenizer.consume();
         Node right = this.expression();
         return this.reOrder(left, right, BinaryNode.Op.AND);
      } else if (this.tokenizer.current().isSymbol("||")) {
         this.tokenizer.consume();
         Node right = this.expression();
         return this.reOrder(left, right, BinaryNode.Op.OR);
      } else {
         return left;
      }
   }

   public List<ParseError> getErrors() {
      return this.errors;
   }

   protected Node relationalExpression() {
      Node left = this.term();
      if (this.tokenizer.current().isSymbol("<")) {
         this.tokenizer.consume();
         Node right = this.relationalExpression();
         return this.reOrder(left, right, BinaryNode.Op.LT);
      } else if (this.tokenizer.current().isSymbol("<=")) {
         this.tokenizer.consume();
         Node right = this.relationalExpression();
         return this.reOrder(left, right, BinaryNode.Op.LT_EQ);
      } else if (this.tokenizer.current().isSymbol("=")) {
         this.tokenizer.consume();
         Node right = this.relationalExpression();
         return this.reOrder(left, right, BinaryNode.Op.EQ);
      } else if (this.tokenizer.current().isSymbol(">=")) {
         this.tokenizer.consume();
         Node right = this.relationalExpression();
         return this.reOrder(left, right, BinaryNode.Op.GT_EQ);
      } else if (this.tokenizer.current().isSymbol(">")) {
         this.tokenizer.consume();
         Node right = this.relationalExpression();
         return this.reOrder(left, right, BinaryNode.Op.GT);
      } else if (this.tokenizer.current().isSymbol("!=")) {
         this.tokenizer.consume();
         Node right = this.relationalExpression();
         return this.reOrder(left, right, BinaryNode.Op.NEQ);
      } else {
         return left;
      }
   }

   protected Node term() {
      Node left = this.product();
      if (this.tokenizer.current().isSymbol("+")) {
         this.tokenizer.consume();
         Node right = this.term();
         return this.reOrder(left, right, BinaryNode.Op.ADD);
      } else if (this.tokenizer.current().isSymbol("-")) {
         this.tokenizer.consume();
         Node right = this.term();
         return this.reOrder(left, right, BinaryNode.Op.SUBTRACT);
      } else if (this.tokenizer.current().isNumber() && this.tokenizer.current().getContents().startsWith("-")) {
         Node right = this.term();
         return this.reOrder(left, right, BinaryNode.Op.ADD);
      } else {
         return left;
      }
   }

   protected Node product() {
      Node left = this.power();
      if (this.tokenizer.current().isSymbol("*")) {
         this.tokenizer.consume();
         Node right = this.product();
         return this.reOrder(left, right, BinaryNode.Op.MULTIPLY);
      } else if (this.tokenizer.current().isSymbol("/")) {
         this.tokenizer.consume();
         Node right = this.product();
         return this.reOrder(left, right, BinaryNode.Op.DIVIDE);
      } else if (this.tokenizer.current().isSymbol("%")) {
         this.tokenizer.consume();
         Node right = this.product();
         return this.reOrder(left, right, BinaryNode.Op.MODULO);
      } else {
         return left;
      }
   }

   protected Node reOrder(Node left, Node right, BinaryNode.Op op) {
      if (right instanceof BinaryNode rightOp && !rightOp.isSealed() && rightOp.getOp().getPriority() == op.getPriority()) {
         this.replaceLeft(rightOp, left, op);
         return right;
      } else {
         return ParserUtil.createBinaryOperation(op, left, right);
      }
   }

   protected void replaceLeft(BinaryNode target, Node newLeft, BinaryNode.Op op) {
      if (target.getLeft() instanceof BinaryNode leftOp && !leftOp.isSealed() && leftOp.getOp().getPriority() == op.getPriority()) {
         this.replaceLeft(leftOp, newLeft, op);
      } else {
         target.setLeft(ParserUtil.createBinaryOperation(op, newLeft, target.getLeft()));
      }
   }

   protected Node power() {
      Node left = this.atom();
      if (!this.tokenizer.current().isSymbol("^") && !this.tokenizer.current().isSymbol("**")) {
         return left;
      }

      this.tokenizer.consume();
      Node right = this.power();
      return this.reOrder(left, right, BinaryNode.Op.POWER);
   }

   protected Node atom() {
      if (this.tokenizer.current().isSymbol("-")) {
         this.tokenizer.consume();
         return new NegationNode(this.atom());
      }

      if (this.tokenizer.current().isSymbol("+") && this.tokenizer.next().isSymbol("(")) {
         this.tokenizer.consume();
      }

      if (this.tokenizer.current().isSymbol("(")) {
         this.tokenizer.consume();
         Node result = this.expression();
         if (result instanceof BinaryNode) {
            ((BinaryNode)result).seal();
         }

         this.expect(Token.TokenType.SYMBOL, ")");
         return result;
      } else if (this.tokenizer.current().isSymbol("|")) {
         this.tokenizer.consume();
         Node exp = this.expression();
         this.expect(Token.TokenType.SYMBOL, "|");
         return new AbsoluteValueNode(exp);
      } else if (this.options.useLetExpressions() && this.tokenizer.current().isKeyword("let")) {
         this.tokenizer.consume();
         return this.letExpression();
      } else if (this.tokenizer.current().isIdentifier()) {
         return this.tokenizer.next().isSymbol("(") ? this.functionCall() : this.variable();
      } else {
         return this.literalAtom();
      }
   }

   protected Node variable() {
      Token variableName = this.tokenizer.consume();
      Integer localVarIndex = this.scope.getLocalVariableIndex(variableName.getContents());
      if (localVarIndex != null) {
         return new LocalVariableNode(localVarIndex);
      }

      int invocationVarIndex = this.scope.getInvocationVarIndex(variableName.getContents());
      if (invocationVarIndex >= 0) {
         return new InvocationVariableNode(invocationVarIndex);
      }

      NamedConstant constant = this.scope.find(variableName.getContents());
      if (constant != null) {
         return Constant.of(constant.getValue());
      }

      this.errors.add(ParseError.error(variableName, String.format("Unknown variable: '%s'", variableName.getContents())));
      return Constant.of(0.0);
   }

   private Node literalAtom() {
      if (this.tokenizer.current().isSymbol("+") && this.tokenizer.next().isNumber()) {
         this.tokenizer.consume();
      }

      if (this.tokenizer.current().isNumber()) {
         double value = Double.parseDouble(this.tokenizer.consume().getContents());
         if (this.tokenizer.current().is(Token.TokenType.ID)) {
            String quantifier = this.tokenizer.current().getContents().intern();
            switch (quantifier) {
               case "n":
                  value /= 1.0E9;
                  this.tokenizer.consume();
                  break;
               case "u":
                  value /= 1000000.0;
                  this.tokenizer.consume();
                  break;
               case "m":
                  value /= 1000.0;
                  this.tokenizer.consume();
                  break;
               case "K":
               case "k":
                  value *= 1000.0;
                  this.tokenizer.consume();
                  break;
               case "M":
                  value *= 1000000.0;
                  this.tokenizer.consume();
                  break;
               case "G":
                  value *= 1.0E9;
                  this.tokenizer.consume();
                  break;
               default:
                  Token token = this.tokenizer.consume();
                  this.errors.add(ParseError.error(token, String.format("Unexpected token: '%s'. Expected a valid quantifier.", token.getSource())));
            }
         }

         return Constant.of(value);
      } else {
         Token token = this.tokenizer.consume();
         this.errors.add(ParseError.error(token, String.format("Unexpected token: '%s'. Expected an expression.", token.getSource())));
         return Constant.of(Double.NaN);
      }
   }

   protected Node letExpression() {
      this.scope = new Scope().withParent(this.scope);
      List<Parser.BindingPair> bindings = new ArrayList<>();

      while (this.tokenizer.current().isNotEnd()) {
         if (this.tokenizer.current().isKeyword("in")) {
            this.tokenizer.consume();
            break;
         }

         if (this.tokenizer.current().isIdentifier()) {
            Token nameToken = this.tokenizer.consume();
            String name = nameToken.getContents();
            Node boundExpression;
            if (!this.tokenizer.current().isSymbol(":=")) {
               Token notEquals = this.tokenizer.current();
               this.errors
                  .add(
                     ParseError.error(notEquals, String.format("Unexpected token: '%s'. Expected ':=' symbol proceeding binding name.", notEquals.getSource()))
                  );
               boundExpression = Constant.of(Double.NaN);
            } else {
               this.tokenizer.consume();
               boundExpression = this.expression();
            }

            if (bindings.stream().anyMatch(bindingPair -> name.equals(bindingPair.identifier()))) {
               this.errors
                  .add(ParseError.error(nameToken, String.format("Cannot bind '%s', this name has already been bound within the let expression", name)));
            } else {
               int index = this.scope.addLocalVariable(name);
               if (index > this.maxLocalVariableIndex) {
                  this.maxLocalVariableIndex = index;
               }

               bindings.add(new Parser.BindingPair(name, boundExpression));
            }
         }

         Token afterBoundExpression = this.tokenizer.current();
         if (afterBoundExpression.isSymbol(",")) {
            this.tokenizer.consume();
         } else if (!afterBoundExpression.isKeyword("in")) {
            Token notIdentifierOrInKeyword = this.tokenizer.current();
            this.errors
               .add(
                  ParseError.error(
                     notIdentifierOrInKeyword, String.format("Unexpected token '%s'. Expected ',' or 'in' keyword.", notIdentifierOrInKeyword.getSource())
                  )
               );
            break;
         }
      }

      Node expression = this.expression();

      for (int i = bindings.size() - 1; i >= 0; i--) {
         Parser.BindingPair pair = bindings.get(i);
         expression = new LocalVariableBindingNode(this.scope.getLocalVariableIndex(pair.identifier()), pair.expression(), expression);
      }

      this.scope = this.scope.getParent();
      return expression;
   }

   protected Node functionCall() {
      Token funToken = this.tokenizer.consume();
      Function fun = this.functionTable.get(funToken.getContents());
      List<Node> params = new ArrayList<>();
      this.tokenizer.consume();

      for (; !this.tokenizer.current().isSymbol(")") && this.tokenizer.current().isNotEnd(); params.add(this.expression())) {
         if (!params.isEmpty()) {
            this.expect(Token.TokenType.SYMBOL, ",");
         }
      }

      this.expect(Token.TokenType.SYMBOL, ")");
      if (fun == null) {
         this.errors.add(ParseError.error(funToken, String.format("Unknown function: '%s'", funToken.getContents())));
         return Constant.of(Double.NaN);
      }

      if (params.size() != fun.getArgNumber() && fun.getArgNumber() >= 0) {
         this.errors
            .add(
               ParseError.error(
                  funToken,
                  String.format(
                     "Number of arguments for function '%s' do not match. Expected: %d, Found: %d", funToken.getContents(), fun.getArgNumber(), params.size()
                  )
               )
            );
         return Constant.of(Double.NaN);
      }

      if (fun instanceof DynamicFunction) {
         return new FunctionNode(params, (DynamicFunction)fun, funToken.getContents());
      }

      if (fun instanceof NativeFunction) {
         return new NativeFunctionNode((NativeFunction)fun, params);
      }

      if (fun instanceof NodeFunction) {
         return ((NodeFunction)fun).createNode(params);
      }

      this.errors.add(ParseError.error(funToken, String.format("Unknown function implementation: '%s", fun.getClass().getName())));
      return Constant.of(Double.NaN);
   }

   protected void expect(Token.TokenType type, String trigger) {
      if (this.tokenizer.current().matches(type, trigger)) {
         this.tokenizer.consume();
      } else {
         this.errors
            .add(
               ParseError.error(this.tokenizer.current(), String.format("Unexpected token '%s'. Expected: '%s'", this.tokenizer.current().getSource(), trigger))
            );
      }
   }

   record BindingPair(String identifier, Node expression) {
   }

   public record ParseOptions(boolean useLetExpressions) {
      public ParseOptions() {
         this(false);
      }
   }
}
