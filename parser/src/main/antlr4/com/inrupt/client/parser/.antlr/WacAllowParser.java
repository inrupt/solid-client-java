// Generated from /Users/imyshor/Projects/solid/java/solid-client-java/parser/src/main/antlr4/com/inrupt/client/parser/WacAllow.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WacAllowParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, AccessParam=2, WS=3, DQUOTE=4;
	public static final int
		RULE_wacAllow = 0;
	private static String[] makeRuleNames() {
		return new String[] {
			"wacAllow"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", null, null, "'\u0022'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "AccessParam", "WS", "DQUOTE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "WacAllow.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public WacAllowParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class WacAllowContext extends ParserRuleContext {
		public List<TerminalNode> AccessParam() { return getTokens(WacAllowParser.AccessParam); }
		public TerminalNode AccessParam(int i) {
			return getToken(WacAllowParser.AccessParam, i);
		}
		public List<TerminalNode> WS() { return getTokens(WacAllowParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(WacAllowParser.WS, i);
		}
		public WacAllowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wacAllow; }
	}

	public final WacAllowContext wacAllow() throws RecognitionException {
		WacAllowContext _localctx = new WacAllowContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_wacAllow);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2);
			match(AccessParam);
			setState(13);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==WS) {
				{
				{
				setState(4);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(3);
					match(WS);
					}
				}

				setState(6);
				match(T__0);
				setState(8);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(7);
					match(WS);
					}
				}

				setState(10);
				match(AccessParam);
				}
				}
				setState(15);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\6\23\4\2\t\2\3\2"+
		"\3\2\5\2\7\n\2\3\2\3\2\5\2\13\n\2\3\2\7\2\16\n\2\f\2\16\2\21\13\2\3\2"+
		"\2\2\3\2\2\2\2\24\2\4\3\2\2\2\4\17\7\4\2\2\5\7\7\5\2\2\6\5\3\2\2\2\6\7"+
		"\3\2\2\2\7\b\3\2\2\2\b\n\7\3\2\2\t\13\7\5\2\2\n\t\3\2\2\2\n\13\3\2\2\2"+
		"\13\f\3\2\2\2\f\16\7\4\2\2\r\6\3\2\2\2\16\21\3\2\2\2\17\r\3\2\2\2\17\20"+
		"\3\2\2\2\20\3\3\2\2\2\21\17\3\2\2\2\5\6\n\17";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}