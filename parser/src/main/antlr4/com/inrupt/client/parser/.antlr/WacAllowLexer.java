// Generated from /Users/imyshor/Projects/solid/java/solid-client-java/parser/src/main/antlr4/com/inrupt/client/parser/WacAllow.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WacAllowLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, AccessParam=2, WS=3, DQUOTE=4;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "AccessParam", "AccessMode", "PermissionGroup", "WS", "DQUOTE", 
			"SP", "HTAB", "ALPHA"
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


	public WacAllowLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "WacAllow.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\6K\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2"+
		"\3\3\3\3\5\3\32\n\3\3\3\3\3\5\3\36\n\3\3\3\3\3\5\3\"\n\3\3\3\3\3\3\3\3"+
		"\3\7\3(\n\3\f\3\16\3+\13\3\5\3-\n\3\3\3\5\3\60\n\3\3\3\3\3\3\4\6\4\65"+
		"\n\4\r\4\16\4\66\3\5\6\5:\n\5\r\5\16\5;\3\6\3\6\6\6@\n\6\r\6\16\6A\3\7"+
		"\3\7\3\b\3\b\3\t\3\t\3\n\3\n\2\2\13\3\3\5\4\7\2\t\2\13\5\r\6\17\2\21\2"+
		"\23\2\3\2\3\4\2C\\c|\2O\2\3\3\2\2\2\2\5\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2"+
		"\2\3\25\3\2\2\2\5\27\3\2\2\2\7\64\3\2\2\2\t9\3\2\2\2\13?\3\2\2\2\rC\3"+
		"\2\2\2\17E\3\2\2\2\21G\3\2\2\2\23I\3\2\2\2\25\26\7.\2\2\26\4\3\2\2\2\27"+
		"\31\5\t\5\2\30\32\5\13\6\2\31\30\3\2\2\2\31\32\3\2\2\2\32\33\3\2\2\2\33"+
		"\35\7?\2\2\34\36\5\13\6\2\35\34\3\2\2\2\35\36\3\2\2\2\36\37\3\2\2\2\37"+
		"!\5\r\7\2 \"\5\13\6\2! \3\2\2\2!\"\3\2\2\2\",\3\2\2\2#)\5\7\4\2$%\5\13"+
		"\6\2%&\5\7\4\2&(\3\2\2\2\'$\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*-\3"+
		"\2\2\2+)\3\2\2\2,#\3\2\2\2,-\3\2\2\2-/\3\2\2\2.\60\5\13\6\2/.\3\2\2\2"+
		"/\60\3\2\2\2\60\61\3\2\2\2\61\62\5\r\7\2\62\6\3\2\2\2\63\65\5\23\n\2\64"+
		"\63\3\2\2\2\65\66\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67\b\3\2\2\28:\5"+
		"\23\n\298\3\2\2\2:;\3\2\2\2;9\3\2\2\2;<\3\2\2\2<\n\3\2\2\2=@\5\17\b\2"+
		">@\5\21\t\2?=\3\2\2\2?>\3\2\2\2@A\3\2\2\2A?\3\2\2\2AB\3\2\2\2B\f\3\2\2"+
		"\2CD\7$\2\2D\16\3\2\2\2EF\7\"\2\2F\20\3\2\2\2GH\7\13\2\2H\22\3\2\2\2I"+
		"J\t\2\2\2J\24\3\2\2\2\r\2\31\35!),/\66;?A\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}