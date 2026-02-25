# Regexcv

Regexcv is a command-line tool that generates NFA and DFA directed graphs in the [Graphviz DOT language](http://graphviz.org/) from a regular expression.

# Features

- Lightweight regex syntax.
- Generates NFA, ε-closure-free NFA, DFA, and minimized DFA graphs.
- Merges parallel graph edges into compact labels such as `\w`.

# Getting Started

Regexcv is written in Java. A Java Runtime Environment (JRE) is required to run it.

Download jar:

```shell
wget https://github.com/nano-go/regexcv/releases/download/v1.0.2/Regexcv-v1.0.2.jar
```

Run:

```shell
java -jar path_to_regexcv_jar -v
```

Or use alias to run:

```shell
alias regexcv='java -jar path_to_regexcv_jar'

regexcv --help
```

# Supported Regular Expression Syntax

## 1. Character Class

A character class matches a single character contained within `[]`. For example, the regex `[abc]` matches `a`, `b`, or `c`.

Inside a character class, you can specify a range with `-`. If `-` appears first or last (for example `[-a]`, `[a-]`), or if either side of `-` is a special escape character such as `\w` or `\d`, then `-` is treated as a literal character.

Escape characters can appear inside a character class. For example, `[\w\s]` matches any word character or whitespace character.

If `^` is the first character inside `[]`, the class is negated and matches any character not in the set.

Examples:

- The regex `[a0-9]` matches: `"a", "0", "1", "2"..."9"`
- The regex `[-]` matches: `"-"`
- The regex `[a-] or [-a]` matches: `"a", "-"`
- The regex `[\w-7]` matches: `"a", "A", "9", "-", "7"`
- The regex `[\n-a]` matches any character in the range `\n-a`
- The regex `[^a-z8]` matches: `"0", "7", "A"`
- The regex `[^]` matches any character.
- The regex `[]` is an empty character class and therefore matches nothing.

## 2. Alternation

Alternation matches any one subexpression separated by `|`.

Examples:

- The regex `a|b` matches: `"a", "b"`
- The regex `a|` matches: `"a", ""`
- The regex `|` matches: `""`

## 3. Escape and Special Characters

Use `\` to introduce a special escape sequence, or to treat a metacharacter as a literal character.

| Escape Char | Description |
| :---------: | :---------- |
| `\w` | Matches any word character. Equivalent to `[a-zA-Z0-9_]`. |
| `\W` | Matches a non-word character. Equivalent to `[^a-zA-Z0-9_]`. |
| `\d` | Matches a digit character. Equivalent to `[0-9]`. |
| `\D` | Matches a non-digit character. Equivalent to `[^0-9]`. |
| `\s` | Matches a whitespace character. Equivalent to `[ \n\r\f\t]`. |
| `\S` | Matches a non-whitespace character. Equivalent to `[^ \n\r\f\t]`. |
| `\n` | Matches a line feed (char code `\u000A`). |
| `\r` | Matches a carriage return (char code `\u000D`). |
| `\f` | Matches a form feed (char code `\u000C`). |
| `\t` | Matches a horizontal tab (char code `\u0009`). |
| `\b` | Matches a backspace (char code `\u0008`). |

You can use `\` to escape metacharacters as literals, such as `\(\)\[\]\*\+\?\.\|`.

`.` is a special character that matches any character.

Examples:

- The regex `\d` matches: `"1", "3", "9"`
- The regex `\w` matches: `"a", "Z", "9", "_"`
- The regex `\*` matches: `"*"`
- The regex `\n` matches new line `\u000A`
- The regex `.` matches: `"a", "*", "&"...`

## 4. Quantifiers

A quantifier specifies how many instances of the previous expression are allowed:

- `*`: zero or more
- `+`: one or more
- `?`: zero or one (optional)

Examples:

- The regex `(foo)*` matches: `"", "foo", "foofoo"`
- The regex `(foo)+` matches: `"foo", "foofoo"`
- The regex `(foo)?` matches: `"", "foo"`

## 5. Unicode

You can use Unicode characters in regular expressions.

Examples

- The regex `(你好)|(Hello)` matches: `"你好", "Hello"`

# Examples

## NFA

The input regex `(abc)*`:

```shell
regexcv -N -e -r '(abc)*' > nfa1.dot && dot -Tpng -o nfa1.png nfa1.dot
```

The output image:

![NFA1](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/nfa1.png)

The input regex `<\w+>[^<]*<\\\w+>`:

```shell
regexcv -N -e -r '<\w+>[^<]*<\\\w+>' > nfa2.dot && dot -Tpng -o nfa2.png nfa2.dot
```

The output image:

![NFA2](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/nfa2.png)

## DFA

The input regex `((\s)|[abc])+(\w|(\Sabc))*`:

```shell
regexcv -D -m -r '((\s)|[abc])+(\w|(\Sabc))*' > dfa1.dot && dot -Tpng -o dfa1.png dfa1.dot
```

The output image:

![DFA1](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/dfa1.png)
