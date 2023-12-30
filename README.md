# Regexcv

Regexcv is a command line tool that is used to generate a DFA/NFA digraph denoted by the [Graphviz Dot Language](http://graphviz.org/) from a regular expression.

# Features

- Simple regex syntax.
- Generating NFA, NFA with epsilon closure, DFA, minimized DFA.
- Merging edges of digraph into a label like '\w'.

# Getting Started

Regexcv is wrote in Java. If you want to run this tool on your computer, the JRE is required.

Download jar:

```shell
wget https://github.com/nano-go/regexcv/releases/download/1.0.0/Regexcv-1.0.0.jar
```

Run:

```shell
java -jar Regexcv-1.0.0.jar -v
```

Or use alias to run:

```shell
alias regexcv='java -jar regexcv_jar_path'

regexcv --help
```

# Supported Regular Expression Syntax

## 1. Character Class

A character class matches a single char that is contained within '[]'. For example, the regex `[abc]` matches `a`, `b` or `c`.

In character class, You can specify a range using `-`, but if the '-' is the first or the last within '[]' like '[-a], [a-]', or the both sides of '-' are a special escape character like '\w' or '\d', the '-' will be treated as a literal character.

Escape characters can exist in the character class. For example, the regex `[\w\s]` matches any word character and space chatacter.

If the `^` is the first within `[]`, this matches a single character that is not contained the character set.

Examples:

- The regex `[a0-9]` matches: `"a", "0", "1", "2"..."9"`
- The regex `[-]` matches: `"-"`
- The regex `[a-] or [-a]` matches: `"a", "-"`
- The regex `[\w-7]` matches: `"a", "A", "9", "-", "7"`
- The regex `[\n-a]` matches any character in the range `\n-a`
- The regex `[^a-z8]` matches: `"0", "7", "A"`
- The regex `[^]` matches any character.
- The regex `[]` matches empty.

## 2. Alternation

Alternation matches any one subexpression seperated by the '|'.

Examples:

- The regex `a|b` matches: `"a", "b"`
- The regex `a|` matches: `"a", ""`
- The regex `|` matches: `""`

## 3. Escape character and special character.

You can use `\` to indicate that a character is special, or should be treated as a literal character.

| Escape Char |                       Description                       |
| :---------: | :-----------------------------------------------------: |
|     \w      |  Matches any word character. Equals to `[a-zA-Z0-9_]`   |
|     \W      | Matches a non-word character. Equals to `[^a-zA-Z0-9_]` |
|     \d      |      Matches a digit character. Equals to `[0-9]`       |
|     \D      |    Matches a non-digit character. Equals to `[^0-9]`    |
|     \s      |   Matches a space character. Equals to `[ \n\r\t\f]`    |
|     \S      | Matches a non-space character. Equals to `[^ \n\r\t\f]` |
|     \n      |          Matches a linefeed(char code \u000A)           |
|     \r      |       Matches a carriage return(char code \u000D)       |
|     \f      |       Matches a horizontal tab(char code \u0009)        |
|     \t      |          Matches a form feed(char code \u000C)          |
|     \b      |         Matches a back space(char code \u0008)          |

You can use `\` to escape a meta character as a literal character like `\(\)\[\]\*\+\?\.\|`

`.` is a special character that can matches any character.

Examples:

- The regex `\d` matches: `"1", "3", "9"`
- The regex `\w` matches: `"a", "Z", "9", "_"`
- The regex `\*` matches: `"*"`
- The regex `\n` matches new line `\u000A`
- The regex `.` matches: `"a", "*", "&"...`

## 4. Quantifiers

A quantifier character specifies how many instances of the previous expression:

- `*`: zero or more
- `+`: one or more
- `?`: zero or one (optional)

Examples:

- The regex `(foo)*` matches: `"", "foo", "foofoo"`
- The regex `(foo)+` matches: `"foo", "foofoo"`
- The regex `(foo)?` matches: `"", "foo"`

## 5. Unicode

You can use unicode characters in regular expression.

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

