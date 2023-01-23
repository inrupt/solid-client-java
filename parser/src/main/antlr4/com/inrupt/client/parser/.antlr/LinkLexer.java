// Generated from /Users/imyshor/Projects/solid/java/solid-client-java/parser/src/main/antlr4/com/inrupt/client/parser/Link.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LinkLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, UriReference=3, LinkParam=4, WS=5;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "UriReference", "UriChar", "LinkParam", "ObsText", "Qdtext", 
			"QuotedPair", "QuotedString", "Tchar", "Token", "WS", "SP", "DQUOTE", 
			"HTAB", "DIGIT", "ALPHA", "VCHAR"
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


	public LinkLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Link.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\7r\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\3\3\3\3\4\3\4\6\4.\n\4\r\4\16\4/\3\4\3\4\3\5\3\5"+
		"\5\5\66\n\5\3\6\3\6\3\6\3\6\5\6<\n\6\3\7\3\7\3\b\3\b\3\b\3\b\5\bD\n\b"+
		"\3\t\3\t\3\t\3\t\3\t\5\tK\n\t\3\n\3\n\3\n\7\nP\n\n\f\n\16\nS\13\n\3\n"+
		"\3\n\3\13\3\13\3\13\5\13Z\n\13\3\f\6\f]\n\f\r\f\16\f^\3\r\3\r\6\rc\n\r"+
		"\r\r\16\rd\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23"+
		"\2\2\24\3\3\5\4\7\5\t\2\13\6\r\2\17\2\21\2\23\2\25\2\27\2\31\7\33\2\35"+
		"\2\37\2!\2#\2%\2\3\2\5\5\2##%]_\u0080\t\2##%),-/\60`b~~\u0080\u0080\4"+
		"\2C\\c|\2t\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\13\3\2\2\2\2\31\3\2\2"+
		"\2\3\'\3\2\2\2\5)\3\2\2\2\7+\3\2\2\2\t\65\3\2\2\2\13\67\3\2\2\2\r=\3\2"+
		"\2\2\17C\3\2\2\2\21E\3\2\2\2\23L\3\2\2\2\25Y\3\2\2\2\27\\\3\2\2\2\31b"+
		"\3\2\2\2\33f\3\2\2\2\35h\3\2\2\2\37j\3\2\2\2!l\3\2\2\2#n\3\2\2\2%p\3\2"+
		"\2\2\'(\7.\2\2(\4\3\2\2\2)*\7=\2\2*\6\3\2\2\2+-\7>\2\2,.\5\t\5\2-,\3\2"+
		"\2\2./\3\2\2\2/-\3\2\2\2/\60\3\2\2\2\60\61\3\2\2\2\61\62\7@\2\2\62\b\3"+
		"\2\2\2\63\66\4#\u0080\2\64\66\5\r\7\2\65\63\3\2\2\2\65\64\3\2\2\2\66\n"+
		"\3\2\2\2\678\5\27\f\28;\7?\2\29<\5\27\f\2:<\5\23\n\2;9\3\2\2\2;:\3\2\2"+
		"\2<\f\3\2\2\2=>\4\u0082\1\2>\16\3\2\2\2?D\5\37\20\2@D\5\33\16\2AD\t\2"+
		"\2\2BD\5\r\7\2C?\3\2\2\2C@\3\2\2\2CA\3\2\2\2CB\3\2\2\2D\20\3\2\2\2EJ\7"+
		"^\2\2FK\5\37\20\2GK\5\33\16\2HK\5%\23\2IK\5\r\7\2JF\3\2\2\2JG\3\2\2\2"+
		"JH\3\2\2\2JI\3\2\2\2K\22\3\2\2\2LQ\5\35\17\2MP\5\17\b\2NP\5\21\t\2OM\3"+
		"\2\2\2ON\3\2\2\2PS\3\2\2\2QO\3\2\2\2QR\3\2\2\2RT\3\2\2\2SQ\3\2\2\2TU\5"+
		"\35\17\2U\24\3\2\2\2VZ\t\3\2\2WZ\5!\21\2XZ\5#\22\2YV\3\2\2\2YW\3\2\2\2"+
		"YX\3\2\2\2Z\26\3\2\2\2[]\5\25\13\2\\[\3\2\2\2]^\3\2\2\2^\\\3\2\2\2^_\3"+
		"\2\2\2_\30\3\2\2\2`c\5\33\16\2ac\5\37\20\2b`\3\2\2\2ba\3\2\2\2cd\3\2\2"+
		"\2db\3\2\2\2de\3\2\2\2e\32\3\2\2\2fg\7\"\2\2g\34\3\2\2\2hi\7$\2\2i\36"+
		"\3\2\2\2jk\7\13\2\2k \3\2\2\2lm\4\62;\2m\"\3\2\2\2no\t\4\2\2o$\3\2\2\2"+
		"pq\4#\u0080\2q&\3\2\2\2\16\2/\65;CJOQY^bd\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}