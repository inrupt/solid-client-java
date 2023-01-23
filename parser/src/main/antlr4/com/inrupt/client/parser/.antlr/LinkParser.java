// Generated from /Users/imyshor/Projects/solid/java/solid-client-java/parser/src/main/antlr4/com/inrupt/client/parser/Link.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LinkParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, UriReference=3, LinkParam=4, WS=5;
	public static final int
		RULE_linkHeader = 0, RULE_link = 1;
	private static String[] makeRuleNames() {
		return new String[] {
			"linkHeader", "link"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "UriReference", "LinkParam", "WS"
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
	public String getGrammarFileName() { return "Link.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LinkParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class LinkHeaderContext extends ParserRuleContext {
		public List<LinkContext> link() {
			return getRuleContexts(LinkContext.class);
		}
		public LinkContext link(int i) {
			return getRuleContext(LinkContext.class,i);
		}
		public LinkHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_linkHeader; }
	}

	public final LinkHeaderContext linkHeader() throws RecognitionException {
		LinkHeaderContext _localctx = new LinkHeaderContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_linkHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(4);
			link();
			setState(9);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(5);
				match(T__0);
				setState(6);
				link();
				}
				}
				setState(11);
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

	public static class LinkContext extends ParserRuleContext {
		public TerminalNode UriReference() { return getToken(LinkParser.UriReference, 0); }
		public List<TerminalNode> LinkParam() { return getTokens(LinkParser.LinkParam); }
		public TerminalNode LinkParam(int i) {
			return getToken(LinkParser.LinkParam, i);
		}
		public List<TerminalNode> WS() { return getTokens(LinkParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(LinkParser.WS, i);
		}
		public LinkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_link; }
	}

	public final LinkContext link() throws RecognitionException {
		LinkContext _localctx = new LinkContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_link);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			match(UriReference);
			setState(23);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1 || _la==WS) {
				{
				{
				setState(14);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(13);
					match(WS);
					}
				}

				setState(16);
				match(T__1);
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(17);
					match(WS);
					}
				}

				setState(20);
				match(LinkParam);
				}
				}
				setState(25);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\7\35\4\2\t\2\4\3"+
		"\t\3\3\2\3\2\3\2\7\2\n\n\2\f\2\16\2\r\13\2\3\3\3\3\5\3\21\n\3\3\3\3\3"+
		"\5\3\25\n\3\3\3\7\3\30\n\3\f\3\16\3\33\13\3\3\3\2\2\4\2\4\2\2\2\36\2\6"+
		"\3\2\2\2\4\16\3\2\2\2\6\13\5\4\3\2\7\b\7\3\2\2\b\n\5\4\3\2\t\7\3\2\2\2"+
		"\n\r\3\2\2\2\13\t\3\2\2\2\13\f\3\2\2\2\f\3\3\2\2\2\r\13\3\2\2\2\16\31"+
		"\7\5\2\2\17\21\7\7\2\2\20\17\3\2\2\2\20\21\3\2\2\2\21\22\3\2\2\2\22\24"+
		"\7\4\2\2\23\25\7\7\2\2\24\23\3\2\2\2\24\25\3\2\2\2\25\26\3\2\2\2\26\30"+
		"\7\6\2\2\27\20\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\5"+
		"\3\2\2\2\33\31\3\2\2\2\6\13\20\24\31";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}