// Generated from /Users/imyshor/Projects/solid/java/solid-client-java/parser/src/main/antlr4/com/inrupt/client/parser/WwwAuthenticate.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WwwAuthenticateParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, AuthParam=2, AuthScheme=3, QuotedString=4, Token=5, Token68=6, 
		WS=7;
	public static final int
		RULE_wwwAuthenticate = 0, RULE_challenge = 1;
	private static String[] makeRuleNames() {
		return new String[] {
			"wwwAuthenticate", "challenge"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "AuthParam", "AuthScheme", "QuotedString", "Token", "Token68", 
			"WS"
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
	public String getGrammarFileName() { return "WwwAuthenticate.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public WwwAuthenticateParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class WwwAuthenticateContext extends ParserRuleContext {
		public List<ChallengeContext> challenge() {
			return getRuleContexts(ChallengeContext.class);
		}
		public ChallengeContext challenge(int i) {
			return getRuleContext(ChallengeContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(WwwAuthenticateParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(WwwAuthenticateParser.WS, i);
		}
		public WwwAuthenticateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wwwAuthenticate; }
	}

	public final WwwAuthenticateContext wwwAuthenticate() throws RecognitionException {
		WwwAuthenticateContext _localctx = new WwwAuthenticateContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_wwwAuthenticate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(4);
			challenge();
			setState(12);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(5);
				match(T__0);
				setState(7);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(6);
					match(WS);
					}
				}

				setState(9);
				challenge();
				}
				}
				setState(14);
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

	public static class ChallengeContext extends ParserRuleContext {
		public TerminalNode AuthScheme() { return getToken(WwwAuthenticateParser.AuthScheme, 0); }
		public List<TerminalNode> WS() { return getTokens(WwwAuthenticateParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(WwwAuthenticateParser.WS, i);
		}
		public List<TerminalNode> Token68() { return getTokens(WwwAuthenticateParser.Token68); }
		public TerminalNode Token68(int i) {
			return getToken(WwwAuthenticateParser.Token68, i);
		}
		public List<TerminalNode> AuthParam() { return getTokens(WwwAuthenticateParser.AuthParam); }
		public TerminalNode AuthParam(int i) {
			return getToken(WwwAuthenticateParser.AuthParam, i);
		}
		public ChallengeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_challenge; }
	}

	public final ChallengeContext challenge() throws RecognitionException {
		ChallengeContext _localctx = new ChallengeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_challenge);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(15);
			match(AuthScheme);
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(16);
				match(WS);
				setState(32);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case Token68:
					{
					setState(17);
					match(Token68);
					}
					break;
				case AuthParam:
					{
					setState(18);
					match(AuthParam);
					setState(29);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(20);
							_errHandler.sync(this);
							_la = _input.LA(1);
							if (_la==WS) {
								{
								setState(19);
								match(WS);
								}
							}

							setState(22);
							match(T__0);
							setState(24);
							_errHandler.sync(this);
							_la = _input.LA(1);
							if (_la==WS) {
								{
								setState(23);
								match(WS);
								}
							}

							setState(26);
							match(AuthParam);
							}
							} 
						}
						setState(31);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(38);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\t*\4\2\t\2\4\3\t"+
		"\3\3\2\3\2\3\2\5\2\n\n\2\3\2\7\2\r\n\2\f\2\16\2\20\13\2\3\3\3\3\3\3\3"+
		"\3\3\3\5\3\27\n\3\3\3\3\3\5\3\33\n\3\3\3\7\3\36\n\3\f\3\16\3!\13\3\5\3"+
		"#\n\3\7\3%\n\3\f\3\16\3(\13\3\3\3\2\2\4\2\4\2\2\2.\2\6\3\2\2\2\4\21\3"+
		"\2\2\2\6\16\5\4\3\2\7\t\7\3\2\2\b\n\7\t\2\2\t\b\3\2\2\2\t\n\3\2\2\2\n"+
		"\13\3\2\2\2\13\r\5\4\3\2\f\7\3\2\2\2\r\20\3\2\2\2\16\f\3\2\2\2\16\17\3"+
		"\2\2\2\17\3\3\2\2\2\20\16\3\2\2\2\21&\7\5\2\2\22\"\7\t\2\2\23#\7\b\2\2"+
		"\24\37\7\4\2\2\25\27\7\t\2\2\26\25\3\2\2\2\26\27\3\2\2\2\27\30\3\2\2\2"+
		"\30\32\7\3\2\2\31\33\7\t\2\2\32\31\3\2\2\2\32\33\3\2\2\2\33\34\3\2\2\2"+
		"\34\36\7\4\2\2\35\26\3\2\2\2\36!\3\2\2\2\37\35\3\2\2\2\37 \3\2\2\2 #\3"+
		"\2\2\2!\37\3\2\2\2\"\23\3\2\2\2\"\24\3\2\2\2#%\3\2\2\2$\22\3\2\2\2%(\3"+
		"\2\2\2&$\3\2\2\2&\'\3\2\2\2\'\5\3\2\2\2(&\3\2\2\2\t\t\16\26\32\37\"&";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}