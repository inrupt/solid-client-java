// Generated from /Users/imyshor/Projects/solid/java/solid-client-java/parser/src/main/antlr4/com/inrupt/client/parser/WwwAuthenticate.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WwwAuthenticateLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, AuthParam=2, AuthScheme=3, QuotedString=4, Token=5, Token68=6, 
		WS=7;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "AuthParam", "AuthScheme", "ObsText", "Qdtext", "QuotedPair", 
			"QuotedString", "Tchar", "Token", "Token68", "WS", "SP", "DQUOTE", "HTAB", 
			"DIGIT", "ALPHA", "VCHAR"
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


	public WwwAuthenticateLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "WwwAuthenticate.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\tq\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\3\3\3\3\3\3\3\5\3,\n\3\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\5\6"+
		"\66\n\6\3\7\3\7\3\7\3\7\3\7\5\7=\n\7\3\b\3\b\3\b\7\bB\n\b\f\b\16\bE\13"+
		"\b\3\b\3\b\3\t\3\t\3\t\5\tL\n\t\3\n\6\nO\n\n\r\n\16\nP\3\13\3\13\3\13"+
		"\6\13V\n\13\r\13\16\13W\3\13\7\13[\n\13\f\13\16\13^\13\13\3\f\3\f\6\f"+
		"b\n\f\r\f\16\fc\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3"+
		"\22\2\2\23\3\3\5\4\7\5\t\2\13\2\r\2\17\6\21\2\23\7\25\b\27\t\31\2\33\2"+
		"\35\2\37\2!\2#\2\3\2\6\5\2##%]_\u0080\t\2##%),-/\60`b~~\u0080\u0080\6"+
		"\2--/\61aa\u0080\u0080\4\2C\\c|\2x\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2"+
		"\2\17\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\3%\3\2\2\2\5\'\3"+
		"\2\2\2\7-\3\2\2\2\t/\3\2\2\2\13\65\3\2\2\2\r\67\3\2\2\2\17>\3\2\2\2\21"+
		"K\3\2\2\2\23N\3\2\2\2\25U\3\2\2\2\27a\3\2\2\2\31e\3\2\2\2\33g\3\2\2\2"+
		"\35i\3\2\2\2\37k\3\2\2\2!m\3\2\2\2#o\3\2\2\2%&\7.\2\2&\4\3\2\2\2\'(\5"+
		"\23\n\2(+\7?\2\2),\5\23\n\2*,\5\17\b\2+)\3\2\2\2+*\3\2\2\2,\6\3\2\2\2"+
		"-.\5\23\n\2.\b\3\2\2\2/\60\4\u0082\1\2\60\n\3\2\2\2\61\66\5\35\17\2\62"+
		"\66\5\31\r\2\63\66\t\2\2\2\64\66\5\t\5\2\65\61\3\2\2\2\65\62\3\2\2\2\65"+
		"\63\3\2\2\2\65\64\3\2\2\2\66\f\3\2\2\2\67<\7^\2\28=\5\35\17\29=\5\31\r"+
		"\2:=\5#\22\2;=\5\t\5\2<8\3\2\2\2<9\3\2\2\2<:\3\2\2\2<;\3\2\2\2=\16\3\2"+
		"\2\2>C\5\33\16\2?B\5\13\6\2@B\5\r\7\2A?\3\2\2\2A@\3\2\2\2BE\3\2\2\2CA"+
		"\3\2\2\2CD\3\2\2\2DF\3\2\2\2EC\3\2\2\2FG\5\33\16\2G\20\3\2\2\2HL\t\3\2"+
		"\2IL\5\37\20\2JL\5!\21\2KH\3\2\2\2KI\3\2\2\2KJ\3\2\2\2L\22\3\2\2\2MO\5"+
		"\21\t\2NM\3\2\2\2OP\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q\24\3\2\2\2RV\5!\21\2"+
		"SV\5\37\20\2TV\t\4\2\2UR\3\2\2\2US\3\2\2\2UT\3\2\2\2VW\3\2\2\2WU\3\2\2"+
		"\2WX\3\2\2\2X\\\3\2\2\2Y[\7?\2\2ZY\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\]\3\2"+
		"\2\2]\26\3\2\2\2^\\\3\2\2\2_b\5\31\r\2`b\5\35\17\2a_\3\2\2\2a`\3\2\2\2"+
		"bc\3\2\2\2ca\3\2\2\2cd\3\2\2\2d\30\3\2\2\2ef\7\"\2\2f\32\3\2\2\2gh\7$"+
		"\2\2h\34\3\2\2\2ij\7\13\2\2j\36\3\2\2\2kl\4\62;\2l \3\2\2\2mn\t\5\2\2"+
		"n\"\3\2\2\2op\4#\u0080\2p$\3\2\2\2\17\2+\65<ACKPUW\\ac\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}